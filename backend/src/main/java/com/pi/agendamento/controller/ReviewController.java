package com.pi.agendamento.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pi.agendamento.dto.request.CreateReviewRequest;
import com.pi.agendamento.dto.response.MyReviewResponse;
import com.pi.agendamento.dto.response.ReviewResponse;
import com.pi.agendamento.service.ReviewService;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// rotas de avaliacao, sob consulta, medico e avaliacao
@Tag(name = "Avaliacoes")
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // avalia a consulta e responde 201 com Location
    @Operation(summary = "Avaliar consulta (PATIENT)",
            description = "So o paciente da consulta, e so se estiver COMPLETED. 409 se ja avaliada.")
    @PostMapping("/appointments/{id}/review")
    public ResponseEntity<ReviewResponse> create(@PathVariable UUID id,
                                                 @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.create(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/reviews/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    // avaliacoes do medico, paginadas
    @Operation(summary = "Avaliacoes do medico",
            description = "Publico. Mais recentes primeiro; o parametro sort eh ignorado.")
    @SecurityRequirements
    @GetMapping("/doctors/{id}/reviews")
    public Page<ReviewResponse> listForDoctor(@PathVariable UUID id,
                                              @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return reviewService.listForDoctor(id, pageable);
    }

    // avaliacoes do paciente logado
    @Operation(summary = "Minhas avaliacoes (PATIENT)")
    @GetMapping("/reviews/me")
    public List<MyReviewResponse> listMine() {
        return reviewService.listMine();
    }

    // remove e responde 204
    @Operation(summary = "Remover avaliacao (ADMIN)", description = "Moderacao.")
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
