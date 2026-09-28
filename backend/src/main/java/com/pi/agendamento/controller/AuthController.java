package com.pi.agendamento.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pi.agendamento.dto.request.LoginRequest;
import com.pi.agendamento.dto.request.RegisterRequest;
import com.pi.agendamento.dto.response.AuthResponse;
import com.pi.agendamento.dto.response.UserResponse;
import com.pi.agendamento.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// rotas de cadastro, login e usuario logado
@Tag(name = "Autenticacao")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // cria paciente e retorna token
    @Operation(summary = "Cadastrar paciente", description = "Publico. Ja devolve o token.")
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/auth/me")
                .build()
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    // autentica e retorna token
    @Operation(summary = "Login", description = "Publico. Colar o `token` da resposta em Authorize.")
    @SecurityRequirements
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.authenticate(request);
    }

    // dados do usuario do token
    @Operation(summary = "Usuario logado")
    @GetMapping("/me")
    public UserResponse me() {
        return userService.getCurrentUser();
    }
}
