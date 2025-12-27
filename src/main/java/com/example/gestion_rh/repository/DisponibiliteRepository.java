package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.DisponibiliteEmploye;
import com.example.gestion_rh.entity.StatutDisponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibiliteRepository extends JpaRepository<DisponibiliteEmploye, Long> {
    List<DisponibiliteEmploye> findByDate(LocalDate date);

    List<DisponibiliteEmploye> findByEmployeIdAndDateBetween(Long employeId, LocalDate start, LocalDate end);

    Optional<DisponibiliteEmploye> findByEmployeIdAndDate(Long employeId, LocalDate date);

    List<DisponibiliteEmploye> findByDateAndStatut(LocalDate date, StatutDisponibilite statut);

    List<DisponibiliteEmploye> findByEmployeDepartementIdAndDate(Long departementId, LocalDate date);
}
