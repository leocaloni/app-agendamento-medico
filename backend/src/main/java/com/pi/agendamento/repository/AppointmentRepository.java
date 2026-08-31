package com.pi.agendamento.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pi.agendamento.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
}
