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
                LocalDate.now().getYear()
        );
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
}