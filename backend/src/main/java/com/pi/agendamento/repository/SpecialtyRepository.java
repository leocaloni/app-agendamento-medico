package com.pi.agendamento.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pi.agendamento.entity.Specialty;

public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {
}
