package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "contrats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Contrat {

    // ✅ ENUMS gardés (projet des filles)
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

    // ✅ Compatibilité : tes anciens champs
    @Column(nullable = false)
    private String typeContrat; // String conservé (pour ancienne DB)

    @Column(nullable = false)
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Column(nullable = false)
    private Double salaireBase;

    private Double heuresHebdomadaires;

    // ✅ Champs ajoutés (projet des filles)
    private String reference;

    @Column(unique = true)
    private String referenceContrat;

    @Enumerated(EnumType.STRING)
    private TypeContrat typeContratEnum;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutContrat statut = StatutContrat.BROUILLON;

    // période d’essai
    private Integer periodeEssaiMois;
    private Integer dureeEssai;
    private LocalDate dateFinPeriodeEssai;

    // poste
    private String poste;
    private String lieuTravail;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(columnDefinition = "TEXT")
    private String avantages;

    @Column(columnDefinition = "TEXT")
    private String contentHtml;

    // fichiers pdf
    private String fichierPdfPath;
    private String cheminFichierPDF;

    // signature
    @Column(unique = true)
    private String signatureToken;

    private LocalDate dateEnvoiSignature;
    private LocalDate dateLimiteSignature;
    private LocalDate dateSignature;
    private String signatureFichierPath;

    @Column(name = "est_signe", nullable = false)
    @Builder.Default
    private boolean estSigne = false;

    // ✅ Relation : une seule relation employe pour éviter conflit JPA
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", unique = true)
    private Employe employe;

    // ✅ Autres relations (projet des filles)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_rh_id")
    private Employe createurRH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signataire_id")
    private Employe signataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ContratTemplate template;

    // ✅ Synchronisation automatique String typeContrat ↔ Enum
    public void setTypeContrat(String typeContrat) {
        this.typeContrat = typeContrat;

        // Essaie de synchroniser enum
        try {
            this.typeContratEnum = typeContrat != null ? TypeContrat.valueOf(typeContrat) : null;
        } catch (IllegalArgumentException e) {
            this.typeContratEnum = TypeContrat.CDI; // fallback safe
        }
    }

    public void setTypeContratEnum(TypeContrat typeContratEnum) {
        this.typeContratEnum = typeContratEnum;
        this.typeContrat = typeContratEnum != null ? typeContratEnum.name() : null;
    }

    // ✅ Méthodes utilitaires conservées
    @Transient
    public TypeContrat getTypeContratEnumMethod() {
        if (typeContrat == null) return null;
        try {
            return TypeContrat.valueOf(typeContrat);
        } catch (Exception e) {
            return null;
        }
    }

    @Transient
    public void setTypeContratEnumMethod(TypeContrat t) {
        this.typeContrat = (t == null) ? null : t.name();
        this.typeContratEnum = t;
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
        return ChronoUnit.MONTHS.between(dateDebut, dateFin);
    }
}
