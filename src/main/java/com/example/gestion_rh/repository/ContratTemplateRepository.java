package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.ContratTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratTemplateRepository extends JpaRepository<ContratTemplate, Long> {
}
