package com.example.gestion_rh.repository.spec;

import com.example.gestion_rh.entity.Employe;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class EmployeSpecification {

    public static Specification<Employe> getSpec(String keyword, Long departementId, Employe.Role role,
            Boolean isActif) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Recherche par mot-clé (Nom, Prénom, Email, Matricule)
            if (StringUtils.hasText(keyword)) {
                String searchLike = "%" + keyword.toLowerCase() + "%";
                Predicate nomLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("nom")), searchLike);
                Predicate prenomLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("prenom")), searchLike);
                Predicate emailLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchLike);
                Predicate matriculeLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("matricule")),
                        searchLike);

                predicates.add(criteriaBuilder.or(nomLike, prenomLike, emailLike, matriculeLike));
            }

            // Filtre par département
            if (departementId != null) {
                predicates.add(criteriaBuilder.equal(root.get("departement").get("id"), departementId));
            }

            // Filtre par rôle
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            // Filtre par statut actif/inactif
            if (isActif != null) {
                predicates.add(criteriaBuilder.equal(root.get("isCompteActif"), isActif));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
