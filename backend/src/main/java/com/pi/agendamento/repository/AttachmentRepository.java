package com.pi.agendamento.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pi.agendamento.entity.Attachment;

// acesso a anexos de consulta
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
}
