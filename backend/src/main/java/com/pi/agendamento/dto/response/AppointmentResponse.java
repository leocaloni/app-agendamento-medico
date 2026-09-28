package com.pi.agendamento.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.pi.agendamento.entity.Appointment;
import com.pi.agendamento.enums.AppointmentStatus;
import com.pi.agendamento.enums.AppointmentType;
import com.pi.agendamento.enums.PaymentType;

// consulta com nomes de medico, paciente e especialidade
public record AppointmentResponse(
        UUID id,
        UUID doctorId,
        String doctorName,
        UUID patientId,
        String patientName,
        UUID specialtyId,
        String specialtyName,
        Instant startAt,
        Instant endAt,
        AppointmentType type,
        AppointmentStatus status,
        PaymentType paymentType,
        // convenio gravado na consulta, nao o atual do paciente; null se PARTICULAR
        HealthPlanSummary healthPlan,
        String patientNotes,
        String doctorNotes,
        String cancellationReason,
        UUID parentAppointmentId,
        Instant createdAt
) {

    // converte a entidade em response
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getUser().getFullName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getFullName(),
                appointment.getSpecialty().getId(),
                appointment.getSpecialty().getName(),
                appointment.getStartAt(),
                appointment.getEndAt(),
                appointment.getType(),
                appointment.getStatus(),
                appointment.getPaymentType(),
                HealthPlanSummary.from(appointment.getHealthPlan()),
                appointment.getPatientNotes(),
                appointment.getDoctorNotes(),
                appointment.getCancellationReason(),
                appointment.getParentAppointment() != null
                        ? appointment.getParentAppointment().getId()
                        : null,
                appointment.getCreatedAt());
    }
}
