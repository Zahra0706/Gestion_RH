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
        if (departementRepository.findByCode(departement.getCode()).isPresent()) {
            throw new RuntimeException("Code de département déjà utilisé");
        }
        // Par défaut, le département est actif
        if (departement.getIsActif() == null) {
            departement.setIsActif(true);
        }
        return departementRepository.save(departement);
    }

    @Override
    public Optional<Departement> getDepartementById(Long id) {
        return departementRepository.findById(id);
    }

    @Override
    public List<Departement> getAllDepartements() {
        return departementRepository.findAll();
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<Departement> getDepartementWithDetails(Long id) {
        try {
            System.out.println("Chargement des détails pour le département ID: " + id);

            // Utilisez d'abord la méthode simple avec manager
            Optional<Departement> departementOpt = departementRepository.findByIdWithManager(id);

            if (departementOpt.isEmpty()) {
                System.out.println("Méthode findByIdWithManager n'a pas trouvé le département");
                departementOpt = departementRepository.findById(id);
            }

            // Forcez le chargement des relations si nécessaire
            departementOpt.ifPresent(departement -> {
                // Pour déboguer
                System.out.println("Département trouvé: " + departement.getNom());
                System.out.println("Manager: " + (departement.getManager() != null ? departement.getManager().getNom() : "null"));

                // Charger les employés
                try {
                    if (departement.getEmployes() != null) {
                        int size = departement.getEmployes().size();
                        System.out.println("Nombre d'employés chargés: " + size);
                    } else {
                        System.out.println("Liste d'employés est null");
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement des employés: " + e.getMessage());
                }

                // Charger les annonces
                try {
                    if (departement.getAnnonces() != null) {
                        int size = departement.getAnnonces().size();
                        System.out.println("Nombre d'annonces chargés: " + size);
                    } else {
                        System.out.println("Liste d'annonces est null");
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement des annonces: " + e.getMessage());
                }
            });

            return departementOpt;
        } catch (Exception e) {
            System.err.println("Erreur grave lors du chargement des détails du département: " + e.getMessage());
            e.printStackTrace();
            return departementRepository.findById(id);
        }
    }

    @Override
    public Departement updateDepartement(Long id, Departement departement) {
        Departement existingDepartement = departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));

        // Vérifier si le code change et s'il est déjà utilisé
        if (!existingDepartement.getCode().equals(departement.getCode())
                && departementRepository.findByCode(departement.getCode()).isPresent()) {
            throw new RuntimeException("Code de département déjà utilisé");
        }

        existingDepartement.setCode(departement.getCode());
        existingDepartement.setNom(departement.getNom());
        existingDepartement.setDescription(departement.getDescription());
        existingDepartement.setManager(departement.getManager());

        // Ne pas changer le statut si non spécifié
        if (departement.getIsActif() != null) {
            existingDepartement.setIsActif(departement.getIsActif());
        }

        return departementRepository.save(existingDepartement);
    }

    @Override
    public void deleteDepartement(Long id) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));

        // Vérifier si le département a des employés
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

    // Méthodes supplémentaires
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
}