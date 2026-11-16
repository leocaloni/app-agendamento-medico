package com.pi.agendamento.dto.response;

import java.math.BigDecimal;

import com.pi.agendamento.entity.HealthInfo;
import com.pi.agendamento.enums.BloodType;

// dados de saude do paciente
public record HealthInfoResponse(
        BloodType bloodType,
        Integer heightCm,
        BigDecimal weightKg,
        String allergies,
        String chronicConditions,
        String continuousMedications,
        String emergencyContactName,
        String emergencyContactPhone
) {

    // converte o embeddable; null vira tudo vazio
    public static HealthInfoResponse from(HealthInfo info) {
        if (info == null) {
            return new HealthInfoResponse(null, null, null, null, null, null, null, null);
        }
        return new HealthInfoResponse(
                info.getBloodType(),
                info.getHeightCm(),
                info.getWeightKg(),
                info.getAllergies(),
                info.getChronicConditions(),
                info.getContinuousMedications(),
                info.getEmergencyContactName(),
                info.getEmergencyContactPhone());
    }
}
