package com.pi.agendamento.dto.response;

import org.springframework.data.domain.Page;


// pagina da busca de medicos e o convenio aplicado no filtro
public record DoctorSearchResponse(
        Page<DoctorSummaryResponse> doctors,
        HealthPlanSummary appliedPlanFilter
) {
}
