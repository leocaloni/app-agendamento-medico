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

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// rotas de busca, perfil e agenda de medicos
@Tag(name = "Medicos")
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final AvailabilityService availabilityService;

    public DoctorController(DoctorService doctorService, AvailabilityService availabilityService) {
        this.doctorService = doctorService;
        this.availabilityService = availabilityService;
    }

    // busca paginada com filtros opcionais
    @Operation(summary = "Buscar medicos",
            description = "Publico; token opcional. Com token de PATIENT e sem healthPlanId, filtra pelo "
                    + "convenio do paciente, a menos que allPlans=true. sort aceita `name` e `rating`.")
    @GetMapping
    public DoctorSearchResponse search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID specialtyId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) UUID healthPlanId,
            @RequestParam(defaultValue = "false") boolean allPlans,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        DoctorSearchFilter filter = new DoctorSearchFilter(
                name, specialtyId, city, state, minRating, healthPlanId, allPlans);
        return doctorService.search(filter, pageable);
    }

    // perfil do medico logado
    @Operation(summary = "Perfil do medico logado (DOCTOR)")
    @GetMapping("/me")
    public DoctorDetailResponse getCurrent() {
        return doctorService.getCurrent();
    }

    // atualiza perfil, especialidades e convenios
    @Operation(summary = "Atualizar perfil, especialidades e convenios (DOCTOR)")
    @PutMapping("/me")
    public DoctorDetailResponse updateProfile(@Valid @RequestBody UpdateDoctorProfileRequest request) {
        return doctorService.updateProfile(request);
    }

    // atualiza expediente e duracoes
    @Operation(summary = "Atualizar expediente e duracoes (DOCTOR)")
    @PutMapping("/me/schedule")
    public DoctorDetailResponse updateSchedule(@Valid @RequestBody UpdateScheduleRequest request) {
        return doctorService.updateSchedule(request);
    }

    // perfil publico com nota
    @Operation(summary = "Perfil publico do medico", description = "Inclui a nota com distribuicao 1-5.")
    @SecurityRequirements
    @GetMapping("/{id}")
    public DoctorDetailResponse getById(@PathVariable UUID id) {
        return doctorService.getById(id);
    }

    // calcula os horarios livres do medico no periodo
    @Operation(summary = "Horarios livres do medico",
            description = "from/to em yyyy-MM-dd; horarios devolvidos em UTC.")
    @GetMapping("/{id}/availability")
    public List<TimeSlotResponse> availability(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "PRIMEIRA_CONSULTA") AppointmentType type) {
        return availabilityService.getAvailability(id, from, to, type);
    }
}
