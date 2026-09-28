package com.pi.agendamento.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pi.agendamento.config.CurrentUserProvider;
import com.pi.agendamento.dto.request.CreateAppointmentRequest;
import com.pi.agendamento.dto.response.AppointmentResponse;
import com.pi.agendamento.dto.response.TimeSlotResponse;
import com.pi.agendamento.entity.Appointment;
import com.pi.agendamento.entity.Doctor;
import com.pi.agendamento.entity.HealthPlan;
import com.pi.agendamento.entity.Specialty;
import com.pi.agendamento.entity.User;
import com.pi.agendamento.enums.AppointmentStatus;
import com.pi.agendamento.enums.AppointmentType;
import com.pi.agendamento.enums.PaymentType;
import com.pi.agendamento.exception.BusinessException;
import com.pi.agendamento.exception.ConflictException;
import com.pi.agendamento.exception.ResourceNotFoundException;
import com.pi.agendamento.repository.AppointmentRepository;
import com.pi.agendamento.repository.DoctorRepository;
import com.pi.agendamento.repository.HealthPlanRepository;
import com.pi.agendamento.repository.SpecialtyRepository;
import com.pi.agendamento.repository.UserRepository;

// agendamento e ciclo de vida das consultas
@Service
public class AppointmentService {

    // limites usados quando o cliente nao manda from/to
    private static final Instant OPEN_START = Instant.EPOCH;
    private static final Instant OPEN_END = Instant.parse("9999-12-31T23:59:59Z");

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final HealthPlanRepository healthPlanRepository;
    private final AvailabilityService availabilityService;
    private final CurrentUserProvider currentUserProvider;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            SpecialtyRepository specialtyRepository,
            HealthPlanRepository healthPlanRepository,
            AvailabilityService availabilityService,
            CurrentUserProvider currentUserProvider) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.specialtyRepository = specialtyRepository;
        this.healthPlanRepository = healthPlanRepository;
        this.availabilityService = availabilityService;
        this.currentUserProvider = currentUserProvider;
    }

    // valida as regras e agenda a consulta do paciente logado
    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {
        User patient = loadCurrentUser();

        Doctor doctor = doctorRepository.findByIdWithDetails(request.doctorId())
                .filter(found -> found.getUser().isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Medico nao encontrado"));

        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .filter(Specialty::isActive)
                .orElseThrow(() -> new BusinessException("Especialidade nao encontrada ou inativa"));
        if (!doctor.hasSpecialty(specialty.getId())) {
            throw new BusinessException("O medico nao atende esta especialidade");
        }

        // pagamento antes da grade por ser a checagem mais barata
        HealthPlan healthPlan = resolvePaymentPlan(request, doctor);

        if (!request.startAt().isAfter(Instant.now())) {
            throw new BusinessException("A consulta deve ser marcada para um horario futuro");
        }

        Duration duration = Duration.ofMinutes(doctor.durationFor(request.type()));
        Instant endAt = request.startAt().plus(duration);

        // antes da grade para separar horario ocupado (409) de horario inexistente (400)
        if (appointmentRepository.existsDoctorOverlap(doctor.getId(), request.startAt(), endAt)) {
            throw new ConflictException("Este horario conflita com outra consulta do medico");
        }

        if (appointmentRepository.existsPatientOverlap(patient.getId(), request.startAt(), endAt)) {
            throw new ConflictException("Voce ja tem outra consulta marcada neste horario");
        }

        // grade recalculada aqui, sem confiar no front
        requireValidSlot(doctor, request);

        Appointment parent = resolveParent(request, patient, doctor);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setSpecialty(specialty);
        appointment.setParentAppointment(parent);
        appointment.setStartAt(request.startAt());
        appointment.setEndAt(endAt);
        appointment.setType(request.type());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setPaymentType(request.paymentType());
        // copia o convenio: a consulta nao depende do plano atual do paciente
        appointment.setHealthPlan(healthPlan);
        appointment.setPatientNotes(request.patientNotes());

        return AppointmentResponse.from(save(appointment));
    }

    // calcula os horarios livres do medico no periodo
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailability(UUID doctorId, LocalDate from, LocalDate to,
                                                  AppointmentType type) {
        return availabilityService.getAvailability(doctorId, from, to, type);
    }

    // consultas do paciente ou agenda do medico logado
    @Transactional(readOnly = true)
    public List<AppointmentResponse> listMine(Instant from, Instant to, AppointmentStatus status) {
        User user = loadCurrentUser();
        // filtro ausente vira limite aberto, para a query nao receber null
        Instant fromOrOpen = from != null ? from : OPEN_START;
        Instant toOrOpen = to != null ? to : OPEN_END;
        Collection<AppointmentStatus> statuses = status != null
                ? List.of(status)
                : List.of(AppointmentStatus.values());

        List<Appointment> appointments = switch (user.getRole()) {
            case PATIENT -> appointmentRepository.findForPatient(
                    user.getId(), fromOrOpen, toOrOpen, statuses);
            case DOCTOR -> appointmentRepository.findForDoctor(
                    doctorRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Medico nao encontrado para o usuario logado"))
                            .getId(),
                    fromOrOpen, toOrOpen, statuses);
            case ADMIN -> throw new BusinessException("ADMIN nao possui agenda propria");
        };
        return appointments.stream().map(AppointmentResponse::from).toList();
    }

    // detalhe da consulta para o paciente ou o medico dela
    @Transactional(readOnly = true)
    public AppointmentResponse getById(UUID id) {
        return AppointmentResponse.from(loadVisible(id));
    }

    // cancela consulta do paciente ou do medico
    @Transactional
    public AppointmentResponse cancel(UUID id, String reason) {
        Appointment appointment = loadVisible(id);
        appointment.cancel(reason);
        return AppointmentResponse.from(appointment);
    }

    // medico marca a consulta como realizada
    @Transactional
    public AppointmentResponse complete(UUID id) {
        Appointment appointment = loadAsOwningDoctor(id);
        appointment.complete();
        return AppointmentResponse.from(appointment);
    }

    // medico marca falta do paciente
    @Transactional
    public AppointmentResponse markNoShow(UUID id) {
        Appointment appointment = loadAsOwningDoctor(id);
        appointment.markNoShow();
        return AppointmentResponse.from(appointment);
    }

    // medico grava anotacoes na consulta
    @Transactional
    public AppointmentResponse updateNotes(UUID id, String doctorNotes) {
        Appointment appointment = loadAsOwningDoctor(id);
        appointment.setDoctorNotes(doctorNotes);
        return AppointmentResponse.from(appointment);
    }

    // a constraint do banco barra a concorrencia; o flush faz o erro subir aqui e nao no commit
    private Appointment save(Appointment appointment) {
        try {
            return appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Este horario acabou de ser reservado");
        }
    }

    private HealthPlan resolvePaymentPlan(CreateAppointmentRequest request, Doctor doctor) {
        if (request.paymentType() == PaymentType.PARTICULAR) {
            // recusa em vez de ignorar, para o front nao achar que gravou convenio
            if (request.healthPlanId() != null) {
                throw new BusinessException(
                        "Consulta PARTICULAR nao aceita healthPlanId");
            }
            return null;
        }

        if (request.healthPlanId() == null) {
            throw new BusinessException("Consulta por CONVENIO exige healthPlanId");
        }
        HealthPlan healthPlan = healthPlanRepository.findById(request.healthPlanId())
                .filter(HealthPlan::isActive)
                .orElseThrow(() -> new BusinessException("Convenio nao encontrado ou inativo"));
        if (!doctor.acceptsPlan(healthPlan.getId())) {
            throw new BusinessException("O medico nao aceita este convenio");
        }
        // nao exige o convenio do paciente: ele so pre-preenche a tela
        return healthPlan;
    }

    private void requireValidSlot(Doctor doctor, CreateAppointmentRequest request) {
        LocalDate date = request.startAt().atZone(AvailabilityService.CLINIC_ZONE).toLocalDate();
        boolean valid = availabilityService.slotsFor(doctor, date, date, request.type()).stream()
                .anyMatch(slot -> slot.startAt().equals(request.startAt()));
        if (!valid) {
            throw new BusinessException("Horario indisponivel para este medico");
        }
    }

    private Appointment resolveParent(CreateAppointmentRequest request, User patient, Doctor doctor) {
        if (request.type() != AppointmentType.RETORNO) {
            return null;
        }
        if (request.parentAppointmentId() == null) {
            throw new BusinessException("Retorno exige parentAppointmentId");
        }
        Appointment parent = appointmentRepository.findById(request.parentAppointmentId())
                .orElseThrow(() -> new BusinessException("Consulta de origem nao encontrada"));
        if (!parent.getPatient().getId().equals(patient.getId())
                || !parent.getDoctor().getId().equals(doctor.getId())) {
            throw new BusinessException("A consulta de origem deve ser do mesmo paciente e medico");
        }
        if (parent.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessException("A consulta de origem precisa estar concluida");
        }
        return parent;
    }

    // so o paciente ou o medico da consulta
    private Appointment loadVisible(UUID id) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta nao encontrada"));

        UUID userId = currentUserProvider.getId();
        boolean isPatient = appointment.getPatient().getId().equals(userId);
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(userId);
        if (!isPatient && !isDoctor) {
            throw new AccessDeniedException("Consulta de outro usuario");
        }
        return appointment;
    }

    private Appointment loadAsOwningDoctor(UUID id) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta nao encontrada"));
        if (!appointment.getDoctor().getUser().getId().equals(currentUserProvider.getId())) {
            throw new AccessDeniedException("Consulta de outro medico");
        }
        return appointment;
    }

    private User loadCurrentUser() {
        return userRepository.findById(currentUserProvider.getId())
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
