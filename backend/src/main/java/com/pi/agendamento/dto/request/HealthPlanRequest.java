package com.pi.agendamento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// criacao ou edicao de convenio
public record HealthPlanRequest(
        @NotBlank @Size(max = 255) String name
) {
}
