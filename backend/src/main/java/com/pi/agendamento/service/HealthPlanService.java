package com.pi.agendamento.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pi.agendamento.dto.request.HealthPlanRequest;
import com.pi.agendamento.dto.response.HealthPlanResponse;
import com.pi.agendamento.entity.HealthPlan;
import com.pi.agendamento.exception.ConflictException;
import com.pi.agendamento.exception.ResourceNotFoundException;
import com.pi.agendamento.repository.HealthPlanRepository;

@Service
public class HealthPlanService {

    private final HealthPlanRepository healthPlanRepository;

    public HealthPlanService(HealthPlanRepository healthPlanRepository) {
        this.healthPlanRepository = healthPlanRepository;
    }

    @Transactional(readOnly = true)
    public List<HealthPlanResponse> listActive() {
        return healthPlanRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(HealthPlanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HealthPlanResponse> listAll() {
        return healthPlanRepository.findAllByOrderByNameAsc().stream()
                .map(HealthPlanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HealthPlanResponse getById(UUID id) {
        return HealthPlanResponse.from(load(id));
    }

    @Transactional
    public HealthPlanResponse create(HealthPlanRequest request) {
        String name = request.name().trim();
        if (healthPlanRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Convenio ja cadastrado");
        }

        HealthPlan healthPlan = new HealthPlan();
        healthPlan.setName(name);
        healthPlan.setActive(true);
        return HealthPlanResponse.from(healthPlanRepository.save(healthPlan));
    }

    // PUT tambem reativa: um convenio desativado volta a ficar ativo ao ser atualizado.
    @Transactional
    public HealthPlanResponse update(UUID id, HealthPlanRequest request) {
        HealthPlan healthPlan = load(id);
        String name = request.name().trim();
        if (!healthPlan.getName().equalsIgnoreCase(name) && healthPlanRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Convenio ja cadastrado");
        }

        healthPlan.setName(name);
        healthPlan.setActive(true);
        return HealthPlanResponse.from(healthPlan);
    }

    // Delete logico: quem ja referencia o plano (paciente, medico, consulta) nao eh alterado,
    // o plano apenas some das listagens publicas.
    @Transactional
    public void deactivate(UUID id) {
        load(id).setActive(false);
    }

    private HealthPlan load(UUID id) {
        return healthPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convenio nao encontrado"));
    }
}
