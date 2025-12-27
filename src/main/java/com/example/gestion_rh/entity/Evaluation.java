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
@Setter
@Getter
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer annee;

    @Enumerated(EnumType.STRING)
    private StatutEvaluation statut;

    private Double scorePerformance;

    private Double scoreCompetences;

    private Double scoreGlobal;

    @Column(columnDefinition = "TEXT")
    private String commentairesManager;

    @Column(columnDefinition = "TEXT")
    private String commentairesRH;

    private LocalDate dateCreation;

    private LocalDate dateCompletion;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employe manager;

    @ManyToOne
    @JoinColumn(name = "rh_id")
    private Employe rh;

    @ManyToOne
    @JoinColumn(name = "createur_rh_id")
    private Employe createurRH;

    @ManyToOne
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

    private Boolean besoinFormation;

    public enum StatutEvaluation {
        EN_ATTENTE, EN_COURS, COMPLETEE, ARCHIVEE
    }
}