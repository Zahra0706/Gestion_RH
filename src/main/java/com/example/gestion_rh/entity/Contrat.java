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

    public enum TypeContrat { CDI, CDD, STAGE, ALTERNANCE, FREELANCE }

    public enum StatutContrat {
        ACTIF,
        BROUILLON,
        TERMINE,
        ARCHIVE,
        EN_ATTENTE_SIGNATURE,
        SIGNE,
        EXPIRE,
        RESILIE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fusion des deux champs reference
    private String reference;
    @Column(unique = true, nullable = false)
    private String referenceContrat;

    // Fusion des deux approches pour typeContrat
    @Column(nullable = false)
    private String typeContrat;  // Version String du premier fichier

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeContrat typeContratEnum;  // Version Enum du deuxième fichier

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutContrat statut = StatutContrat.BROUILLON;

    @Column(nullable = false)
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Column(nullable = false)
    private Double salaireBase;

    private Double heuresHebdomadaires;

    // Fusion des deux approches pour période d'essai
    private Integer periodeEssaiMois;
    private Integer dureeEssai;
    private LocalDate dateFinPeriodeEssai;

    @Column(nullable = false)
    private String poste;
    private String lieuTravail;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(columnDefinition = "TEXT")
    private String avantages;

    @Column(columnDefinition = "TEXT")
    private String contentHtml;

    // Fusion des deux champs pour fichier PDF
    private String fichierPdfPath;
    private String cheminFichierPDF;

    @Column(unique = true)
    private String signatureToken;
    private LocalDate dateEnvoiSignature;
    private LocalDate dateLimiteSignature;
    private LocalDate dateSignature;
    private String signatureFichierPath;

    // Changez Boolean en boolean (type primitif)
    @Column(name = "est_signe", nullable = false)
    @Builder.Default
    private boolean estSigne = false;

    // Fusion des deux relations employe
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false, insertable = false, updatable = false)
    private Employe employeRelation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_rh_id")
    private Employe createurRH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signataire_id")
    private Employe signataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ContratTemplate template;

    // Méthodes utilitaires des deux fichiers
    @Transient
    public TypeContrat getTypeContratEnumMethod() {
        if (typeContrat == null) return null;
        try { return TypeContrat.valueOf(typeContrat); }
        catch (Exception e) { return null; }
    }

    @Transient
    public void setTypeContratEnumMethod(TypeContrat t) {
        this.typeContrat = (t == null) ? null : t.name();
    }

    @Transient
    public boolean isExpire() {
        return dateFin != null && LocalDate.now().isAfter(dateFin);
    }

    @Transient
    public boolean isSigneMethod() {
        return StatutContrat.SIGNE.equals(this.statut);
    }

    @Transient
    public long getDureeContrat() {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.MONTHS.between(dateDebut, dateFin);
    }
}