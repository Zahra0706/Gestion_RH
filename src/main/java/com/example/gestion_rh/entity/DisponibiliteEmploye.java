package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "disponibilites_employes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "employe_id", "date" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibiliteEmploye {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDisponibilite statut;

    private String raison;

    @Builder.Default
    private Boolean estPlanifie = false;

    @ManyToOne
    @JoinColumn(name = "conge_lien_id")
    private Conge congeLien;

    @ManyToOne
    @JoinColumn(name = "modifie_par_id")
    private Employe modifiePar;

    private LocalDateTime dateModification;

    private String commentaire;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}
