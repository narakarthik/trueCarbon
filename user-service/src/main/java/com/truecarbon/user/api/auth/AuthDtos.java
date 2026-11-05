package com.truecarbon.user.api.auth;

import jakarta.validation.constraints.*;

public class AuthDtos {

    public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 120) String name,
        @NotBlank @Email                    String email,
        @NotBlank @Size(min = 8, max = 72)  String password
    ) {}

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank        String password
    ) {}

    public record RefreshRequest(
        @NotBlank String refreshToken
    ) {}

    public record AuthResponse(
        String      accessToken,
        String      refreshToken,
        String      tokenType,
        long        expiresIn,
        UserInfo    user
    ) {
        public static final String BEARER = "Bearer";
        public record UserInfo(String id, String name, String email) {}
    }
}
