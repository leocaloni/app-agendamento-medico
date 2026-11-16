package com.pi.agendamento.entity;

import java.math.BigDecimal;

import com.pi.agendamento.enums.BloodType;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// dados de saude que o paciente informa; colunas na propria tabela users
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class HealthInfo {

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type", length = 20)
    private BloodType bloodType;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "allergies", length = 1000)
    private String allergies;

    @Column(name = "chronic_conditions", length = 1000)
    private String chronicConditions;

    @Column(name = "continuous_medications", length = 1000)
    private String continuousMedications;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;
}
