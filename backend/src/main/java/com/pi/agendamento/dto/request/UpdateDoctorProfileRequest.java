package com.pi.agendamento.dto.request;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDoctorProfileRequest(
        @Size(max = 2000) String bio,
        @Size(max = 255) String city,
        @Size(max = 255) String state,
        @Size(max = 255) String address,
        @NotEmpty Set<@NotNull UUID> specialtyIds,
        // Lista vazia eh valida e significa "so atendo particular".
        Set<@NotNull UUID> acceptedPlanIds
) {
}
