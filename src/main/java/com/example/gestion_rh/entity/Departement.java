package com.example.gestion_rh.entity;

import jakarta.persistence.*;
import lombok.*;
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
    ;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String nom;

    private String description;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employe manager;

    @OneToMany(mappedBy = "departement")
    private List<Employe> employes;

    @OneToMany(mappedBy = "departement", cascade = CascadeType.ALL)
    private List<Annonce> annonces;

    @Column(name = "is_actif", nullable = false)
    @Builder.Default
    private Boolean isActif = true;

    @Override
    public String toString() {
        return "Departement{id=" + id + ", nom=" + nom + "}";
    }
}
