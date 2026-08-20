package dev.codequiz.config;

import dev.codequiz.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Без явного SecurityFilterChain Spring Boot сам подставляет дефолтную
// конфигурацию: ВСЕ эндпоинты требуют логина, и включена стандартная
// форма /login — этот бин отключает дефолт и задаёт наши правила.
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
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Чтение категорий — любому авторизованному пользователю
                        // (USER или ADMIN, роль неважна, важен сам факт логина).
                        .requestMatchers(HttpMethod.GET, "/categories/**").authenticated()
                        // Создание/изменение/удаление — только ADMIN. hasRole
                        // сверяется с "ROLE_ADMIN" из UserPrincipal.getAuthorities().
                        .requestMatchers(HttpMethod.POST, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("ADMIN")
                        // Темы — те же правила, что у категорий: чтение всем
                        // авторизованным, запись только ADMIN.
                        .requestMatchers(HttpMethod.GET, "/topics/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/topics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/topics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/topics/**").hasRole("ADMIN")
                        // Вопросы и ответы — целиком ADMIN-only (даже GET),
                        // т.к. QuestionDto/AnswerDto содержат explanation/correct —
                        // см. комментарий в QuestionController.
                        .requestMatchers("/questions/**").hasRole("ADMIN")
                        .requestMatchers("/answers/**").hasRole("ADMIN")
                        // /users/me/** — свой профиль, любому авторизованному.
                        // ВАЖНО: это правило должно идти РАНЬШЕ следующего
                        // (/users/{id}), иначе Spring может сопоставить "me"
                        // как значение {id} и применить ADMIN-ограничение
                        // к собственному профилю обычного пользователя.
                        .requestMatchers("/users/me/**").authenticated()
                        // Просмотр/изменение чужого аккаунта — только ADMIN.
                        .requestMatchers(HttpMethod.GET, "/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/users/{id}/status").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}