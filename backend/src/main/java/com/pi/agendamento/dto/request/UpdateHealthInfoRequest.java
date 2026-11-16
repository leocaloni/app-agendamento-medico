package com.pi.agendamento.dto.request;

import java.math.BigDecimal;

import com.pi.agendamento.enums.BloodType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

// dados de saude do paciente; PUT substitui tudo, campo null limpa
public record UpdateHealthInfoRequest(
        BloodType bloodType,
        @Min(30) @Max(250) Integer heightCm,
        @DecimalMin("1.0") @DecimalMax("400.0") @Digits(integer = 3, fraction = 2) BigDecimal weightKg,
        @Size(max = 1000) String allergies,
        @Size(max = 1000) String chronicConditions,
        @Size(max = 1000) String continuousMedications,
        @Size(max = 255) String emergencyContactName,
        @Size(max = 20) String emergencyContactPhone
) {
}
