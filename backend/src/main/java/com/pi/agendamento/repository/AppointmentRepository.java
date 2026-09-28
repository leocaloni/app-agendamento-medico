package com.pi.agendamento.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pi.agendamento.entity.Appointment;
import com.pi.agendamento.enums.AppointmentStatus;

// acesso a consultas e checagens de sobreposicao
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // consultas agendadas do medico que tocam o periodo
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.doctor.id = :doctorId
              AND a.status = 'SCHEDULED'
              AND a.startAt < :to
              AND a.endAt > :from
            """)
    List<Appointment> findActiveInRange(
            @Param("doctorId") UUID doctorId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    // indica se o intervalo sobrepoe consulta agendada do medico
    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.doctor.id = :doctorId
              AND a.status = 'SCHEDULED'
              AND a.startAt < :endAt
              AND a.endAt > :startAt
            """)
    boolean existsDoctorOverlap(
            @Param("doctorId") UUID doctorId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt);

    // indica se o intervalo sobrepoe consulta agendada do paciente
    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.patient.id = :patientId
              AND a.status = 'SCHEDULED'
              AND a.startAt < :endAt
              AND a.endAt > :startAt
            """)
    boolean existsPatientOverlap(
            @Param("patientId") UUID patientId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt);

    // consultas do paciente no periodo; sem parametro nulo porque o postgres nao infere o tipo
    @EntityGraph(attributePaths = {"doctor", "doctor.user", "patient", "specialty", "healthPlan"})
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.patient.id = :patientId
              AND a.startAt >= :from
              AND a.startAt < :to
              AND a.status IN :statuses
            ORDER BY a.startAt
            """)
    List<Appointment> findForPatient(
            @Param("patientId") UUID patientId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("statuses") Collection<AppointmentStatus> statuses);

    // agenda do medico no periodo
    @EntityGraph(attributePaths = {"doctor", "doctor.user", "patient", "specialty", "healthPlan"})
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.doctor.id = :doctorId
              AND a.startAt >= :from
              AND a.startAt < :to
              AND a.status IN :statuses
            ORDER BY a.startAt
            """)
    List<Appointment> findForDoctor(
            @Param("doctorId") UUID doctorId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("statuses") Collection<AppointmentStatus> statuses);

    // busca a consulta com as relacoes que o response le
    @EntityGraph(attributePaths = {"doctor", "doctor.user", "patient", "specialty", "healthPlan"})
    @Query("SELECT a FROM Appointment a WHERE a.id = :id")
    Optional<Appointment> findByIdWithDetails(@Param("id") UUID id);

    // total de consultas agendadas
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'SCHEDULED'")
    long countScheduled();
}
