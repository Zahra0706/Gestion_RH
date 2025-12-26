package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {

    // Employé : récupérer ses contrats
    List<Contrat> findByEmployeIdOrderByDateDebutDesc(Long employeId);

    // Pour garder compatibilité avec code existant (si utilisé)
    Optional<Contrat> findFirstByEmployeIdOrderByDateDebutDesc(Long employeId);

    // Signature
    Optional<Contrat> findBySignatureToken(String signatureToken);

    // Filtres simples
    List<Contrat> findByTypeContrat(String typeContrat);

    List<Contrat> findByDateFinBefore(LocalDate date);

    Optional<Contrat> findByEmployeIdAndStatut(Long employeId, Contrat.StatutContrat statut);
}
