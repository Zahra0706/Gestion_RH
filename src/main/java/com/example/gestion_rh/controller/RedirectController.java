package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.impl.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RedirectController {

    private final CustomUserDetailsService customUserDetailsService;

    @GetMapping("/redirect-by-role")
    public String redirectByRole(Authentication authentication, jakarta.servlet.http.HttpSession session) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }

        String email = authentication.getName();
        Employe employe = customUserDetailsService.getEmployeByEmail(email);

        if (employe != null && Boolean.TRUE.equals(employe.getPremiereConnexion())) {
            session.setAttribute("premiereConnexion", true);
            return "redirect:/auth/premiere-connexion";
        }

        // Nettoyer la session si ce n'est plus une première connexion
        session.removeAttribute("premiereConnexion");

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElse("ROLE_EMPLOYE");

        return switch (role) {
            case "ROLE_SUPER_ADMIN", "ROLE_ADMIN" -> "redirect:/admin/dashboard";
            case "ROLE_RH" -> "redirect:/rh/dashboard";
            case "ROLE_MANAGER" -> "redirect:/manager/dashboard";
            case "ROLE_EMPLOYE" -> "redirect:/employe/dashboard";
            default -> "redirect:/dashboard";
        };
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/redirect-by-role";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/auth/login";
    }
}