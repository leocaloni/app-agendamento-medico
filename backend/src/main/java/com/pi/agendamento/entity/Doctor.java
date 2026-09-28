package com.pi.agendamento.entity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.pi.agendamento.enums.AppointmentType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// perfil de medico, ligado 1-1 ao usuario
@Entity
@Table(
        name = "doctor",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_doctor_user", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_doctor_crm", columnNames = {"crm_number", "crm_uf"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "crm_number", nullable = false)
    private String crmNumber;

    @Column(name = "crm_uf", nullable = false, length = 2)
    private String crmUf;

    @Column(length = 2000)
    private String bio;

    private String city;

    private String state;

    private String address;

    @Column(nullable = false)
    private LocalTime workStartTime;

    @Column(nullable = false)
    private LocalTime workEndTime;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "doctor_work_days",
            joinColumns = @JoinColumn(name = "doctor_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "work_day", nullable = false)
    private Set<DayOfWeek> workDays = new HashSet<>();

    @Column(nullable = false)
    private int firstVisitDurationMin;

    @Column(nullable = false)
    private int returnDurationMin;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "doctor_specialty",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "specialty_id")
    )
    private Set<Specialty> specialties = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "doctor_health_plan",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "health_plan_id")
    )

    private Set<HealthPlan> acceptedPlans = new HashSet<>();

    // indica se o medico atende no dia da semana da data
    public boolean worksOn(LocalDate date) {
        return workDays.contains(date.getDayOfWeek());
    }

    // duracao em minutos para o tipo de consulta
    public int durationFor(AppointmentType type) {
        return type == AppointmentType.RETORNO ? returnDurationMin : firstVisitDurationMin;
    }

    // indica se o medico atende a especialidade
    public boolean hasSpecialty(UUID specialtyId) {
        return specialties.stream()
                .anyMatch(specialty -> specialty.getId().equals(specialtyId));
    }

    // indica se o medico aceita o convenio
    public boolean acceptsPlan(UUID healthPlanId) {
        return acceptedPlans.stream()
                .anyMatch(healthPlan -> healthPlan.getId().equals(healthPlanId));
    }
}