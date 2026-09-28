package com.pi.agendamento.dto.request;

import jakarta.validation.constraints.Size;

// motivo opcional do cancelamento
public record CancelAppointmentRequest(@Size(max = 500) String reason) {
}
