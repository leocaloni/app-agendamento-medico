package com.pi.agendamento.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

// metadados do exame enviados junto com o arquivo (multipart)
public record CreateExamRequest(
        @NotBlank @Size(max = 150) String title,
        @NotNull @PastOrPresent LocalDate examDate,
        @Size(max = 150) String laboratory
) {
}
