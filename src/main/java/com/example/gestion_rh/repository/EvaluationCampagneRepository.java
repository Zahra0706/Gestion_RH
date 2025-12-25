package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.EvaluationCampagne;
import com.example.gestion_rh.entity.StatutCampagne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationCampagneRepository extends JpaRepository<EvaluationCampagne, Long> {
    List<EvaluationCampagne> findByAnnee(int annee);

    List<EvaluationCampagne> findByStatut(StatutCampagne statut);

    Optional<EvaluationCampagne> findByAnneeAndNomCampagne(int annee, String nomCampagne);
}
