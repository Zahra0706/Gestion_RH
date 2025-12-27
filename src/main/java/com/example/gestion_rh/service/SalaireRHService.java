package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.SalaireDTO;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.Salaire;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SalaireRHService {
    // 3.1. Calcul automatique
    Salaire calculerSalaireAutomatique(Long employeId, String moisAnnee);

    Double calculerHeuresSupplementaires(Employe employe, Double heuresSupplementaires);

    Double calculerCotisations(Double salaireBrut);

    Double calculerPrimeAnciennete(Employe employe);

    // 3.2. Gestion manuelle
    Salaire creerSalaireManuel(SalaireDTO salaireDTO);

    Salaire modifierSalaire(Long salaireId, SalaireDTO modifications);

    Salaire validerSalaire(Long salaireId);

    Salaire marquerCommePaye(Long salaireId);

    void deleteSalaire(Long id);

    // 3.3. Statistiques
    Map<String, Object> getStatistiques(String moisAnnee, String statut);

    Map<String, Double> getEvolutionMasseSalariale(int nbMois);

    void updateSalaire(Salaire salaire);

    Map<String, Double> getRepartitionSalairesParDepartement(String moisAnnee);

    List<SalaireDTO> getEvolutionSalairesEmploye(Long employeId, int nbMois);

    // Autres
    Salaire getSalaireById(Long id);

    List<Salaire> getSalairesByCriteria(String moisAnnee, String statut);
}
