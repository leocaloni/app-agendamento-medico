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

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // Uma unica query para montar a grade inteira do periodo.
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

    /**
     * Sobreposicao de intervalo: nova.start < existente.end AND nova.end > existente.start.
     * A UNIQUE(doctor_id, start_at) so pega colisao de inicio exato — ela nao impede uma
     * primeira consulta de 50min as 10:00 conviver com um retorno de 20min as 10:20.
     */
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

    /**
     * EntityGraph nas quatro relacoes LAZY que o response le: sem ele, uma listagem de
     * 20 consultas dispara 60+ queries.
     *
     * Sem parametro anulavel de proposito. A forma `(:from IS NULL OR a.startAt >= :from)`
     * gera `(? is null or start_at >= ?)`, e o Postgres recusa com "could not determine data
     * type of parameter" — o bind isolado do `is null` nao tem tipo inferivel. O service
     * substitui os nulos por limites abertos e por todos os status.
     */
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

    @EntityGraph(attributePaths = {"doctor", "doctor.user", "patient", "specialty", "healthPlan"})
    @Query("SELECT a FROM Appointment a WHERE a.id = :id")
    Optional<Appointment> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'SCHEDULED'")
    long countScheduled();
}
