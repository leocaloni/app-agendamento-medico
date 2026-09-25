package com.pi.agendamento.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateAppointmentNotesRequest(@Size(max = 2000) String doctorNotes) {
}
