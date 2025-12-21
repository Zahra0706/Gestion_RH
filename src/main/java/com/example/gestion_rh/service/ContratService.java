package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Contrat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ContratService {
    List<Contrat> getAllActiveContrats();

    Optional<Contrat> getContratById(Long id);

    Contrat saveContrat(Contrat contrat);

    Contrat archiverContrat(Long id);

    // Validation & Envoi
    Contrat validerContrat(Long id);

    void envoyerContrat(Long id);

    Map<String, Long> getContratStats();

    List<Contrat> getContratsByEmploye(Long employeId);
}
