package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, Long> {

    List<Reunion> findByCreateurId(Long createurId);

    List<Reunion> findByDepartementId(Long departementId);

    // ✅ Tri des réunions par date (manager / employé)
    List<Reunion> findByDepartementIdOrderByDateHeureDebutDesc(Long departementId);

    Optional<Reunion> findByIdAndDepartementId(Long id, Long departementId);

    // ✅ Réunions générales (sans département) - ajout version 2
    List<Reunion> findByDepartementIsNullOrderByDateHeureDebutDesc();

    // ✅ Réunions où un employé est participant
    @Query("SELECT r FROM Reunion r WHERE :employeId IN (SELECT p.id FROM r.participants p)")
    List<Reunion> findByParticipantId(@Param("employeId") Long employeId);
}
