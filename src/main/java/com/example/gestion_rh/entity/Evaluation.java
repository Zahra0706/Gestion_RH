package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Fusion : Integer au lieu de int (plus safe)
    @Column(nullable = false)
    private Integer annee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutEvaluation statut = StatutEvaluation.EN_ATTENTE;

    // ✅ Fusion : Double au lieu de double (nullable)
    private Double scorePerformance;
    private Double scoreCompetences;
    private Double scoreGlobal;

    // ✅ Fusion des types TEXT / length
    @Column(columnDefinition = "TEXT")
    private String commentairesManager;

    @Column(columnDefinition = "TEXT")
    private String commentairesRH;

    @Column(nullable = false)
    @Builder.Default
    private LocalDate dateCreation = LocalDate.now();

    private LocalDate dateCompletion;

    // ✅ Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employe manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rh_id")
    private Employe rh;

    // ✅ Champs ajoutés version des filles
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_rh_id")
    private Employe createurRH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campagne_id")
    private EvaluationCampagne campagne;

    private LocalDate dateEvaluation;

    private Double scoreObjectifs;

    @Column(columnDefinition = "TEXT")
    private String pointsForts;

    @Column(columnDefinition = "TEXT")
    private String axesAmelioration;

    @Column(columnDefinition = "TEXT")
    private String planAction;

    @Builder.Default
    private Boolean besoinFormation = false;

    // ✅ Enum identique
    public enum StatutEvaluation {
        EN_ATTENTE, EN_COURS, COMPLETEE, ARCHIVEE
    }
}
