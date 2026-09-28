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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// catalogo de convenios: leitura publica, escrita so ADMIN
@Tag(name = "Convenios", description = "Leitura publica; escrita somente ADMIN")
@RestController
@RequestMapping("/api")
public class HealthPlanController {

    private final HealthPlanService healthPlanService;

    public HealthPlanController(HealthPlanService healthPlanService) {
        this.healthPlanService = healthPlanService;
    }

    // lista os ativos
    @Operation(summary = "Listar convenios ativos")
    @SecurityRequirements
    @GetMapping("/health-plans")
    public List<HealthPlanResponse> list() {
        return healthPlanService.listActive();
    }

    // busca por id
    @Operation(summary = "Buscar convenio por id")
    @SecurityRequirements
    @GetMapping("/health-plans/{id}")
    public HealthPlanResponse getById(@PathVariable UUID id) {
        return healthPlanService.getById(id);
    }

    // cria e responde 201 com Location
    @Operation(summary = "Criar convenio (ADMIN)")
    @PostMapping("/health-plans")
    public ResponseEntity<HealthPlanResponse> create(@Valid @RequestBody HealthPlanRequest request) {
        HealthPlanResponse response = healthPlanService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    // atualiza e reativa
    @Operation(summary = "Atualizar convenio (ADMIN)")
    @PutMapping("/health-plans/{id}")
    public HealthPlanResponse update(@PathVariable UUID id, @Valid @RequestBody HealthPlanRequest request) {
        return healthPlanService.update(id, request);
    }

    // desativa e responde 204
    @Operation(summary = "Desativar convenio (ADMIN)", description = "Soft delete: marca como inativo.")
    @DeleteMapping("/health-plans/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        healthPlanService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    // lista todos, inclusive inativos
    @Operation(summary = "Listar todos, inclusive inativos (ADMIN)")
    @GetMapping("/admin/health-plans")
    public List<HealthPlanResponse> listAll() {
        return healthPlanService.listAll();
    }
}
