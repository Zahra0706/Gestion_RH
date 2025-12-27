package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.ContratEmployeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/employe/contrats")
@RequiredArgsConstructor
public class EmployeContratController {

    private final ContratEmployeService contratEmployeService;
    private final EmployeRepository employeRepository;

    private Employe currentEmploye(Authentication authentication) {
        String email = authentication.getName();
        return employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));
    }

    // Liste des contrats de l'employé
    @GetMapping
    public String liste(Model model, Authentication authentication) {
        Employe employe = currentEmploye(authentication);
        List<Contrat> contrats = contratEmployeService.getContratsEmploye(employe.getId());
        model.addAttribute("employe", employe);
        model.addAttribute("contrats", contrats);
        return "employe/contrats/index";
    }

    // Télécharger le PDF du contrat (si fichierPdfPath est renseigné)
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> telechargerPdf(@PathVariable Long id, Authentication authentication) {
        Employe employe = currentEmploye(authentication);
        Contrat contrat = contratEmployeService.getContratPourEmploye(id, employe.getId());

        if (contrat.getFichierPdfPath() == null || contrat.getFichierPdfPath().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = Path.of(contrat.getFichierPdfPath());
            if (!Files.exists(path)) return ResponseEntity.notFound().build();

            byte[] bytes = Files.readAllBytes(path);

            String filename = "contrat_" + (contrat.getReference() != null ? contrat.getReference() : contrat.getId()) + ".pdf";
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encoded)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(bytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Page signature via token (lien envoyé par RH/Manager)
    @GetMapping("/signer")
    public String signerForm(@RequestParam("token") String token, Model model) {
        Contrat contrat = contratEmployeService.getContratByToken(token);
        model.addAttribute("contrat", contrat);
        model.addAttribute("token", token);
        return "employe/contrats/signer";
    }

    // Upload signature
    @PostMapping("/signer")
    public String signerSubmit(@RequestParam("token") String token,
                               @RequestParam("signatureFile") MultipartFile signatureFile,
                               RedirectAttributes ra) {
        try {
            Contrat contrat = contratEmployeService.signerContrat(token, signatureFile);
            ra.addFlashAttribute("successMessage", "Contrat signé avec succès. Merci !");
            // Après signature, RH pourra consulter (contrat.estSigne = true / statut ACTIF)
            return "redirect:/employe/contrats";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/employe/contrats/signer?token=" + token;
        }
    }
}
