package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.PdfContratService;
import com.example.gestion_rh.entity.ContratTemplate;
import com.example.gestion_rh.repository.ContratTemplateRepository;
import com.example.gestion_rh.service.ContractGenerationService;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfContratServiceImpl implements PdfContratService {

    private static final String PDF_DIRECTORY = "./contrats-pdf/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ContractGenerationService contractGenerationService;
    private final ContratTemplateRepository templateRepository;

    @Override
    public byte[] genererContratPDF(Contrat contrat, Employe employe) {
        // Use the new dynamic template system
        ContratTemplate template = contrat.getTemplate();
        if (template == null) {
            template = templateRepository.findAll().stream()
                    .filter(ContratTemplate::isDefault)
                    .findFirst()
                    .orElse(null);
        }

        String htmlContent;
        if (template != null) {
            htmlContent = contractGenerationService.generateHtml(template, contrat, employe);
        } else {
            // Fallback to legacy hardcoded generation if no templates in DB
            htmlContent = genererHtmlContrat(contrat, employe);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ConverterProperties converterProperties = new ConverterProperties();
            HtmlConverter.convertToPdf(htmlContent, outputStream, converterProperties);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Erreur lors de la génération du PDF: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    @Override
    public String genererEtSauvegarderPDF(Contrat contrat, Employe employe) {
        // Create directory if it doesn't exist
        try {
            Path directory = Paths.get(PDF_DIRECTORY);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
        } catch (IOException e) {
            log.error("Erreur lors de la création du dossier PDF: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la création du dossier", e);
        }

        // Generate PDF
        byte[] pdfContent = genererContratPDF(contrat, employe);

        // Create filename
        String filename = String.format("%scontrat_%s_%s.pdf",
                PDF_DIRECTORY,
                contrat.getReferenceContrat().replace("/", "_"),
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        // Save to file
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(pdfContent);
            log.info("PDF sauvegardé avec succès: {}", filename);
            return filename;
        } catch (IOException e) {
            log.error("Erreur lors de la sauvegarde du PDF: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la sauvegarde du PDF", e);
        }
    }

    private String genererHtmlContrat(Contrat contrat, Employe employe) {
        String dateDebut = contrat.getDateDebut() != null ? contrat.getDateDebut().format(DATE_FORMATTER) : "_________";
        String dateFin = contrat.getDateFin() != null ? contrat.getDateFin().format(DATE_FORMATTER) : "Indéterminée";
        String dateSignature = LocalDate.now().format(DATE_FORMATTER);
        String salaire = contrat.getSalaireBase() != null ? String.format("%.2f", contrat.getSalaireBase())
                : "_________";
        String poste = contrat.getPoste() != null ? contrat.getPoste()
                : (employe.getPoste() != null ? employe.getPoste().name() : "EMPLOYÉ");
        String employeNom = employe.getNom() + " " + employe.getPrenom();
        String adresse = employe.getAdresse() != null ? employe.getAdresse() : "Adresse non renseignée";
        String dateNaissance = employe.getDateNaissance() != null ? employe.getDateNaissance().format(DATE_FORMATTER)
                : "01/01/1990";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page {
                            size: A4;
                            margin: 2cm;
                        }

                        body {
                            font-family: 'Arial', 'Helvetica', sans-serif;
                            font-size: 11pt;
                            line-height: 1.6;
                            color: #000;
                            margin: 0;
                            padding: 0;
                        }

                        .header {
                            text-align: center;
                            border-bottom: 3px solid #003366;
                            padding-bottom: 15px;
                            margin-bottom: 30px;
                        }

                        .company-logo {
                            font-size: 24pt;
                            font-weight: bold;
                            color: #003366;
                            margin-bottom: 5px;
                        }

                        .company-info {
                            font-size: 9pt;
                            color: #666;
                        }

                        h1 {
                            text-align: center;
                            font-size: 16pt;
                            font-weight: bold;
                            text-transform: uppercase;
                            margin: 30px 0;
                            color: #003366;
                        }

                        h2 {
                            font-size: 13pt;
                            font-weight: bold;
                            color: #003366;
                            margin-top: 25px;
                            margin-bottom: 10px;
                            border-bottom: 1px solid #003366;
                            padding-bottom: 5px;
                        }

                        .reference {
                            text-align: right;
                            font-size: 10pt;
                            color: #666;
                            margin-bottom: 20px;
                        }

                        .section {
                            margin-bottom: 20px;
                        }

                        .parties {
                            margin: 20px 0;
                        }

                        .partie {
                            margin-bottom: 15px;
                            padding: 10px;
                            background-color: #f5f5f5;
                            border-left: 3px solid #003366;
                        }

                        .partie-title {
                            font-weight: bold;
                            color: #003366;
                            margin-bottom: 5px;
                        }

                        .highlight {
                            background-color: #ffeb3b;
                            padding: 2px 5px;
                            font-weight: bold;
                        }

                        .info-grid {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 15px;
                            margin: 15px 0;
                        }

                        .info-item {
                            padding: 10px;
                            background-color: #f9f9f9;
                            border: 1px solid #ddd;
                        }

                        .info-label {
                            font-weight: bold;
                            color: #003366;
                            font-size: 10pt;
                            margin-bottom: 5px;
                        }

                        .info-value {
                            font-size: 11pt;
                        }

                        .signature-section {
                            margin-top: 50px;
                            page-break-inside: avoid;
                        }

                        .signature-grid {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 30px;
                            margin-top: 30px;
                        }

                        .signature-box {
                            border: 1px solid #ddd;
                            padding: 15px;
                            min-height: 100px;
                            text-align: center;
                        }

                        .signature-label {
                            font-weight: bold;
                            color: #003366;
                            margin-bottom: 10px;
                        }

                        .signature-note {
                            font-size: 9pt;
                            color: #666;
                            font-style: italic;
                            margin-top: 5px;
                        }

                        .footer {
                            margin-top: 40px;
                            padding-top: 15px;
                            border-top: 2px solid #003366;
                            font-size: 8pt;
                            color: #666;
                            text-align: center;
                            page-break-inside: avoid;
                        }

                        .legal-notice {
                            font-size: 9pt;
                            color: #666;
                            padding: 10px;
                            background-color: #f0f0f0;
                            margin: 15px 0;
                            border-left: 3px solid #ffc107;
                        }

                        ul {
                            margin: 10px 0;
                            padding-left: 25px;
                        }

                        li {
                            margin: 5px 0;
                        }

                        .confidential {
                            color: #d32f2f;
                            font-weight: bold;
                            font-size: 9pt;
                            text-align: center;
                            margin: 10px 0;
                        }
                    </style>
                </head>
                <body>
                    <!-- En-tête -->
                    <div class="header">
                        <div class="company-logo">TECHSOLUTIONS SARL</div>
                        <div class="company-info">
                            123 Boulevard Zerktouni, Casablanca 20000<br/>
                            Tél: +212 5 22 XX XX XX | Email: contact@techsolutions.ma<br/>
                            RC: 123456 | ICE: 002123456000000 | Patente: 12345678
                        </div>
                    </div>

                    <div class="reference">
                        Référence: <span class="highlight">%s</span><br/>
                        Date d'émission: %s
                    </div>

                    <h1>Contrat de Travail à Durée %s</h1>

                    <div class="confidential">DOCUMENT CONFIDENTIEL</div>

                    <!-- Parties -->
                    <h2>Article 1 - Les Parties Contractantes</h2>
                    <div class="parties">
                        <div class="partie">
                            <div class="partie-title">L'EMPLOYEUR :</div>
                            <strong>TECHSOLUTIONS SARL</strong>, Société à Responsabilité Limitée<br/>
                            Capital social : 100.000,00 MAD<br/>
                            Siège social : 123 Boulevard Zerktouni, Casablanca<br/>
                            RC n° 123456 - ICE : 002123456000000<br/>
                            Représentée par <span class="highlight">M. Directeur Général</span>, en sa qualité de Gérant
                        </div>

                        <div class="partie">
                            <div class="partie-title">LE SALARIÉ :</div>
                            M./Mme <span class="highlight">%s</span><br/>
                            Né(e) le : %s<br/>
                            Adresse : %s<br/>
                            CIN n° : _______________<br/>
                            Ci-après dénommé(e) "le Salarié"
                        </div>
                    </div>

                    <!-- Déclaration CNSS -->
                    <div class="legal-notice">
                        <strong>IMPORTANT :</strong> La déclaration préalable à l'embauche de <span class="highlight">%s</span>
                        a été effectuée auprès de la Caisse Nationale de Sécurité Sociale (CNSS) conformément aux dispositions
                        du Code du Travail. Le salarié pourra exercer son droit d'accès et de rectification auprès de cet organisme.
                    </div>

                    <!-- Objet du contrat -->
                    <h2>Article 2 - Objet du Contrat</h2>
                    <div class="section">
                        <p>Le présent contrat a pour objet de définir les modalités et conditions d'emploi du salarié au sein de la société.</p>

                        <div class="info-grid">
                            <div class="info-item">
                                <div class="info-label">Fonction</div>
                                <div class="info-value"><span class="highlight">%s</span></div>
                            </div>
                            <div class="info-item">
                                <div class="info-label">Type de contrat</div>
                                <div class="info-value"><span class="highlight">%s</span></div>
                            </div>
                        </div>

                        <p>Le salarié s'engage à exercer ses fonctions avec diligence et professionnalisme, conformément aux
                        directives de sa hiérarchie et au règlement intérieur de l'entreprise.</p>
                    </div>

                    <!-- Durée du contrat -->
                    <h2>Article 3 - Durée du Contrat et Période d'Essai</h2>
                    <div class="section">
                        <div class="info-grid">
                            <div class="info-item">
                                <div class="info-label">Date d'effet</div>
                                <div class="info-value"><span class="highlight">%s</span></div>
                            </div>
                            <div class="info-item">
                                <div class="info-label">Date de fin</div>
                                <div class="info-value"><span class="highlight">%s</span></div>
                            </div>
                            <div class="info-item">
                                <div class="info-label">Période d'essai</div>
                                <div class="info-value">%s jours</div>
                            </div>
                            <div class="info-item">
                                <div class="info-label">Temps de travail</div>
                                <div class="info-value">%s heures/semaine</div>
                            </div>
                        </div>

                        <p>Durant la période d'essai, chacune des parties pourra rompre le contrat sans préavis ni indemnité,
                        conformément aux dispositions légales en vigueur.</p>
                    </div>

                    <!-- Rémunération -->
                    <h2>Article 4 - Rémunération et Avantages</h2>
                    <div class="section">
                        <div class="info-item" style="margin-bottom: 15px;">
                            <div class="info-label">Salaire mensuel brut</div>
                            <div class="info-value" style="font-size: 14pt; color: #2e7d32;">
                                <strong><span class="highlight">%s MAD</span></strong>
                            </div>
                        </div>

                        <p>Ce salaire s'entend brut, avant déduction des cotisations sociales et fiscales obligatoires.</p>
                        <p>Le salaire sera versé mensuellement par virement bancaire avant le 5 de chaque mois.</p>

                        %s
                    </div>

                    <!-- Congés -->
                    <h2>Article 5 - Congés et Repos</h2>
                    <div class="section">
                        <p>Le salarié bénéficie des congés légaux suivants :</p>
                        <ul>
                            <li><strong>Congés payés :</strong> 1,5 jour ouvrable par mois de travail effectif (18 jours par an)</li>
                            <li><strong>Jours fériés :</strong> Conformément au calendrier officiel marocain</li>
                            <li><strong>Repos hebdomadaire :</strong> Généralement le week-end (vendredi et samedi ou samedi et dimanche)</li>
                            <li><strong>Autres congés :</strong> Conformément aux dispositions du Code du Travail (mariage, naissance, décès, etc.)</li>
                        </ul>
                    </div>

                    <!-- Obligations du salarié -->
                    <h2>Article 6 - Obligations du Salarié</h2>
                    <div class="section">
                        <p>Le salarié s'engage à :</p>
                        <ul>
                            <li>Respecter le règlement intérieur de l'entreprise dont un exemplaire lui a été remis</li>
                            <li>Exécuter ses missions avec professionnalisme et ponctualité</li>
                            <li>Respecter les procédures et directives établies par l'employeur</li>
                            <li>Informer immédiatement son employeur en cas d'absence ou de retard</li>
                            <li>Maintenir à jour ses compétences professionnelles</li>
                        </ul>
                    </div>

                    <!-- Confidentialité -->
                    <h2>Article 7 - Confidentialité et Protection des Données</h2>
                    <div class="section">
                        <div class="legal-notice">
                            <strong>CLAUSE DE CONFIDENTIALITÉ</strong><br/>
                            Le salarié s'engage à observer la plus stricte confidentialité concernant :
                            <ul>
                                <li>Les informations techniques, commerciales et financières de l'entreprise</li>
                                <li>Les données personnelles des clients et partenaires</li>
                                <li>Les projets en cours de développement</li>
                                <li>Toute information dont il aurait connaissance dans l'exercice de ses fonctions</li>
                            </ul>
                            Cette obligation de confidentialité demeure valable pendant et après la cessation du contrat de travail.
                        </div>
                    </div>

                    <!-- Rupture du contrat -->
                    <h2>Article 8 - Rupture du Contrat</h2>
                    <div class="section">
                        <p>Le présent contrat pourra être rompu dans les conditions prévues par le Code du Travail marocain :</p>
                        <ul>
                            <li><strong>Démission :</strong> Avec respect d'un préavis légal</li>
                            <li><strong>Licenciement :</strong> Pour motif légitime, avec respect de la procédure légale</li>
                            <li><strong>Rupture conventionnelle :</strong> D'un commun accord entre les parties</li>
                            <li><strong>Force majeure :</strong> Événement imprévisible rendant impossible la poursuite du contrat</li>
                        </ul>
                    </div>

                    <!-- Dispositions finales -->
                    <h2>Article 9 - Dispositions Finales</h2>
                    <div class="section">
                        <p>Le présent contrat est régi par les dispositions du Code du Travail marocain (Loi n° 65-99).</p>
                        <p>Tout litige relatif à l'interprétation ou à l'exécution du présent contrat sera soumis à la
                        juridiction compétente du lieu du siège social de l'employeur.</p>
                        <p>Le contrat est établi en deux (2) exemplaires originaux, dont un pour chaque partie.</p>
                    </div>

                    <!-- Signatures -->
                    <div class="signature-section">
                        <p style="text-align: center; margin: 20px 0;">
                            <strong>Fait à Casablanca, le %s</strong>
                        </p>

                        <div class="signature-grid">
                            <div class="signature-box">
                                <div class="signature-label">L'EMPLOYEUR</div>
                                <div style="height: 80px;"></div>
                                <div class="signature-note">
                                    Signature et cachet de l'entreprise<br/>
                                    Nom et qualité du signataire
                                </div>
                            </div>

                            <div class="signature-box">
                                <div class="signature-label">LE SALARIÉ</div>
                                <div style="height: 80px;"></div>
                                <div class="signature-note">
                                    Précédée de la mention manuscrite<br/>
                                    "Lu et approuvé"
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Pied de page -->
                    <div class="footer">
                        <strong>TECHSOLUTIONS SARL</strong><br/>
                        Société à Responsabilité Limitée au capital de 100.000,00 MAD<br/>
                        RC: 123456 - ICE: 002123456000000 - Patente: 12345678<br/>
                        Siège social: 123 Boulevard Zerktouni, Casablanca 20000<br/>
                        Tél: +212 5 22 XX XX XX | Email: contact@techsolutions.ma<br/>
                        <div style="margin-top: 10px; font-size: 7pt;">
                            Ce document est confidentiel et ne peut être reproduit sans autorisation.<br/>
                            Généré automatiquement le %s
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        contrat.getReferenceContrat(),
                        dateSignature,
                        contrat.getTypeContratEnum() == Contrat.TypeContrat.CDI ? "Indéterminée" : "Déterminée",                        employeNom,
                        dateNaissance,
                        adresse,
                        employeNom,
                        poste,
                        contrat.getTypeContrat().toString(),
                        dateDebut,
                        dateFin,
                        contrat.getDureeEssai() != null ? contrat.getDureeEssai().toString() : "90",
                        contrat.getHeuresHebdomadaires() != null ? contrat.getHeuresHebdomadaires().toString() : "44",
                        salaire,
                        contrat.getAvantages() != null && !contrat.getAvantages().isEmpty()
                                ? "<div class=\"legal-notice\"><strong>Avantages complémentaires :</strong><br/>"
                                        + contrat.getAvantages() + "</div>"
                                : "",
                        dateSignature,
                        dateSignature);
    }
}
