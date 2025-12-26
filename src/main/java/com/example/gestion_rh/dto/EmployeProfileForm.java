package com.example.gestion_rh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeProfileForm {

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String email;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance;

    // Téléphone optionnel
    @Pattern(regexp = "^$|^[0-9+()\\s-]{6,20}$", message = "Téléphone invalide")
    private String telephone;

    private String adresse;
}
