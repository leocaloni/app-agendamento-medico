package com.pi.agendamento.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// le o id do usuario logado no contexto de seguranca
@Component
public class CurrentUserProvider {

    // id do usuario logado, vazio se anonimo
    public Optional<UUID> findId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UUID userId) {
            return Optional.of(userId);
        }
        return Optional.empty();
    }

    // id do usuario logado, erro se anonimo
    public UUID getId() {
        return findId().orElseThrow(
                () -> new AuthenticationCredentialsNotFoundException("Usuario nao autenticado"));
    }
}
