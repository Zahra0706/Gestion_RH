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

    private String referencePaiement;

    @Column(nullable = false)
    private String moisAnnee;

    @Column(nullable = false)
    private double salaireBrut;

    private double heuresTravaillees;

    private double heuresSupplementaires;

    private double tauxHeureSupplementaire;

    private double primeAnciennete;

    private double primePerformance;

    private double primes;

    private double cotisationsSociales;

    private double impots;

    private double retenues;

    @Column(nullable = false)
    private double salaireNet;

    private String modePaiement;

    private String statutPaiement;

    @Column(length = 1000)
    private String commentaires;

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