package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "annonces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Annonce {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(length = 2000, nullable = false)
    private String contenu;

    @Column(nullable = false)
    private LocalDateTime datePublication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAnnonce type;

    @ManyToOne
    @JoinColumn(name = "createur_id", nullable = false)
    private Employe createur;

    @ManyToOne
    @JoinColumn(name = "departement_id")
    private Departement departement;

    public enum TypeAnnonce {
        GENERALE, DEPARTEMENT, IMPORTANTE, INFORMATION
    }
}
