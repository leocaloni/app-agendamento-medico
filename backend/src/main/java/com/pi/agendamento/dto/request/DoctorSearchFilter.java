package com.pi.agendamento.dto.request;

import java.util.UUID;

// filtros opcionais da busca de medicos
public record DoctorSearchFilter(
        String name,
        UUID specialtyId,
        String city,
        String state,
        Double minRating,
        UUID healthPlanId,
        boolean allPlans
) {
}
