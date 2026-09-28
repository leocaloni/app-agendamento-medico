package com.pi.agendamento.dto.response;

// media e total de avaliacoes do medico; media null se nao houver
public record RatingStats(Double average, Long total) {
}
