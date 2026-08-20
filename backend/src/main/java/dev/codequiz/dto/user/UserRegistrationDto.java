package dev.codequiz.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Request-объект для регистрации нового пользователя.
// Приходит из тела POST /auth/register. Содержит только те поля, которые
// пользователь реально вводит сам — id, role, xp, status, даты и т.д. сюда
// не попадают, их проставляет сервер.
@Schema(description = "Данные для регистрации нового пользователя")
public class UserRegistrationDto {

    @Schema(description = "Имя пользователя (логин)", example = "sergej_dev")
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    private String userName;

    @Schema(description = "Email пользователя", example = "sergej@example.com")
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @Schema(description = "Номер телефона в международном формате", example = "+491701234567")
    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Некорректный формат телефона")
    private String phone;

    // Обратите внимание: здесь именно "сырой" пароль, а не passwordHash.
    // Хеширование происходит в сервисе (например, через BCryptPasswordEncoder),
    // а не в DTO и не в entity — DTO лишь переносит то, что ввёл пользователь.
    @Schema(description = "Пароль, минимум 8 символов", example = "MyStrongPass123")
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен быть не короче 8 символов")
    private String password;

    public UserRegistrationDto() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}