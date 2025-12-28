package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Conge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArchivageService {

    private final CongeService congeService;
    private final ExportService exportService;
    private final EmailService emailService;

    // Run at midnight on the 1st of every month
    @Scheduled(cron = "0 0 0 1 * ?")
    public void archiverRapportsMensuels() {
        log.info("Démarrage de l'archivage mensuel...");

        // Previous month
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate start = lastMonth.atDay(1);
        LocalDate end = lastMonth.atEndOfMonth();

        // Get leaves that overlap with last month or started in last month
        // Simpler approach: Filter all valid/finished leaves for ones intersecting last
        // month
        List<Conge> allConges = congeService.getAllConges();

        List<Conge> moisConges = allConges.stream()
                .filter(c -> c.getStatut() == Conge.StatutConge.VALIDE_RH)
                .filter(c -> !c.getDateDebut().isAfter(end) && !c.getDateFin().isBefore(start))
                .collect(Collectors.toList());

        if (moisConges.isEmpty()) {
            log.info("Aucun congé validé pour le mois précédent.");
            return;
        }

        try {
            // Generate Reports
            byte[] excelContent = exportService.generateCongesExcel(moisConges);
            byte[] pdfContent = exportService.generateCongesPdf(moisConges);

            String subject = "Rapport Mensuel des Congés - " + lastMonth;
            String body = "<h1>Rapport Mensuel</h1><p>Veuillez trouver ci-joint les rapports des congés pour le mois de "
                    + lastMonth + ".</p>";

            // Send to Direction (Replace with actual direction email or logic)
            String directionEmail = "direction@example.com";

            emailService.envoyerRapport(directionEmail, subject, body, excelContent,
                    "Rapport_Conges_" + lastMonth + ".xlsx");
            emailService.envoyerRapport(directionEmail, subject, body, pdfContent,
                    "Rapport_Conges_" + lastMonth + ".pdf");

            log.info("Rapports mensuels envoyés à la direction.");

        } catch (Exception e) {
            log.error("Erreur lors de l'archivage mensuel", e);
        }
    }
}
