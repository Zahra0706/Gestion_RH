package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "evaluation_campagnes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationCampagne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomCampagne;

    private int annee;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private LocalDate dateLimiteManager;

    @Enumerated(EnumType.STRING)
    private StatutCampagne statut;

    @ManyToOne
    @JoinColumn(name = "createur_rh_id")
    private Employe createurRH;

    @OneToMany(mappedBy = "campagne", cascade = CascadeType.ALL)
    private List<Evaluation> evaluations;
}
