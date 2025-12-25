package com.example.gestion_rh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaireDTO {
    private Long id;
    private Long employeId;
    private String employeNomComplet;
    private String referencePaiement;
    private String moisAnnee;
    private Double salaireBase;
    private Double heuresTravaillees;
    private Double heuresSupplementaires;
    private Double tauxHeureSupplementaire;
    private Double primeAnciennete;
    private Double primePerformance;
    private Double primes;
    private Double cotisationsSociales;
    private Double impots;
    private Double retenues;
    private Double salaireNet;
    private String modePaiement;
    private String statutPaiement;
    private String commentaires;
    private LocalDate datePaiement;
}
