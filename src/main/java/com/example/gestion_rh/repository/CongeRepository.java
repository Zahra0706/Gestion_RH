package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Conge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CongeRepository extends JpaRepository<Conge, Long> {
    List<Conge> findByEmployeId(Long employeId);
    List<Conge> findByStatut(Conge.StatutConge statut);
    List<Conge> findByManagerValidateurId(Long managerId);
    List<Conge> findByRhValidateurId(Long rhId);

    @Query("SELECT c FROM Conge c WHERE c.employe.departement.id = :departementId AND c.statut = :statut")
    List<Conge> findByDepartementAndStatut(@Param("departementId") Long departementId,
                                           @Param("statut") Conge.StatutConge statut);

    @Query("SELECT COUNT(c) FROM Conge c WHERE c.employe.id = :employeId AND YEAR(c.dateDebut) = :annee")
    long countCongeByEmployeAndAnnee(@Param("employeId") Long employeId, @Param("annee") int annee);
}
