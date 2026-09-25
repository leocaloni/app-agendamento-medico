package com.pi.agendamento.dto.response;

import java.util.UUID;

import com.pi.agendamento.entity.HealthPlan;

public record HealthPlanSummary(UUID id, String name) {

    public static HealthPlanSummary from(HealthPlan healthPlan) {
        if (healthPlan == null) {
            return null;
        }
        return new HealthPlanSummary(healthPlan.getId(), healthPlan.getName());
    }
}
