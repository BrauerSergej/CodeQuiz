package dev.codequiz.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh-токен для получения новой пары access/refresh токенов")
public class RefreshTokenDto {

    @NotBlank
    @Schema(description = "Ранее выданный refresh-токен")
    private String refreshToken;

    public RefreshTokenDto() {
    }

    public RefreshTokenDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}