package com.pi.agendamento.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pi.agendamento.entity.Exam;

// acesso aos exames dos pacientes
public interface ExamRepository extends JpaRepository<Exam, UUID> {

    // biblioteca do paciente, mais recentes primeiro
    @EntityGraph(attributePaths = {"patient", "sharedWith"})
    @Query("SELECT e FROM Exam e WHERE e.patient.id = :patientId ORDER BY e.examDate DESC, e.uploadedAt DESC")
    List<Exam> findByPatientId(@Param("patientId") UUID patientId);

    // exame com dono e compartilhamentos carregados
    @EntityGraph(attributePaths = {"patient", "sharedWith"})
    @Query("SELECT e FROM Exam e WHERE e.id = :id")
    Optional<Exam> findByIdWithDetails(@Param("id") UUID id);

    // exames compartilhados com a consulta
    @Query("""
            SELECT e FROM Exam e JOIN e.sharedWith a
            WHERE a.id = :appointmentId
            ORDER BY e.examDate DESC, e.uploadedAt DESC
            """)
    List<Exam> findSharedWithAppointment(@Param("appointmentId") UUID appointmentId);

    // indica se o exame foi compartilhado com alguma consulta do medico
    @Query("""
            SELECT COUNT(a) > 0 FROM Exam e JOIN e.sharedWith a
            WHERE e.id = :examId AND a.doctor.user.id = :doctorUserId
            """)
    boolean isSharedWithDoctor(@Param("examId") UUID examId, @Param("doctorUserId") UUID doctorUserId);

    long countByPatientId(UUID patientId);
}
