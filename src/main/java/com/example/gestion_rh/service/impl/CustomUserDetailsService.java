package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeRepository employeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employe employe = employeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email));

        // Vérifier si le compte est actif
        if (employe.getIsCompteActif() == null || !employe.getIsCompteActif()) {
            throw new UsernameNotFoundException("Compte désactivé. Contactez l'administrateur.");
        }

        // Vérifier si le mot de passe est défini
        if (employe.getPassword() == null || employe.getPassword().isEmpty()) {
            throw new UsernameNotFoundException("Mot de passe non défini pour l'utilisateur: " + email);
        }

        // Convertir le rôle en autorités Spring Security
        String role = "ROLE_" + employe.getRole().name();
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role)
        );

        // Créer l'utilisateur Spring Security
        return User.builder()
                .username(employe.getEmail())
                .password(employe.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!employe.getIsCompteActif())
                .build();
    }

    // Méthode supplémentaire pour obtenir l'employé depuis UserDetails
    public Employe getEmployeByEmail(String email) {
        return employeRepository.findByEmail(email).orElse(null);
    }
}
