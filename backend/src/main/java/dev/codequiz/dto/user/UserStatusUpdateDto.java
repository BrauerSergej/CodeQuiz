package dev.codequiz.dto.user;

import dev.codequiz.domain.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

// Request-объект для смены статуса аккаунта — используется в админ-эндпоинте,
// например PATCH /admin/users/{id}/status. Отдельный DTO, а не часть
// UserUpdateDto, потому что это разные роли доступа: свой профиль редактирует
// сам пользователь, а accountStatus (блокировка/разблокировка) — только
// администратор. Смешивать их в один DTO означало бы, что оба поля защищены
// одной и той же проверкой прав, что неверно.
@Schema(description = "Данные для смены статуса аккаунта пользователя (администратором)")
public class UserStatusUpdateDto {

    @Schema(description = "Новый статус аккаунта", example = "BLOCKED")
    @NotNull(message = "Статус аккаунта обязателен")
    private AccountStatus accountStatus;

    public UserStatusUpdateDto() {
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }
}