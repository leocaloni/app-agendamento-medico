package com.pi.agendamento.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pi.agendamento.entity.HealthPlan;

public interface HealthPlanRepository extends JpaRepository<HealthPlan, UUID> {

    List<HealthPlan> findByActiveTrueOrderByNameAsc();

    List<HealthPlan> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
