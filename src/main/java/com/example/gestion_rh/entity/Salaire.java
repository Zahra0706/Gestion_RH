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

    @Column(nullable = false)
    private String moisAnnee; // Format: "MM-YYYY"

    @Column(nullable = false)
    private double salaireBrut;

    private double primes;

    private double retenues;

    @Column(nullable = false)
    private double salaireNet;

    @Column(nullable = false)
    private LocalDate datePaiement;

    private String cheminFichierPDF;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
}
