package com.pi.agendamento.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.pi.agendamento.enums.AppointmentStatus;
import com.pi.agendamento.enums.AppointmentType;
import com.pi.agendamento.enums.PaymentType;
import com.pi.agendamento.exception.BusinessException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Entidade agendamento, 1-N com paciente, 1-N com medico, 1-N com especialidade, 1-N com anexos,
// 1-N com consulta pai (para retorno), 1-N com convenio
@Entity
@Table(
        name = "appointment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_appointment_doctor_start",
                columnNames = {"doctor_id", "start_at"}),
        indexes = {
                @Index(name = "idx_appointment_doctor_start", columnList = "doctor_id, start_at"),
                @Index(name = "idx_appointment_patient", columnList = "patient_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_appointment_id")
    private Appointment parentAppointment;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_plan_id")
    private HealthPlan healthPlan;

    @Column(length = 1000)
    private String patientNotes;

    @Column(length = 2000)
    private String doctorNotes;

    @Column(length = 500)
    private String cancellationReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "appointment",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Attachment> attachments = new ArrayList<>();

    public void cancel(String reason) {
        requireScheduled("cancelar");
        this.status = AppointmentStatus.CANCELLED;
        this.cancellationReason = reason;
    }

    public void complete() {
        requireScheduled("concluir");
        this.status = AppointmentStatus.COMPLETED;
    }

    public void markNoShow() {
        requireScheduled("marcar como falta");
        this.status = AppointmentStatus.NO_SHOW;
    }

    public boolean canBeReviewed() {
        return status == AppointmentStatus.COMPLETED;
    }

    public boolean isReturn() {
        return type == AppointmentType.RETORNO;
    }

    private void requireScheduled(String acao) {
        if (status != AppointmentStatus.SCHEDULED) {
            throw new BusinessException(
                    "Nao é possível " + acao + " uma consulta com status " + status);
        }
    }
}