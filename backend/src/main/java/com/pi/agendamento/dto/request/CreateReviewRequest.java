package com.pi.agendamento.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

// nota e comentario da avaliacao
public record CreateReviewRequest(
        @Min(1) @Max(5) int rating,
        @Size(max = 1000) String comment
) {
}
