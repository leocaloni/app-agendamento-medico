package com.pi.agendamento.dto.response;

import org.springframework.data.domain.Page;


public record DoctorSearchResponse(
        Page<DoctorSummaryResponse> doctors,
        HealthPlanSummary appliedPlanFilter
) {
}
