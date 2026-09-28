package com.pi.agendamento.dto.response;

// token jwt e usuario autenticado
public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
