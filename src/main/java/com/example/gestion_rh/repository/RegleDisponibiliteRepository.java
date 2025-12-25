package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.RegleDisponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegleDisponibiliteRepository extends JpaRepository<RegleDisponibilite, Long> {
    Optional<RegleDisponibilite> findByCode(String code);
}
