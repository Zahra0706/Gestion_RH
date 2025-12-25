package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = true)
    private Employe employe;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employe manager;

    @ManyToOne
    @JoinColumn(name = "createur_rh_id")
    private Employe createurRH;

    @ManyToOne
    @JoinColumn(name = "campagne_id", nullable = false)
    private EvaluationCampagne campagne;

    @Column(nullable = false)
    private Integer annee;

    private LocalDate dateCreation;

    private LocalDate dateEvaluation;

    private LocalDate dateCompletion;

    @Enumerated(EnumType.STRING)
    private StatutEvaluation statut;

    private Double scorePerformance; // 1-5

    private Double scoreCompetences; // 1-5

    private Double scoreObjectifs; // 1-5

    private Double scoreGlobal;

    @Column(columnDefinition = "TEXT")
    private String commentairesManager;

    @Column(columnDefinition = "TEXT")
    private String commentairesRH;

    @Column(columnDefinition = "TEXT")
    private String pointsForts;

    @Column(columnDefinition = "TEXT")
    private String axesAmelioration;

    @Column(columnDefinition = "TEXT")
    private String planAction;

    private Boolean besoinFormation;
}