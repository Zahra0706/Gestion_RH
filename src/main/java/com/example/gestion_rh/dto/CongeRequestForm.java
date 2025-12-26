package com.example.gestion_rh.dto;

import com.example.gestion_rh.entity.Conge;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class CongeRequestForm {

    @NotNull(message = "La date de début est obligatoire")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFin;

    @NotNull(message = "Le type de congé est obligatoire")
    private Conge.TypeConge typeConge;

    @Size(max = 255, message = "Le motif ne doit pas dépasser 255 caractères")
    private String motif;

    // getters / setters
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public Conge.TypeConge getTypeConge() { return typeConge; }
    public void setTypeConge(Conge.TypeConge typeConge) { this.typeConge = typeConge; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
}
