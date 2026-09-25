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

//Entidade convenio, N-N com medico, 1-N com usuario, 1-N com appointment
@Entity
@Table(
        name = "health_plan",
        uniqueConstraints = @UniqueConstraint(name = "uk_health_plan_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
public class HealthPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active = true;
}
