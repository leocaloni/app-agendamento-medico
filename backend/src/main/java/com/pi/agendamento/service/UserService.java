package com.pi.agendamento.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pi.agendamento.config.CurrentUserProvider;
import com.pi.agendamento.config.JwtService;
import com.pi.agendamento.dto.request.CreateAdminRequest;
import com.pi.agendamento.dto.request.CreateDoctorRequest;
import com.pi.agendamento.dto.request.LoginRequest;
import com.pi.agendamento.dto.request.RegisterRequest;
import com.pi.agendamento.dto.request.UpdateProfileRequest;
import com.pi.agendamento.dto.response.AuthResponse;
import com.pi.agendamento.dto.response.DoctorResponse;
import com.pi.agendamento.dto.response.UserResponse;
import com.pi.agendamento.entity.Doctor;
import com.pi.agendamento.entity.HealthPlan;
import com.pi.agendamento.entity.Specialty;
import com.pi.agendamento.entity.User;
import com.pi.agendamento.enums.Role;
import com.pi.agendamento.exception.BusinessException;
import com.pi.agendamento.exception.ConflictException;
import com.pi.agendamento.repository.DoctorRepository;
import com.pi.agendamento.repository.HealthPlanRepository;
import com.pi.agendamento.repository.SpecialtyRepository;
import com.pi.agendamento.repository.UserRepository;

@Service
public class UserService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final String INVALID_CREDENTIALS = "Email ou senha invalidos";

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final HealthPlanRepository healthPlanRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserProvider currentUserProvider;

    private final String dummyPasswordHash;

    public UserService(
            UserRepository userRepository,
            DoctorRepository doctorRepository,
            SpecialtyRepository specialtyRepository,
            HealthPlanRepository healthPlanRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
        this.healthPlanRepository = healthPlanRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.currentUserProvider = currentUserProvider;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-timing-only");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = newUser(request.fullName(), request.email(), request.password(),
                request.cpf(), request.phone(), request.birthDate(), Role.PATIENT);
        user.setHealthPlan(resolveHealthPlan(request.healthPlanId()));
        userRepository.save(user);
        return authResponseFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email())).orElse(null);

        String hash = user != null ? user.getPasswordHash() : dummyPasswordHash;
        boolean passwordMatches = passwordEncoder.matches(request.password(), hash);

        if (user == null || !passwordMatches || !user.isActive()) {
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }
        return authResponseFor(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return UserResponse.from(loadCurrentUser());
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = loadCurrentUser();

        if (request.healthPlanId() != null && user.getRole() != Role.PATIENT) {
            throw new BusinessException("Convenio so pode ser informado por pacientes");
        }

        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        user.setBirthDate(request.birthDate());
        user.setHealthPlan(resolveHealthPlan(request.healthPlanId()));

        return UserResponse.from(user);
    }

    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        User user = newUser(request.fullName(), request.email(), request.password(),
                request.cpf(), request.phone(), request.birthDate(), Role.DOCTOR);

        String crmUf = request.crmUf().toUpperCase(Locale.ROOT);
        String crmNumber = request.crmNumber().trim();
        if (doctorRepository.existsByCrmNumberAndCrmUf(crmNumber, crmUf)) {
            throw new ConflictException("CRM ja cadastrado para esta UF");
        }
        if (!request.workStartTime().isBefore(request.workEndTime())) {
            throw new BusinessException("Horario de inicio do expediente deve ser anterior ao de fim");
        }

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setCrmNumber(crmNumber);
        doctor.setCrmUf(crmUf);
        doctor.setBio(request.bio());
        doctor.setCity(request.city());
        doctor.setState(request.state());
        doctor.setAddress(request.address());
        doctor.setWorkStartTime(request.workStartTime());
        doctor.setWorkEndTime(request.workEndTime());
        doctor.setWorkDays(new HashSet<>(request.workDays()));
        doctor.setFirstVisitDurationMin(request.firstVisitDurationMin());
        doctor.setReturnDurationMin(request.returnDurationMin());
        doctor.setSpecialties(resolveSpecialties(request.specialtyIds()));
        doctor.setAcceptedPlans(resolveHealthPlans(request.acceptedPlanIds()));

        userRepository.save(user);
        doctorRepository.save(doctor);
        return DoctorResponse.from(doctor);
    }

    @Transactional
    public UserResponse createAdmin(CreateAdminRequest request) {
        User user = newUser(request.fullName(), request.email(), request.password(),
                request.cpf(), request.phone(), request.birthDate(), Role.ADMIN);
        userRepository.save(user);
        return UserResponse.from(user);
    }

    private User newUser(String fullName, String email, String password, String cpf,
                         String phone, LocalDate birthDate, Role role) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email ja cadastrado");
        }
        if (userRepository.existsByCpf(cpf)) {
            throw new ConflictException("CPF ja cadastrado");
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setCpf(cpf);
        user.setPhone(phone);
        user.setBirthDate(birthDate);
        user.setRole(role);
        return user;
    }

    private User loadCurrentUser() {
        return userRepository.findById(currentUserProvider.getId())
                .filter(User::isActive)
                .orElseThrow(() -> new BadCredentialsException("Usuario do token nao encontrado ou inativo"));
    }

    private HealthPlan resolveHealthPlan(UUID healthPlanId) {
        if (healthPlanId == null) {
            return null;
        }
        return healthPlanRepository.findById(healthPlanId)
                .filter(HealthPlan::isActive)
                .orElseThrow(() -> new BusinessException("Convenio nao encontrado ou inativo"));
    }

    private Set<HealthPlan> resolveHealthPlans(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        List<HealthPlan> plans = healthPlanRepository.findAllById(ids);
        if (plans.size() != ids.size() || !plans.stream().allMatch(HealthPlan::isActive)) {
            throw new BusinessException("Convenio nao encontrado ou inativo");
        }
        return new HashSet<>(plans);
    }

    private Set<Specialty> resolveSpecialties(Set<UUID> ids) {
        List<Specialty> specialties = specialtyRepository.findAllById(ids);
        if (specialties.size() != ids.size() || !specialties.stream().allMatch(Specialty::isActive)) {
            throw new BusinessException("Especialidade nao encontrada ou inativa");
        }
        return new HashSet<>(specialties);
    }

    private AuthResponse authResponseFor(User user) {
        String token = jwtService.generateToken(user.getId(), user.getRole());
        return new AuthResponse(token, TOKEN_TYPE, jwtService.getExpirationSeconds(), UserResponse.from(user));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
