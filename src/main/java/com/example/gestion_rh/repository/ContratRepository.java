package com.example.gestion_rh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.gestion_rh.entity.Contrat;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
    Optional<Contrat> findByEmployeId(Long employeId);

    List<Contrat> findByTypeContrat(String typeContrat);

    List<Contrat> findByDateFinBefore(LocalDate date);


    @Query("""
   SELECT c
   FROM Contrat c
   WHERE c.employe.departement.id = :deptId
   ORDER BY c.employe.nom ASC, c.employe.prenom ASC
""")
    List<Contrat> findByDepartementId(@Param("deptId") Long deptId);
}