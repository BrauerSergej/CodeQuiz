package dev.codequiz.security;

import dev.codequiz.domain.User;
import dev.codequiz.domain.enums.AccountStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

// Spring Security работает не с вашей entity User напрямую, а с интерфейсом
// UserDetails — это обёртка (адаптер), которая "переводит" ваши поля
// (accountRole, accountStatus, passwordHash) на язык, понятный Spring Security
// (getAuthorities, isEnabled, getPassword). Сам User внутри не меняется.
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    // Пригодится там, где после аутентификации нужен полный доступ к данным
    // пользователя (например, чтобы достать id для JwtService.generateAccessToken).
    public User getUser() {
        return user;
    }

    // Spring Security ожидает роли с префиксом "ROLE_" — это его внутренняя
    // конвенция для hasRole("USER") в правилах доступа (см. SecurityConfig).
    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getAccountRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    // Логин у нас идёт по email (см. UserRepository.findByEmail), поэтому
    // "username" с точки зрения Spring Security — это email пользователя,
    // а не поле userName в entity.
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // false для BLOCKED — эта проверка выполняется заново при КАЖДОМ запросе
    // (см. JwtAuthenticationFilter, который каждый раз заново вызывает
    // CustomUserDetailsService), поэтому если администратор заблокирует
    // пользователя, уже выданный ранее JWT-токен перестанет работать на
    // следующий же запрос — не нужно ждать истечения токена.
    @Override
    public boolean isAccountNonLocked() {
        return user.getAccountStatus() != AccountStatus.BLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // false для UNCONFIRMED и DELETED — неподтверждённый email не должен давать
    // доступ к защищённым эндпоинтам, даже если каким-то образом получить токен.
    @Override
    public boolean isEnabled() {
        return user.getAccountStatus() == AccountStatus.ACTIVE;
    }
}