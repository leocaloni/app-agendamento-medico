package com.pi.agendamento.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// Helper de usuario logado
@Component
public class CurrentUserProvider {

    public Optional<UUID> findId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UUID userId) {
            return Optional.of(userId);
        }
        return Optional.empty();
    }

    public UUID getId() {
        return findId().orElseThrow(
                () -> new AuthenticationCredentialsNotFoundException("Usuario nao autenticado"));
    }
}
