package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Departement;
import java.util.List;
import java.util.Optional;

public interface DepartementService {
    Departement saveDepartement(Departement departement);
    Optional<Departement> getDepartementById(Long id);
    List<Departement> getAllDepartements();
    Departement updateDepartement(Long id, Departement departement);
    void deleteDepartement(Long id);
    List<Departement> getDepartementsActifs();
    List<Departement> getDepartementsInactifs();
    void activerDepartement(Long id);
    void desactiverDepartement(Long id);

    List<Departement> getDepartementsSansManager();
}