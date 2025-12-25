package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.CongeService;
import com.example.gestion_rh.service.EmployeService;
import com.example.gestion_rh.service.SalaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/employe")
@RequiredArgsConstructor
public class EmployeController {

    private final EmployeService employeService;
    private final CongeService congeService;
    private final SalaireService salaireService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Employe employe = (Employe) session.getAttribute("employe");
        model.addAttribute("employe", employe);

        // Récupérer les congés en cours
        List<Conge> conges = congeService.getCongesByEmploye(employe.getId());
        model.addAttribute("conges", conges);

        // Calculer les jours restants
        double joursRestants = congeService.calculerJoursCongesRestants(
                employe.getId(),
                LocalDate.now().getYear());
        model.addAttribute("joursRestants", joursRestants);

        return "employe/dashboard";
    }

    @GetMapping("/conges")
    public String mesConges(HttpSession session, Model model) {
        Employe employe = (Employe) session.getAttribute("employe");
        List<Conge> conges = congeService.getCongesByEmploye(employe.getId());
        model.addAttribute("conges", conges);
        return "employe/conges";
    }

    @GetMapping("/conges/demander")
    public String showDemandeCongeForm(Model model) {
        model.addAttribute("conge", new Conge());
        model.addAttribute("typesConge", Conge.TypeConge.values());
        return "employe/demande-conge";
    }

    @PostMapping("/conges/demander")
    public String demanderConge(@ModelAttribute Conge conge,
            HttpSession session) {
        Employe employe = (Employe) session.getAttribute("employe");
        conge.setEmploye(employe);
        conge.setStatut(Conge.StatutConge.EN_ATTENTE_MANAGER);

        congeService.demanderConge(conge);
        return "redirect:/employe/conges";
    }

    @GetMapping("/salaires")
    public String mesSalaires(HttpSession session, Model model) {
        Employe employe = (Employe) session.getAttribute("employe");
        model.addAttribute("salaires", salaireService.getSalairesByEmploye(employe.getId()));
        return "employe/salaires";
    }

    @GetMapping("/profil")
    public String monProfil(HttpSession session, Model model) {
        Employe employe = (Employe) session.getAttribute("employe");
        model.addAttribute("employe", employe);
        return "employe/profil";
    }

    @PostMapping("/profil/update")
    public String updateProfil(@ModelAttribute Employe employeForm,
            HttpSession session) {
        Employe sessionEmploye = (Employe) session.getAttribute("employe");
        Employe updated = employeService.updateEmploye(sessionEmploye.getId(), employeForm);
        session.setAttribute("employe", updated);
        return "redirect:/employe/profil";
    }

    // ==================== CONTRATS ====================

    private final com.example.gestion_rh.service.ContratService contratService;
    private final com.example.gestion_rh.service.PdfContratService pdfContratService;

    @GetMapping("/contrats")
    public String mesContrats(HttpSession session, Model model) {
        Employe employe = (Employe) session.getAttribute("employe");

        // Récupérer tous les contrats de l'employé connecté
        java.util.List<com.example.gestion_rh.entity.Contrat> contrats = contratService.getAllContrats().stream()
                .filter(c -> c.getEmploye() != null && c.getEmploye().getId().equals(employe.getId()))
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("contrats", contrats);
        model.addAttribute("employe", employe);
        return "employe/contrats";
    }

    @GetMapping("/contrats/{id}")
    public String voirContrat(@PathVariable Long id, HttpSession session, Model model) {
        Employe employe = (Employe) session.getAttribute("employe");

        com.example.gestion_rh.entity.Contrat contrat = contratService.getContratById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

        // SÉCURITÉ: Vérifier que le contrat appartient bien à l'employé connecté
        if (!contrat.getEmploye().getId().equals(employe.getId())) {
            throw new RuntimeException("Accès non autorisé à ce contrat");
        }

        model.addAttribute("contrat", contrat);
        model.addAttribute("employe", employe);
        return "employe/contrat-detail";
    }

    @GetMapping("/contrats/{id}/pdf")
    public org.springframework.http.ResponseEntity<byte[]> telechargerContratPdf(
            @PathVariable Long id,
            HttpSession session) {
        Employe employe = (Employe) session.getAttribute("employe");

        com.example.gestion_rh.entity.Contrat contrat = contratService.getContratById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

        // SÉCURITÉ: Vérifier que le contrat appartient bien à l'employé connecté
        if (!contrat.getEmploye().getId().equals(employe.getId())) {
            throw new RuntimeException("Accès non autorisé à ce contrat");
        }

        byte[] pdfContent = pdfContratService.genererContratPDF(contrat, employe);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=contrat_" + contrat.getReferenceContrat().replace("/", "_") + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }
}