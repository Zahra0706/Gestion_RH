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
@Getter
@Setter
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
    @Builder.Default
    private StatutConge statut = StatutConge.EN_ATTENTE_MANAGER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priorite priorite = Priorite.NORMAL;

    @Column(length = 500)
    private String commentaireManager;

    @Column(length = 500)
    private String commentaireRH;

    // Champs d'audit
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_validateur_id")
    private Employe managerValidateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rh_validateur_id")
    private Employe rhValidateur;

    // Callbacks JPA
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Initialisation automatique du statut si non défini
        if (statut == null) {
            statut = StatutConge.EN_ATTENTE_MANAGER;
        }

        // Initialisation automatique de la priorité si non définie
        if (priorite == null) {
            priorite = Priorite.NORMAL;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        // Mise à jour automatique de la date de traitement lors des changements de statut
        if (statut != null && (
                statut == StatutConge.VALIDE_MANAGER ||
                        statut == StatutConge.VALIDE_RH ||
                        statut == StatutConge.REFUSE_MANAGER ||
                        statut == StatutConge.REFUSE_RH ||
                        statut == StatutConge.ANNULE)) {
            dateTraitement = LocalDateTime.now();
        }
    }

    // Enums fusionnés
    public enum TypeConge {
        PAYE,
        MALADIE,
        MATERNITE,
        PATERNITE,
        SANS_SOLDE
    }

    public enum StatutConge {
        // Statuts du premier fichier
        EN_ATTENTE_MANAGER,
        VALIDE_MANAGER,
        REFUSE_MANAGER,
        EN_ATTENTE_RH,
        VALIDE_RH,
        REFUSE_RH,
        ANNULE,

        // Statuts additionnels du deuxième fichier
        EN_ATTENTE_INFO,
        RENVOYE_MANAGER
    }

    public enum Priorite {
        URGENT,
        NORMAL
    }


    @Transient
    public long getNombreJours() {
        if (dateDebut != null && dateFin != null) {
            return ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
        }
        return 0;
    }

    @Transient
    public boolean isEnAttenteRH() {
        return statut == StatutConge.VALIDE_MANAGER ||
                statut == StatutConge.EN_ATTENTE_RH ||
                statut == StatutConge.RENVOYE_MANAGER;
    }

    @Transient
    public boolean isUrgent() {
        return priorite == Priorite.URGENT;
    }


    @Transient
    public boolean isValide() {
        return statut == StatutConge.VALIDE_RH;
    }

    @Transient
    public boolean isRefuse() {
        return statut == StatutConge.REFUSE_MANAGER ||
                statut == StatutConge.REFUSE_RH;
    }

    @Transient
    public boolean isEnAttenteManager() {
        return statut == StatutConge.EN_ATTENTE_MANAGER ||
                statut == StatutConge.EN_ATTENTE_INFO;
    }

    @Transient
    public boolean isEnAttenteInfo() {
        return statut == StatutConge.EN_ATTENTE_INFO;
    }

    @Transient
    public boolean isAnnule() {
        return statut == StatutConge.ANNULE;
    }

    public void setStatutAvecTraitement(StatutConge nouveauStatut) {
        this.statut = nouveauStatut;

        // Mise à jour automatique de la date de traitement pour les statuts finaux
        if (nouveauStatut == StatutConge.VALIDE_RH ||
                nouveauStatut == StatutConge.REFUSE_MANAGER ||
                nouveauStatut == StatutConge.REFUSE_RH ||
                nouveauStatut == StatutConge.ANNULE) {
            this.dateTraitement = LocalDateTime.now();
        }

        // Mise à jour de la date de modification
        this.updatedAt = LocalDateTime.now();
    }

    @Transient
    public boolean isEnCours(LocalDate date) {
        if (date == null || dateDebut == null || dateFin == null) {
            return false;
        }
        return !date.isBefore(dateDebut) && !date.isAfter(dateFin) && isValide();
    }

    @Transient
    public boolean isEnCoursAujourdhui() {
        return isEnCours(LocalDate.now());
    }

    @Transient
    public boolean isFutur() {
        if (dateDebut == null) {
            return false;
        }
        return LocalDate.now().isBefore(dateDebut);
    }


    @Transient
    public boolean isPasse() {
        if (dateFin == null) {
            return false;
        }
        return LocalDate.now().isAfter(dateFin);
    }
}