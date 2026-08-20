package dev.codequiz.dto.user;

import dev.codequiz.domain.enums.AccountRole;
import dev.codequiz.domain.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// Response-объект — то, что отдаём клиенту в ответ (например, GET /users/me).
// Ключевой момент: здесь НЕТ passwordHash. Entity User никогда не должен
// отдаваться наружу напрямую (через @RequestBody/@ResponseBody) именно
// потому, что она содержит passwordHash — для этого и существует
// отдельный "чистый" response-объект.
@Schema(description = "Публичные данные пользователя, возвращаемые клиенту")
public class UserDto {

    @Schema(description = "Идентификатор пользователя", example = "1")
    private Long id;

    @Schema(description = "Имя пользователя", example = "sergej_dev")
    private String userName;

    @Schema(description = "Email пользователя", example = "sergej@example.com")
    private String email;

    @Schema(description = "Телефон пользователя", example = "+491701234567")
    private String phone;

    @Schema(description = "Роль аккаунта")
    private AccountRole accountRole;

    @Schema(description = "Очки опыта", example = "150")
    private int xp;

    @Schema(description = "Статус аккаунта")
    private AccountStatus accountStatus;

    @Schema(description = "Дата регистрации")
    private LocalDateTime createdAt;

    public UserDto() {
    }

    // Конструктор, которым удобно собирать response прямо из entity в сервисе:
    // new UserDto(user.getId(), user.getUserName(), ...)
    public UserDto(Long id, String userName, String email, String phone,
                   AccountRole accountRole, int xp, AccountStatus accountStatus,
                   LocalDateTime createdAt) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.accountRole = accountRole;
        this.xp = xp;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public AccountRole getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(AccountRole accountRole) {
        this.accountRole = accountRole;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}