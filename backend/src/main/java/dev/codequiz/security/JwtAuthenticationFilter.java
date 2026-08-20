package dev.codequiz.security;

import dev.codequiz.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// OncePerRequestFilter гарантирует, что этот код выполнится ровно один раз
// на запрос (а не повторно при внутренних forward/include) — стандартная
// база для написания собственных Servlet-фильтров.
//
// Что делает: смотрит заголовок Authorization, и если там валидный
// "Bearer <токен>" — сам "логинит" пользователя в SecurityContext на время
// этого запроса. JWT здесь заменяет собой сессию: никакого состояния между
// запросами сервер не хранит (см. SessionCreationPolicy.STATELESS в
// SecurityConfig) — каждый запрос доказывает, кто он, заново своим токеном.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Нет заголовка или он не в формате "Bearer ..." — не наша забота,
        // пропускаем запрос дальше как есть. Если эндпоинт защищённый,
        // дальше по цепочке Spring Security сам вернёт 401 — этот фильтр
        // не решает, разрешён доступ или нет, только устанавливает "личность".
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        String email;
        try {
            email = jwtService.extractEmail(token);
        } catch (Exception e) {
            // Битый/просроченный/с неверной подписью токен — не 500 ошибка,
            // просто продолжаем без аутентификации, дальше решит SecurityConfig.
            filterChain.doFilter(request, response);
            return;
        }

        // Проверка "аутентификация ещё не установлена" — на случай, если
        // выше по цепочке фильтров это уже сделал кто-то другой (например,
        // при нескольких SecurityFilterChain), не перезаписываем это дважды.
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}