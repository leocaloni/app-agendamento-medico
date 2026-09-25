package com.pi.agendamento.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pi.agendamento.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // null quando o medico ainda nao tem nenhuma avaliacao
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor.id = :doctorId")
    Double findAverageRatingByDoctorId(@Param("doctorId") UUID doctorId);

    long countByDoctorId(UUID doctorId);
}
