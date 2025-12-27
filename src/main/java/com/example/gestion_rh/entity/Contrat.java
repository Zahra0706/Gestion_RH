package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Contrat "superset" compatible avec les branches Manager et RH.
 *
 * - Compatible Manager : typeContrat est un String (affichage simple dans Thymeleaf).
 * - Compatible RH : enums TypeContrat/StatutContrat + champs de signature (token, dates, fichiers).
 *
 * Remarque : La création + export PDF du contrat peut être réalisée côté RH/Manager.
 * Ici on gère surtout la lecture et la signature côté Employé.
 */
@Entity
@Table(name = "contrats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contrat {

    public enum TypeContrat { CDI, CDD, STAGE, ALTERNANCE, FREELANCE }

    public enum StatutContrat { ACTIF, BROUILLON, TERMINE, ARCHIVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Gardé en String pour compatibilité avec la branche Manager (templates utilisent ${c.typeContrat}).
     * On stocke en général la valeur de l'enum (ex: "CDI").
     */
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

    // Champs RH (optionnels)
    private String reference;      // ex: CT-2025-001
    private String poste;          // intitulé poste
    private String lieuTravail;    // ex: Casablanca / siège
    private String observations;   // notes RH

    /**
     * Chemin du PDF généré (par RH/Manager) pour téléchargement par l'employé.
     * Ex: uploads/contrats/CT-2025-001.pdf
     */
    private String fichierPdfPath;

    // --- Signature employé ---
    @Column(unique = true)
    private String signatureToken;                 // lien unique pour signer
    private LocalDate dateEnvoiSignature;          // date d'envoi du contrat à signer
    private LocalDate dateLimiteSignature;         // deadline
    private LocalDate dateSignature;               // date effective de signature

    private String signatureFichierPath;           // ex: uploads/signatures/contrat_12_signature.png
    private boolean estSigne = false;

    // Liens
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    // Helpers (facultatifs)
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
