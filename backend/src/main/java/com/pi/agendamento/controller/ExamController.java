package com.pi.agendamento.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pi.agendamento.dto.request.CreateExamRequest;
import com.pi.agendamento.dto.response.ExamFileResponse;
import com.pi.agendamento.dto.response.ExamResponse;
import com.pi.agendamento.dto.response.SharedExamResponse;
import com.pi.agendamento.service.ExamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// exames do paciente e compartilhamento com consultas
@Tag(name = "Exames")
@RestController
@RequestMapping("/api")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    // upload multipart: arquivo + title, examDate, laboratory
    @Operation(summary = "Enviar exame (PATIENT)", description = "PDF, JPEG ou PNG ate 10MB.")
    @PostMapping(value = "/exams", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExamResponse> upload(@Valid @ModelAttribute CreateExamRequest request,
                                               @RequestPart("file") MultipartFile file) {
        ExamResponse response = examService.upload(request, file);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/exams/{id}/download")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Meus exames (PATIENT)")
    @GetMapping("/exams/me")
    public List<ExamResponse> listMine() {
        return examService.listMine();
    }

    @Operation(summary = "Baixar exame", description = "Dono do exame ou medico de uma consulta com que ele foi compartilhado.")
    @GetMapping("/exams/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        ExamFileResponse file = examService.download(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(file.resource());
    }

    @Operation(summary = "Apagar exame (PATIENT)")
    @DeleteMapping("/exams/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        examService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Compartilhar exame com uma consulta (PATIENT)",
            description = "O medico da consulta passa a ver e baixar o exame.")
    @PutMapping("/exams/{id}/shares/{appointmentId}")
    public ExamResponse share(@PathVariable UUID id, @PathVariable UUID appointmentId) {
        return examService.share(id, appointmentId);
    }

    @Operation(summary = "Parar de compartilhar exame (PATIENT)")
    @DeleteMapping("/exams/{id}/shares/{appointmentId}")
    public ResponseEntity<Void> unshare(@PathVariable UUID id, @PathVariable UUID appointmentId) {
        examService.unshare(id, appointmentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Exames compartilhados com a consulta", description = "Paciente ou medico da consulta.")
    @GetMapping("/appointments/{appointmentId}/exams")
    public List<SharedExamResponse> listForAppointment(@PathVariable UUID appointmentId) {
        return examService.listForAppointment(appointmentId);
    }
}
