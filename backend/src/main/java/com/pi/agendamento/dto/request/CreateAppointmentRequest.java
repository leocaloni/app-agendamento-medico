package com.pi.agendamento.dto.request;

import java.time.Instant;
import java.util.UUID;

import com.pi.agendamento.enums.AppointmentType;
import com.pi.agendamento.enums.PaymentType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// pedido de agendamento do paciente
public record CreateAppointmentRequest(
        @NotNull UUID doctorId,
        @NotNull UUID specialtyId,
        @NotNull AppointmentType type,
        // sem default: o back nao deduz pelo convenio do paciente
        @NotNull PaymentType paymentType,
        UUID healthPlanId,
        @NotNull Instant startAt,
        @Size(max = 1000) String patientNotes,
        UUID parentAppointmentId
) {
}
