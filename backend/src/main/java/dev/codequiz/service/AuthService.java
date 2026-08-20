package dev.codequiz.service;

import dev.codequiz.domain.ConfirmationCode;
import dev.codequiz.domain.User;
import dev.codequiz.domain.enums.AccountRole;
import dev.codequiz.domain.enums.AccountStatus;
import dev.codequiz.dto.confirmation.ConfirmationCodeVerifyDto;
import dev.codequiz.dto.user.UserDto;
import dev.codequiz.dto.user.UserLoginDto;
import dev.codequiz.dto.user.UserRegistrationDto;
import dev.codequiz.dto.user.UserSaveDto;
import dev.codequiz.exception.AccountNotActiveException;
import dev.codequiz.exception.ConfirmationCodeExpiredException;
import dev.codequiz.exception.EmailAlreadyExistsException;
import dev.codequiz.exception.InvalidConfirmationCodeException;
import dev.codequiz.exception.InvalidCredentialsException;
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

    private final UserRepository userRepository;
    private final ConfirmationCodeRepository confirmationCodeRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       ConfirmationCodeRepository confirmationCodeRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.confirmationCodeRepository = confirmationCodeRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
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

        ConfirmationCode confirmationCode = confirmationCodeRepository
                .findTopByAccountOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new InvalidConfirmationCodeException("Код подтверждения не найден"));

        if (confirmationCode.getUsedAt() != null) {
            throw new InvalidConfirmationCodeException("Код уже был использован");
        }
        if (confirmationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ConfirmationCodeExpiredException("Срок действия кода истёк, запросите новый");
        }
        // matches(), а не equals() — сравниваем введённый "сырой" код с уже
        // захешированным значением в БД, тем же способом, что и пароль.
        if (!passwordEncoder.matches(verifyDto.getCode(), confirmationCode.getCodeHash())) {
            throw new InvalidConfirmationCodeException("Неверный код подтверждения");
        }

        confirmationCode.setUsedAt(LocalDateTime.now());
        confirmationCodeRepository.save(confirmationCode);

        user.setAccountStatus(AccountStatus.ACTIVE);
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

    private void issueConfirmationCode(User user) {
        String rawCode = generateSixDigitCode();

        ConfirmationCode confirmationCode = new ConfirmationCode();
        confirmationCode.setAccount(user);
        confirmationCode.setCodeHash(passwordEncoder.encode(rawCode));
        confirmationCode.setExpiresAt(LocalDateTime.now().plusMinutes(CONFIRMATION_CODE_TTL_MINUTES));
        confirmationCode.setCreatedAt(LocalDateTime.now());
        confirmationCodeRepository.save(confirmationCode);

        // TODO: отправка rawCode пользователю на email. spring-boot-starter-mail
        // уже подключён в pom.xml, но сам EmailService с реальной отправкой
        // письма ещё не реализован — это отдельная задача, не относящаяся
        // к сервисному слою аутентификации как таковому.
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