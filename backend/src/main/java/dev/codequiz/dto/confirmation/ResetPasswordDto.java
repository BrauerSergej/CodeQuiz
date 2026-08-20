package dev.codequiz.dto.confirmation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Request-объект для завершения сброса пароля — POST /auth/reset-password.
// Как и в ConfirmationCodeVerifyDto, email передаётся отдельно (query-параметр
// в контроллере), потому что на этом шаге пользователь тоже не аутентифицирован —
// он же и забыл пароль, войти по логину/паролю, чтобы себя идентифицировать, не может.
@Schema(description = "Код сброса пароля и новый пароль")
public class ResetPasswordDto {

    @Schema(description = "Код, полученный на email", example = "482913")
    @NotBlank(message = "Код обязателен")
    @Pattern(regexp = "^[0-9]{6}$", message = "Код должен состоять из 6 цифр")
    private String code;

    @Schema(description = "Новый пароль, минимум 8 символов", example = "NewStrongPass456")
    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 8, message = "Пароль должен быть не короче 8 символов")
    private String newPassword;

    public ResetPasswordDto() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
