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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/auth/me")
                .build()
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.authenticate(request);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return userService.getCurrentUser();
    }
}
