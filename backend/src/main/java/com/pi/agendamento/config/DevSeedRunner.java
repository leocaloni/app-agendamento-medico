package com.pi.agendamento.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pi.agendamento.entity.Doctor;
import com.pi.agendamento.entity.HealthPlan;
import com.pi.agendamento.entity.Specialty;
import com.pi.agendamento.entity.User;
import com.pi.agendamento.enums.Role;
import com.pi.agendamento.repository.DoctorRepository;
import com.pi.agendamento.repository.HealthPlanRepository;
import com.pi.agendamento.repository.SpecialtyRepository;
import com.pi.agendamento.repository.UserRepository;

// popula o banco de dev, que zera a cada start pelo create-drop
@Component
public class DevSeedRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevSeedRunner.class);

    private static final String ADMIN_EMAIL = "admin@teste.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_CPF = "00000000000";
    private static final String DEFAULT_PASSWORD = "senha123";

    private static final Set<DayOfWeek> WEEKDAYS = Set.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final HealthPlanRepository healthPlanRepository;
    private final PasswordEncoder passwordEncoder;

    public DevSeedRunner(
            UserRepository userRepository,
            DoctorRepository doctorRepository,
            SpecialtyRepository specialtyRepository,
            HealthPlanRepository healthPlanRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
        this.healthPlanRepository = healthPlanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // cria convenios, especialidades, admin, pacientes e medicos se o banco estiver vazio
    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        HealthPlan unimed = healthPlan("Unimed", true);
        HealthPlan bradesco = healthPlan("Bradesco Saude", true);
        HealthPlan sulAmerica = healthPlan("SulAmerica", true);
        HealthPlan amil = healthPlan("Amil", true);
        HealthPlan notreDame = healthPlan("NotreDame Intermedica", true);
        // inativo de proposito, para testar a listagem de ativos
        HealthPlan portoSeguro = healthPlan("Porto Seguro", false);

        Specialty cardiologia = specialty("Cardiologia", "Coracao e sistema circulatorio",
                true, "Traga eletrocardiograma e exames de sangue recentes (ate 6 meses).");
        Specialty ortopedia = specialty("Ortopedia", "Ossos, articulacoes, musculos e ligamentos",
                true, "Traga o raio-X ou a ressonancia da regiao afetada, se ja tiver feito.");
        Specialty dermatologia = specialty("Dermatologia", "Pele, cabelo e unhas", false, null);
        Specialty pediatria = specialty("Pediatria", "Saude de criancas e adolescentes", false, null);
        Specialty ginecologia = specialty("Ginecologia", "Saude da mulher", false, null);
        Specialty clinicaGeral = specialty("Clinica Geral", "Atendimento inicial e acompanhamento geral", false, null);
        Specialty oftalmologia = specialty("Oftalmologia", "Saude dos olhos e da visao", false, null);
        Specialty neurologia = specialty("Neurologia", "Sistema nervoso central e periferico", false, null);
        Specialty psiquiatria = specialty("Psiquiatria", "Saude mental e transtornos psiquiatricos", false, null);
        Specialty endocrinologia = specialty("Endocrinologia", "Hormonios e metabolismo", false, null);

        User admin = user("Administrador", ADMIN_EMAIL, ADMIN_PASSWORD, ADMIN_CPF,
                "11900000000", LocalDate.of(1985, 3, 12), Role.ADMIN, null);
        userRepository.save(admin);

        // um paciente particular e dois com convenios diferentes
        patient("Mariana Alves", "mariana@teste.com", "11111111111", "11911111111",
                LocalDate.of(1992, 7, 4), null);
        patient("Bruno Cardoso", "bruno@teste.com", "22222222222", "11922222222",
                LocalDate.of(1988, 1, 23), unimed);
        patient("Carla Menezes", "carla@teste.com", "33333333333", "11933333333",
                LocalDate.of(1996, 11, 9), amil);

        doctor("Ana Beatriz Ribeiro", "ana.ribeiro@teste.com", "10000000001", "11940000001",
                "123456", "SP", "Cardiologista com foco em prevencao e hipertensao.",
                "Sao Paulo", "SP", "Av. Paulista, 1000 - Bela Vista",
                LocalTime.of(8, 0), LocalTime.of(17, 0), WEEKDAYS, 40, 20,
                Set.of(cardiologia), Set.of(unimed, bradesco, sulAmerica));

        doctor("Carlos Eduardo Lima", "carlos.lima@teste.com", "10000000002", "11940000002",
                "234567", "SP", "Dermatologia clinica e cirurgica.",
                "Sao Paulo", "SP", "Rua Augusta, 2300 - Consolacao",
                LocalTime.of(9, 0), LocalTime.of(18, 0),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), 30, 15,
                Set.of(dermatologia), Set.of(amil, notreDame));

        doctor("Fernanda Souza", "fernanda.souza@teste.com", "10000000003", "11940000003",
                "345678", "SP", "Pediatra, atende do recem-nascido ao adolescente.",
                "Santo Andre", "SP", "Rua Siqueira Campos, 120 - Centro",
                LocalTime.of(7, 0), LocalTime.of(13, 0), WEEKDAYS, 30, 20,
                Set.of(pediatria, clinicaGeral), Set.of(unimed));

        // aceita convenio inativo: desativar nao limpa quem ja referencia
        doctor("Marcelo Tavares", "marcelo.tavares@teste.com", "10000000004", "11940000004",
                "456789", "SP", "Ortopedia com foco em joelho e quadril.",
                "Campinas", "SP", "Av. Norte-Sul, 450 - Cambui",
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), 45, 25,
                Set.of(ortopedia), Set.of(bradesco, amil, portoSeguro));

        doctor("Juliana Prado", "juliana.prado@teste.com", "10000000005", "21940000005",
                "567890", "RJ", "Ginecologia e obstetricia.",
                "Rio de Janeiro", "RJ", "Rua Voluntarios da Patria, 88 - Botafogo",
                LocalTime.of(10, 0), LocalTime.of(19, 0),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY), 40, 20,
                Set.of(ginecologia), Set.of(sulAmerica, unimed));

        // sem convenio, so atende particular
        doctor("Rafael Monteiro", "rafael.monteiro@teste.com", "10000000006", "11940000006",
                "678901", "SP", "Neurologia clinica, cefaleia e epilepsia.",
                "Sao Paulo", "SP", "Rua Haddock Lobo, 585 - Cerqueira Cesar",
                LocalTime.of(8, 0), LocalTime.of(14, 0),
                Set.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY), 50, 30,
                Set.of(neurologia), Set.of());

        doctor("Patricia Nogueira", "patricia.nogueira@teste.com", "10000000007", "31940000007",
                "789012", "MG", "Psiquiatria adulta.",
                "Belo Horizonte", "MG", "Av. do Contorno, 4700 - Funcionarios",
                LocalTime.of(13, 0), LocalTime.of(20, 0), WEEKDAYS, 50, 50,
                Set.of(psiquiatria), Set.of(amil));

        doctor("Gustavo Ferreira", "gustavo.ferreira@teste.com", "10000000008", "41940000008",
                "890123", "PR", "Oftalmologia geral e cirurgia refrativa.",
                "Curitiba", "PR", "Rua XV de Novembro, 700 - Centro",
                LocalTime.of(8, 0), LocalTime.of(17, 0),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), 30, 15,
                Set.of(oftalmologia), Set.of(unimed, bradesco, sulAmerica, amil, notreDame));

        doctor("Luciana Castro", "luciana.castro@teste.com", "10000000009", "11940000009",
                "901234", "SP", "Endocrinologia, diabetes e tireoide.",
                "Sao Paulo", "SP", "Av. Bras Leme, 1200 - Santana",
                LocalTime.of(9, 0), LocalTime.of(15, 0),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), 40, 20,
                Set.of(endocrinologia, clinicaGeral), Set.of(notreDame));

        doctor("Thiago Almeida", "thiago.almeida@teste.com", "10000000010", "11940000010",
                "012345", "SP", "Clinico geral, atende tambem aos sabados.",
                "Guarulhos", "SP", "Av. Paulo Faccini, 300 - Macedo",
                LocalTime.of(7, 0), LocalTime.of(19, 0),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), 20, 15,
                Set.of(clinicaGeral), Set.of(bradesco, unimed));

        log.info("Seed: {} convenios, {} especialidades, {} usuarios ({} medicos). Admin: {} / {}",
                healthPlanRepository.count(), specialtyRepository.count(), userRepository.count(),
                doctorRepository.count(), ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    private HealthPlan healthPlan(String name, boolean active) {
        HealthPlan healthPlan = new HealthPlan();
        healthPlan.setName(name);
        healthPlan.setActive(active);
        return healthPlanRepository.save(healthPlan);
    }

    private Specialty specialty(String name, String description,
                                boolean requiresPriorExams, String examRequestMessage) {
        Specialty specialty = new Specialty();
        specialty.setName(name);
        specialty.setDescription(description);
        specialty.setRequiresPriorExams(requiresPriorExams);
        specialty.setExamRequestMessage(examRequestMessage);
        return specialtyRepository.save(specialty);
    }

    private void patient(String fullName, String email, String cpf, String phone,
                         LocalDate birthDate, HealthPlan healthPlan) {
        userRepository.save(user(fullName, email, DEFAULT_PASSWORD, cpf, phone, birthDate,
                Role.PATIENT, healthPlan));
    }

    private void doctor(String fullName, String email, String cpf, String phone,
                        String crmNumber, String crmUf, String bio,
                        String city, String state, String address,
                        LocalTime workStartTime, LocalTime workEndTime, Set<DayOfWeek> workDays,
                        int firstVisitDurationMin, int returnDurationMin,
                        Set<Specialty> specialties, Set<HealthPlan> acceptedPlans) {

        User user = user(fullName, email, DEFAULT_PASSWORD, cpf, phone,
                LocalDate.of(1980, 1, 1), Role.DOCTOR, null);
        userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setCrmNumber(crmNumber);
        doctor.setCrmUf(crmUf);
        doctor.setBio(bio);
        doctor.setCity(city);
        doctor.setState(state);
        doctor.setAddress(address);
        doctor.setWorkStartTime(workStartTime);
        doctor.setWorkEndTime(workEndTime);
        doctor.setWorkDays(new HashSet<>(workDays));
        doctor.setFirstVisitDurationMin(firstVisitDurationMin);
        doctor.setReturnDurationMin(returnDurationMin);
        doctor.setSpecialties(new HashSet<>(specialties));
        doctor.setAcceptedPlans(new HashSet<>(acceptedPlans));
        doctorRepository.save(doctor);
    }

    private User user(String fullName, String email, String password, String cpf, String phone,
                      LocalDate birthDate, Role role, HealthPlan healthPlan) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setCpf(cpf);
        user.setPhone(phone);
        user.setBirthDate(birthDate);
        user.setRole(role);
        user.setHealthPlan(healthPlan);
        return user;
    }
}
