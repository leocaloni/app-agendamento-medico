package com.pi.agendamento.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// exame guardado pelo paciente; o medico so ve quando o exame eh compartilhado com uma consulta dele
@Entity
@Table(
        name = "exam",
        uniqueConstraints = @UniqueConstraint(name = "uk_exam_storage_key", columnNames = "storage_key"),
        indexes = @Index(name = "idx_exam_patient", columnList = "patient_id")
)
@Getter
@Setter
@NoArgsConstructor
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false)
    private LocalDate examDate;

    @Column(length = 150)
    private String laboratory;

    // nome original sanitizado, so para exibicao
    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 100)
    private String contentType;

    private long sizeBytes;

    // caminho no storage; o arquivo nao vai pro banco
    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "exam_appointment",
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "appointment_id")
    )
    private Set<Appointment> sharedWith = new HashSet<>();

    public boolean isOwnedBy(UUID userId) {
        return patient.getId().equals(userId);
    }
}
