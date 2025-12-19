package com.example.gestion_rh.controller;

import com.example.gestion_rh.repository.AnnonceRepository;
import com.example.gestion_rh.repository.DepartementRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final AnnonceRepository annonceRepository;

    @ModelAttribute
    public void addGlobalAttributes(org.springframework.ui.Model model,
            jakarta.servlet.http.HttpServletRequest request) {
        // Ajouter les compteurs pour le menu
        model.addAttribute("menuCountEmployes", employeRepository.count());
        model.addAttribute("menuCountDepartements", departementRepository.count());
        model.addAttribute("menuCountAnnonces", annonceRepository.count());
        model.addAttribute("requestURI", request.getRequestURI());
    }
}