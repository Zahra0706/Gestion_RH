package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "salaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Salaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String referencePaiement; // ex: "PAY-2024-12-001"

    @Column(nullable = false)
    private String moisAnnee; // Format: "MM-YYYY"

    @Column(nullable = false)
    private double salaireBrut;

    private double heuresTravaillees;

    private double heuresSupplementaires;

    private double tauxHeureSupplementaire;

    private double primeAnciennete;

    private double primePerformance;

    private double primes; // Autre primes

    private double cotisationsSociales;

    private double impots;

    private double retenues; // Autres retenues

    @Column(nullable = false)
    private double salaireNet;

    private String modePaiement; // VIREMENT, CHEQUE, ESPECES

    private String statutPaiement; // CALCULE, VALIDE, PAYE, PROBLEME

    @Column(length = 1000)
    private String commentaires;

    @Column(nullable = true)
    private LocalDate datePaiement;

    private String cheminFichierPDF;

    @Column(name = "drive_link")
    private String driveLink;

    @Column(name = "drive_upload_date")
    private java.time.LocalDateTime driveUploadDate;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
}
