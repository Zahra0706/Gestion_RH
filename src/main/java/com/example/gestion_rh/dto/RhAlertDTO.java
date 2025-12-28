package com.example.gestion_rh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RhAlertDTO {

    public enum AlertType {
        CONFLICT, // Trop d'absences simultanées
        LOW_BALANCE, // Solde < 5 jours
        PENDING_48H, // Demande en attente > 48h
        QUOTA_EXCEEDED // Dépassement quota
    }

    public enum Severity {
        INFO, WARNING, DANGER
    }

    private AlertType type;
    private Severity severity;
    private String message;
    private Long employeId;
    private String employeNom;
    private Long congeId;
    private String actionUrl;
}
