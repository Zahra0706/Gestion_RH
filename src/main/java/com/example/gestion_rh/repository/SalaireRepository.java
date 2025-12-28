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

    // ✅ Méthodes communes
    List<Salaire> findByEmployeId(Long employeId);

    Optional<Salaire> findByEmployeIdAndMoisAnnee(Long employeId, String moisAnnee);

    @Query("SELECT s FROM Salaire s WHERE s.employe.departement.id = :departementId AND s.moisAnnee = :moisAnnee")
    List<Salaire> findByDepartementAndMois(@Param("departementId") Long departementId,
                                           @Param("moisAnnee") String moisAnnee);

    // ✅ Ajouts version filles
    List<Salaire> findByStatutPaiement(String statutPaiement);

    List<Salaire> findByMoisAnnee(String moisAnnee);

    List<Salaire> findByMoisAnneeAndStatutPaiement(String moisAnnee, String statutPaiement);

    // ✅ Salaire moyen par département
    @Query("SELECT AVG(s.salaireNet) FROM Salaire s WHERE s.employe.departement.id = :departementId AND s.moisAnnee = :moisAnnee")
    Double findSalaireMoyenParDepartement(@Param("departementId") Long departementId,
                                          @Param("moisAnnee") String moisAnnee);

    // ✅ Top salaires (tu peux limiter à 5 côté service)
    @Query("SELECT s FROM Salaire s WHERE s.moisAnnee = :moisAnnee ORDER BY s.salaireNet DESC")
    List<Salaire> findTop5Salaires(@Param("moisAnnee") String moisAnnee);
}
