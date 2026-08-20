package dev.codequiz.dto.user;

import dev.codequiz.domain.enums.AccountRole;
import dev.codequiz.domain.enums.AccountStatus;

// Внутренний DTO, который НЕ приходит из контроллера напрямую (поэтому здесь
// нет @Schema и Bean Validation — этот объект никогда не сериализуется в JSON
// и не проверяется через @Valid, он существует только внутри Java-кода).
//
// Назначение: чётко разделить "то, что прислал клиент" (RegisterRequest,
// с сырым паролем) от "то, что реально уходит в User.save()" (уже с готовым
// passwordHash и проставленными значениями по умолчанию для role/status/xp).
// Собирается в сервисе примерно так:
//
//   String hash = passwordEncoder.encode(request.getPassword());
//   UserSaveDto saveDto = new UserSaveDto(request.getUserName(), request.getEmail(),
//           request.getPhone(), hash, AccountRole.USER, AccountStatus.PENDING);
//   User user = mapToEntity(saveDto);
//   userRepository.save(user);
//
// Без такого разделения велик соблазн один раз хешировать пароль и по ошибке
// хешировать его повторно при следующем сохранении того же объекта.
public class UserSaveDto {

    private String userName;
    private String email;
    private String phone;
    private String passwordHash;
    private AccountRole accountRole;
    private AccountStatus accountStatus;

    public UserSaveDto() {
    }

    public UserSaveDto(String userName, String email, String phone, String passwordHash,
                       AccountRole accountRole, AccountStatus accountStatus) {
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.accountRole = accountRole;
        this.accountStatus = accountStatus;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public AccountRole getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(AccountRole accountRole) {
        this.accountRole = accountRole;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }
}