package dev.codequiz.security.service;

import dev.codequiz.domain.User;
import dev.codequiz.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

// Отвечает только за сам токен: создать, прочитать, проверить подпись/срок
// действия. НЕ отвечает за то, кто и когда его выдаёт (это AuthController)
// и не ходит в БД (это UserRepository/CustomUserDetailsService) — чистая
// функция вокруг библиотеки jjwt.
//
// Access и refresh токены подписываются РАЗНЫМИ ключами (accessKey/refreshKey)
// и живут разное время: access — короткий (по умолчанию 15 минут), чтобы даже
// его утечка была не критична; refresh — длинный (по умолчанию 7 дней),
// используется только для получения нового access-токена через /auth/refresh,
// сам по себе доступ к защищённым эндпоинтам не даёт (см. isTokenValid —
// защищённые эндпоинты проверяют токен именно accessKey).
@Service
public class JwtService {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    // Время жизни токенов — константы прямо в коде, как в g_67_shop
    // (там же не через конфиг, а просто числа в generateAccessToken/
    // generateRefreshToken). Это не секрет, поэтому не обязательно
    // выносить в переменные окружения.
    private static final long ACCESS_EXPIRATION_MILLIS = 15 * 60 * 1000;           // 15 минут
    private static final long REFRESH_EXPIRATION_MILLIS = 7 * 24 * 60 * 60 * 1000; // 7 дней

    // @Value смотрит напрямую на имя переменной окружения (без слоя
    // application-local.yaml, как было раньше с jwt.access-secret) —
    // JWT_ACCESS_SECRET и JWT_REFRESH_SECRET обязаны быть заданы в
    // окружении, иначе приложение не запустится.
    public JwtService(@Value("${JWT_ACCESS_SECRET}") String accessSecret,
                      @Value("${JWT_REFRESH_SECRET}") String refreshSecret) {
        // Decoders.BASE64.decode — секреты хранятся в Base64, а не как
        // обычный текстовый пароль: HS256 требует ключ ровно нужной
        // битовой длины (минимум 256 бит), а не произвольную строку.
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
    }

    // subject = email (то же значение, что UserPrincipal.getUsername());
    // userId и role добавлены отдельными claim'ами, чтобы их можно было
    // прочитать из токена без похода в БД.
    public String generateAccessToken(UserPrincipal principal) {
        return generateToken(principal, accessKey, ACCESS_EXPIRATION_MILLIS);
    }

    // Refresh-токен намеренно НЕ несёт claim role — роль могла смениться
    // (например, админ заблокировал/понизил пользователя) за долгое время
    // жизни refresh-токена, а /auth/refresh всё равно должен подтягивать
    // актуального пользователя из БД перед выпуском нового access-токена.
    public String generateRefreshToken(UserPrincipal principal) {
        User user = principal.getUser();
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(REFRESH_EXPIRATION_MILLIS)))
                .signWith(refreshKey)
                .compact();
    }

    private String generateToken(UserPrincipal principal, SecretKey key, long expirationMillis) {
        User user = principal.getUser();
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getAccountRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String accessToken) {
        return extractAllClaims(accessToken, accessKey).getSubject();
    }

    public Long extractUserId(String accessToken) {
        return extractAllClaims(accessToken, accessKey).get("userId", Long.class);
    }

    // Проверяет ДВЕ вещи: (1) что подпись валидна и токен не истёк — это
    // делает сам extractAllClaims, бросая исключение при проблеме,
    // (2) что email внутри токена совпадает с email пользователя, для
    // которого сейчас загружены свежие данные из БД через UserDetailsService.
    // Используется только для access-токена — именно он проверяется на
    // каждый защищённый запрос в JwtAuthenticationFilter.
    public boolean isTokenValid(String accessToken, UserDetails userDetails) {
        try {
            String email = extractEmail(accessToken);
            return email.equals(userDetails.getUsername())
                    && !isExpired(accessToken, accessKey);
        } catch (JwtException | IllegalArgumentException e) {
            // Некорректная подпись, битый токен, неверный формат — всё это
            // означает "токен невалиден", а не "500 ошибка сервера".
            return false;
        }
    }

    // Извлекает email из refresh-токена И проверяет его подлинность/срок
    // жизни (refreshKey) за один проход — используется в /auth/refresh
    // перед тем как выпустить новую пару токенов. Бросает JwtException,
    // если refresh-токен битый/просрочен/подписан не тем ключом —
    // AuthController должен это исключение поймать и вернуть 401.
    public String extractEmailFromRefreshToken(String refreshToken) {
        return extractAllClaims(refreshToken, refreshKey).getSubject();
    }

    private boolean isExpired(String token, SecretKey key) {
        return extractAllClaims(token, key).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}