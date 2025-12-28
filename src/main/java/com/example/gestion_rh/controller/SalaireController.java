package com.example.gestion_rh.controller;

import com.example.gestion_rh.dto.SalaireDTO;
import com.example.gestion_rh.entity.Salaire;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.SalaireRHService;
import com.example.gestion_rh.service.SalaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/rh/salaires")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'ADMIN', 'SUPER_ADMIN')")
public class SalaireController {

    private final SalaireRHService salaireRHService;
    private final SalaireService salaireService; // For PDF generation re-use if needed
    private final EmployeRepository employeRepository;

    @GetMapping
    public String listeSalaires(@RequestParam(required = false) String mois,
            @RequestParam(required = false) String statut,
            Model model) {
        List<Salaire> salaires;
        String moisAnnee = (mois != null) ? mois : "";
        String statutActuel = (statut != null) ? statut : "TOUS";

        salaires = salaireRHService.getSalairesByCriteria(moisAnnee, statutActuel);

        model.addAttribute("salaires", salaires);
        model.addAttribute("moisActuel", moisAnnee);
        model.addAttribute("statutActuel", statutActuel);

        // Stats logic - pass criteria if we want filtered stats?
        // User asked "changer les statistique de salire , je veux afficher les statique
        // en se basant sur le total des salaire"
        // This likely implies Global Stats or Filtered Stats.
        // Let's pass the criteria to stats if possible, OR just keep monthly stats.
        // For now, keep generic stats, but we might need to improve this later.
        try {
            model.addAttribute("stats", salaireRHService.getStatistiques(moisAnnee, statutActuel));
        } catch (Exception e) {
            // ignore
        }

        return "rh/salaires/liste";
    }

    @GetMapping("/calculer")
    public String formCalcul(Model model) {
        model.addAttribute("employes", employeRepository.findAll());
        model.addAttribute("moisActuel", YearMonth.now().toString());
        return "rh/salaires/calculer";
    }

    @GetMapping("/calculer/{employeId}")
    public String formCalculEmploye(@PathVariable Long employeId, @RequestParam(required = false) String mois,
            Model model) {
        String moisAnnee = (mois != null && !mois.isEmpty()) ? mois : YearMonth.now().toString();

        // Try to calculate automatically for preview
        try {
            Salaire salaire = salaireRHService.calculerSalaireAutomatique(employeId, moisAnnee);
            model.addAttribute("salaire", salaire);
            model.addAttribute("employe", salaire.getEmploye());
            model.addAttribute("mois", moisAnnee);
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du calcul: " + e.getMessage());
            model.addAttribute("employes", employeRepository.findAll());
            return "rh/salaires/calculer";
        }

        return "rh/salaires/calculer_details";
    }

    @PostMapping("/sauvegarder")
    public String sauvegarderSalaireManuel(
            @ModelAttribute SalaireDTO salaireDTO,
            RedirectAttributes redirectAttributes) {
        try {

            salaireRHService.creerSalaireManuel(salaireDTO);

            redirectAttributes.addFlashAttribute("success", "Salaire enregistré avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/salaires?mois=" + salaireDTO.getMoisAnnee();
    }

    @GetMapping("/{id}")
    public String detailsSalaire(@PathVariable Long id, Model model) {
        Salaire salaire = salaireRHService.getSalaireById(id);
        model.addAttribute("salaire", salaire);
        return "rh/salaires/details";
    }

    @PostMapping("/{id}/valider")
    public String validerSalaire(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Salaire s = salaireRHService.validerSalaire(id);
            redirectAttributes.addFlashAttribute("success", "Salaire validé pour " + s.getEmploye().getNom());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/salaires";
    }

    @PostMapping("/{id}/payer")
    public String payerSalaire(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Salaire s = salaireRHService.marquerCommePaye(id);
            redirectAttributes.addFlashAttribute("success",
                    "Salaire marqué comme PAYÉ pour " + s.getEmploye().getNom());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/salaires";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimerSalaire(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            salaireRHService.deleteSalaire(id);
            redirectAttributes.addFlashAttribute("success", "Salaire supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/rh/salaires";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<ByteArrayResource> telechargerPDF(@PathVariable Long id) {
        byte[] pdfContent = salaireService.genererFicheDePaiePDF(id); // Using the improved method in SalaireService

        Salaire salaire = salaireRHService.getSalaireById(id);
        String filename = "Bulletin_Paie_" + salaire.getMoisAnnee() + "_" + salaire.getEmploye().getMatricule()
                + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(pdfContent));
    }

    private final com.example.gestion_rh.service.GoogleDriveService googleDriveService;

    @org.springframework.beans.factory.annotation.Value("${google.drive.folder.id}")
    private String driveFolderId;

    @PostMapping("/{id}/upload-drive")
    public String uploadToDrive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // 1. Récupérer le salaire
            Salaire salaire = salaireRHService.getSalaireById(id);

            // 2. Générer le PDF
            byte[] pdfContent = salaireService.genererFicheDePaiePDF(id);

            // 3. Nom du fichier
            String fileName = String.format("Fiche-Paie-%s-%s.pdf",
                    salaire.getEmploye().getMatricule(),
                    salaire.getMoisAnnee());

            // 4. Upload vers Drive
            String driveLink = googleDriveService.uploadFile(
                    fileName, pdfContent, "application/pdf", driveFolderId);

            // 5. Sauvegarder le lien dans la DB
            salaire.setDriveLink(driveLink);
            salaire.setDriveUploadDate(java.time.LocalDateTime.now());
            salaireRHService.updateSalaire(salaire);

            redirectAttributes.addFlashAttribute("driveSuccess", true);
            redirectAttributes.addFlashAttribute("driveLink", driveLink);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("driveError", "Erreur: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("credentials.json")) {
                redirectAttributes.addFlashAttribute("driveError",
                        "Veuillez configurer credentials.json pour Google Drive.");
            }
        }
        return "redirect:/rh/salaires";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String moisAnnee = YearMonth.now().toString();
        model.addAttribute("stats", salaireRHService.getStatistiques(moisAnnee, "TOUS"));
        model.addAttribute("evolution", salaireRHService.getEvolutionMasseSalariale(6));
        return "rh/salaires/dashboard";
    }
}
