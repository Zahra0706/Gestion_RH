package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reunions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reunion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    private String description;

    @Column(nullable = false)
    private LocalDateTime dateHeureDebut;

    @Column(nullable = false)
    private LocalDateTime dateHeureFin;

    private String lieu;

    @ManyToOne
    @JoinColumn(name = "createur_id", nullable = false)
    private Employe createur;

    @ManyToOne
    @JoinColumn(name = "departement_id")
    private Departement departement;

    @ManyToMany
    @JoinTable(
            name = "reunion_participants",
            joinColumns = @JoinColumn(name = "reunion_id"),
            inverseJoinColumns = @JoinColumn(name = "employe_id")
    )
    private List<Employe> participants;
}
