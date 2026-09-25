package com.pi.agendamento.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pi.agendamento.entity.User;
import com.pi.agendamento.enums.Role;
import com.pi.agendamento.repository.UserRepository;

// Seed de admin para dev
@Component
public class DevSeedRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevSeedRunner.class);

    private static final String ADMIN_EMAIL = "admin@teste.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_CPF = "00000000000";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevSeedRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        if (userRepository.existsByEmail(ADMIN_EMAIL) || userRepository.existsByCpf(ADMIN_CPF)) {
            log.warn("Seed: nenhum admin no banco, mas {} ou o CPF padrao ja estao em uso. Admin nao criado.", ADMIN_EMAIL);
            return;
        }

        User admin = new User();
        admin.setFullName("Administrador");
        admin.setEmail(ADMIN_EMAIL);
        admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setCpf(ADMIN_CPF);
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        log.info("Seed: admin padrao criado ({} / {})", ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}
