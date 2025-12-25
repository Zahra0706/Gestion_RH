package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Salaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaireRepository extends JpaRepository<Salaire, Long> {
        List<Salaire> findByEmployeId(Long employeId);

        Optional<Salaire> findByEmployeIdAndMoisAnnee(Long employeId, String moisAnnee);

        List<Salaire> findByStatutPaiement(String statut);

        List<Salaire> findByMoisAnnee(String moisAnnee);

        List<Salaire> findByMoisAnneeAndStatutPaiement(String moisAnnee, String statutPaiement);

        @Query("SELECT s FROM Salaire s WHERE s.employe.departement.id = :departementId AND s.moisAnnee = :moisAnnee")
        List<Salaire> findByDepartementAndMois(@Param("departementId") Long departementId,
                        @Param("moisAnnee") String moisAnnee);

        @Query("SELECT AVG(s.salaireNet) FROM Salaire s WHERE s.employe.departement.id = :departementId AND s.moisAnnee = :moisAnnee")
        Double findSalaireMoyenParDepartement(@Param("departementId") Long departementId,
                        @Param("moisAnnee") String moisAnnee);

        @Query("SELECT s FROM Salaire s WHERE s.moisAnnee = :moisAnnee ORDER BY s.salaireNet DESC LIMIT 5")
        List<Salaire> findTop5Salaires(@Param("moisAnnee") String moisAnnee);
}
