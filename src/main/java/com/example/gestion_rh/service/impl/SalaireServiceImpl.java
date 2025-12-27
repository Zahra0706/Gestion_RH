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

    @Override
    public Salaire genererSalaire(Long employeId, String moisAnnee, double primes, double retenues) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        if (salaireRepository.findByEmployeIdAndMoisAnnee(employeId, moisAnnee).isPresent()) {
            throw new RuntimeException("Salaire déjà généré pour ce mois");
        }

        if (employe.getContrat() == null) {
            throw new RuntimeException("Aucun contrat actif pour cet employé");
        }
        if (employe.getContrat().getSalaireBase() == null) {
            throw new RuntimeException("Salaire de base manquant dans le contrat");
        }

        double salaireBrut = employe.getContrat().getSalaireBase().doubleValue();
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

        //  1) Sauvegarder d'abord pour obtenir l'ID
        salaire = salaireRepository.save(salaire);

        //  2) (Optionnel) Générer le PDF ici juste pour vérifier que ça marche
        // byte[] pdfContent = genererFicheDePaiePDF(salaire.getId());

        return salaire;
    }

    /*@Override
    public Salaire genererSalaire(Long employeId, String moisAnnee, double primes, double retenues) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        // Vérifier si le salaire pour ce mois existe déjà
        if (salaireRepository.findByEmployeIdAndMoisAnnee(employeId, moisAnnee).isPresent()) {
            throw new RuntimeException("Salaire déjà généré pour ce mois");
        }

        // Correction : Utiliser doubleValue() pour convertir BigDecimal en double
        double salaireBrut = employe.getContrat().getSalaireBase().doubleValue();
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
    }*/

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

            // Titre
            Paragraph titre = new Paragraph("FICHE DE PAIE")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16);
            document.add(titre);

            // Informations employé
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Employé: " + salaire.getEmploye().getNom() + " " +
                    salaire.getEmploye().getPrenom()));
            document.add(new Paragraph("Matricule: " + salaire.getEmploye().getMatricule()));
            document.add(new Paragraph("Département: " +
                    (salaire.getEmploye().getDepartement() != null ?
                            salaire.getEmploye().getDepartement().getNom() : "N/A")));
            document.add(new Paragraph("Mois: " + salaire.getMoisAnnee()));
            document.add(new Paragraph("Date de paiement: " + salaire.getDatePaiement()));

            // Tableau des montants
            float[] columnWidths = {200f, 100f};
            Table table = new Table(columnWidths);

            table.addCell("Salaire Brut");
            table.addCell(String.format("%.2f €", salaire.getSalaireBrut()));

            table.addCell("Primes");
            table.addCell(String.format("+ %.2f €", salaire.getPrimes()));

            table.addCell("Retenues");
            table.addCell(String.format("- %.2f €", salaire.getRetenues()));

            table.addCell("SALAIRE NET");
            table.addCell(String.format("%.2f €", salaire.getSalaireNet()))
                    .setBold();

            document.add(table);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    // Méthode privée pour générer le PDF à partir de l'objet Salaire (si nécessaire)
    private byte[] genererFicheDePaiePDF(Salaire salaire) {
        return genererFicheDePaiePDF(salaire.getId());
    }

    @Override
    public List<Salaire> getSalairesByDepartementAndMois(Long departementId, String moisAnnee) {
        return salaireRepository.findByDepartementAndMois(departementId, moisAnnee);
    }
}