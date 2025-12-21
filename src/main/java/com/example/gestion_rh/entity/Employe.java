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
    private Boolean isCompteActif = true;  // Par défaut le compte est actif

    @Column(name = "premiere_connexion", nullable = false)
    @Builder.Default
    private Boolean premiereConnexion = true;  // Pour forcer le changement de mot de passe

    @ManyToOne
    @JoinColumn(name = "departement_id")
    private Departement departement;

    @OneToOne(mappedBy = "employe", cascade = CascadeType.ALL)
    private Contrat contrat;

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
    @Override
    public String toString() {
        return "Employe{id=" + id + ", nom=" + nom + "}";
    }

}