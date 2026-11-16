package com.pi.agendamento.dto.request;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// edicao do perfil do medico logado
public record UpdateDoctorProfileRequest(
        @Size(max = 2000) String bio,
        @Size(max = 255) String city,
        @Size(max = 255) String state,
        @Size(max = 255) String address,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal consultationPrice,
        @NotEmpty Set<@NotNull UUID> specialtyIds,
        // vazio = so atende particular
        Set<@NotNull UUID> acceptedPlanIds
) {
}
