package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Optional<Employe> findByEmail(String email);
    Optional<Employe> findByMatricule(String matricule);
    List<Employe> findByDepartementId(Long departementId);
    List<Employe> findByRole(Employe.Role role);

    @Query("SELECT COUNT(e) FROM Employe e WHERE e.departement.id = :departementId")
    long countByDepartement(@Param("departementId") Long departementId);

    @Query("SELECT e FROM Employe e WHERE e.dateEmbauche <= :date")
    List<Employe> findByAnciennete(@Param("date") LocalDate date);

    List<Employe> findByIsCompteActifTrue();

    List<Employe> findByIsCompteActifFalse();

    List<Employe> findByDepartementIdAndIsCompteActifTrue(Long id);
}


