package com.pi.agendamento.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.pi.agendamento.config.CurrentUserProvider;
import com.pi.agendamento.dto.request.CreateExamRequest;
import com.pi.agendamento.dto.response.ExamFileResponse;
import com.pi.agendamento.dto.response.ExamResponse;
import com.pi.agendamento.dto.response.SharedExamResponse;
import com.pi.agendamento.entity.Appointment;
import com.pi.agendamento.entity.Exam;
import com.pi.agendamento.entity.User;
import com.pi.agendamento.enums.AppointmentStatus;
import com.pi.agendamento.exception.BusinessException;
import com.pi.agendamento.exception.ResourceNotFoundException;
import com.pi.agendamento.repository.AppointmentRepository;
import com.pi.agendamento.repository.ExamRepository;
import com.pi.agendamento.repository.UserRepository;

// biblioteca de exames do paciente e compartilhamento com consultas
@Service
public class ExamService {

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;
    private static final int MAX_EXAMS_PER_PATIENT = 50;

    // tipo aceito -> assinatura dos primeiros bytes e extensao
    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
            "application/pdf", new byte[] {0x25, 0x50, 0x44, 0x46},
            "image/jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "image/png", new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
    private static final Map<String, String> EXTENSIONS = Map.of(
            "application/pdf", "pdf",
            "image/jpeg", "jpg",
            "image/png", "png");

    private final ExamRepository examRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final CurrentUserProvider currentUserProvider;

    public ExamService(
            ExamRepository examRepository,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService,
            CurrentUserProvider currentUserProvider) {
        this.examRepository = examRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.currentUserProvider = currentUserProvider;
    }

    // valida e guarda o exame do paciente logado
    @Transactional
    public ExamResponse upload(CreateExamRequest request, MultipartFile file) {
        User patient = loadCurrentUser();

        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo obrigatorio");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException("Arquivo maior que 10MB");
        }
        if (examRepository.countByPatientId(patient.getId()) >= MAX_EXAMS_PER_PATIENT) {
            throw new BusinessException("Limite de " + MAX_EXAMS_PER_PATIENT + " exames atingido");
        }

        byte[] content = readBytes(file);
        // o tipo vem do conteudo; o Content-Type do request eh controlado pelo cliente
        String contentType = detectContentType(content);
        requireDeclaredTypeMatches(file.getContentType(), contentType);

        // nunca o nome original na chave: eh input do usuario e abre path traversal
        String storageKey = "exams/" + patient.getId() + "/" + UUID.randomUUID() + "." + EXTENSIONS.get(contentType);

        Exam exam = new Exam();
        exam.setPatient(patient);
        exam.setTitle(request.title().trim());
        exam.setExamDate(request.examDate());
        exam.setLaboratory(trimToNull(request.laboratory()));
        exam.setFileName(sanitizeFileName(file.getOriginalFilename(), EXTENSIONS.get(contentType)));
        exam.setContentType(contentType);
        exam.setSizeBytes(content.length);
        exam.setStorageKey(storageKey);

        fileStorageService.store(content, storageKey);
        // se a transacao nao confirmar, o arquivo nao fica orfao
        afterCompletion(committed -> {
            if (!committed) {
                fileStorageService.delete(storageKey);
            }
        });

        return ExamResponse.from(examRepository.saveAndFlush(exam));
    }

    // biblioteca do paciente logado
    @Transactional(readOnly = true)
    public List<ExamResponse> listMine() {
        return examRepository.findByPatientId(currentUserProvider.getId()).stream()
                .map(ExamResponse::from)
                .toList();
    }

    // arquivo para o dono ou para o medico de uma consulta com que foi compartilhado
    @Transactional(readOnly = true)
    public ExamFileResponse download(UUID id) {
        Exam exam = examRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exame nao encontrado"));

        UUID userId = currentUserProvider.getId();
        if (!exam.isOwnedBy(userId) && !examRepository.isSharedWithDoctor(id, userId)) {
            throw new AccessDeniedException("Exame de outro paciente");
        }
        return new ExamFileResponse(
                fileStorageService.load(exam.getStorageKey()),
                exam.getFileName(),
                exam.getContentType(),
                exam.getSizeBytes());
    }

    // remove o registro e, depois do commit, o arquivo
    @Transactional
    public void delete(UUID id) {
        Exam exam = loadOwned(id);
        String storageKey = exam.getStorageKey();
        examRepository.delete(exam);
        afterCompletion(committed -> {
            if (committed) {
                fileStorageService.delete(storageKey);
            }
        });
    }

    // libera o exame para o medico da consulta; repetir nao duplica
    @Transactional
    public ExamResponse share(UUID id, UUID appointmentId) {
        Exam exam = loadOwned(id);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta nao encontrada"));

        if (!appointment.getPatient().getId().equals(exam.getPatient().getId())) {
            throw new AccessDeniedException("Consulta de outro paciente");
        }
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED
                && appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessException("So eh possivel compartilhar com consulta agendada ou realizada");
        }

        exam.getSharedWith().add(appointment);
        return ExamResponse.from(exam);
    }

    // tira o acesso do medico daquela consulta
    @Transactional
    public void unshare(UUID id, UUID appointmentId) {
        Exam exam = loadOwned(id);
        exam.getSharedWith().removeIf(appointment -> appointment.getId().equals(appointmentId));
    }

    // exames compartilhados com a consulta, para o paciente ou o medico dela
    @Transactional(readOnly = true)
    public List<SharedExamResponse> listForAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta nao encontrada"));

        UUID userId = currentUserProvider.getId();
        boolean isPatient = appointment.getPatient().getId().equals(userId);
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(userId);
        if (!isPatient && !isDoctor) {
            throw new AccessDeniedException("Consulta de outro usuario");
        }

        return examRepository.findSharedWithAppointment(appointmentId).stream()
                .map(SharedExamResponse::from)
                .toList();
    }

    private Exam loadOwned(UUID id) {
        Exam exam = examRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exame nao encontrado"));
        if (!exam.isOwnedBy(currentUserProvider.getId())) {
            throw new AccessDeniedException("Exame de outro paciente");
        }
        return exam;
    }

    private User loadCurrentUser() {
        return userRepository.findById(currentUserProvider.getId())
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado", ex);
        }
    }

    private static String detectContentType(byte[] content) {
        return MAGIC_BYTES.entrySet().stream()
                .filter(entry -> startsWith(content, entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new BusinessException("Tipo de arquivo nao aceito. Envie PDF, JPEG ou PNG"));
    }

    private static void requireDeclaredTypeMatches(String declared, String detected) {
        if (declared == null || declared.isBlank()) {
            return;
        }
        String normalized = declared.toLowerCase(Locale.ROOT).split(";")[0].trim();
        if (normalized.equals("image/jpg")) {
            normalized = "image/jpeg";
        }
        // octet-stream eh o que alguns clientes mandam quando nao sabem o tipo
        if (!normalized.equals("application/octet-stream") && !normalized.equals(detected)) {
            throw new BusinessException("Content-Type nao corresponde ao conteudo do arquivo");
        }
    }

    private static boolean startsWith(byte[] content, byte[] prefix) {
        return content.length >= prefix.length
                && Arrays.equals(Arrays.copyOf(content, prefix.length), prefix);
    }

    // so a parte depois da ultima barra, sem caracteres estranhos
    private static String sanitizeFileName(String original, String extension) {
        String name = original == null ? "" : original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1)
                .replaceAll("[^\\p{L}\\p{N} ._()-]", "_")
                .trim();
        if (name.isBlank() || name.chars().allMatch(ch -> ch == '.')) {
            name = "exame." + extension;
        }
        return name.length() > 255 ? name.substring(name.length() - 255) : name;
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void afterCompletion(Consumer<Boolean> action) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                action.accept(status == STATUS_COMMITTED);
            }
        });
    }
}
