package com.pi.agendamento.controller;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pi.agendamento.dto.request.UpdateProfileRequest;
import com.pi.agendamento.dto.response.UserResponse;
import com.pi.agendamento.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/me")
    public UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(request);
    }
}
