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
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int annee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutEvaluation statut;

    private double scorePerformance;

    private double scoreCompetences;

    private double scoreGlobal;

    @Column(length = 1000)
    private String commentairesManager;

    @Column(length = 1000)
    private String commentairesRH;

    @Column(nullable = false)
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

    public enum StatutEvaluation {
        EN_ATTENTE, EN_COURS, COMPLETEE, ARCHIVEE
    }
}