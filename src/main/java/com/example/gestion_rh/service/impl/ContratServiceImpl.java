package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.repository.ContratRepository;
import com.example.gestion_rh.service.ContratService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContratServiceImpl implements ContratService {

    private final ContratRepository contratRepository;

    @Override
    public List<Contrat> getAllActiveContrats() {
        return contratRepository.findByStatut(Contrat.StatutContrat.ACTIF);
    }

    @Override
    public Optional<Contrat> getContratById(Long id) {
        return contratRepository.findById(id);
    }

    @Override
    @Transactional
    public Contrat saveContrat(Contrat contrat) {
        // Business Rule: If creating a NEW ACTIVE contract, archive the previous active
        // one for this employee
        if (contrat.getId() == null && contrat.getStatut() == Contrat.StatutContrat.ACTIF) {
            Optional<Contrat> currentActive = contratRepository.findByEmployeIdAndStatut(
                    contrat.getEmploye().getId(), Contrat.StatutContrat.ACTIF);

            if (currentActive.isPresent()) {
                Contrat old = currentActive.get();
                old.setStatut(Contrat.StatutContrat.ARCHIVE);
                old.setDateFin(LocalDate.now()); // Close it properly if not set? Or leave as is.
                contratRepository.save(old);
            }
        }
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat archiverContrat(Long id) {
        Contrat contrat = contratRepository.findById(id).orElseThrow(() -> new RuntimeException("Contrat non trouvé"));
        contrat.setStatut(Contrat.StatutContrat.ARCHIVE);
        return contratRepository.save(contrat);
    }

    private final com.example.gestion_rh.service.QrCodeService qrCodeService;
    private final com.example.gestion_rh.service.EmailService emailService;
    private final com.example.gestion_rh.service.ExportService exportService;

    @Override
    @Transactional
    public Contrat validerContrat(Long id) {
        Contrat contrat = contratRepository.findById(id).orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

        // Validation logic
        contrat.setReferenceContrat("REF-" + LocalDate.now().getYear() + "-" + String.format("%04d", contrat.getId()));

        // Ensure status logic if needed (e.g. move from DRAFT to ACTIVE/VALIDATED)
        // For now, assuming ACTIVE is the main state.

        contratRepository.save(contrat);
        return contrat;
    }

    @Override
    public void envoyerContrat(Long id) {
        Contrat contrat = contratRepository.findById(id).orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

        // Token generation
        String token = java.util.UUID.randomUUID().toString();
        contrat.setSignatureToken(token);
        contrat.setDateEnvoiSignature(LocalDate.now());
        contrat.setDateLimiteSignature(LocalDate.now().plusDays(7));

        contratRepository.save(contrat);

        // QR Code Generation
        String signLink = "http://localhost:8080/employe/contrats/signer?token=" + token;
        byte[] qrCode = qrCodeService.generateQrCode(signLink, 200, 200);

        // PDF Generation
        byte[] pdf = exportService.generateContratPdf(contrat, qrCode);

        // Send Email
        String subject = "Signature de votre contrat de travail";
        String body = "<h1>Bonjour " + contrat.getEmploye().getPrenom() + "</h1>"
                + "<p>Veuillez trouver ci-joint votre contrat de travail validé.</p>"
                + "<p>Merci de le signer électroniquement en scannant le QR Code ou en cliquant ci-dessous :</p>"
                + "<a href='" + signLink + "'>Signer mon contrat</a>"
                + "<p>Date limite : " + contrat.getDateLimiteSignature() + "</p>";

        emailService.envoyerRapport(
                contrat.getEmploye().getEmail(),
                subject,
                body,
                pdf,
                "Contrat_" + contrat.getReferenceContrat() + ".pdf");
    }

    @Override
    public Map<String, Long> getContratStats() {
        Map<String, Long> stats = new HashMap<>();
        for (Contrat.TypeContrat type : Contrat.TypeContrat.values()) {
            stats.put(type.name(), contratRepository.countByTypeContrat(type));
        }
        return stats;
    }

    @Override
    public List<Contrat> getContratsByEmploye(Long employeId) {
        return contratRepository.findByEmployeId(employeId);
    }
}
