package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.EvaluationCriteres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationCriteresRepository extends JpaRepository<EvaluationCriteres, Long> {
    List<EvaluationCriteres> findByCategorie(String categorie);

    List<EvaluationCriteres> findByDepartementId(Long departementId);

    List<EvaluationCriteres> findByCategorieAndDepartementId(String categorie, Long departementId);
}
