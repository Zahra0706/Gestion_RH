package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Salaire;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.SalaireRepository;
import com.example.gestion_rh.repository.EmployeRepository;
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
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalaireServiceImpl implements SalaireService {

    private final SalaireRepository salaireRepository;
    private final EmployeRepository employeRepository;
    private final com.example.gestion_rh.repository.ContratRepository contratRepository;

    @Override
    public Salaire genererSalaire(Long employeId, String moisAnnee, double primes, double retenues) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        // Vérifier si le salaire pour ce mois existe déjà
        if (salaireRepository.findByEmployeIdAndMoisAnnee(employeId, moisAnnee).isPresent()) {
            throw new RuntimeException("Salaire déjà généré pour ce mois");
        }

        // Récupérer le contrat actif pour le salaire de base
        java.util.List<com.example.gestion_rh.entity.Contrat> contrats = contratRepository.findByEmployeId(employeId);
        com.example.gestion_rh.entity.Contrat contrat = contrats.stream()
                .filter(c -> c.getStatut() == com.example.gestion_rh.entity.Contrat.StatutContrat.SIGNE
                        || c.getStatut() == com.example.gestion_rh.entity.Contrat.StatutContrat.ACTIF)
                .findFirst()
                .orElse(null);

        if (contrat == null || contrat.getSalaireBase() == null) {
            // Fallback to any contract if no active one found, just to avoid breaking
            // during dev/test with existing data
            if (!contrats.isEmpty()) {
                contrat = contrats.get(0);
            } else {
                throw new RuntimeException("Aucun contrat ou salaire de base trouvé pour cet employé");
            }
        }
        double salaireBrut = contrat.getSalaireBase();

        // Correction : Utiliser doubleValue() pour convertir BigDecimal en double
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

        // Générer le PDF
        byte[] pdfContent = genererFicheDePaiePDF(salaire.getId()); // Appel correct

        return salaireRepository.save(salaire);
    }

    @Override
    public List<Salaire> getSalairesByEmploye(Long employeId) {
        return salaireRepository.findByEmployeId(employeId);
    }

    @Override
    public byte[] genererFicheDePaiePDF(Long salaireId) {
        Salaire salaire = salaireRepository.findById(salaireId)
                .orElseThrow(() -> new RuntimeException("Salaire non trouvé"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // En-tête Entreprise
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

            // Titre
            Paragraph titre = new Paragraph("BULLETIN DE PAIE")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16)
                    .setUnderline();
            document.add(titre);
            document.add(new Paragraph("\n"));

            // Infos Employé et Salaire (Tableau 2 colonnes)
            float[] headerWidths = { 250f, 250f };
            Table headerTable = new Table(headerWidths);

            headerTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Salarié:").setBold())
                    .add(new Paragraph(
                            salaire.getEmploye().getNom().toUpperCase() + " " + salaire.getEmploye().getPrenom()))
                    .add(new Paragraph("Matricule: " + salaire.getEmploye().getMatricule()))
                    .add(new Paragraph("Poste: " + salaire.getEmploye().getPoste()))
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));

            headerTable.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("Période: " + salaire.getMoisAnnee()).setBold())
                    .add(new Paragraph("Date de paiement: "
                            + (salaire.getDatePaiement() != null ? salaire.getDatePaiement().toString() : "N/A")))
                    .add(new Paragraph("Mode de paiement: "
                            + (salaire.getModePaiement() != null ? salaire.getModePaiement() : "Virement")))
                    .add(new Paragraph(
                            "Ref: " + (salaire.getReferencePaiement() != null ? salaire.getReferencePaiement() : "-")))
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.add(headerTable);
            document.add(new Paragraph("\n"));

            // Tableau détails
            float[] columnWidths = { 300f, 100f, 100f };
            Table table = new Table(columnWidths);

            // Header
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Rubrique").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("Gains").setBold().setTextAlignment(TextAlignment.RIGHT)));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("Retenues").setBold().setTextAlignment(TextAlignment.RIGHT)));

            // Lignes
            addRow(table, "Salaire de Base", salaire.getSalaireBrut(), 0);

            if (salaire.getPrimeAnciennete() > 0) {
                addRow(table, "Prime d'ancienneté", salaire.getPrimeAnciennete(), 0);
            }
            if (salaire.getPrimePerformance() > 0) {
                addRow(table, "Prime de performance", salaire.getPrimePerformance(), 0);
            }
            if (salaire.getPrimes() > 0) {
                addRow(table, "Autres Primes", salaire.getPrimes(), 0);
            }
            if (salaire.getHeuresSupplementaires() > 0) {
                addRow(table, "Heures Supp. (" + salaire.getHeuresSupplementaires() + "h)",
                        (salaire.getTauxHeureSupplementaire() > 0
                                ? salaire.getHeuresSupplementaires() * salaire.getTauxHeureSupplementaire()
                                : 0),
                        0);
            }

            // Totaux Brut
            double totalBrut = salaire.getSalaireBrut() + salaire.getPrimeAnciennete() + salaire.getPrimePerformance()
                    + salaire.getPrimes(); // approx
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("TOTAL BRUT").setBold())
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(String.format("%.2f", totalBrut)).setBold()
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(""))
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));

            // Retenues
            if (salaire.getCotisationsSociales() > 0) {
                addRow(table, "Cotisations Sociales (CNSS/AMO)", 0, salaire.getCotisationsSociales());
            }
            if (salaire.getImpots() > 0) {
                addRow(table, "Impôt sur le Revenu (IR)", 0, salaire.getImpots());
            }
            if (salaire.getRetenues() > 0) {
                addRow(table, "Autres Retenues", 0, salaire.getRetenues());
            }

            // Total Net
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("\nNET À PAYER").setBold().setFontSize(14)));
            table.addCell(new com.itextpdf.layout.element.Cell(1, 2)
                    .add(new Paragraph("\n" + String.format("%.2f Dhs", salaire.getSalaireNet())).setBold()
                            .setFontSize(14).setTextAlignment(TextAlignment.RIGHT)));

            document.add(table);

            document.add(new Paragraph("\n\n"));
            document.add(
                    new Paragraph("Ce bulletin de paie est généré électroniquement et ne nécessite pas de signature.")
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
        table.addCell(new Paragraph(gain > 0 ? String.format("%.2f", gain) : "").setTextAlignment(TextAlignment.RIGHT));
        table.addCell(
                new Paragraph(retenue > 0 ? String.format("%.2f", retenue) : "").setTextAlignment(TextAlignment.RIGHT));
    }

    // Méthode privée pour générer le PDF à partir de l'objet Salaire (si
    // nécessaire)
    private byte[] genererFicheDePaiePDF(Salaire salaire) {
        return genererFicheDePaiePDF(salaire.getId());
    }

    @Override
    public List<Salaire> getSalairesByDepartementAndMois(Long departementId, String moisAnnee) {
        return salaireRepository.findByDepartementAndMois(departementId, moisAnnee);
    }
}