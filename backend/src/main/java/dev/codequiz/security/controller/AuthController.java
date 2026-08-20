package dev.codequiz.security.controller;

import dev.codequiz.domain.User;
import dev.codequiz.security.dto.AuthResponseDto;
import dev.codequiz.security.dto.RefreshTokenDto;
import dev.codequiz.dto.confirmation.ConfirmationCodeVerifyDto;
import dev.codequiz.dto.confirmation.ResetPasswordDto;
import dev.codequiz.dto.user.UserDto;
import dev.codequiz.dto.user.UserLoginDto;
import dev.codequiz.dto.user.UserRegistrationDto;
import dev.codequiz.mapper.UserMapper;
import dev.codequiz.security.CustomUserDetailsService;
import dev.codequiz.security.service.JwtService;
import dev.codequiz.security.UserPrincipal;
import dev.codequiz.service.AuthService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Единственный контроллер, доступный БЕЗ access-токена (см. SecurityConfig,
// "/auth/**".permitAll()) — логично: если бы для входа уже требовался
// токен, никто не смог бы его первый раз получить. /auth/refresh тоже
// здесь, хотя ему и нужен токен — но это refresh-токен, а не access,
// поэтому обычная проверка через JwtAuthenticationFilter (которая знает
// только про access) для него не подходит, проверяем вручную внутри метода.
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Регистрация, подтверждение email, вход и обновление токена")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthService authService,
                          JwtService jwtService,
                          UserMapper userMapper,
                          CustomUserDetailsService userDetailsService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegistrationDto dto) {
        UserDto created = authService.register(dto);
        // 201 Created — стандартный статус для "ресурс успешно создан",
        // а не 200 OK, который подразумевал бы, что ресурс уже существовал.
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // email передаётся отдельным query-параметром, а не в теле — см.
    // пояснение в AuthService.confirmEmail, почему на этом шаге пользователь
    // ещё не аутентифицирован и не может быть определён иначе.
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmEmail(@RequestParam String email,
                                             @Valid @RequestBody ConfirmationCodeVerifyDto dto) {
        authService.confirmEmail(email, dto);
        // 204 No Content — операция успешна, отдавать в ответе нечего.
        return ResponseEntity.noContent().build();
    }

    // Повторная отправка кода подтверждения — на случай, если письмо не
    // дошло или истёк срок действия предыдущего кода.
    @PostMapping("/resend-code")
    public ResponseEntity<Void> resendCode(@RequestParam String email) {
        authService.resendConfirmationCode(email);
        return ResponseEntity.noContent().build();
    }

    // Запрос кода для сброса пароля. Всегда отвечает 204, даже если email
    // не зарегистрирован — см. пояснение в AuthService.forgotPassword,
    // почему здесь намеренно нет UserNotFoundException.
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.noContent().build();
    }

    // Завершение сброса пароля — код из письма + новый пароль.
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestParam String email,
                                              @Valid @RequestBody ResetPasswordDto dto) {
        authService.resetPassword(email, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody UserLoginDto dto) {
        // AuthService.login() уже проверил email/пароль/статус аккаунта —
        // здесь остаётся только выпустить пару токенов для уже
        // провалидированного пользователя.
        User user = authService.login(dto);
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);
        return ResponseEntity.ok(new AuthResponseDto(accessToken, refreshToken, userMapper.toDto(user)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenDto dto) {
        try {
            // Подпись/срок жизни refresh-токена проверяются здесь же:
            // extractEmailFromRefreshToken бросит JwtException, если
            // токен битый, просрочен или подписан не refreshKey.
            String email = jwtService.extractEmailFromRefreshToken(dto.getRefreshToken());

            // Пользователя загружаем заново из БД (а не берём из токена),
            // потому что за время жизни refresh-токена роль или статус
            // аккаунта могли измениться (например, админ заблокировал).
            UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(email);
            User user = principal.getUser();

            String newAccessToken = jwtService.generateAccessToken(principal);
            String newRefreshToken = jwtService.generateRefreshToken(principal);
            return ResponseEntity.ok(new AuthResponseDto(newAccessToken, newRefreshToken, userMapper.toDto(user)));
        } catch (JwtException | IllegalArgumentException e) {
            // Битый/просроченный/поддельный refresh-токен — 401, пользователю
            // придётся залогиниться заново через /auth/login.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}