package com.pi.agendamento.dto.response;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.pi.agendamento.entity.Doctor;
import com.pi.agendamento.entity.HealthPlan;
import com.pi.agendamento.entity.Specialty;

// medico resumido para a listagem da busca
public record DoctorSummaryResponse(
        UUID id,
        String fullName,
        String crm,
        String city,
        String state,
        Set<String> specialties,
        Set<String> acceptedPlans,
        RatingSummaryResponse rating
) {

    // monta o resumo com a nota ja calculada na busca
    public static DoctorSummaryResponse from(Doctor doctor, Double ratingAvg, long ratingCount) {
        return new DoctorSummaryResponse(
                doctor.getId(),
                doctor.getUser().getFullName(),
                crm(doctor),
                doctor.getCity(),
                doctor.getState(),
                names(doctor.getSpecialties().stream().map(Specialty::getName).toList()),
                names(doctor.getAcceptedPlans().stream().map(HealthPlan::getName).toList()),
                new RatingSummaryResponse(ratingAvg, ratingCount));
    }

    static String crm(Doctor doctor) {
        return doctor.getCrmNumber() + "/" + doctor.getCrmUf();
    }

    private static Set<String> names(Iterable<String> values) {
        Set<String> sorted = new TreeSet<>();
        values.forEach(sorted::add);
        return sorted;
    }
}
