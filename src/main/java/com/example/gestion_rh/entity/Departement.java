package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "departements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String nom;

    private String description;

    // ✅ Manager facultatif + FK safe (comme projet des filles)
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "manager_id",
            nullable = true,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Employe manager;

    // ✅ Relations
    @OneToMany(mappedBy = "departement")
    private List<Employe> employes;

    @OneToMany(mappedBy = "departement", cascade = CascadeType.ALL)
    private List<Annonce> annonces;

    // ✅ Actif par défaut
    @Column(name = "is_actif", nullable = false)
    @Builder.Default
    private Boolean isActif = true;

    // ✅ Date création (ajout projet filles)
    @Column(name = "date_creation")
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    // ✅ Pour debug / logs (chez toi)
    @Override
    public String toString() {
        return "Departement{id=" + id + ", nom=" + nom + "}";
    }
}
