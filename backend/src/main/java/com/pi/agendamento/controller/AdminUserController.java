package com.pi.agendamento.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pi.agendamento.dto.request.CreateAdminRequest;
import com.pi.agendamento.dto.request.CreateDoctorRequest;
import com.pi.agendamento.dto.response.DoctorResponse;
import com.pi.agendamento.dto.response.UserResponse;
import com.pi.agendamento.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// rotas de admin para criar usuarios
@Tag(name = "Admin - usuarios", description = "Somente ADMIN")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // cria medico e aponta Location para o perfil publico
    @Operation(summary = "Criar medico")
    @PostMapping("/doctors")
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        DoctorResponse response = userService.createDoctor(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/doctors/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    // cria outro admin
    @Operation(summary = "Criar admin")
    @PostMapping("/admins")
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        UserResponse response = userService.createAdmin(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
}
