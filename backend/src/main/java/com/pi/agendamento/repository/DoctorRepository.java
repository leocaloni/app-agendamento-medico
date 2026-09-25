package com.pi.agendamento.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pi.agendamento.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    boolean existsByCrmNumberAndCrmUf(String crmNumber, String crmUf);
}
