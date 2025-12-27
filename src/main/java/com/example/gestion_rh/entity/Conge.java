package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

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

    public enum TypeConge {
        PAYE, MALADIE, MATERNITE, PATERNITE, SANS_SOLDE
    }

    public enum StatutConge {
        EN_ATTENTE_MANAGER, VALIDE_MANAGER, REFUSE_MANAGER,
        EN_ATTENTE_RH, VALIDE_RH, REFUSE_RH, ANNULE
    }
}