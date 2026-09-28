package com.pi.agendamento.dto.response;

// Mesmo formato de DoctorRatingResponse, sem distribution: usado na busca, onde o grafico nao aparece.
public record RatingSummaryResponse(Double average, long total) {
}
