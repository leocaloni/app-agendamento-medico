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

// acesso a avaliacoes e estatisticas de nota
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // indica se a consulta ja foi avaliada
    boolean existsByAppointmentId(UUID appointmentId);

    // avaliacoes do medico, com autor carregado junto
    @EntityGraph(attributePaths = "author")
    Page<Review> findByDoctorId(UUID doctorId, Pageable pageable);

    // avaliacoes do paciente, com medico e consulta carregados junto
    @EntityGraph(attributePaths = {"doctor", "doctor.user", "appointment"})
    List<Review> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);

    // media e total do medico; sem avaliacao vem media null e total 0
    @Query("""
            SELECT new com.pi.agendamento.dto.response.RatingStats(
                AVG(r.rating), COUNT(r.id))
            FROM Review r WHERE r.doctor.id = :doctorId
            """)
    RatingStats getStatsByDoctorId(@Param("doctorId") UUID doctorId);

    // quantidade por nota; notas sem avaliacao nao aparecem
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.doctor.id = :doctorId GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("doctorId") UUID doctorId);
}
