package com.example.gestion_rh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDTO {
    private Long id;
    private String title;
    private String start; // ISO format: yyyy-MM-dd
    private String end; // ISO format: yyyy-MM-dd
    private String color;
    private String type;
    private String departement;
    private String employeNom;
    private String statut;
}
