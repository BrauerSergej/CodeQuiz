package dev.codequiz.service;

import dev.codequiz.domain.User;
import dev.codequiz.domain.enums.AccountStatus;
import dev.codequiz.dto.user.UserDto;
import dev.codequiz.dto.user.UserLoginDto;
import dev.codequiz.dto.user.UserRegistrationDto;
import dev.codequiz.dto.user.UserSaveDto;
import dev.codequiz.exception.AccountNotActiveException;
import dev.codequiz.exception.EmailAlreadyExistsException;
import dev.codequiz.exception.InvalidCredentialsException;
import dev.codequiz.exception.UsernameAlreadyExistsException;
import dev.codequiz.mapper.UserMapper;
import dev.codequiz.repository.ConfirmationCodeRepository;
import dev.codequiz.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Юнит-тесты сервисного слоя — все зависимости (репозитории, маппер,
// PasswordEncoder, EmailService) заменены моками через @Mock. Это значит,
// что тесты не трогают реальную БД или SMTP — проверяется только логика
// самого AuthService (условия, которые приводят к тому или иному исключению
// или результату), изолированно от остальной системы.
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConfirmationCodeRepository confirmationCodeRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    private AuthService authService;

    // Создаём сервис вручную (не через Spring-контекст) — @InjectMocks тоже
    // сработал бы, но явный конструктор яснее показывает, какие зависимости
    // у AuthService есть, не заглядывая в его исходник.
    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, confirmationCodeRepository,
                userMapper, passwordEncoder, emailService);
    }

    @Test
    void register_savesUserWithUnconfirmedStatus_whenEmailAndUserNameAreFree() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("new@example.com");
        dto.setUserName("newuser");
        dto.setPhone("+491234567890");
        dto.setPassword("StrongPass123");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByUserName("newuser")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123")).thenReturn("hashed-password");

        // ВАЖНО: userMapper — мок, а не настоящий MapStruct-класс, поэтому
        // сам он ничего не копирует. Настоящий UserMapperImpl при вызове
        // toEntity(saveDto) переносит поля из saveDto в новый User —
        // здесь мы этот перенос имитируем вручную через thenAnswer,
        // читая реальный аргумент, с которым его вызвал AuthService.
        User mappedEntity = new User();
        when(userMapper.toEntity(any(UserSaveDto.class))).thenAnswer(invocation -> {
            UserSaveDto saveDto = invocation.getArgument(0);
            mappedEntity.setUserName(saveDto.getUserName());
            mappedEntity.setEmail(saveDto.getEmail());
            mappedEntity.setPhone(saveDto.getPhone());
            mappedEntity.setPasswordHash(saveDto.getPasswordHash());
            mappedEntity.setAccountRole(saveDto.getAccountRole());
            mappedEntity.setAccountStatus(saveDto.getAccountStatus());
            return mappedEntity;
        });
        // save() в реальном репозитории возвращает сохранённую сущность —
        // здесь просто отдаём назад тот же объект, что был передан.
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto expectedDto = new UserDto();
        when(userMapper.toDto(any(User.class))).thenReturn(expectedDto);

        UserDto result = authService.register(dto);

        assertThat(result).isSameAs(expectedDto);
        // Проверяем, что регистрация действительно создаёт аккаунт со
        // статусом UNCONFIRMED, а не сразу ACTIVE — ключевое бизнес-правило,
        // без него можно было бы обойти подтверждение email вовсе.
        assertThat(mappedEntity.getAccountStatus()).isEqualTo(AccountStatus.UNCONFIRMED);
        // И что код подтверждения реально пытались отправить на почту.
        verify(emailService).sendConfirmationCode(anyString(), anyString());
    }

    @Test
    void register_throwsEmailAlreadyExists_whenEmailIsTaken() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("taken@example.com");

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(EmailAlreadyExistsException.class);

        // Раз email уже занят, до сохранения пользователя дело даже
        // доходить не должно — проверяем, что save() не вызывался.
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsUsernameAlreadyExists_whenUserNameIsTaken() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("free@example.com");
        dto.setUserName("takenname");

        when(userRepository.existsByEmail("free@example.com")).thenReturn(false);
        when(userRepository.existsByUserName("takenname")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsUser_whenCredentialsAreValidAndAccountActive() {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("user@example.com");
        dto.setPassword("CorrectPass1");

        User user = new User();
        user.setPasswordHash("hashed");
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CorrectPass1", "hashed")).thenReturn(true);

        User result = authService.login(dto);

        assertThat(result).isSameAs(user);
    }

    @Test
    void login_throwsInvalidCredentials_whenEmailNotFound() {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("unknown@example.com");
        dto.setPassword("whatever");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throwsInvalidCredentials_whenPasswordIsWrong() {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("user@example.com");
        dto.setPassword("WrongPass");

        User user = new User();
        user.setPasswordHash("hashed");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "hashed")).thenReturn(false);

        // Намеренно та же ошибка, что и при "email не найден" — см.
        // пояснение в самом AuthService про защиту от user enumeration.
        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throwsAccountNotActive_whenAccountIsNotActiveYet() {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("user@example.com");
        dto.setPassword("CorrectPass1");

        User user = new User();
        user.setPasswordHash("hashed");
        user.setAccountStatus(AccountStatus.UNCONFIRMED);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CorrectPass1", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(AccountNotActiveException.class);
    }
}