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

    // ✅ Méthodes communes
    List<Contrat> findByEmployeId(Long employeId);

    List<Contrat> findByTypeContrat(String typeContrat);

    List<Contrat> findByDateFinBefore(LocalDate date);

    // ✅ Tri par dateDebut (version riche)
    List<Contrat> findByEmployeIdOrderByDateDebutDesc(Long employeId);

    Optional<Contrat> findFirstByEmployeIdOrderByDateDebutDesc(Long employeId);

    // ✅ Signature / référence (version filles)
    Optional<Contrat> findBySignatureToken(String signatureToken);

    Optional<Contrat> findByReferenceContrat(String referenceContrat);

    // ✅ Statuts
    List<Contrat> findByStatut(Contrat.StatutContrat statut);

    Optional<Contrat> findByEmployeIdAndStatut(Long employeId, Contrat.StatutContrat statut);

    // ✅ Createur RH
    List<Contrat> findByCreateurRHId(Long createurId);

    // ✅ Date fin entre
    List<Contrat> findByDateFinBetween(LocalDate start, LocalDate end);

    // ✅ Enum TypeContrat + Statut
    List<Contrat> findByTypeContratAndStatut(Contrat.TypeContrat type, Contrat.StatutContrat statut);

    // ✅ Counts (stats)
    long countByStatut(Contrat.StatutContrat statut);

    long countByTypeContrat(Contrat.TypeContrat type);

    long countByEmployeDepartementId(Long departementId);

    // ✅ Contrats expirant bientôt
    @Query("SELECT c FROM Contrat c WHERE c.dateFin BETWEEN :today AND :dateLimite AND c.statut = 'SIGNE'")
    List<Contrat> findContratsExpirantBientot(
            @Param("today") LocalDate today,
            @Param("dateLimite") LocalDate dateLimite
    );

    // ✅ Requête : contrats par département (ton fichier)
    @Query("""
        SELECT c
        FROM Contrat c
        WHERE c.employe.departement.id = :deptId
        ORDER BY c.employe.nom ASC, c.employe.prenom ASC
    """)
    List<Contrat> findByDepartementId(@Param("deptId") Long deptId);

    // ✅ Méthode par défaut pour compatibilité (version filles)
    default Optional<Contrat> findActiveContractByEmployeId(Long employeId) {
        return findByEmployeIdOrderByDateDebutDesc(employeId).stream()
                .filter(c -> c.getStatut() == Contrat.StatutContrat.SIGNE
                        || c.getStatut() == Contrat.StatutContrat.ACTIF)
                .findFirst();
    }
}
