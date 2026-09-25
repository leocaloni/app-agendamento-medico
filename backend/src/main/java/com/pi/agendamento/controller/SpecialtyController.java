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

import jakarta.validation.Valid;

//Catalogo de especialidades: leitura publica, escrita restrita a ADMIN pelo SecurityConfig
@RestController
@RequestMapping("/api")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @GetMapping("/specialties")
    public List<SpecialtyResponse> list() {
        return specialtyService.listActive();
    }

    @GetMapping("/specialties/{id}")
    public SpecialtyResponse getById(@PathVariable UUID id) {
        return specialtyService.getById(id);
    }

    @PostMapping("/specialties")
    public ResponseEntity<SpecialtyResponse> create(@Valid @RequestBody SpecialtyRequest request) {
        SpecialtyResponse response = specialtyService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/specialties/{id}")
    public SpecialtyResponse update(@PathVariable UUID id, @Valid @RequestBody SpecialtyRequest request) {
        return specialtyService.update(id, request);
    }

    @DeleteMapping("/specialties/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        specialtyService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/specialties")
    public List<SpecialtyResponse> listAll() {
        return specialtyService.listAll();
    }
}
