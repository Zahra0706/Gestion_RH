package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Salaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Nouveau champ version filles (optionnel)
    private String referencePaiement;

    // ✅ Champ commun
    @Column(nullable = false)
    private String moisAnnee; // Format: "MM-YYYY"

    // ✅ Champ commun
    @Column(nullable = false)
    private double salaireBrut;

    // ✅ Champs ajoutés (version filles)
    private double heuresTravaillees;
    private double heuresSupplementaires;
    private double tauxHeureSupplementaire;

    private double primeAnciennete;
    private double primePerformance;

    // ✅ Champ commun (primes globales)
    private double primes;

    // ✅ Champs ajoutés (charges)
    private double cotisationsSociales;
    private double impots;

    // ✅ Champ commun
    private double retenues;

    // ✅ Champ commun
    @Column(nullable = false)
    private double salaireNet;

    // ✅ Champs ajoutés
    private String modePaiement;
    private String statutPaiement;

    @Column(length = 1000)
    private String commentaires;

    // ✅ Fusion : on enlève nullable=false pour éviter erreurs sur anciennes données
    private LocalDate datePaiement;

    // ✅ Champ commun
    private String cheminFichierPDF;

    // ✅ Champs Drive ajoutés
    @Column(name = "drive_link")
    private String driveLink;

    @Column(name = "drive_upload_date")
    private LocalDateTime driveUploadDate;

    // ✅ Relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
}
