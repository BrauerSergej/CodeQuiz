package dev.codequiz.security.dto;

import dev.codequiz.dto.user.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;

// Response-объект логина/рефреша — пара токенов, которую клиент должен
// сохранить: accessToken подставляется в заголовок
// "Authorization: Bearer <accessToken>" во все обычные запросы,
// refreshToken хранится отдельно (не в localStorage вместе с access —
// это ответственность фронтенда) и используется только для запроса
// POST /auth/refresh, когда accessToken истёк.
@Schema(description = "Результат успешного входа: пара JWT-токенов и данные пользователя")
public class AuthResponseDto {

    @Schema(description = "Короткоживущий токен для обычных запросов")
    private String accessToken;

    @Schema(description = "Долгоживущий токен для обновления accessToken через /auth/refresh")
    private String refreshToken;

    @Schema(description = "Данные вошедшего пользователя")
    private UserDto user;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String accessToken, String refreshToken, UserDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}