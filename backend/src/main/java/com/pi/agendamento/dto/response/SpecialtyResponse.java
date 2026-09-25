package com.pi.agendamento.dto.response;

import java.util.UUID;

import com.pi.agendamento.entity.Specialty;

public record SpecialtyResponse(
        UUID id,
        String name,
        String description,
        boolean requiresPriorExams,
        String examRequestMessage,
        boolean active
) {

    public static SpecialtyResponse from(Specialty specialty) {
        return new SpecialtyResponse(
                specialty.getId(),
                specialty.getName(),
                specialty.getDescription(),
                specialty.isRequiresPriorExams(),
                specialty.getExamRequestMessage(),
                specialty.isActive());
    }
}
