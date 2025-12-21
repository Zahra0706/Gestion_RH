package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.EmployeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final EmployeService employeService;

    public GlobalControllerAdvice(EmployeService employeService) {
        this.employeService = employeService;
    }

    @ModelAttribute("user")
    public Employe addUserToModel(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return employeService.getEmployeByEmail(email).orElse(null);
        }
        return null;
    }
}