package com.example.gestion_rh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.gestion_rh.entity.Contrat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
    Optional<Contrat> findByEmployeId(Long employeId);
    List<Contrat> findByTypeContrat(String typeContrat);
    List<Contrat> findByDateFinBefore(LocalDate date);
}