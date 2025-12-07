package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, Long> {
    List<Reunion> findByCreateurId(Long createurId);
    List<Reunion> findByDepartementId(Long departementId);

    @Query("SELECT r FROM Reunion r WHERE :employeId IN (SELECT p.id FROM r.participants p)")
    List<Reunion> findByParticipantId(@Param("employeId") Long employeId);
}
