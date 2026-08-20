package dev.codequiz.config;

import dev.codequiz.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Без явного SecurityFilterChain Spring Boot сам подставляет дефолтную
// конфигурацию: ВСЕ эндпоинты требуют логина, и включена стандартная
// форма /login (см. "Please sign in" в браузере) — именно её вы и видели.
// Этот бин отключает дефолт и задаёт наши правила: /auth/** и Swagger
// открыты без токена, всё остальное — только с валидным JWT.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF-защита нужна для форм с сессионными cookie — у нас
                // stateless JWT API, куки для аутентификации не используются,
                // поэтому CSRF отключаем.
                .csrf(csrf -> csrf.disable())
                // STATELESS — сервер не хранит сессию пользователя между
                // запросами, каждый запрос аутентифицируется заново по
                // токену из заголовка Authorization.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Наш фильтр проверяет JWT из заголовка Authorization ДО
                // стандартного фильтра логина по логину/паролю — он нам
                // не нужен, но заменить его сразу нельзя, поэтому просто
                // ставим свой фильтр перед ним в цепочке.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}