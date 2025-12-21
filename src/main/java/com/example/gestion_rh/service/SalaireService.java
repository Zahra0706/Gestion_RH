package com.example.gestion_rh.service;


import com.example.gestion_rh.entity.Salaire;
import java.util.List;

public interface SalaireService {
    Salaire genererSalaire(Long employeId, String moisAnnee, double primes, double retenues);
    List<Salaire> getSalairesByEmploye(Long employeId);
    byte[] genererFicheDePaiePDF(Long salaireId);
    List<Salaire> getSalairesByDepartementAndMois(Long departementId, String moisAnnee);
}
