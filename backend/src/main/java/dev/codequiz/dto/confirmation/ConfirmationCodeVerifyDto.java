package dev.codequiz.dto.confirmation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Request-объект для подтверждения email — POST /auth/confirm.
// Пользователь вводит код, который получил на почту; сервис хеширует
// введённое значение и сравнивает с codeHash, хранящимся в БД (сам код
// в открытом виде в базе не хранится — как и с паролем).
@Schema(description = "Код подтверждения email")
public class ConfirmationCodeVerifyDto {

    @Schema(description = "Код подтверждения, полученный на email", example = "482913")
    @NotBlank(message = "Код обязателен")
    @Pattern(regexp = "^[0-9]{6}$", message = "Код должен состоять из 6 цифр")
    private String code;

    public ConfirmationCodeVerifyDto() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}