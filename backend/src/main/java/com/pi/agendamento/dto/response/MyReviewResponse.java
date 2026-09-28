package com.pi.agendamento.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.pi.agendamento.entity.Review;

// Resposta de GET /api/reviews/me. Separada de ReviewResponse de proposito: a data da consulta
// ao lado do primeiro nome, na listagem publica, diria quando cada paciente foi atendido.
public record MyReviewResponse(
        UUID id,
        int rating,
        String comment,
        Instant createdAt,
        UUID doctorId,
        String doctorName,
        Instant appointmentStartAt
) {

    public static MyReviewResponse from(Review review) {
        return new MyReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getDoctor().getId(),
                review.getDoctor().getUser().getFullName(),
                review.getAppointment().getStartAt());
    }
}
