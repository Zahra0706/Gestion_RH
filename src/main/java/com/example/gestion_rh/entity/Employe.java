package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "employes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Boolean isCompteActif = true; // Par défaut le compte est actif

    @Column(name = "premiere_connexion", nullable = false)
    @Builder.Default
    private Boolean premiereConnexion = true; // Pour forcer le changement de mot de passe

    @ManyToOne
    @JoinColumn(name = "departement_id")
    private Departement departement;

    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL)
    private List<Contrat> contrats;

    @OneToMany(mappedBy = "createurRH")
    private List<Contrat> contratsCrees;

    @OneToMany(mappedBy = "signataire")
    private List<Contrat> contratsSignes;

    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL)
    private List<Conge> conges;

    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL)
    private List<Salaire> salaires;

    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL)
    private List<Evaluation> evaluations;

    @OneToMany(mappedBy = "createur")
    private List<Reunion> reunionsCrees;

    @ManyToMany(mappedBy = "participants")
    private List<Reunion> reunions;

    public enum Poste {
        DEVELOPPEUR, ANALYSTE, DESIGNER, CHEF_PROJET, RH, MANAGER, ADMIN
    }

    public enum Role {
        EMPLOYE, MANAGER, RH, ADMIN, SUPER_ADMIN
    }

}