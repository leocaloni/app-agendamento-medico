package com.pi.agendamento.dto.response;

import java.time.Instant;

// horario livre na agenda do medico
public record TimeSlotResponse(Instant startAt, Instant endAt) {
}
