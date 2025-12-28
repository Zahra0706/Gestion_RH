package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "employes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String matricule;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    private String telephone;

    @Column(nullable = false)
    private String adresse;

    private LocalDate dateNaissance;

    @Column(nullable = false)
    private LocalDate dateEmbauche;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Poste poste;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_compte_actif", nullable = false)
    @Builder.Default
    private Boolean isCompteActif = true;

    @Column(name = "premiere_connexion", nullable = false)
    @Builder.Default
    private Boolean premiereConnexion = true;

    // ✅ Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    private Departement departement;

    // ✅ Contrat principal (1-1)
    @OneToOne(mappedBy = "employe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Contrat contrat;

    // ✅ Contrats créés par RH (ajout filles)
    @OneToMany(mappedBy = "createurRH", fetch = FetchType.LAZY)
    private List<Contrat> contratsCrees;

    // ✅ Contrats signés (ajout filles)
    @OneToMany(mappedBy = "signataire", fetch = FetchType.LAZY)
    private List<Contrat> contratsSignes;

    // ✅ Congés
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Conge> conges;

    // ✅ Salaires
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Salaire> salaires;

    // ✅ Evaluations
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Evaluation> evaluations;

    // ✅ Réunions
    @OneToMany(mappedBy = "createur", fetch = FetchType.LAZY)
    private List<Reunion> reunionsCrees;

    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    private List<Reunion> reunions;

    // ✅ ENUMS (identiques dans les 2 projets)
    public enum Poste {
        DEVELOPPEUR, ANALYSTE, DESIGNER, CHEF_PROJET, RH, MANAGER, ADMIN
    }

    public enum Role {
        EMPLOYE, MANAGER, RH, ADMIN, SUPER_ADMIN
    }

    // ✅ Pour debug/logs (ta version)
    @Override
    public String toString() {
        return "Employe{id=" + id + ", nom=" + nom + "}";
    }
}
