package com.pi.agendamento.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pi.agendamento.config.CurrentUserProvider;
import com.pi.agendamento.dto.request.DoctorSearchFilter;
import com.pi.agendamento.dto.request.UpdateDoctorProfileRequest;
import com.pi.agendamento.dto.request.UpdateScheduleRequest;
import com.pi.agendamento.dto.response.DoctorDetailResponse;
import com.pi.agendamento.dto.response.DoctorSearchProjection;
import com.pi.agendamento.dto.response.DoctorSearchResponse;
import com.pi.agendamento.dto.response.DoctorSummaryResponse;
import com.pi.agendamento.dto.response.HealthPlanSummary;
import com.pi.agendamento.entity.Doctor;
import com.pi.agendamento.entity.HealthPlan;
import com.pi.agendamento.entity.Specialty;
import com.pi.agendamento.entity.User;
import com.pi.agendamento.enums.Role;
import com.pi.agendamento.exception.BusinessException;
import com.pi.agendamento.exception.ResourceNotFoundException;
import com.pi.agendamento.repository.DoctorRepository;
import com.pi.agendamento.repository.HealthPlanRepository;
import com.pi.agendamento.repository.SpecialtyRepository;
import com.pi.agendamento.repository.UserRepository;

// busca, perfil e agenda de medicos
@Service
public class DoctorService {

    // whitelist do sort: o valor vai direto para a query nativa, entao so colunas conhecidas
    private static final Map<String, String> SORTABLE = Map.of(
            "rating", "rating",
            "name", "name");

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final HealthPlanRepository healthPlanRepository;
    private final ReviewService reviewService;
    private final CurrentUserProvider currentUserProvider;

    public DoctorService(
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            SpecialtyRepository specialtyRepository,
            HealthPlanRepository healthPlanRepository,
            ReviewService reviewService,
            CurrentUserProvider currentUserProvider) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.specialtyRepository = specialtyRepository;
        this.healthPlanRepository = healthPlanRepository;
        this.reviewService = reviewService;
        this.currentUserProvider = currentUserProvider;
    }

    // busca medicos com filtros, paginada
    @Transactional(readOnly = true)
    public DoctorSearchResponse search(DoctorSearchFilter filter, Pageable pageable) {
        HealthPlan planFilter = resolvePlanFilter(filter);

        // duas queries: ids e nota paginados primeiro, entidades depois; paginar com fetch join seria em memoria
        Page<Object[]> rows = doctorRepository.search(
                trimToNull(filter.name()),
                filter.specialtyId(),
                trimToNull(filter.city()),
                trimToNull(filter.state()),
                filter.minRating(),
                planFilter != null ? planFilter.getId() : null,
                sortedPage(pageable));

        List<DoctorSearchProjection> projections = rows.getContent().stream()
                .map(DoctorSearchProjection::from)
                .toList();


        Map<UUID, Doctor> byId = loadDoctors(projections.stream()
                .map(DoctorSearchProjection::doctorId)
                .toList());

        List<DoctorSummaryResponse> content = projections.stream()
                .map(projection -> DoctorSummaryResponse.from(
                        byId.get(projection.doctorId()),
                        projection.ratingAvg(),
                        projection.ratingCount()))
                .toList();

        Page<DoctorSummaryResponse> doctors =
                new PageImpl<>(content, rows.getPageable(), rows.getTotalElements());
        return new DoctorSearchResponse(doctors, HealthPlanSummary.from(planFilter));
    }

    // perfil publico de medico ativo
    @Transactional(readOnly = true)
    public DoctorDetailResponse getById(UUID id) {
        Doctor doctor = doctorRepository.findByIdWithDetails(id)
                .filter(found -> found.getUser().isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Medico nao encontrado"));
        return detail(doctor);
    }

    // perfil do medico logado
    @Transactional(readOnly = true)
    public DoctorDetailResponse getCurrent() {
        return detail(loadCurrentDoctor());
    }

    // atualiza perfil, especialidades e convenios do medico logado
    @Transactional
    public DoctorDetailResponse updateProfile(UpdateDoctorProfileRequest request) {
        Doctor doctor = loadCurrentDoctor();
        doctor.setBio(request.bio());
        doctor.setCity(request.city());
        doctor.setState(request.state());
        doctor.setAddress(request.address());
        doctor.setSpecialties(resolveSpecialties(request.specialtyIds()));
        doctor.setAcceptedPlans(resolveHealthPlans(request.acceptedPlanIds()));
        return detail(doctor);
    }

    // atualiza expediente e duracoes do medico logado
    @Transactional
    public DoctorDetailResponse updateSchedule(UpdateScheduleRequest request) {
        if (!request.workStartTime().isBefore(request.workEndTime())) {
            throw new BusinessException("Horario de inicio do expediente deve ser anterior ao de fim");
        }

        Doctor doctor = loadCurrentDoctor();
        doctor.setWorkStartTime(request.workStartTime());
        doctor.setWorkEndTime(request.workEndTime());
        doctor.setWorkDays(new HashSet<>(request.workDays()));
        doctor.setFirstVisitDurationMin(request.firstVisitDurationMin());
        doctor.setReturnDurationMin(request.returnDurationMin());
        return detail(doctor);
    }

    private HealthPlan resolvePlanFilter(DoctorSearchFilter filter) {
        if (filter.allPlans()) {
            return null;
        }
        if (filter.healthPlanId() != null) {
            return healthPlanRepository.findById(filter.healthPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Convenio nao encontrado"));
        }
        // sem checar active: convenio desativado continua valendo para quem ja o tem
        return currentUserProvider.findId()
                .flatMap(userRepository::findById)
                .filter(user -> user.getRole() == Role.PATIENT)
                .map(User::getHealthPlan)
                .orElse(null);
    }

    private Map<UUID, Doctor> loadDoctors(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return doctorRepository.findAllWithDetails(ids).stream()
                .collect(Collectors.toMap(Doctor::getId, Function.identity()));
    }

    private Pageable sortedPage(Pageable pageable) {
        Sort sort = Sort.unsorted();
        for (Sort.Order order : pageable.getSort()) {
            String column = SORTABLE.get(order.getProperty().toLowerCase(Locale.ROOT));
            if (column != null) {
                sort = sort.and(Sort.by(order.getDirection(), column));
            }
        }
        if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        }
        // desempate por id para a paginacao nao repetir nem pular medicos
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                sort.and(Sort.by(Sort.Direction.ASC, "doctor_id")));
    }

    private Doctor loadCurrentDoctor() {
        // pelo usuario do token, nunca por id do path
        return doctorRepository.findByUserId(currentUserProvider.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Medico nao encontrado para o usuario logado"));
    }

    private DoctorDetailResponse detail(Doctor doctor) {
        return DoctorDetailResponse.from(doctor, reviewService.getRating(doctor.getId()));
    }

    private Set<Specialty> resolveSpecialties(Set<UUID> ids) {
        List<Specialty> specialties = specialtyRepository.findAllById(ids);
        if (specialties.size() != ids.size() || !specialties.stream().allMatch(Specialty::isActive)) {
            throw new BusinessException("Especialidade nao encontrada ou inativa");
        }
        return new HashSet<>(specialties);
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

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
