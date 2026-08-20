package dev.codequiz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Отдельный bean, а не "new BCryptPasswordEncoder()" прямо в сервисах —
// чтобы PasswordEncoder можно было внедрять через конструктор (проще
// тестировать, легко подменить мок в юнит-тестах) и чтобы при переходе
// на полноценный Spring Security этот bean просто переиспользовался,
// а не создавался заново.
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}