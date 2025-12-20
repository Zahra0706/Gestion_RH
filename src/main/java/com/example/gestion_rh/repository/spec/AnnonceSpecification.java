package com.example.gestion_rh.repository.spec;

import com.example.gestion_rh.entity.Annonce;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AnnonceSpecification {

    public static Specification<Annonce> getSpec(String keyword, Annonce.TypeAnnonce type, Long departementId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Recherche par mot-clé (Titre, Contenu)
            if (StringUtils.hasText(keyword)) {
                String searchLike = "%" + keyword.toLowerCase() + "%";
                Predicate titreLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("titre")), searchLike);
                Predicate contenuLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("contenu")), searchLike);

                predicates.add(criteriaBuilder.or(titreLike, contenuLike));
            }

            // Filtre par type
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            // Filtre par département
            if (departementId != null) {
                predicates.add(criteriaBuilder.equal(root.get("departement").get("id"), departementId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
