package com.pi.agendamento.dto.request;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateScheduleRequest(
        @NotNull LocalTime workStartTime,
        @NotNull LocalTime workEndTime,
        @NotEmpty Set<@NotNull DayOfWeek> workDays,
        @Min(10) @Max(180) int firstVisitDurationMin,
        @Min(10) @Max(180) int returnDurationMin
) {
}
