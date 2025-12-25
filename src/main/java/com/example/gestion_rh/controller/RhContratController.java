package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.entity.ContratTemplate;
import com.example.gestion_rh.repository.ContratTemplateRepository;
import com.example.gestion_rh.service.ContratService;
import com.example.gestion_rh.service.EmployeService;
import com.example.gestion_rh.service.PdfContratService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rh/contrats")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'ADMIN', 'SUPER_ADMIN')")
public class RhContratController {

    private final ContratService contratService;
    private final EmployeService employeService;
    private final PdfContratService pdfContratService;
    private final ContratTemplateRepository templateRepository;

    @GetMapping
    public String getAllContrats(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String dept,
            Model model) {

        // Get all contracts first
        List<Contrat> contrats = contratService.getAllContrats();

        // Apply filters if provided
        if (type != null && !type.isEmpty()) {
            contrats = contrats.stream()
                    .filter(c -> c.getTypeContrat() != null && c.getTypeContrat().name().equals(type))
                    .collect(Collectors.toList());
        }

        if (statut != null && !statut.isEmpty()) {
            contrats = contrats.stream()
                    .filter(c -> c.getStatut() != null && c.getStatut().name().equals(statut))
                    .collect(Collectors.toList());
        }

        if (dept != null && !dept.isEmpty()) {
            contrats = contrats.stream()
                    .filter(c -> c.getEmploye() != null
                            && c.getEmploye().getDepartement() != null
                            && c.getEmploye().getDepartement().getNom().equalsIgnoreCase(dept))
                    .collect(Collectors.toList());
        }

        // Add attributes to model
        model.addAttribute("employes", employeService.getAllEmployes());
        model.addAttribute("contrats", contrats);
        model.addAttribute("stats", contratService.getContratStats());

        // Pass filter values back to view to maintain selected state
        model.addAttribute("selectedType", type != null ? type : "");
        model.addAttribute("selectedStatut", statut != null ? statut : "");
        model.addAttribute("selectedDept", dept != null ? dept : "");

        return "rh/contrats/liste";
    }

    @GetMapping("/nouveau")
    public String nouveauContratForm(Model model) {
        model.addAttribute("contrat", new Contrat());
        model.addAttribute("employes", employeService.getAllEmployes());
        model.addAttribute("typesContrat", Contrat.TypeContrat.values());
        return "rh/contrats/form";
    }

    @PostMapping
    public String saveContrat(@ModelAttribute Contrat contrat, RedirectAttributes redirectAttributes) {
        try {
            // Default logic before save
            if (contrat.getId() == null) {
                contrat.setStatut(Contrat.StatutContrat.BROUILLON);
            }
            contratService.saveContrat(contrat);
            redirectAttributes.addFlashAttribute("success", "Contrat enregistré avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement: " + e.getMessage());
            return "redirect:/rh/contrats/nouveau";
        }
        return "redirect:/rh/contrats";
    }

    @GetMapping("/{id}")
    public String detailsContrat(@PathVariable Long id, Model model) {
        model.addAttribute("contrat", contratService.getContratById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid contract Id:" + id)));
        return "rh/contrats/details";
    }

    @PostMapping("/{id}/archiver")
    public String archiverContrat(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contratService.archiverContrat(id);
            redirectAttributes.addFlashAttribute("success", "Contrat archivé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/contrats";
    }

    @PostMapping("/{id}/valider")
    public String validerContrat(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contratService.validerContrat(id);
            redirectAttributes.addFlashAttribute("success", "Contrat validé et référencé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/contrats/" + id;
    }

    @PostMapping("/{id}/envoyer")
    public String envoyerContrat(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contratService.envoyerContrat(id);
            redirectAttributes.addFlashAttribute("success", "Contrat envoyé à l'employé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/contrats/" + id;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadContratPdf(@PathVariable Long id) {
        Contrat contrat = contratService.getContratById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrat non trouvé: " + id));

        byte[] pdfContent = pdfContratService.genererContratPDF(contrat, contrat.getEmploye());

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=contrat_" + contrat.getReferenceContrat().replace("/", "_") + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @GetMapping("/generer/{employeId}")
    public String showGenererContratForm(@PathVariable Long employeId, Model model) {
        // Find employee
        var employe = employeService.getEmployeById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        model.addAttribute("employe", employe);
        return "rh/contrats/generer";
    }

    @PostMapping("/generer")
    public String genererContrat(@RequestParam Long employeId,
            @RequestParam String typeContrat,
            @RequestParam Double salaire,
            @RequestParam String dateDebut,
            RedirectAttributes redirectAttributes) {
        try {
            var employe = employeService.getEmployeById(employeId)
                    .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

            Map<String, Object> params = Map.of(
                    "typeContrat", typeContrat,
                    "salaire", salaire,
                    "dateDebut", java.time.LocalDate.parse(dateDebut));

            contratService.genererContrat(employe, params);
            redirectAttributes.addFlashAttribute("success", "Contrat généré avec succès.");
            return "redirect:/rh/contrats";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la génération: " + e.getMessage());
            return "redirect:/rh/contrats";
        }
    }

    // 1. Liste des contrats
    @GetMapping("/expirant")
    public String getContratsExpirant(Model model) {
        model.addAttribute("contrats", contratService.getContratsExpirantSoon());
        model.addAttribute("stats", contratService.getContratStats());
        model.addAttribute("title", "Contrats expirant bientôt");
        return "rh/contrats/liste"; // Reusing the list template
    }

    @GetMapping("/en-attente")
    public String getContratsEnAttente(Model model) {
        model.addAttribute("contrats", contratService.getContratsByStatut(Contrat.StatutContrat.EN_ATTENTE_SIGNATURE));
        model.addAttribute("stats", contratService.getContratStats());
        model.addAttribute("title", "Contrats en attente de signature");
        return "rh/contrats/liste";
    }

    // 2. Création contrat
    @GetMapping("/creer")
    public String creerContratForm(Model model) {
        model.addAttribute("contrat", new Contrat());
        model.addAttribute("employes", employeService.getAllEmployes());
        model.addAttribute("typesContrat", Contrat.TypeContrat.values());
        return "rh/contrats/form";
    }

    @PostMapping("/creer")
    public String creerContrat(@ModelAttribute Contrat contrat,
            @RequestParam(required = false) Long employeId,
            RedirectAttributes redirectAttributes) {
        try {
            // Assuming current RH ID is 1L for now or handled by security context (not
            // implemented here)
            Long currentRhId = 1L;

            // If employeId is in the form separately or part of 'contrat.employe.id'.
            // The form likely binds 'contrat.employe' if selects are used correctly.
            // If not, we use the param.
            Long empId = employeId;
            if (empId == null && contrat.getEmploye() != null) {
                empId = contrat.getEmploye().getId();
            }

            // Check if employee already has a contract
            if (empId != null) {
                Contrat existingContrat = contratService.getContratByEmployeId(empId);
                if (existingContrat != null) {
                    redirectAttributes.addFlashAttribute("info",
                            "Cet employé a déjà un contrat existant. Vous avez été redirigé vers sa modification.");
                    return "redirect:/rh/contrats/" + existingContrat.getId() + "/editer";
                }
            }

            contratService.creerContrat(contrat, empId, currentRhId);
            redirectAttributes.addFlashAttribute("success", "Contrat créé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création: " + e.getMessage());
            return "redirect:/rh/contrats/creer";
        }
        return "redirect:/rh/contrats";
    }

    // 3. Gestion contrat
    @PostMapping("/{id}/signer")
    public String signerContrat(@PathVariable Long id, @RequestParam(required = false) Long signataireId,
            RedirectAttributes redirectAttributes) {
        try {
            // If signataireId is not passed, use the contract's employee ID
            Long sId = signataireId;
            if (sId == null) {
                var c = contratService.getContratById(id).orElse(null);
                if (c != null && c.getEmploye() != null)
                    sId = c.getEmploye().getId();
            }

            contratService.signerContrat(id, sId);
            redirectAttributes.addFlashAttribute("success", "Contrat signé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la signature: " + e.getMessage());
        }
        return "redirect:/rh/contrats/" + id;
    }

    // 4. Statistiques
    @GetMapping("/statistiques")
    public String getStatistiques(Model model) {
        model.addAttribute("detailedStats", contratService.getDetailedStats());
        return "rh/contrats/statistiques"; // Requires a new template or reuse dashboard
    }

    @GetMapping("/{id}/editer")
    public String showEditor(@PathVariable Long id, Model model) {
        Contrat contrat = contratService.getContratById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid contract Id:" + id));

        // Initialize with default template if empty (memory only)
        if (contrat.getContentHtml() == null || contrat.getContentHtml().trim().isEmpty()) {
            String generatedHtml = contratService.generateContractHtml(id);
            contrat.setContentHtml(generatedHtml);
        }

        model.addAttribute("contrat", contrat);
        return "rh/contrats/editer";
    }

    @PostMapping("/{id}/editer")
    public String saveContent(@PathVariable Long id, @RequestParam String contentHtml,
            RedirectAttributes redirectAttributes) {
        try {
            contratService.updateContratContent(id, contentHtml);
            redirectAttributes.addFlashAttribute("success", "Contenu du contrat mis à jour");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/contrats/" + id + "/editer";
    }

    // ==================== MODIFICATION DES DONNÉES DU CONTRAT ====================

    @GetMapping("/{id}/modifier")
    public String showModifierForm(@PathVariable Long id, Model model) {
        Contrat contrat = contratService.getContratById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrat non trouvé: " + id));
        model.addAttribute("contrat", contrat);
        model.addAttribute("templates", templateRepository.findAll());
        return "rh/contrats/modifier";
    }

    @PostMapping("/{id}/modifier")
    public String modifierContrat(@PathVariable Long id,
            @ModelAttribute Contrat contratForm,
            RedirectAttributes redirectAttributes) {
        try {
            // Récupérer le contrat existant
            Contrat contratExistant = contratService.getContratById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Contrat non trouvé: " + id));

            // Mettre à jour les champs modifiables
            contratExistant.setTypeContrat(contratForm.getTypeContrat());
            contratExistant.setPoste(contratForm.getPoste());
            contratExistant.setDateDebut(contratForm.getDateDebut());
            contratExistant.setDateFin(contratForm.getDateFin());
            contratExistant.setDureeEssai(contratForm.getDureeEssai());
            contratExistant.setSalaireBase(contratForm.getSalaireBase());
            contratExistant.setHeuresHebdomadaires(contratForm.getHeuresHebdomadaires());
            contratExistant.setAvantages(contratForm.getAvantages());

            // Mettre à jour le template si fourni
            if (contratForm.getTemplate() != null && contratForm.getTemplate().getId() != null) {
                ContratTemplate template = templateRepository
                        .findById(contratForm.getTemplate().getId()).orElse(null);
                contratExistant.setTemplate(template);
                // Si on change de template, on réinitialise peut-être le contenu généré?
                // Pour l'instant on laisse l'utilisateur décider s'il veut régénérer via
                // l'éditeur HTML.
            }

            // Sauvegarder
            contratService.saveContrat(contratExistant);

            redirectAttributes.addFlashAttribute("success",
                    "Les données du contrat ont été mises à jour avec succès");
            return "redirect:/rh/contrats/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la modification: " + e.getMessage());
            return "redirect:/rh/contrats/" + id + "/modifier";
        }
    }
}
