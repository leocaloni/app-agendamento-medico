package com.pi.agendamento.dto.response;

// Projection da query de estatisticas. AVG vem null quando o medico nao tem avaliacao.
public record RatingStats(Double average, Long total) {
}
