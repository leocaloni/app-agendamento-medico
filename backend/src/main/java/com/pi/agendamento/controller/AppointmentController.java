package com.pi.agendamento.controller;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pi.agendamento.dto.request.CancelAppointmentRequest;
import com.pi.agendamento.dto.request.CreateAppointmentRequest;
import com.pi.agendamento.dto.request.UpdateAppointmentNotesRequest;
import com.pi.agendamento.dto.response.AppointmentResponse;
import com.pi.agendamento.enums.AppointmentStatus;
import com.pi.agendamento.service.AppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    // Mesma rota para paciente e medico: o service olha a role do usuario logado.
    @GetMapping("/me")
    public List<AppointmentResponse> listMine(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) AppointmentStatus status) {
        return appointmentService.listMine(from, to, status);
    }

    @GetMapping("/{id}")
    public AppointmentResponse getById(@PathVariable UUID id) {
        return appointmentService.getById(id);
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable UUID id,
                                      @Valid @RequestBody(required = false) CancelAppointmentRequest request) {
        return appointmentService.cancel(id, request != null ? request.reason() : null);
    }

    @PatchMapping("/{id}/complete")
    public AppointmentResponse complete(@PathVariable UUID id) {
        return appointmentService.complete(id);
    }

    @PatchMapping("/{id}/no-show")
    public AppointmentResponse markNoShow(@PathVariable UUID id) {
        return appointmentService.markNoShow(id);
    }

    @PatchMapping("/{id}/notes")
    public AppointmentResponse updateNotes(@PathVariable UUID id,
                                           @Valid @RequestBody UpdateAppointmentNotesRequest request) {
        return appointmentService.updateNotes(id, request.doctorNotes());
    }
}
