package com.pi.agendamento.dto.response;

import java.util.UUID;

import com.pi.agendamento.entity.HealthPlan;

public record HealthPlanResponse(UUID id, String name, boolean active) {

    public static HealthPlanResponse from(HealthPlan healthPlan) {
        return new HealthPlanResponse(healthPlan.getId(), healthPlan.getName(), healthPlan.isActive());
    }
}
