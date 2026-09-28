package com.pi.agendamento.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pi.agendamento.dto.request.SpecialtyRequest;
import com.pi.agendamento.dto.response.SpecialtyResponse;
import com.pi.agendamento.entity.Specialty;
import com.pi.agendamento.exception.BusinessException;
import com.pi.agendamento.exception.ConflictException;
import com.pi.agendamento.exception.ResourceNotFoundException;
import com.pi.agendamento.repository.SpecialtyRepository;

// catalogo de especialidades
@Service
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    public SpecialtyService(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    // lista as especialidades ativas
    @Transactional(readOnly = true)
    public List<SpecialtyResponse> listActive() {
        return specialtyRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(SpecialtyResponse::from)
                .toList();
    }

    // lista todas, inclusive inativas
    @Transactional(readOnly = true)
    public List<SpecialtyResponse> listAll() {
        return specialtyRepository.findAllByOrderByNameAsc().stream()
                .map(SpecialtyResponse::from)
                .toList();
    }

    // busca especialidade por id
    @Transactional(readOnly = true)
    public SpecialtyResponse getById(UUID id) {
        return SpecialtyResponse.from(load(id));
    }

    // cria especialidade com nome unico
    @Transactional
    public SpecialtyResponse create(SpecialtyRequest request) {
        String name = request.name().trim();
        if (specialtyRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Especialidade ja cadastrada");
        }
        validateExamMessage(request);

        Specialty specialty = new Specialty();
        apply(specialty, name, request);
        return SpecialtyResponse.from(specialtyRepository.save(specialty));
    }

    // atualiza especialidade; tambem reativa se estava inativa
    @Transactional
    public SpecialtyResponse update(UUID id, SpecialtyRequest request) {
        Specialty specialty = load(id);
        String name = request.name().trim();
        if (!specialty.getName().equalsIgnoreCase(name) && specialtyRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Especialidade ja cadastrada");
        }
        validateExamMessage(request);

        apply(specialty, name, request);
        return SpecialtyResponse.from(specialty);
    }

    // desativa sem apagar, para manter os vinculos existentes
    @Transactional
    public void deactivate(UUID id) {
        load(id).setActive(false);
    }

    private void apply(Specialty specialty, String name, SpecialtyRequest request) {
        specialty.setName(name);
        specialty.setDescription(request.description());
        specialty.setRequiresPriorExams(request.requiresPriorExams());
        specialty.setExamRequestMessage(request.requiresPriorExams() ? request.examRequestMessage().trim() : null);
        specialty.setActive(true);
    }

    private void validateExamMessage(SpecialtyRequest request) {
        if (request.requiresPriorExams()
                && (request.examRequestMessage() == null || request.examRequestMessage().isBlank())) {
            throw new BusinessException("examRequestMessage eh obrigatorio quando requiresPriorExams eh true");
        }
    }

    private Specialty load(UUID id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidade nao encontrada"));
    }
}
