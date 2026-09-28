package com.pi.agendamento.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pi.agendamento.dto.response.TimeSlotResponse;
import com.pi.agendamento.entity.Appointment;
import com.pi.agendamento.entity.Doctor;
import com.pi.agendamento.enums.AppointmentType;
import com.pi.agendamento.exception.BusinessException;
import com.pi.agendamento.exception.ResourceNotFoundException;
import com.pi.agendamento.repository.AppointmentRepository;
import com.pi.agendamento.repository.DoctorRepository;

// gera em memoria os horarios livres: expediente menos consultas marcadas
@Service
public class AvailabilityService {

    // fixo porque o expediente eh hora local da clinica; systemDefault mudaria entre maquina e container
    public static final ZoneId CLINIC_ZONE = ZoneId.of("America/Sao_Paulo");

    private static final int MAX_RANGE_DAYS = 60;

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public AvailabilityService(DoctorRepository doctorRepository,
                               AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // calcula os horarios livres do medico no periodo
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailability(UUID doctorId, LocalDate from, LocalDate to,
                                                  AppointmentType type) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .filter(found -> found.getUser().isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Medico nao encontrado"));
        return slotsFor(doctor, from, to, type);
    }

    // mesma grade, com o medico ja carregado
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> slotsFor(Doctor doctor, LocalDate from, LocalDate to,
                                           AppointmentType type) {
        if (from.isAfter(to)) {
            throw new BusinessException("O parametro 'from' deve ser anterior ou igual a 'to'");
        }
        // limita o periodo para nao gerar grade gigante
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days > MAX_RANGE_DAYS) {
            throw new BusinessException("O intervalo nao pode passar de " + MAX_RANGE_DAYS + " dias");
        }

        Instant rangeStart = from.atStartOfDay(CLINIC_ZONE).toInstant();
        Instant rangeEnd = to.plusDays(1).atStartOfDay(CLINIC_ZONE).toInstant();
        List<Appointment> taken =
                appointmentRepository.findActiveInRange(doctor.getId(), rangeStart, rangeEnd);

        Duration duration = Duration.ofMinutes(doctor.durationFor(type));
        Instant now = Instant.now();
        List<TimeSlotResponse> slots = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (!doctor.worksOn(date)) {
                continue;
            }
            // minutos do dia em vez de LocalTime, que daria a volta na meia-noite
            int openMinute = doctor.getWorkStartTime().toSecondOfDay() / 60;
            int closeMinute = doctor.getWorkEndTime().toSecondOfDay() / 60;
            int step = (int) duration.toMinutes();

            for (int minute = openMinute; minute + step <= closeMinute; minute += step) {
                Instant slotStart = date.atTime(LocalTime.ofSecondOfDay(minute * 60L))
                        .atZone(CLINIC_ZONE)
                        .toInstant();
                Instant slotEnd = slotStart.plus(duration);

                if (slotStart.isBefore(now) || overlaps(taken, slotStart, slotEnd)) {
                    continue;
                }
                slots.add(new TimeSlotResponse(slotStart, slotEnd));
            }
        }
        return slots;
    }

    private static boolean overlaps(List<Appointment> taken, Instant start, Instant end) {
        return taken.stream().anyMatch(appointment ->
                start.isBefore(appointment.getEndAt()) && end.isAfter(appointment.getStartAt()));
    }
}
