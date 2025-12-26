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
@Getter
@Setter

public class Contrat {

    public enum TypeContrat { CDI, CDD, STAGE, ALTERNANCE, FREELANCE }

    public enum StatutContrat { ACTIF, BROUILLON, TERMINE, ARCHIVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String typeContrat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutContrat statut = StatutContrat.BROUILLON;

    @Column(nullable = false)
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Column(nullable = false)
    private Double salaireBase;

    private Double heuresHebdomadaires;

    // Période d'essai
    private Integer periodeEssaiMois;
    private LocalDate dateFinPeriodeEssai;


    private String reference;
    private String poste;
    private String lieuTravail;
    private String observations;


    private String fichierPdfPath;

    @Column(unique = true)
    private String signatureToken;
    private LocalDate dateEnvoiSignature;
    private LocalDate dateLimiteSignature;
    private LocalDate dateSignature;

    private String signatureFichierPath;
    private boolean estSigne = false;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;


    @Transient
    public TypeContrat getTypeContratEnum() {
        if (typeContrat == null) return null;
        try { return TypeContrat.valueOf(typeContrat); }
        catch (Exception e) { return null; }
    }

    public void setTypeContratEnum(TypeContrat t) {
        this.typeContrat = (t == null) ? null : t.name();
    }
}
