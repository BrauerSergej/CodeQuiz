package dev.codequiz.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Request-объект для смены пароля уже аутентифицированным пользователем.
// Отдельный DTO, а не часть UserUpdateDto — потому что это принципиально
// другая операция: требует старый пароль для подтверждения личности
// (иначе злоумышленник, перехвативший сессию, мог бы тихо сменить пароль
// без знания текущего) и отдельную валидацию на новый пароль.
@Schema(description = "Данные для смены пароля")
public class PasswordChangeDto {

    @Schema(description = "Текущий пароль пользователя, для подтверждения личности")
    @NotBlank(message = "Текущий пароль обязателен")
    private String oldPassword;

    @Schema(description = "Новый пароль, минимум 8 символов", example = "NewStrongPass456")
    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 8, message = "Пароль должен быть не короче 8 символов")
    private String newPassword;

    public PasswordChangeDto() {
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}