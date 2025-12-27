package com.example.gestion_rh.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(DataIntegrityViolationException ex,
            RedirectAttributes redirectAttributes) {
        String message = ex.getMessage();

        // Try to identify the specific constraint
        if (message.contains("UK") && message.contains("contrats")) {
            redirectAttributes.addFlashAttribute("error",
                    "Cet employé possède déjà un contrat actif (ou une contrainte d'unicité est violée). Veuillez vérifier la liste des contrats.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Erreur de base de données : Intégrité des données violée.");
        }

        // Redirect back to list or creation page usually, but since we don't know where
        // it came from exactly,
        // we might redirect to a safe place or try to infer.
        // For now, redirecting to the contracts list is safest.
        return "redirect:/rh/contrats";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Une erreur inattendue est survenue : " + ex.getMessage());
        // Redirect to a valid existing path to avoid loop
        return "redirect:/rh/dashboard";
    }
}
