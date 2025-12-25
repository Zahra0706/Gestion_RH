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

    @Column(unique = true, nullable = false)
    private String referenceContrat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeContrat typeContrat; // Correspond à "typeTravail" demandé, maintenu en Enum pour la sécurité

    @Column(nullable = false)
    private String poste;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutContrat statut;

    @Column(nullable = false)
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Column(nullable = false)
    private Double salaireBase;

    private Double heuresHebdomadaires;

    private Integer dureeEssai; // En jours

    private LocalDate dateSignature;
    private LocalDate dateFinPeriodeEssai;

    @Column(columnDefinition = "TEXT")
    private String avantages;

    @Column(columnDefinition = "TEXT")
    private String contentHtml;

    private String cheminFichierPDF;
    private String signatureToken;

    @Column(name = "est_signe", nullable = false)
    @Builder.Default
    private Boolean estSigne = false;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_rh_id")
    private Employe createurRH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signataire_id")
    private Employe signataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ContratTemplate template;

    // Méthodes utilitaires
    public boolean isExpire() {
        return dateFin != null && LocalDate.now().isAfter(dateFin);
    }

    public boolean isSigne() {
        return StatutContrat.SIGNE.equals(this.statut);
    }

    public long getDureeContrat() {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.MONTHS.between(dateDebut, dateFin);
    }

    public enum TypeContrat {
        CDI, CDD, STAGE, ALTERNANCE, FREELANCE
    }

    public enum StatutContrat {
        BROUILLON, EN_ATTENTE_SIGNATURE, SIGNE, EXPIRE, RESILIE, ACTIF, ARCHIVE
    }
}
