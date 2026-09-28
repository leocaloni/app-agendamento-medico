package com.pi.agendamento.dto.request;

import jakarta.validation.constraints.NotBlank;

// credenciais de login
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password
) {
}
