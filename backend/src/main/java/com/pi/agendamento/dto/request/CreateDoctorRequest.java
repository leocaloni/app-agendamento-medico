package com.pi.agendamento.dto.request;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateDoctorRequest(
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "deve conter 11 digitos, sem mascara") String cpf,
        @Size(max = 20) String phone,
        @Past LocalDate birthDate,

        @NotBlank @Size(max = 20) String crmNumber,
        @NotBlank @Pattern(regexp = "[A-Za-z]{2}", message = "deve ser a sigla da UF com 2 letras") String crmUf,
        @Size(max = 2000) String bio,
        @Size(max = 255) String city,
        @Size(max = 255) String state,
        @Size(max = 255) String address,
        @NotNull LocalTime workStartTime,
        @NotNull LocalTime workEndTime,
        @NotEmpty Set<@NotNull DayOfWeek> workDays,
        @Positive int firstVisitDurationMin,
        @Positive int returnDurationMin,
        @NotEmpty Set<@NotNull UUID> specialtyIds,
        Set<@NotNull UUID> acceptedPlanIds
) {
}
