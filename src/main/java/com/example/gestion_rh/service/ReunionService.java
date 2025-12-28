package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Reunion;

import java.util.List;

public interface ReunionService {

    List<Reunion> getReunionsVisiblesPourEmploye(Long employeId, Long departementId);
}
