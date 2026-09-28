package com.pi.agendamento.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.pi.agendamento.entity.Review;

// avaliacao publica; so o primeiro nome para nao expor quem consultou com quem
public record ReviewResponse(
        UUID id,
        int rating,
        String comment,
        String authorFirstName,
        Instant createdAt
) {

    // converte a entidade em response
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getAuthor().getFirstName(),
                review.getCreatedAt());
    }
}
