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

import com.pi.agendamento.dto.request.SpecialtyRequest;
import com.pi.agendamento.dto.response.SpecialtyResponse;
import com.pi.agendamento.service.SpecialtyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// catalogo de especialidades: leitura publica, escrita so ADMIN
@Tag(name = "Especialidades", description = "Leitura publica; escrita somente ADMIN")
@RestController
@RequestMapping("/api")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    // lista as ativas
    @Operation(summary = "Listar especialidades ativas")
    @SecurityRequirements
    @GetMapping("/specialties")
    public List<SpecialtyResponse> list() {
        return specialtyService.listActive();
    }

    // busca por id
    @Operation(summary = "Buscar especialidade por id")
    @SecurityRequirements
    @GetMapping("/specialties/{id}")
    public SpecialtyResponse getById(@PathVariable UUID id) {
        return specialtyService.getById(id);
    }

    // cria e responde 201 com Location
    @Operation(summary = "Criar especialidade (ADMIN)")
    @PostMapping("/specialties")
    public ResponseEntity<SpecialtyResponse> create(@Valid @RequestBody SpecialtyRequest request) {
        SpecialtyResponse response = specialtyService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    // atualiza e reativa
    @Operation(summary = "Atualizar especialidade (ADMIN)")
    @PutMapping("/specialties/{id}")
    public SpecialtyResponse update(@PathVariable UUID id, @Valid @RequestBody SpecialtyRequest request) {
        return specialtyService.update(id, request);
    }

    // desativa e responde 204
    @Operation(summary = "Desativar especialidade (ADMIN)", description = "Soft delete: marca como inativa.")
    @DeleteMapping("/specialties/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        specialtyService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    // lista todas, inclusive inativas
    @Operation(summary = "Listar todas, inclusive inativas (ADMIN)")
    @GetMapping("/admin/specialties")
    public List<SpecialtyResponse> listAll() {
        return specialtyService.listAll();
    }
}
