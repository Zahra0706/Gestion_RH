package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
    // Méthodes du premier fichier uniquement
    List<Contrat> findByEmployeIdOrderByDateDebutDesc(Long employeId);
    Optional<Contrat> findFirstByEmployeIdOrderByDateDebutDesc(Long employeId);
    Optional<Contrat> findBySignatureToken(String signatureToken);
    List<Contrat> findByTypeContrat(String typeContrat);
    List<Contrat> findByDateFinBefore(LocalDate date);
    Optional<Contrat> findByEmployeIdAndStatut(Long employeId, Contrat.StatutContrat statut);

    // Méthodes du deuxième fichier uniquement
    List<Contrat> findByEmployeId(Long employeId);
    Optional<Contrat> findByReferenceContrat(String referenceContrat);
    List<Contrat> findByStatut(Contrat.StatutContrat statut);
    List<Contrat> findByCreateurRHId(Long createurId);
    List<Contrat> findByDateFinBetween(LocalDate start, LocalDate end);
    List<Contrat> findByTypeContratAndStatut(Contrat.TypeContrat type, Contrat.StatutContrat statut);
    long countByStatut(Contrat.StatutContrat statut);
    long countByTypeContrat(Contrat.TypeContrat type);
    long countByEmployeDepartementId(Long departementId);

    @Query("SELECT c FROM Contrat c WHERE c.dateFin BETWEEN :today AND :dateLimite AND c.statut = 'SIGNE'")
    List<Contrat> findContratsExpirantBientot(
            @Param("today") LocalDate today,
            @Param("dateLimite") LocalDate dateLimite);

    // Méthode par défaut pour compatibilité
    default Optional<Contrat> findActiveContractByEmployeId(Long employeId) {
        return findByEmployeId(employeId).stream()
                .filter(c -> c.getStatut() == Contrat.StatutContrat.SIGNE
                        || c.getStatut() == Contrat.StatutContrat.ACTIF)
                .findFirst();
    }
}