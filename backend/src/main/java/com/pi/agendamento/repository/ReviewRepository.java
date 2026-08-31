package com.pi.agendamento.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pi.agendamento.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
}
