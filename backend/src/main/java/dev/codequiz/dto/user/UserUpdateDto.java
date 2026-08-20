package dev.codequiz.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Request-объект для редактирования профиля уже существующим пользователем.
// Осознанно НЕ включает email и пароль — смена email обычно требует
// повторного подтверждения (через ConfirmationCode), а смена пароля
// вынесена в отдельный PasswordChangeDto. Здесь только то, что можно
// менять свободно, без дополнительной верификации.
@Schema(description = "Данные для обновления профиля пользователя")
public class UserUpdateDto {

    @Schema(description = "Новое имя пользователя", example = "sergej_dev")
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    private String userName;

    @Schema(description = "Новый номер телефона", example = "+491701234567")
    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Некорректный формат телефона")
    private String phone;

    public UserUpdateDto() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}