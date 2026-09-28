package com.pi.agendamento.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pi.agendamento.entity.Specialty;

// acesso a especialidades
public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {

    // lista as ativas por nome
    List<Specialty> findByActiveTrueOrderByNameAsc();

    // lista todas por nome, inclusive inativas
    List<Specialty> findAllByOrderByNameAsc();

    // indica se o nome ja existe, ignorando caixa
    boolean existsByNameIgnoreCase(String name);
}
