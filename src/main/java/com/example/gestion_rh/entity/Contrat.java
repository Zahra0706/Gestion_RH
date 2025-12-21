package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "contrats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contrat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeContrat typeContrat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutContrat statut;

    @Column(nullable = false)
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Column(nullable = false)
    private Double salaireBase;

    private Double heuresHebdomadaires;

    // Période d'essai
    private Integer periodeEssaiMois;
    private LocalDate dateFinPeriodeEssai;

    // Autres infos
    @Column(columnDefinition = "TEXT")
    private String avantages;

    private String convention;

    private String fichierContrat;

    // Validation & Signature
    @Column(unique = true)
    private String referenceContrat;

    private String signatureToken;
    private LocalDate dateEnvoiSignature;
    private LocalDate dateLimiteSignature;
    private boolean estSigne;

    // Use ManyToOne to allow history
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    public enum TypeContrat {
        CDI, CDD, STAGE, ALTERNANCE, FREELANCE
    }

    public enum StatutContrat {
        ACTIF, BROUILLON, TERMINE, ARCHIVE
    }
}
