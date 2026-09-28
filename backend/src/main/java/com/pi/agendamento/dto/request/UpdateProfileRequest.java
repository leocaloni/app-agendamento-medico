package com.pi.agendamento.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;


// edicao do proprio perfil
public record UpdateProfileRequest(
        @NotBlank @Size(max = 255) String fullName,
        @Size(max = 20) String phone,
        @Past LocalDate birthDate,
        UUID healthPlanId
) {
}
