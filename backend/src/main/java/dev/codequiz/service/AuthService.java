package dev.codequiz.service;

import dev.codequiz.domain.ConfirmationCode;
import dev.codequiz.domain.User;
import dev.codequiz.domain.enums.AccountRole;
import dev.codequiz.domain.enums.AccountStatus;
import dev.codequiz.dto.confirmation.ConfirmationCodeVerifyDto;
import dev.codequiz.dto.confirmation.ResetPasswordDto;
import dev.codequiz.dto.user.UserDto;
import dev.codequiz.dto.user.UserLoginDto;
import dev.codequiz.dto.user.UserRegistrationDto;
import dev.codequiz.dto.user.UserSaveDto;
import dev.codequiz.exception.AccountNotActiveException;
import dev.codequiz.exception.ConfirmationCodeExpiredException;
import dev.codequiz.exception.EmailAlreadyConfirmedException;
import dev.codequiz.exception.EmailAlreadyExistsException;
import dev.codequiz.exception.InvalidConfirmationCodeException;
import dev.codequiz.exception.InvalidCredentialsException;
import dev.codequiz.exception.ResendTooSoonException;
import dev.codequiz.exception.UsernameAlreadyExistsException;
import dev.codequiz.exception.UserNotFoundException;
import dev.codequiz.mapper.UserMapper;
import dev.codequiz.repository.ConfirmationCodeRepository;
import dev.codequiz.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

// Отвечает за жизненный цикл аутентификации: регистрация, подтверждение
// email, вход. UserService (отдельный класс) отвечает за то, что происходит
// с УЖЕ существующим, аутентифицированным пользователем — профиль, смена
// пароля, статус. Такое разделение соответствует тому, как обычно
// разбиваются контроллеры: AuthController (/auth/**) и UserController (/users/**).
@Service
public class AuthService {

    // Время жизни кода подтверждения email. Константа, а не магическое число
    // в коде метода — чтобы значение было видно сразу и легко менялось в одном месте.
    private static final int CONFIRMATION_CODE_TTL_MINUTES = 15;

    // Минимальный интервал между двумя выдачами кода одному аккаунту —
    // защита от спама (иначе можно было бы дёргать /auth/resend-code
    // в цикле и заваливать чужой email письмами).
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private final UserRepository userRepository;
    private final ConfirmationCodeRepository confirmationCodeRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       ConfirmationCodeRepository confirmationCodeRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.confirmationCodeRepository = confirmationCodeRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // @Transactional — если что-то упадёт между save(user) и save(confirmationCode)
    // (например, обрыв соединения с БД), откатятся ОБА изменения. Без этого можно
    // было бы получить "повисший" аккаунт без кода подтверждения, который
    // невозможно активировать.
    @Transactional
    public UserDto register(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("Email уже занят: " + dto.getEmail());
        }
        if (userRepository.existsByUserName(dto.getUserName())) {
            throw new UsernameAlreadyExistsException("Имя пользователя уже занято: " + dto.getUserName());
        }

        // Хеширование — единственное место, где сырой пароль вообще существует
        // в памяти дольше одной строки кода. Дальше по коду его уже нет.
        String passwordHash = passwordEncoder.encode(dto.getPassword());

        // Собираем UserSaveDto (см. пояснение в самом классе) — там, где
        // проставляются значения, которые пользователь не выбирает сам:
        // роль по умолчанию USER, статус UNCONFIRMED до подтверждения email.
        UserSaveDto saveDto = new UserSaveDto(
                dto.getUserName(),
                dto.getEmail(),
                dto.getPhone(),
                passwordHash,
                AccountRole.USER,
                AccountStatus.UNCONFIRMED
        );

        User user = userMapper.toEntity(saveDto);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        issueConfirmationCode(user);

        return userMapper.toDto(user);
    }

    // Подтверждение принимает email отдельно от ConfirmationCodeVerifyDto —
    // сам DTO содержит только код, потому что на этом шаге пользователь ещё
    // не аутентифицирован (аккаунт UNCONFIRMED), значит определить "кто он" можно
    // только по email, который он вводит вместе с кодом (например,
    // POST /auth/confirm?email=... с телом ConfirmationCodeVerifyDto).
    @Transactional
    public void confirmEmail(String email, ConfirmationCodeVerifyDto verifyDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: " + email));

        validateAndConsumeCode(user, verifyDto.getCode());

        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // Повторная отправка кода подтверждения — на случай, если письмо
    // потерялось, ушло в спам, или пользователь не успел ввести код за
    // 15 минут. Работает только для ещё не подтверждённых аккаунтов:
    // для ACTIVE присылать код смысла нет, пользователю нужно просто войти.
    @Transactional
    public void resendConfirmationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: " + email));

        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new EmailAlreadyConfirmedException("Email уже подтверждён, войдите в аккаунт");
        }

        requireCooldownElapsed(user);
        issueConfirmationCode(user);
    }

    // Запрос на сброс пароля. Намеренно НЕ бросает UserNotFoundException,
    // если email не зарегистрирован — в отличие от resendConfirmationCode
    // и register(). Разница в чувствительности: раскрытие факта "такой
    // email уже зарегистрирован" при РЕГИСТРАЦИИ — обычная практика (иначе
    // нельзя вежливо сообщить пользователю, что делать), а вот раскрытие
    // того же факта через forgot-password — классический вектор для
    // перебора чужих email по базе (user enumeration), поэтому здесь ответ
    // одинаковый независимо от того, существует аккаунт или нет: реальная
    // отправка кода просто молча не происходит для несуществующего email.
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            requireCooldownElapsed(user);
            issueConfirmationCode(user);
        });
    }

    // Email передаётся отдельно от ResetPasswordDto по той же причине,
    // что и в confirmEmail — пользователь не аутентифицирован.
    @Transactional
    public void resetPassword(String email, ResetPasswordDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: " + email));

        validateAndConsumeCode(user, dto.getCode());

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // Возвращает entity User, а не UserDto — потому что следующий шаг (Security)
    // достанет отсюда id и accountRole для генерации JWT-токена, а DTO этого
    // не отдаёт. Контроллер уровнем выше при необходимости сам замаппит в
    // UserDto для ответа клиенту.
    @Transactional(readOnly = true)
    public User login(UserLoginDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Неверный email или пароль"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Неверный email или пароль");
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Аккаунт не активирован — подтвердите email");
        }

        return user;
    }

    // Общая проверка кода — используется и в confirmEmail (email), и в
    // resetPassword (пароль), т.к. по сути это один и тот же механизм
    // "короткоживущий одноразовый код на email", просто с разными
    // последствиями после успешной проверки (какое поле аккаунта меняется).
    private void validateAndConsumeCode(User user, String rawCode) {
        ConfirmationCode confirmationCode = confirmationCodeRepository
                .findTopByAccountOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new InvalidConfirmationCodeException("Код не найден, запросите новый"));

        if (confirmationCode.getUsedAt() != null) {
            throw new InvalidConfirmationCodeException("Код уже был использован");
        }
        if (confirmationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ConfirmationCodeExpiredException("Срок действия кода истёк, запросите новый");
        }
        // matches(), а не equals() — сравниваем введённый "сырой" код с уже
        // захешированным значением в БД, тем же способом, что и пароль.
        if (!passwordEncoder.matches(rawCode, confirmationCode.getCodeHash())) {
            throw new InvalidConfirmationCodeException("Неверный код");
        }

        confirmationCode.setUsedAt(LocalDateTime.now());
        confirmationCodeRepository.save(confirmationCode);
    }

    private void requireCooldownElapsed(User user) {
        confirmationCodeRepository.findTopByAccountOrderByCreatedAtDesc(user).ifPresent(lastCode -> {
            LocalDateTime cooldownEnd = lastCode.getCreatedAt().plusSeconds(RESEND_COOLDOWN_SECONDS);
            if (cooldownEnd.isAfter(LocalDateTime.now())) {
                throw new ResendTooSoonException("Код уже был отправлен недавно, подождите немного перед повторным запросом");
            }
        });
    }

    private void issueConfirmationCode(User user) {
        String rawCode = generateSixDigitCode();

        ConfirmationCode confirmationCode = new ConfirmationCode();
        confirmationCode.setAccount(user);
        confirmationCode.setCodeHash(passwordEncoder.encode(rawCode));
        confirmationCode.setExpiresAt(LocalDateTime.now().plusMinutes(CONFIRMATION_CODE_TTL_MINUTES));
        confirmationCode.setCreatedAt(LocalDateTime.now());
        confirmationCodeRepository.save(confirmationCode);

        // rawCode отправляется пользователю на email в открытом виде — это
        // нормально для кода подтверждения (в отличие от пароля): он
        // одноразовый, коротко живёт (15 минут) и в БД хранится не сам код,
        // а его хеш (codeHash выше), так что даже утечка БД не раскрывает код.
        emailService.sendConfirmationCode(user.getEmail(), rawCode);
    }

    // SecureRandom, а не Math.random() — код подтверждения это, по сути,
    // короткий секрет (как одноразовый пароль), Math.random() предсказуем
    // и не годится для чего-либо, связанного с безопасностью.
    private String generateSixDigitCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}