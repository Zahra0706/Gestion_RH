package com.example.gestion_rh.dto;

import com.example.gestion_rh.entity.Conge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CongeValidationDTO {
    private Conge conge;
    private int soldeConges;
    private int joursUtilisesAnnee;
    private List<Conge> historiqueEmploye;
    private List<Conge> absencesEquipe;
    private boolean chevauchementDetecte;
    private String messageAlerte;

    // Nouveaux champs pour validation approfondie
    private double tauxAbsenteisme; // pourcentage
    private String impactEquipeDetails;
    private Map<String, Boolean> conformite; // Check-list (ex: "Respect délai", "Solde suffisant")
    private Double ancienneteMois;
    private boolean periodeInclutFeries;
}
