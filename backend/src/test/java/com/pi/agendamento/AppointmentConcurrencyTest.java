package com.pi.agendamento;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pi.agendamento.dto.request.CreateAppointmentRequest;
import com.pi.agendamento.entity.Appointment;
import com.pi.agendamento.entity.Doctor;
import com.pi.agendamento.entity.Specialty;
import com.pi.agendamento.entity.User;
import com.pi.agendamento.enums.AppointmentStatus;
import com.pi.agendamento.enums.AppointmentType;
import com.pi.agendamento.enums.PaymentType;
import com.pi.agendamento.enums.Role;
import com.pi.agendamento.exception.BusinessException;
import com.pi.agendamento.repository.AppointmentRepository;
import com.pi.agendamento.repository.DoctorRepository;
import com.pi.agendamento.repository.SpecialtyRepository;
import com.pi.agendamento.repository.UserRepository;
import com.pi.agendamento.service.AppointmentService;
import com.pi.agendamento.service.AvailabilityService;

/**
 * O unico teste automatizado do projeto.
 *
 * Nao da para reproduzir concorrencia no Postman: ninguem clica duas vezes em 10ms, e eh
 * exatamente onde o bug fica invisivel ate acontecer com usuario real.
 *
 * Exige Postgres de verdade (o container do docker-compose) — H2 nao trata constraint da
 * mesma forma, e a constraint EXCLUDE do segundo cenario nem existe fora do Postgres.
 *
 * O teste nao eh @Transactional de proposito: as duas threads precisam commitar de verdade.
 */
@SpringBootTest
class AppointmentConcurrencyTest {

    private static final int FIRST_VISIT_MIN = 60;
    private static final int RETURN_MIN = 20;

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SpecialtyRepository specialtyRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DataSource dataSource;

    private UUID doctorId;
    private UUID specialtyId;
    private UUID patientAId;
    private UUID patientBId;
    private UUID completedParentId;
    private Instant slot;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();

        Specialty specialty = new Specialty();
        specialty.setName("Concorrencia " + UUID.randomUUID());
        specialtyRepository.save(specialty);
        specialtyId = specialty.getId();

        User doctorUser = newUser("Medico Concorrencia", Role.DOCTOR);
        Doctor doctor = new Doctor();
        doctor.setUser(doctorUser);
        doctor.setCrmNumber(uniqueDigits(9));
        doctor.setCrmUf("SP");
        doctor.setWorkStartTime(LocalTime.of(8, 0));
        doctor.setWorkEndTime(LocalTime.of(18, 0));
        doctor.setWorkDays(new HashSet<>(Set.of(DayOfWeek.values())));
        // 60 e 20 para que 10:00 caia nas duas grades (08:00+60 e 08:00+20)
        // e 10:20 caia na grade de retorno.
        doctor.setFirstVisitDurationMin(FIRST_VISIT_MIN);
        doctor.setReturnDurationMin(RETURN_MIN);
        doctor.setSpecialties(new HashSet<>(Set.of(specialty)));
        doctorRepository.save(doctor);
        doctorId = doctor.getId();

        User patientA = newUser("Paciente A", Role.PATIENT);
        User patientB = newUser("Paciente B", Role.PATIENT);
        patientAId = patientA.getId();
        patientBId = patientB.getId();

        slot = LocalDate.now(AvailabilityService.CLINIC_ZONE)
                .plusDays(7)
                .atTime(LocalTime.of(10, 0))
                .atZone(AvailabilityService.CLINIC_ZONE)
                .toInstant();

        // Consulta pai concluida, no passado, para o retorno do paciente B.
        // COMPLETED nao entra no predicado da constraint, entao nao ocupa horario.
        Appointment parent = new Appointment();
        parent.setPatient(patientB);
        parent.setDoctor(doctor);
        parent.setSpecialty(specialty);
        parent.setStartAt(Instant.now().minus(Duration.ofDays(30)));
        parent.setEndAt(Instant.now().minus(Duration.ofDays(30)).plus(Duration.ofMinutes(FIRST_VISIT_MIN)));
        parent.setType(AppointmentType.PRIMEIRA_CONSULTA);
        parent.setStatus(AppointmentStatus.COMPLETED);
        parent.setPaymentType(PaymentType.PARTICULAR);
        appointmentRepository.save(parent);
        completedParentId = parent.getId();
    }

    @Test
    @DisplayName("a constraint de sobreposicao existe no banco")
    void constraintDeSobreposicaoExiste() throws Exception {
        // Guarda contra a protecao sumir em silencio: a constraint nao vem das entidades,
        // eh criada pelo DatabaseExtensionsRunner.
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("""
                     SELECT pg_get_constraintdef(oid) AS def
                     FROM pg_constraint
                     WHERE conrelid = 'appointment'::regclass
                       AND conname = 'uk_appointment_no_overlap'
                     """)) {
            assertThat(rs.next()).as("uk_appointment_no_overlap nao existe").isTrue();
            assertThat(rs.getString("def")).contains("EXCLUDE").contains("SCHEDULED");
        }
    }

    @Test
    @DisplayName("duas requisicoes no mesmo startAt: exatamente uma vence")
    void mesmoHorarioExato() throws Exception {
        Outcome outcome = race(
                () -> book(patientAId, AppointmentType.PRIMEIRA_CONSULTA, slot, null),
                () -> book(patientBId, AppointmentType.PRIMEIRA_CONSULTA, slot, null));

        assertThat(outcome.successes).as("apenas uma das duas pode ter sido gravada. %s", outcome)
                .isEqualTo(1);
        assertThat(outcome.failures).hasSize(1);
        assertThat(outcome.failures.get(0))
                .as("a perdedora deve falhar com erro de negocio, nao com erro inesperado")
                .isInstanceOf(BusinessException.class);
        assertThat(appointmentRepository.countScheduled()).isEqualTo(1);
    }

    /**
     * O caso que a UNIQUE(doctor_id, start_at) nao cobre: os dois startAt sao diferentes,
     * mas os intervalos se sobrepoem (10:00-11:00 contra 10:20-10:40). Sem a constraint
     * EXCLUDE as duas gravam e o medico fica com a agenda dobrada.
     */
    @Test
    @DisplayName("sobreposicao parcial com startAt diferentes: exatamente uma vence")
    void sobreposicaoParcial() throws Exception {
        Instant dentro = slot.plus(Duration.ofMinutes(20));

        Outcome outcome = race(
                () -> book(patientAId, AppointmentType.PRIMEIRA_CONSULTA, slot, null),
                () -> book(patientBId, AppointmentType.RETORNO, dentro, completedParentId));

        assertThat(outcome.successes)
                .as("10:00-11:00 e 10:20-10:40 se sobrepoem: so uma pode existir. %s", outcome)
                .isEqualTo(1);
        assertThat(outcome.failures).hasSize(1);
        assertThat(outcome.failures.get(0)).isInstanceOf(BusinessException.class);
        assertThat(appointmentRepository.countScheduled()).isEqualTo(1);
    }

    private Outcome race(Callable<Void> first, Callable<Void> second) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<Throwable>> futures = new ArrayList<>();
            for (Callable<Void> action : List.of(first, second)) {
                futures.add(pool.submit(() -> {
                    start.await();
                    try {
                        action.call();
                        return null;
                    } catch (Throwable ex) {
                        return ex;
                    }
                }));
            }
            start.countDown();
            pool.shutdown();
            assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

            Outcome outcome = new Outcome();
            for (Future<Throwable> future : futures) {
                Throwable ex = future.get();
                if (ex == null) {
                    outcome.successes++;
                } else {
                    outcome.failures.add(ex);
                }
            }
            return outcome;
        } finally {
            pool.shutdownNow();
        }
    }

    private Void book(UUID patientId, AppointmentType type, Instant startAt, UUID parentId) {
        authenticateAs(patientId);
        try {
            appointmentService.create(new CreateAppointmentRequest(
                    doctorId, specialtyId, type, PaymentType.PARTICULAR, null,
                    startAt, null, parentId));
            return null;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // Cada thread tem o proprio SecurityContext; sem isto o CurrentUserProvider nao acha ninguem.
    private void authenticateAs(UUID userId) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))));
        SecurityContextHolder.setContext(context);
    }

    private User newUser(String name, Role role) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(UUID.randomUUID() + "@teste.com");
        user.setPasswordHash(passwordEncoder.encode("senha123"));
        user.setCpf(uniqueDigits(11));
        user.setRole(role);
        return userRepository.save(user);
    }

    private static String uniqueDigits(int length) {
        String digits = UUID.randomUUID().toString().replaceAll("\\D", "");
        while (digits.length() < length) {
            digits += UUID.randomUUID().toString().replaceAll("\\D", "");
        }
        return digits.substring(0, length);
    }

    private static final class Outcome {
        private int successes;
        private final List<Throwable> failures = new ArrayList<>();

        @Override
        public String toString() {
            List<String> descriptions = failures.stream()
                    .map(ex -> ex.getClass().getSimpleName() + ": " + ex.getMessage())
                    .toList();
            return "sucessos=" + successes + " falhas=" + descriptions;
        }
    }
}
