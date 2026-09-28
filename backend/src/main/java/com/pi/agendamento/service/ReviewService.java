package com.pi.agendamento.service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pi.agendamento.config.CurrentUserProvider;
import com.pi.agendamento.dto.request.CreateReviewRequest;
import com.pi.agendamento.dto.response.DoctorRatingResponse;
import com.pi.agendamento.dto.response.MyReviewResponse;
import com.pi.agendamento.dto.response.ReviewResponse;
import com.pi.agendamento.entity.Appointment;
import com.pi.agendamento.entity.Review;
import com.pi.agendamento.exception.BusinessException;
import com.pi.agendamento.exception.ConflictException;
import com.pi.agendamento.exception.ResourceNotFoundException;
import com.pi.agendamento.repository.AppointmentRepository;
import com.pi.agendamento.repository.DoctorRepository;
import com.pi.agendamento.repository.ReviewRepository;

// avaliacoes de consultas e nota dos medicos
@Service
public class ReviewService {

    // ordem fixa; o sort do cliente eh ignorado porque propriedade inexistente viraria 500
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt")
            .and(Sort.by(Sort.Direction.ASC, "id"));

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentUserProvider currentUserProvider;

    public ReviewService(
            ReviewRepository reviewRepository,
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository,
            CurrentUserProvider currentUserProvider) {
        this.reviewRepository = reviewRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // paciente avalia a propria consulta realizada
    @Transactional
    public ReviewResponse create(UUID appointmentId, CreateReviewRequest request) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta nao encontrada"));

        // dono antes do status, para nao revelar o estado da consulta a terceiros
        if (!appointment.getPatient().getId().equals(currentUserProvider.getId())) {
            throw new AccessDeniedException("Consulta de outro paciente");
        }
        if (!appointment.canBeReviewed()) {
            throw new BusinessException("So eh possivel avaliar uma consulta concluida");
        }
        if (reviewRepository.existsByAppointmentId(appointmentId)) {
            throw new ConflictException("Esta consulta ja foi avaliada");
        }

        Review review = new Review();
        review.setAppointment(appointment);
        // medico e autor vem da consulta, nunca do request
        review.setDoctor(appointment.getDoctor());
        review.setAuthor(appointment.getPatient());
        review.setRating(request.rating());
        review.setComment(request.comment());

        return ReviewResponse.from(save(review));
    }

    // avaliacoes do medico, mais recentes primeiro
    @Transactional(readOnly = true)
    public Page<ReviewResponse> listForDoctor(UUID doctorId, Pageable pageable) {
        doctorRepository.findById(doctorId)
                .filter(doctor -> doctor.getUser().isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Medico nao encontrado"));

        Pageable newestFirst = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), NEWEST_FIRST);
        return reviewRepository.findByDoctorId(doctorId, newestFirst).map(ReviewResponse::from);
    }

    // avaliacoes do paciente logado
    @Transactional(readOnly = true)
    public List<MyReviewResponse> listMine() {
        return reviewRepository.findByAuthorIdOrderByCreatedAtDesc(currentUserProvider.getId()).stream()
                .map(MyReviewResponse::from)
                .toList();
    }

    // remove avaliacao, usado na moderacao
    @Transactional
    public void delete(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliacao nao encontrada"));
        reviewRepository.delete(review);
    }

    // media, total e distribuicao do medico; nao chamar em laco
    @Transactional(readOnly = true)
    public DoctorRatingResponse getRating(UUID doctorId) {
        Map<Integer, Long> distribution = new TreeMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            distribution.put(rating, 0L);
        }
        for (Object[] row : reviewRepository.getRatingDistribution(doctorId)) {
            distribution.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return DoctorRatingResponse.from(reviewRepository.getStatsByDoctorId(doctorId), distribution);
    }

    // a UNIQUE do banco barra avaliacao dupla simultanea; o flush faz o erro subir aqui
    private Review save(Review review) {
        try {
            return reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Esta consulta ja foi avaliada");
        }
    }
}
