package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Annonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long> {
    List<Annonce> findByDepartementId(Long departementId);
    List<Annonce> findByCreateurId(Long createurId);
    List<Annonce> findByType(Annonce.TypeAnnonce type);

    @Query("SELECT a FROM Annonce a WHERE a.departement IS NULL OR a.departement.id = :departementId ORDER BY a.datePublication DESC")
    List<Annonce> findAnnoncesForDepartement(@Param("departementId") Long departementId);
}
