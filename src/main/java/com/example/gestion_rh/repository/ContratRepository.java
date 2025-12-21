package com.example.gestion_rh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.gestion_rh.entity.Contrat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
    List<Contrat> findByEmployeId(Long employeId);

    Optional<Contrat> findByEmployeIdAndStatut(Long employeId, Contrat.StatutContrat statut);

    List<Contrat> findByTypeContrat(Contrat.TypeContrat typeContrat);

    List<Contrat> findByStatut(Contrat.StatutContrat statut);

    List<Contrat> findByDateFinBeforeAndStatut(LocalDate date, Contrat.StatutContrat statut);

    long countByTypeContrat(Contrat.TypeContrat type);
}