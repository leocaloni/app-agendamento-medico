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

import jakarta.validation.Valid;

// Rotas espalhadas entre recursos (consulta, medico, avaliacao); acesso definido no SecurityConfig
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

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

    // Ordenacao fixa em mais recentes primeiro; o service ignora `sort`.
    @GetMapping("/doctors/{id}/reviews")
    public Page<ReviewResponse> listForDoctor(@PathVariable UUID id,
                                              @PageableDefault(size = 10) Pageable pageable) {
        return reviewService.listForDoctor(id, pageable);
    }

    @GetMapping("/reviews/me")
    public List<MyReviewResponse> listMine() {
        return reviewService.listMine();
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
