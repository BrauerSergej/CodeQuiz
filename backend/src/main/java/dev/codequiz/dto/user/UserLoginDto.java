package dev.codequiz.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Request-объект для входа. Только email + пароль — раз мы решили,
// что логин идёт по email, а не по username (см. UserRepository).
@Schema(description = "Данные для входа пользователя")
public class UserLoginDto {

    @Schema(description = "Email пользователя", example = "sergej@example.com")
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @Schema(description = "Пароль пользователя")
    @NotBlank(message = "Пароль обязателен")
    private String password;

    public UserLoginDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}