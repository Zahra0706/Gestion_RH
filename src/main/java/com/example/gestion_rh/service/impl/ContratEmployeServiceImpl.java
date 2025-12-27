package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.repository.ContratRepository;
import com.example.gestion_rh.service.ContratEmployeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratEmployeServiceImpl implements ContratEmployeService {

    private final ContratRepository contratRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public List<Contrat> getContratsEmploye(Long employeId) {
        return contratRepository.findByEmployeIdOrderByDateDebutDesc(employeId);
    }

    @Override
    public Contrat getContratPourEmploye(Long contratId, Long employeId) {
        Contrat c = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));
        if (c.getEmploye() == null || c.getEmploye().getId() == null || !c.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Accès interdit à ce contrat");
        }
        return c;
    }

    @Override
    public Contrat getContratByToken(String token) {
        return contratRepository.findBySignatureToken(token)
                .orElseThrow(() -> new RuntimeException("Lien de signature invalide"));
    }

    @Override
    @Transactional
    public Contrat signerContrat(String token, MultipartFile fichierSignature) {
        Contrat contrat = getContratByToken(token);

        if (contrat.isEstSigne()) {
            return contrat; // déjà signé
        }

        if (contrat.getDateLimiteSignature() != null && LocalDate.now().isAfter(contrat.getDateLimiteSignature())) {
            throw new RuntimeException("La date limite de signature est dépassée");
        }

        if (fichierSignature == null || fichierSignature.isEmpty()) {
            throw new RuntimeException("Veuillez ajouter un fichier de signature");
        }

        // Sauvegarde du fichier (png/jpg/pdf)
        String original = fichierSignature.getOriginalFilename() == null ? "signature" : fichierSignature.getOriginalFilename();
        String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");

        Path dir = Paths.get(uploadDir, "signatures");
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve("contrat_" + contrat.getId() + "_" + System.currentTimeMillis() + "_" + safeName);
            fichierSignature.transferTo(target.toFile());
            contrat.setSignatureFichierPath(target.toString());
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'enregistrer la signature : " + e.getMessage());
        }

        contrat.setEstSigne(true);
        contrat.setDateSignature(LocalDate.now());

        // Optionnel : mettre le contrat ACTIF une fois signé
        if (contrat.getStatut() == null || contrat.getStatut() == Contrat.StatutContrat.BROUILLON) {
            contrat.setStatut(Contrat.StatutContrat.ACTIF);
        }

        // Une fois signé, on invalide le token (sécurité)
        contrat.setSignatureToken(null);

        return contratRepository.save(contrat);
    }
}
