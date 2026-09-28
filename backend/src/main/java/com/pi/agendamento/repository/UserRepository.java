package com.pi.agendamento.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pi.agendamento.entity.User;
import com.pi.agendamento.enums.Role;

// acesso a usuarios
public interface UserRepository extends JpaRepository<User, UUID> {

    // indica se ja existe usuario com o perfil
    boolean existsByRole(Role role);

    // busca usuario pelo email de login
    Optional<User> findByEmail(String email);

    // indica se o email ja esta cadastrado
    boolean existsByEmail(String email);

    // indica se o cpf ja esta cadastrado
    boolean existsByCpf(String cpf);
}
