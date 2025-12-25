package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ContratTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Column(length = 500)
    private String description;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String contenuHtml;

    @Enumerated(EnumType.STRING)
    private Contrat.TypeContrat typeContrat;

    private boolean isDefault = false;
}
