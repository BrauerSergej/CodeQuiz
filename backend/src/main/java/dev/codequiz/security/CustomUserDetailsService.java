package dev.codequiz.security;

import dev.codequiz.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Реализация UserDetailsService — контракта Spring Security "по имени
// пользователя достать его данные". JwtAuthenticationFilter вызывает этот
// метод на КАЖДЫЙ запрос с валидным токеном, а не один раз при логине —
// поэтому статус аккаунта (см. UserPrincipal.isEnabled/isAccountNonLocked)
// всегда проверяется по актуальным данным из БД, а не по "снимку" на момент
// выдачи токена.
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
    }
}
