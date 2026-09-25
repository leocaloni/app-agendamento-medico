package com.pi.agendamento.dto.response;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.pi.agendamento.entity.Doctor;
import com.pi.agendamento.entity.HealthPlan;
import com.pi.agendamento.entity.Specialty;

public record DoctorResponse(
        UUID id,
        UserResponse user,
        String crmNumber,
        String crmUf,
        String bio,
        String city,
        String state,
        String address,
        LocalTime workStartTime,
        LocalTime workEndTime,
        Set<DayOfWeek> workDays,
        int firstVisitDurationMin,
        int returnDurationMin,
        Set<UUID> specialtyIds,
        Set<UUID> acceptedPlanIds
) {

    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                UserResponse.from(doctor.getUser()),
                doctor.getCrmNumber(),
                doctor.getCrmUf(),
                doctor.getBio(),
                doctor.getCity(),
                doctor.getState(),
                doctor.getAddress(),
                doctor.getWorkStartTime(),
                doctor.getWorkEndTime(),
                Set.copyOf(doctor.getWorkDays()),
                doctor.getFirstVisitDurationMin(),
                doctor.getReturnDurationMin(),
                doctor.getSpecialties().stream().map(Specialty::getId).collect(Collectors.toSet()),
                doctor.getAcceptedPlans().stream().map(HealthPlan::getId).collect(Collectors.toSet()));
    }
}
