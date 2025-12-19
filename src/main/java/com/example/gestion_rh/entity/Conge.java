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
    @Column(name = "type_conge", nullable = false)
    private TypeConge type;  // ✅ MODIFIÉ : typeConge → type

    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutConge statut;

    @Column(length = 500)
    private String commentaireManager;

    @Column(name = "commentairerh", length = 500)
    private String commentaireRH;


    @Column(name = "date_demande")
    private LocalDateTime dateDemande;

    @Column(name = "date_validation_manager")
    private LocalDateTime dateValidationManager;

    @Column(name = "date_refus")
    private LocalDateTime dateRefus;

    @Column(name = "motif_refus", length = 500)
    private String motifRefus;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne
    @JoinColumn(name = "manager_validateur_id")
    private Employe managerValidateur;

    @ManyToOne
    @JoinColumn(name = "rh_validateur_id")
    private Employe rhValidateur;

    public enum TypeConge {
        CONGE_PAYE,      // ✅ AJOUTÉ en premier
        PAYE,            // ✅ Gardé pour compatibilité avec données existantes
        CONGE_MALADIE,
        CONGE_SANS_SOLDE,
        CONGE_MATERNITE,
        CONGE_PATERNITE
    }

    public enum StatutConge {
        EN_ATTENTE_MANAGER,
        VALIDE_MANAGER,
        REFUSE_MANAGER,
        VALIDE_RH,
        REFUSE_RH,
        ANNULE
    }

    public long getNombreJours() {
        return ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
    }
}