package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.repository.DepartementRepository;
import com.example.gestion_rh.service.DepartementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartementServiceImpl implements DepartementService {

    private final DepartementRepository departementRepository;

    @Override
    public Departement saveDepartement(Departement departement) {
        if (departement.getCode() == null || departement.getCode().isBlank()) {
            throw new RuntimeException("Code de département obligatoire");
        }

        if (departementRepository.findByCode(departement.getCode()).isPresent()) {
            throw new RuntimeException("Code de département déjà utilisé");
        }

        if (departement.getIsActif() == null) {
            departement.setIsActif(true);
        }

        return departementRepository.save(departement);
    }

    @Override
    public Optional<Departement> getDepartementById(Long id) {
        return departementRepository.findById(id);
    }

    // ✅ Optionnel : si tu veux tout charger (manager + employés + annonces)
    public Optional<Departement> getDepartementByIdWithDetails(Long id) {
        try {
            return departementRepository.findByIdWithDetails(id);
        } catch (Exception e) {
            return departementRepository.findById(id);
        }
    }

    @Override
    public List<Departement> getAllDepartements() {
        return departementRepository.findAll();
    }

    @Override
    public Departement updateDepartement(Long id, Departement departement) {

        Departement existing = departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));

        if (departement.getCode() != null && !departement.getCode().equals(existing.getCode())) {
            if (departementRepository.findByCode(departement.getCode()).isPresent()) {
                throw new RuntimeException("Code de département déjà utilisé");
            }
            existing.setCode(departement.getCode());
        }

        if (departement.getNom() != null) existing.setNom(departement.getNom());
        if (departement.getDescription() != null) existing.setDescription(departement.getDescription());

        // manager peut être null (ex : suppression du manager)
        existing.setManager(departement.getManager());

        if (departement.getIsActif() != null) {
            existing.setIsActif(departement.getIsActif());
        }

        return departementRepository.save(existing);
    }

    @Override
    public void deleteDepartement(Long id) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));

        if (departement.getEmployes() != null && !departement.getEmployes().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer un département contenant des employés");
        }

        departementRepository.deleteById(id);
    }

    @Override
    public List<Departement> getDepartementsActifs() {
        return departementRepository.findByIsActifTrue();
    }

    @Override
    public List<Departement> getDepartementsInactifs() {
        return departementRepository.findByIsActifFalse();
    }

    @Override
    public void activerDepartement(Long id) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));
        departement.setIsActif(true);
        departementRepository.save(departement);
    }

    @Override
    public void desactiverDepartement(Long id) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));
        departement.setIsActif(false);
        departementRepository.save(departement);
    }

    // ✅ Méthodes supplémentaires
    public long countDepartementsActifs() {
        return departementRepository.countDepartementsActifs();
    }

    public long countDepartementsInactifs() {
        return departementRepository.countDepartementsInactifs();
    }

    public List<Departement> getDepartementsSansManager() {
        return departementRepository.findDepartementsSansManager();
    }

    public List<Departement> searchByNom(String nom) {
        return departementRepository.searchByNom(nom);
    }

    // ✅ ✅ ✅ AJOUT UNIQUEMENT ICI (rien d’autre changé)
    @Override
    public Optional<Departement> getDepartementWithDetails(Long id) {
        return getDepartementByIdWithDetails(id);
    }
}
