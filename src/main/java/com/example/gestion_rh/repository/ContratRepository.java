package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {

        // 1. Méthodes basiques
        List<Contrat> findByEmployeId(Long employeId);

        Optional<Contrat> findByReferenceContrat(String referenceContrat);

        List<Contrat> findByStatut(Contrat.StatutContrat statut);

        // 2. Méthodes avancées
        List<Contrat> findByCreateurRHId(Long createurId);

        List<Contrat> findByDateFinBetween(java.time.LocalDate start, java.time.LocalDate end);

        List<Contrat> findByTypeContratAndStatut(Contrat.TypeContrat type, Contrat.StatutContrat statut);

        @org.springframework.data.jpa.repository.Query("SELECT c FROM Contrat c WHERE c.dateFin BETWEEN :today AND :dateLimite AND c.statut = 'SIGNE'")
        List<Contrat> findContratsExpirantBientot(
                        @org.springframework.data.repository.query.Param("today") java.time.LocalDate today,
                        @org.springframework.data.repository.query.Param("dateLimite") java.time.LocalDate dateLimite);

        // 3. Statistiques
        long countByStatut(Contrat.StatutContrat statut);

        long countByTypeContrat(Contrat.TypeContrat type);

        long countByEmployeDepartementId(Long departementId);

        // Helper helper for finding active contract (optional but useful)
        default Optional<Contrat> findActiveContractByEmployeId(Long employeId) {
                return findByEmployeId(employeId).stream()
                                .filter(c -> c.getStatut() == Contrat.StatutContrat.SIGNE
                                                || c.getStatut() == Contrat.StatutContrat.ACTIF)
                                .findFirst();
        }
}