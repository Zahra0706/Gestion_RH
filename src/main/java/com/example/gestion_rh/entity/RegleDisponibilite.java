package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "regles_disponibilite")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegleDisponibilite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String description;

    @Builder.Default
    private Boolean active = true;

    private String conditionLogique; // Logic description or SpEL

    private String action; // CHANGER_STATUT, NOTIFIER, GENERER_ALERTE
}
