package com.pi.agendamento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpecialtyRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String description,
        boolean requiresPriorExams,
        @Size(max = 1000) String examRequestMessage
) {
}
