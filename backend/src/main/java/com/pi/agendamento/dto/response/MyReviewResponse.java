package com.pi.agendamento.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.pi.agendamento.entity.Review;

// avaliacao do proprio paciente; separada da publica para nao expor a data da consulta
public record MyReviewResponse(
        UUID id,
        int rating,
        String comment,
        Instant createdAt,
        UUID doctorId,
        String doctorName,
        Instant appointmentStartAt
) {

    // converte a entidade em response
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
