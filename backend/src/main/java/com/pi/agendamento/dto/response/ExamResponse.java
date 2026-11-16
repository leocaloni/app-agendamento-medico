package com.pi.agendamento.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.pi.agendamento.entity.Appointment;
import com.pi.agendamento.entity.Exam;

// exame na biblioteca do proprio paciente, com as consultas com que foi compartilhado
public record ExamResponse(
        UUID id,
        String title,
        LocalDate examDate,
        String laboratory,
        String fileName,
        String contentType,
        long sizeBytes,
        Instant uploadedAt,
        Set<UUID> sharedAppointmentIds
) {

    // converte a entidade em response
    public static ExamResponse from(Exam exam) {
        return new ExamResponse(
                exam.getId(),
                exam.getTitle(),
                exam.getExamDate(),
                exam.getLaboratory(),
                exam.getFileName(),
                exam.getContentType(),
                exam.getSizeBytes(),
                exam.getUploadedAt(),
                exam.getSharedWith().stream().map(Appointment::getId).collect(Collectors.toSet()));
    }
}
