package com.pi.agendamento.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pi.agendamento.dto.request.HealthPlanRequest;
import com.pi.agendamento.dto.response.HealthPlanResponse;
import com.pi.agendamento.service.HealthPlanService;

import jakarta.validation.Valid;

//Catalogo de convenios: leitura publica, escrita restrita a ADMIN pelo SecurityConfig
@RestController
@RequestMapping("/api")
public class HealthPlanController {

    private final HealthPlanService healthPlanService;

    public HealthPlanController(HealthPlanService healthPlanService) {
        this.healthPlanService = healthPlanService;
    }

    @GetMapping("/health-plans")
    public List<HealthPlanResponse> list() {
        return healthPlanService.listActive();
    }

    @GetMapping("/health-plans/{id}")
    public HealthPlanResponse getById(@PathVariable UUID id) {
        return healthPlanService.getById(id);
    }

    @PostMapping("/health-plans")
    public ResponseEntity<HealthPlanResponse> create(@Valid @RequestBody HealthPlanRequest request) {
        HealthPlanResponse response = healthPlanService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/health-plans/{id}")
    public HealthPlanResponse update(@PathVariable UUID id, @Valid @RequestBody HealthPlanRequest request) {
        return healthPlanService.update(id, request);
    }

    @DeleteMapping("/health-plans/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        healthPlanService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/health-plans")
    public List<HealthPlanResponse> listAll() {
        return healthPlanService.listAll();
    }
}
