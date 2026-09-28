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

// catalogo de convenios
@Service
public class HealthPlanService {

    private final HealthPlanRepository healthPlanRepository;

    public HealthPlanService(HealthPlanRepository healthPlanRepository) {
        this.healthPlanRepository = healthPlanRepository;
    }

    // lista os convenios ativos
    @Transactional(readOnly = true)
    public List<HealthPlanResponse> listActive() {
        return healthPlanRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(HealthPlanResponse::from)
                .toList();
    }

    // lista todos, inclusive inativos
    @Transactional(readOnly = true)
    public List<HealthPlanResponse> listAll() {
        return healthPlanRepository.findAllByOrderByNameAsc().stream()
                .map(HealthPlanResponse::from)
                .toList();
    }

    // busca convenio por id
    @Transactional(readOnly = true)
    public HealthPlanResponse getById(UUID id) {
        return HealthPlanResponse.from(load(id));
    }

    // cria convenio com nome unico
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

    // atualiza convenio; tambem reativa se estava inativo
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

    // desativa sem apagar; quem ja referencia o convenio nao muda
    @Transactional
    public void deactivate(UUID id) {
        load(id).setActive(false);
    }

    private HealthPlan load(UUID id) {
        return healthPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convenio nao encontrado"));
    }
}
