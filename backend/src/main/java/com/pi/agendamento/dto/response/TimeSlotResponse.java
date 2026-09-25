package com.pi.agendamento.dto.response;

import java.time.Instant;

public record TimeSlotResponse(Instant startAt, Instant endAt) {
}
