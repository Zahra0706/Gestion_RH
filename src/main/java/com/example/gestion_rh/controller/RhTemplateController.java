package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.entity.ContratTemplate;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.ContratRepository;
import com.example.gestion_rh.repository.ContratTemplateRepository;
import com.example.gestion_rh.service.ContractGenerationService;
import com.example.gestion_rh.service.EmployeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/rh/contrats/templates")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'ADMIN', 'SUPER_ADMIN')")
public class RhTemplateController {

    private final ContratTemplateRepository templateRepository;
    private final ContractGenerationService generationService;
    private final EmployeService employeService;
    private final ContratRepository contratRepository;

    @GetMapping("/liste")
    public String listeTemplates(Model model) {
        model.addAttribute("templates", templateRepository.findAll());
        return "rh/contrats/templates/liste";
    }

    @GetMapping("/nouveau")
    public String nouveauTemplate(Model model) {
        model.addAttribute("template", new ContratTemplate());
        return "rh/contrats/templates/form";
    }

    @GetMapping("/modifier/{id}")
    public String modifierTemplate(@PathVariable Long id, Model model) {
        ContratTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template invalide"));
        model.addAttribute("template", template);
        return "rh/contrats/templates/form";
    }

    @PostMapping("/sauvegarder")
    public String sauvegarderTemplate(@ModelAttribute ContratTemplate template) {
        if (template.isDefault()) {
            // Unset other defaults if this one is set to default
            templateRepository.findAll().forEach(t -> {
                if (t.isDefault() && !t.getId().equals(template.getId())) {
                    t.setDefault(false);
                    templateRepository.save(t);
                }
            });
        }
        templateRepository.save(template);
        return "redirect:/rh/contrats/templates/liste";
    }

    @PostMapping("/supprimer/{id}")
    public String supprimerTemplate(@PathVariable Long id) {
        templateRepository.deleteById(id);
        return "redirect:/rh/contrats/templates/liste";
    }

    @PostMapping("/set-default/{id}")
    public String setDefault(@PathVariable Long id) {
        templateRepository.findAll().forEach(t -> {
            t.setDefault(t.getId().equals(id));
            templateRepository.save(t);
        });
        return "redirect:/rh/contrats/templates/liste";
    }

    @GetMapping("/generer/{id}")
    public String editeurContrat(@PathVariable Long id,
            @RequestParam(required = false) Long employeId,
            Model model) {
        ContratTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template invalide"));

        model.addAttribute("template", template);
        model.addAttribute("employes", employeService.getAllEmployes());

        // Pre-fill if employee selected
        if (employeId != null) {
            Employe employe = employeService.getEmployeById(employeId).orElse(null);
            // Try to find active contract for this employee to pre-fill contract vars
            // This logic can be expanded. For now, we just pass the employees.
            model.addAttribute("selectedEmploye", employe);
        }

        return "rh/contrats/templates/editor";
    }

    @PostMapping("/generer/preview")
    @ResponseBody
    public ResponseEntity<String> previewContrat(@RequestBody Map<String, Object> payload) {
        Long templateId = Long.valueOf(payload.get("templateId").toString());
        Long employeId = payload.get("employeId") != null ? Long.valueOf(payload.get("employeId").toString()) : null;
        String customHtml = (String) payload.get("htmlContent");

        ContratTemplate template = templateRepository.findById(templateId).orElseThrow();

        // Mock objects for preview
        Employe employe = employeId != null ? employeService.getEmployeById(employeId).orElse(new Employe())
                : new Employe();
        Contrat contrat = new Contrat(); // Empty contract for defaults
        // If we had a real contract ID passed, we would fetch it.

        // Use custom HTML from editor if provided, otherwise template default
        // But here the user might have edited the variables placeholders directly?
        // Actually, usually we replace variables in the content.
        // If the user sends back HTML, it might already contain placeholders.

        // Temporary: Create a dummy template object with the content from the editor
        ContratTemplate tempTemplate = new ContratTemplate();
        tempTemplate.setContenuHtml(customHtml);

        String finalHtml = generationService.generateHtml(tempTemplate, contrat, employe);

        return ResponseEntity.ok(finalHtml);
    }

    @PostMapping("/generer/download")
    public ResponseEntity<byte[]> downloadPdf(@RequestParam Long templateId,
            @RequestParam Long employeId,
            @RequestParam String htmlContent) {

        // Logic similar to preview, but return PDF
        ContratTemplate tempTemplate = new ContratTemplate();
        tempTemplate.setContenuHtml(htmlContent);

        Employe employe = employeService.getEmployeById(employeId).orElse(new Employe());
        Contrat contrat = new Contrat(); // Or fetch

        String finalHtml = generationService.generateHtml(tempTemplate, contrat, employe);
        byte[] pdf = generationService.generatePdfFromHtml(finalHtml);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=contrat.pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
