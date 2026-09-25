package com.pi.agendamento.dto.response;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
