package com.pi.agendamento.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pi.agendamento.entity.User;
import com.pi.agendamento.enums.Role;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByRole(Role role);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);
}
