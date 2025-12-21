package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.entity.ContratTemplate;
import com.example.gestion_rh.entity.Employe;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ContractGenerationService {

    public String generateHtml(ContratTemplate template, Contrat contrat, Employe employe) {
        String html = template.getContenuHtml();
        Map<String, String> variables = buildVariables(contrat, employe);

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            // Replace ${variable} with value
            html = html.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return html;
    }

    public byte[] generatePdfFromHtml(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            HtmlConverter.convertToPdf(html, out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur de génération PDF depuis HTML", e);
        }
    }

    private Map<String, String> buildVariables(Contrat contrat, Employe employe) {
        Map<String, String> vars = new HashMap<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Employee
        vars.put("employe.nom_complet", employe.getNom() + " " + employe.getPrenom());
        vars.put("employe.nom", employe.getNom());
        vars.put("employe.prenom", employe.getPrenom());
        vars.put("employe.adresse", employe.getAdresse() != null ? employe.getAdresse() : "[Adresse]");
        vars.put("employe.date_naissance",
                employe.getDateNaissance() != null ? employe.getDateNaissance().format(dtf) : "[Date Naissance]");
        vars.put("employe.lieu_naissance", "[Lieu Naissance]"); // Field missing in Entity, using placeholder
        vars.put("employe.matricule", employe.getMatricule() != null ? employe.getMatricule() : "[Matricule]");
        vars.put("employe.poste", employe.getPoste() != null ? employe.getPoste().name() : "[Poste]");
        vars.put("employe.departement",
                employe.getDepartement() != null ? employe.getDepartement().getNom() : "[Département]");

        // Company
        vars.put("entreprise.nom", "TechInnovate SA");
        vars.put("entreprise.adresse_siege", "123 Avenue Mohamed VI, Casablanca");
        vars.put("entreprise.siret", "123 456 789 00012");
        vars.put("entreprise.ape", "6202A");
        vars.put("entreprise.representant_legal", "M. KARIM Directeur");

        // Contract
        if (contrat != null) {
            vars.put("contrat.type", contrat.getTypeContrat() != null ? contrat.getTypeContrat().name() : "[Type]");
            vars.put("contrat.date_debut",
                    contrat.getDateDebut() != null ? contrat.getDateDebut().format(dtf) : "[Date Début]");
            vars.put("contrat.date_fin",
                    contrat.getDateFin() != null ? contrat.getDateFin().format(dtf) : "Non applicable");
            vars.put("contrat.salaire_brut", String.format("%.2f", contrat.getSalaireBase()));
            vars.put("contrat.periode_essai",
                    contrat.getPeriodeEssaiMois() != null ? contrat.getPeriodeEssaiMois() + " mois" : "Non spécifiée");
            vars.put("contrat.duree_hebdomadaire",
                    contrat.getHeuresHebdomadaires() != null ? contrat.getHeuresHebdomadaires().toString() : "44");
            vars.put("contrat.lieu_travail", "Casablanca, Siège");
            vars.put("reference_contrat",
                    contrat.getReferenceContrat() != null ? contrat.getReferenceContrat() : "PROVISOIRE");
        } else {
            // Defaults for preview without contract context
            vars.put("contrat.type", "[Type]");
            vars.put("contrat.date_debut", "[Date Début]");
            vars.put("contrat.date_fin", "[Date Fin]");
            vars.put("contrat.salaire_brut", "[Salaire]");
            vars.put("contrat.periode_essai", "[Période Essai]");
            vars.put("contrat.duree_hebdomadaire", "[Heures]");
            vars.put("contrat.lieu_travail", "[Lieu Travail]");
            vars.put("reference_contrat", "REF-" + LocalDate.now().getYear() + "-XXXX");
        }

        // Misc
        vars.put("date_generation", LocalDate.now().format(dtf));
        vars.put("article_code_travail", "L. 1221-1");

        return vars;
    }
}
