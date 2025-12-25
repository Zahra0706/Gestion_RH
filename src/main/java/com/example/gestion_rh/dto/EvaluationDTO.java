package com.example.gestion_rh.dto;

import com.example.gestion_rh.entity.StatutEvaluation;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EvaluationDTO {
    private Long id;
    private Long employeId;
    private Long managerId;
    private Long campagneId;

    private Double scorePerformance;
    private Double scoreCompetences;
    private Double scoreObjectifs;
    private Double scoreGlobal;

    private String commentairesManager;
    private String commentairesRH;
    private String pointsForts;
    private String axesAmelioration;
    private String planAction;

    private Boolean besoinFormation;

    private LocalDate dateEvaluation;
    private StatutEvaluation statut;
}
