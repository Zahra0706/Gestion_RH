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
    public String redirectByRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/auth/login";
        }

        // Vérifier si c'est la première connexion
        String email = auth.getName();
        Employe employe = customUserDetailsService.getEmployeByEmail(email);

        if (employe != null && Boolean.TRUE.equals(employe.getPremiereConnexion())) {
            return "redirect:/auth/premiere-connexion";
        }

        // Redirection selon le rôle
        return auth.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> {
                    String role = grantedAuthority.getAuthority();
                    return switch (role) {
                        case "ROLE_SUPER_ADMIN", "ROLE_ADMIN" -> "redirect:/admin/dashboard";
                        case "ROLE_RH" -> "redirect:/rh/dashboard";
                        case "ROLE_MANAGER" -> "redirect:/manager/dashboard";
                        case "ROLE_EMPLOYE" -> "redirect:/employe/dashboard";
                        default -> "redirect:/dashboard";
                    };
                })
                .orElse("redirect:/dashboard");
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