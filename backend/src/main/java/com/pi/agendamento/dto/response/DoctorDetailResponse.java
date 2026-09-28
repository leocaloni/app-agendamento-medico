package com.pi.agendamento.dto.response;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import com.pi.agendamento.entity.Doctor;

public record DoctorDetailResponse(
        UUID id,
        String fullName,
        String crm,
        String bio,
        String city,
        String state,
        String address,
        Set<DayOfWeek> workDays,
        LocalTime workStartTime,
        LocalTime workEndTime,
        int firstVisitDurationMin,
        int returnDurationMin,
        Set<SpecialtyResponse> specialties,
        Set<HealthPlanResponse> acceptedPlans,
        DoctorRatingResponse rating
) {

    public static DoctorDetailResponse from(Doctor doctor, DoctorRatingResponse rating) {
        return new DoctorDetailResponse(
                doctor.getId(),
                doctor.getUser().getFullName(),
                DoctorSummaryResponse.crm(doctor),
                doctor.getBio(),
                doctor.getCity(),
                doctor.getState(),
                doctor.getAddress(),
                new TreeSet<>(doctor.getWorkDays()),
                doctor.getWorkStartTime(),
                doctor.getWorkEndTime(),
                doctor.getFirstVisitDurationMin(),
                doctor.getReturnDurationMin(),
                doctor.getSpecialties().stream()
                        .map(SpecialtyResponse::from)
                        .sorted(Comparator.comparing(SpecialtyResponse::name))
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                doctor.getAcceptedPlans().stream()
                        .map(HealthPlanResponse::from)
                        .sorted(Comparator.comparing(HealthPlanResponse::name))
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                rating);
    }
}
