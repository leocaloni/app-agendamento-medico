package com.pi.agendamento.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pi.agendamento.entity.HealthPlan;

// acesso a convenios
public interface HealthPlanRepository extends JpaRepository<HealthPlan, UUID> {

    // lista os ativos por nome
    List<HealthPlan> findByActiveTrueOrderByNameAsc();

    // lista todos por nome, inclusive inativos
    List<HealthPlan> findAllByOrderByNameAsc();

    // indica se o nome ja existe, ignorando caixa
    boolean existsByNameIgnoreCase(String name);
}
