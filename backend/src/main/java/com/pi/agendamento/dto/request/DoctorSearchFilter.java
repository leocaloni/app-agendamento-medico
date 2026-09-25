package com.pi.agendamento.dto.request;

import java.util.UUID;

// Filtros de GET /api/doctors. Todos opcionais e combinaveis.
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
