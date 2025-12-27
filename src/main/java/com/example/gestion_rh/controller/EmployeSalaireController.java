package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.Salaire;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.SalaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/employe/salaires")
@RequiredArgsConstructor
public class EmployeSalaireController {

    private final SalaireService salaireService;
    private final EmployeRepository employeRepository;

    private Employe currentEmploye(Authentication authentication) {
        String email = authentication.getName(); // username/email
        return employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé introuvable: " + email));
    }

    // Page liste des fiches de paie
    @GetMapping
    public String index(Authentication authentication, Model model) {
        Employe employe = currentEmploye(authentication);
        List<Salaire> salaires = salaireService.getSalairesByEmploye(employe.getId());

        model.addAttribute("employe", employe);
        model.addAttribute("salaires", salaires);

        return "employe/salaires/index";
    }

    // Télécharger PDF
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id, Authentication authentication) {
        Employe employe = currentEmploye(authentication);

        //  Sécurité + récupération de l'objet salaire (pas juste boolean)
        List<Salaire> salairesEmploye = salaireService.getSalairesByEmploye(employe.getId());
        Salaire salaire = salairesEmploye.stream()
                .filter(s -> s.getId() != null && s.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (salaire == null) {
            // 404 ou 403 : ici on choisit 403 pour éviter de révéler si l'ID existe
            return ResponseEntity.status(403).build();
        }

        byte[] pdf;
        try {
            pdf = salaireService.genererFicheDePaiePDF(id);
        } catch (RuntimeException ex) {
            // Erreur génération PDF (salaire introuvable, iText, etc.)
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("Erreur lors de la génération du PDF: " + ex.getMessage())
                            .getBytes(StandardCharsets.UTF_8));
        }

        if (pdf == null || pdf.length == 0) {
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("PDF vide ou non généré.".getBytes(StandardCharsets.UTF_8));
        }

        //  Nom de fichier propre
        String matricule = (employe.getMatricule() != null && !employe.getMatricule().isBlank())
                ? employe.getMatricule()
                : "employe";

        String moisAnnee = (salaire.getMoisAnnee() != null && !salaire.getMoisAnnee().isBlank())
                ? salaire.getMoisAnnee().replace("/", "-").replace(" ", "_")
                : "mois";

        String filename = "fiche_paie_" + matricule + "_" + moisAnnee + ".pdf";

        // Content-Disposition compatible navigateurs + UTF-8
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        String contentDisposition = "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
