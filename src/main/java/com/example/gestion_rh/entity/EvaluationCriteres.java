package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evaluation_criteres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationCriteres {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categorie; // PERFORMANCE, COMPETENCES, OBJECTIFS

    private String code; // ex: "PERF01"

    private String description;

    private int poids; // 1-10

    @Column(columnDefinition = "TEXT")
    private String exempleConcret;

    @ManyToOne
    @JoinColumn(name = "departement_id")
    private Departement departement; // Optionnel
}
