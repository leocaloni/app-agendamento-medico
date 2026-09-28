package com.pi.agendamento.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pi.agendamento.dto.response.RatingStats;
import com.pi.agendamento.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByAppointmentId(UUID appointmentId);

    // author no EntityGraph: o response le o primeiro nome, e sem ele cada linha dispara uma query.
    @EntityGraph(attributePaths = "author")
    Page<Review> findByDoctorId(UUID doctorId, Pageable pageable);

    // Medico e consulta no EntityGraph pelo mesmo motivo: MyReviewResponse le nome e startAt.
    @EntityGraph(attributePaths = {"doctor", "doctor.user", "appointment"})
    List<Review> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);

    // Sem avaliacao: average null e total 0.
    @Query("""
            SELECT new com.pi.agendamento.dto.response.RatingStats(
                AVG(r.rating), COUNT(r.id))
            FROM Review r WHERE r.doctor.id = :doctorId
            """)
    RatingStats getStatsByDoctorId(@Param("doctorId") UUID doctorId);

    // Linhas [rating, count]; notas sem avaliacao nao aparecem.
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.doctor.id = :doctorId GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("doctorId") UUID doctorId);
}
