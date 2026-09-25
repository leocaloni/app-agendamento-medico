package com.pi.agendamento.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pi.agendamento.dto.request.DoctorSearchFilter;
import com.pi.agendamento.dto.request.UpdateDoctorProfileRequest;
import com.pi.agendamento.dto.request.UpdateScheduleRequest;
import com.pi.agendamento.dto.response.DoctorDetailResponse;
import com.pi.agendamento.dto.response.DoctorSearchResponse;
import com.pi.agendamento.dto.response.TimeSlotResponse;
import com.pi.agendamento.enums.AppointmentType;
import com.pi.agendamento.service.AvailabilityService;
import com.pi.agendamento.service.DoctorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final AvailabilityService availabilityService;

    public DoctorController(DoctorService doctorService, AvailabilityService availabilityService) {
        this.doctorService = doctorService;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public DoctorSearchResponse search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID specialtyId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) UUID healthPlanId,
            @RequestParam(defaultValue = "false") boolean allPlans,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        DoctorSearchFilter filter = new DoctorSearchFilter(
                name, specialtyId, city, state, minRating, healthPlanId, allPlans);
        return doctorService.search(filter, pageable);
    }

    @GetMapping("/me")
    public DoctorDetailResponse getCurrent() {
        return doctorService.getCurrent();
    }

    @PutMapping("/me")
    public DoctorDetailResponse updateProfile(@Valid @RequestBody UpdateDoctorProfileRequest request) {
        return doctorService.updateProfile(request);
    }

    @PutMapping("/me/schedule")
    public DoctorDetailResponse updateSchedule(@Valid @RequestBody UpdateScheduleRequest request) {
        return doctorService.updateSchedule(request);
    }

    @GetMapping("/{id}")
    public DoctorDetailResponse getById(@PathVariable UUID id) {
        return doctorService.getById(id);
    }

    // Grade gerada em memoria; nao ha tabela de horarios.
    @GetMapping("/{id}/availability")
    public List<TimeSlotResponse> availability(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "PRIMEIRA_CONSULTA") AppointmentType type) {
        return availabilityService.getAvailability(id, from, to, type);
    }
}
