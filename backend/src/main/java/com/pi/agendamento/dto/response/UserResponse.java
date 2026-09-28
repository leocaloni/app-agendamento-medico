package com.pi.agendamento.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import com.pi.agendamento.entity.User;
import com.pi.agendamento.enums.Role;


// dados publicos do usuario, sem senha nem cpf
public record UserResponse(
        UUID id,
        String fullName,
        String firstName,
        String email,
        String phone,
        LocalDate birthDate,
        Role role,
        HealthPlanSummary healthPlan
) {

    // converte a entidade em response
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getFirstName(),
                user.getEmail(),
                user.getPhone(),
                user.getBirthDate(),
                user.getRole(),
                HealthPlanSummary.from(user.getHealthPlan()));
    }
}
