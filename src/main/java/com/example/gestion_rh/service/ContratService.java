package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.entity.Employe;

import java.util.Map;

public interface ContratService {
    Contrat genererContrat(Employe employe, Map<String, Object> parametres);

    byte[] genererContratPDF(Long contratId);

    Contrat saveContrat(Contrat contrat);

    Contrat getContratByEmployeId(Long employeId);

    // New methods
    java.util.Optional<Contrat> getContratByReference(String reference);

    java.util.List<Contrat> getContratsExpirantBientot(int jours);

    java.util.List<Contrat> getContratsByStatut(Contrat.StatutContrat statut);

    Map<String, Long> getDetailedStats(); // For the charts

    java.util.List<Contrat> getAllActiveContrats();

    Map<String, Object> getContratStats();

    // --- Requested Refactoring Methods ---

    // 1. CRUD
    Contrat creerContrat(Contrat contrat, Long employeId, Long rhId);

    Contrat updateContrat(Long id, Contrat contrat);

    void deleteContrat(Long id);

    java.util.List<Contrat> getAllContrats();

    // 2. Business
    Contrat signerContrat(Long contratId, Long signataireId);

    void envoyerContratParEmail(Long contratId);

    java.util.List<Contrat> getContratsExpirantSoon();

    // 3. Validation
    boolean validerContrat(Contrat contrat);

    String genererReferenceContrat();

    java.util.Optional<Contrat> getContratById(Long id);

    void archiverContrat(Long id);

    void validerContrat(Long id);

    void envoyerContrat(Long id);

    void updateContratContent(Long id, String contentHtml);

    String generateContractHtml(Long id);
}
