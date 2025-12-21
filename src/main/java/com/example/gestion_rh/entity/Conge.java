package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "conges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeConge typeConge;

    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutConge statut;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Priorite priorite = Priorite.NORMAL;

    @Column(length = 500)
    private String commentaireManager;

    @Column(length = 500)
    private String commentaireRH;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne
    @JoinColumn(name = "manager_validateur_id")
    private Employe managerValidateur;

    @ManyToOne
    @JoinColumn(name = "rh_validateur_id")
    private Employe rhValidateur;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TypeConge {
        PAYE, MALADIE, MATERNITE, PATERNITE, SANS_SOLDE
    }

    public enum StatutConge {
        EN_ATTENTE_MANAGER, VALIDE_MANAGER, REFUSE_MANAGER,
        EN_ATTENTE_RH, VALIDE_RH, REFUSE_RH, ANNULE,
        EN_ATTENTE_INFO, RENVOYE_MANAGER
    }

    public enum Priorite {
        URGENT, NORMAL
    }

    // Calculate number of days between dates
    public long getNombreJours() {
        if (dateDebut != null && dateFin != null) {
            return ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
        }
        return 0;
    }

    // Check if this leave is pending RH validation
    public boolean isEnAttenteRH() {
        return statut == StatutConge.VALIDE_MANAGER || statut == StatutConge.EN_ATTENTE_RH;
    }

    // Check if urgent
    public boolean isUrgent() {
        return priorite == Priorite.URGENT;
    }
}