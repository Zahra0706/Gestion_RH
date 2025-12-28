package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.AnnonceRepository;
import com.example.gestion_rh.repository.DepartementRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.EmployeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final AnnonceRepository annonceRepository;
    private final EmployeService employeService;

    @ModelAttribute
    public void addGlobalAttributes(org.springframework.ui.Model model,
            jakarta.servlet.http.HttpServletRequest request) {
        // Ajouter les compteurs pour le menu
        model.addAttribute("menuCountEmployes", employeRepository.count());
        model.addAttribute("menuCountDepartements", departementRepository.count());
        model.addAttribute("menuCountAnnonces", annonceRepository.count());
        model.addAttribute("requestURI", request.getRequestURI());
    }

    @ModelAttribute("user")
    public Employe getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            return employeService.getEmployeByEmail(email).orElse(null);
        }
        return null;
    }
}