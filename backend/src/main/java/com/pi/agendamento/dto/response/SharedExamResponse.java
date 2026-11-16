package com.pi.agendamento.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.pi.agendamento.entity.Exam;

// exame visto pelo medico; sem as outras consultas do paciente
public record SharedExamResponse(
        UUID id,
        String title,
        LocalDate examDate,
        String laboratory,
        String fileName,
        String contentType,
        long sizeBytes,
        Instant uploadedAt
) {

    // converte a entidade em response
    public static SharedExamResponse from(Exam exam) {
        return new SharedExamResponse(
                exam.getId(),
                exam.getTitle(),
                exam.getExamDate(),
                exam.getLaboratory(),
                exam.getFileName(),
                exam.getContentType(),
                exam.getSizeBytes(),
                exam.getUploadedAt());
    }
}
