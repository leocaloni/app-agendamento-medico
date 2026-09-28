package com.pi.agendamento.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// criacao de admin
public record CreateAdminRequest(
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "deve conter 11 digitos, sem mascara") String cpf,
        @Size(max = 20) String phone,
        @Past LocalDate birthDate
) {
}
