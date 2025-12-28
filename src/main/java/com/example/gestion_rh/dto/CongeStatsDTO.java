package com.example.gestion_rh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CongeStatsDTO {
    private long enAttente;
    private long approuvesMois;
    private long approuvesAnnee;
    private long refusesMois;
    private double tauxApprobation;
    private double moyenneDuree;

    // Par type
    private long congesPaye;
    private long congesMaladie;
    private long congesMaternite;
    private long congesPaternite;
    private long congesSansSolde;
}
