package com.pi.agendamento.dto.response;

import java.util.UUID;

import com.pi.agendamento.entity.HealthPlan;

// convenio do catalogo
public record HealthPlanResponse(UUID id, String name, boolean active) {

    // converte a entidade em response
    public static HealthPlanResponse from(HealthPlan healthPlan) {
        return new HealthPlanResponse(healthPlan.getId(), healthPlan.getName(), healthPlan.isActive());
    }
}
