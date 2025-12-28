package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Salaire;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.repository.SalaireRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.repository.ContratRepository;
import com.example.gestion_rh.service.SalaireService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalaireServiceImpl implements SalaireService {

    private final SalaireRepository salaireRepository;
    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;

    @Override
    public Salaire genererSalaire(Long employeId, String moisAnnee, double primes, double retenues) {

        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        // Vérifier si le salaire pour ce mois existe déjà
        if (salaireRepository.findByEmployeIdAndMoisAnnee(employeId, moisAnnee).isPresent()) {
            throw new RuntimeException("Salaire déjà généré pour ce mois");
        }

        // ✅ Récupérer tous les contrats de l'employé
        List<Contrat> contrats = contratRepository.findByEmployeId(employeId);

        if (contrats.isEmpty()) {
            throw new RuntimeException("Aucun contrat trouvé pour cet employé");
        }

        // ✅ Récupérer le contrat actif (SIGNE ou ACTIF) sinon dernier contrat
        Contrat contrat = contrats.stream()
                .filter(c -> c.getStatut() == Contrat.StatutContrat.SIGNE ||
                        c.getStatut() == Contrat.StatutContrat.ACTIF)
                .findFirst()
                .orElse(contrats.stream()
                        .max(Comparator.comparing(Contrat::getDateDebut))
                        .orElse(contrats.get(0)));

        if (contrat.getSalaireBase() == null) {
            throw new RuntimeException("Le contrat trouvé ne contient pas de salaire de base");
        }

        // ✅ Conversion propre BigDecimal -> double
        double salaireBrut = contrat.getSalaireBase();
        double salaireNet = salaireBrut + primes - retenues;

        Salaire salaire = Salaire.builder()
                .moisAnnee(moisAnnee)
                .salaireBrut(salaireBrut)
                .primes(primes)
                .retenues(retenues)
                .salaireNet(salaireNet)
                .datePaiement(LocalDate.now())
                .employe(employe)
                .build();

        // ✅ sauvegarder d'abord pour générer l'ID
        Salaire savedSalaire = salaireRepository.save(salaire);

        // ✅ Générer PDF après sauvegarde
        byte[] pdfContent = genererFicheDePaiePDF(savedSalaire.getId());

        return savedSalaire;
    }

    @Override
    public List<Salaire> getSalairesByEmploye(Long employeId) {
        return salaireRepository.findByEmployeId(employeId);
    }

    @Override
    public List<Salaire> getSalairesByDepartementAndMois(Long departementId, String moisAnnee) {
        return salaireRepository.findByDepartementAndMois(departementId, moisAnnee);
    }

    @Override
    public byte[] genererFicheDePaiePDF(Long salaireId) {

        Salaire salaire = salaireRepository.findById(salaireId)
                .orElseThrow(() -> new RuntimeException("Salaire non trouvé"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            Paragraph ecole = new Paragraph("TECH SOLUTIONS")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(18);
            document.add(ecole);

            Paragraph adresse = new Paragraph(
                    "123 Avenue de l'Innovation, Casablanca, Maroc\nRC: 123456 | ICE: 0011223344")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10);
            document.add(adresse);
            document.add(new Paragraph("\n"));

            Paragraph titre = new Paragraph("BULLETIN DE PAIE")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16)
                    .setUnderline();
            document.add(titre);
            document.add(new Paragraph("\n"));

            float[] headerWidths = {250f, 250f};
            Table headerTable = new Table(headerWidths);

            headerTable.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("Salarié:").setBold())
                    .add(new Paragraph(salaire.getEmploye().getNom().toUpperCase() + " " + salaire.getEmploye().getPrenom()))
                    .add(new Paragraph("Matricule: " + salaire.getEmploye().getMatricule()))
                    .add(new Paragraph("Poste: " + salaire.getEmploye().getPoste()))
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));

            headerTable.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("Période: " + salaire.getMoisAnnee()).setBold())
                    .add(new Paragraph("Date de paiement: " + (salaire.getDatePaiement() != null ? salaire.getDatePaiement() : "N/A")))
                    .add(new Paragraph("Mode de paiement: " + (salaire.getModePaiement() != null ? salaire.getModePaiement() : "Virement")))
                    .add(new Paragraph("Ref: " + (salaire.getReferencePaiement() != null ? salaire.getReferencePaiement() : "-")))
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.add(headerTable);
            document.add(new Paragraph("\n"));

            float[] columnWidths = {300f, 100f, 100f};
            Table table = new Table(columnWidths);

            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Rubrique").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("Gains").setBold().setTextAlignment(TextAlignment.RIGHT)));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("Retenues").setBold().setTextAlignment(TextAlignment.RIGHT)));

            addRow(table, "Salaire de Base", salaire.getSalaireBrut(), 0);

            if (salaire.getPrimes() > 0) addRow(table, "Primes", salaire.getPrimes(), 0);
            if (salaire.getRetenues() > 0) addRow(table, "Retenues", 0, salaire.getRetenues());

            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("\nNET À PAYER").setBold().setFontSize(14)));
            table.addCell(new com.itextpdf.layout.element.Cell(1, 2)
                    .add(new Paragraph("\n" + String.format("%.2f Dhs", salaire.getSalaireNet())).setBold()
                            .setFontSize(14).setTextAlignment(TextAlignment.RIGHT)));

            document.add(table);

            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Ce bulletin de paie est généré électroniquement et ne nécessite pas de signature.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setItalic());

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private void addRow(Table table, String label, double gain, double retenue) {
        table.addCell(new Paragraph(label));
        table.addCell(new Paragraph(gain > 0 ? String.format("%.2f", gain) : "")
                .setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Paragraph(retenue > 0 ? String.format("%.2f", retenue) : "")
                .setTextAlignment(TextAlignment.RIGHT));
    }
}
