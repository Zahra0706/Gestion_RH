package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.CongeService;
import com.example.gestion_rh.service.SalaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/employe")
@RequiredArgsConstructor
public class EmployeController {

    private final EmployeRepository employeRepository;
    private final CongeService congeService;
    private final SalaireService salaireService;

    private Employe getCurrentEmploye(Authentication authentication) {
        String email = authentication.getName();
        return employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé : " + email));
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        Employe employe = getCurrentEmploye(authentication);

        // Historique congés
        List<Conge> conges = congeService.getCongesByEmploye(employe.getId());

        // 5 dernières demandes
        List<Conge> dernieresDemandes = conges.stream()
                .sorted(Comparator.comparing(Conge::getDateDebut, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5)
                .toList();

        // KPI congés
        long totalConges = conges.size();

        long congesEnAttente = conges.stream()
                .filter(c -> c.getStatut() == Conge.StatutConge.EN_ATTENTE_MANAGER
                        || c.getStatut() == Conge.StatutConge.EN_ATTENTE_RH)
                .count();

        long congesValides = conges.stream()
                .filter(c -> c.getStatut() == Conge.StatutConge.VALIDE_RH)
                .count();

        // Solde congés
        double joursRestants = congeService.calculerJoursCongesRestants(
                employe.getId(),
                LocalDate.now().getYear()
        );

        // Nombre de fiches de paie disponibles
        long salairesCount = salaireService.getSalairesByEmploye(employe.getId()).size();

        // ===== Model =====
        model.addAttribute("employe", employe);
        model.addAttribute("joursRestants", joursRestants);

        model.addAttribute("totalConges", totalConges);
        model.addAttribute("congesEnAttente", congesEnAttente);
        model.addAttribute("congesValides", congesValides);

        model.addAttribute("salairesCount", salairesCount);
        model.addAttribute("dernieresDemandes", dernieresDemandes);

        // (optionnel)
        model.addAttribute("totalEmployes", 0);
        model.addAttribute("employesActifs", 0);
        model.addAttribute("employesInactifs", 0);

        model.addAttribute("totalDepartements", 0);
        model.addAttribute("departementsAvecManager", 0);
        model.addAttribute("tauxOccupation", 0);

        model.addAttribute("statistiquesParRole", Map.of());
        model.addAttribute("derniersEmployes", List.of());
        model.addAttribute("derniersEmployes", List.of());
        model.addAttribute("departementsSansManager", List.of());

        return "employe/dashboard";
    }
}
