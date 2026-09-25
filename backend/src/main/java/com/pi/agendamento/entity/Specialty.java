package com.pi.agendamento.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Entidade especialidade, N-N com medico, 1-N com appointment
@Entity
@Table(
        name = "specialty",
        uniqueConstraints = @UniqueConstraint(name = "uk_specialty_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean requiresPriorExams = false;

    @Column(length = 1000)
    private String examRequestMessage;

    @Column(nullable = false)
    private boolean active = true;
}