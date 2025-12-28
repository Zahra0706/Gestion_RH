package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.entity.ContratTemplate;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.ContratRepository;
import com.example.gestion_rh.repository.ContratTemplateRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.ContractGenerationService;
import com.example.gestion_rh.service.ContratService;
import com.example.gestion_rh.service.EmailService;
import com.example.gestion_rh.service.PdfContratService;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContratServiceImpl implements ContratService {

    private final ContratTemplateRepository contratTemplateRepository;
    private final ContractGenerationService contractGenerationService;
    private final ContratRepository contratRepository;
    private final EmployeRepository employeRepository;
    private final EmailService emailService;
    private final PdfContratService pdfContratService;

    @Override
    public Optional<Contrat> getContratByReference(String reference) {
        return contratRepository.findByReferenceContrat(reference);
    }

    @Override
    public List<Contrat> getContratsExpirantBientot(int jours) {
        return contratRepository.findContratsExpirantBientot(LocalDate.now(), LocalDate.now().plusDays(jours));
    }

    @Override
    public List<Contrat> getContratsByStatut(Contrat.StatutContrat statut) {
        return contratRepository.findByStatut(statut);
    }

    @Override
    public Map<String, Long> getDetailedStats() {
        return Map.of(
                "CDI", contratRepository.countByTypeContrat(Contrat.TypeContrat.CDI),
                "CDD", contratRepository.countByTypeContrat(Contrat.TypeContrat.CDD),
                "SIGNE", contratRepository.countByStatut(Contrat.StatutContrat.SIGNE),
                "BROUILLON", contratRepository.countByStatut(Contrat.StatutContrat.BROUILLON));
    }

    @Override
    public Contrat genererContrat(Employe employe, Map<String, Object> parametres) {
        Contrat contrat = new Contrat();
        contrat.setEmploye(employe);
        contrat.setDateDebut((LocalDate) parametres.get("dateDebut"));
        contrat.setSalaireBase((Double) parametres.get("salaire"));
        contrat.setTypeContratEnum(Contrat.TypeContrat.valueOf((String) parametres.get("typeContrat")));
        // Nouveaux champs obligatoires
        String posteNom = "EMPLOYE";
        if (employe.getPoste() != null) {
            posteNom = employe.getPoste().name();
        }
        contrat.setPoste(posteNom);
        contrat.setStatut(Contrat.StatutContrat.BROUILLON);
        contrat.setReferenceContrat(genererReferenceContrat());
        contrat.setDureeEssai(90);

        return contratRepository.save(contrat);
    }

    @Override
    public byte[] genererContratPDF(Long contratId) {
        if (contratId == null) {
            throw new IllegalArgumentException("Contrat ID ne peut pas être null");
        }
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

        Employe employe = contrat.getEmploye();

        // Build HTML content matching the visual model provided
        String htmlContent;
        if (contrat.getContentHtml() != null && !contrat.getContentHtml().isEmpty()) {
            htmlContent = contrat.getContentHtml();
        } else {
            htmlContent = generateModelHtml(contrat, employe);
        }

        ByteArrayOutputStream target = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlContent, target);
        return target.toByteArray();
    }

    @Override
    public Contrat saveContrat(Contrat contrat) {
        if (contrat.getReferenceContrat() == null) {
            contrat.setReferenceContrat(
                    "CTR-" + LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue())
                            + "-" + System.currentTimeMillis() % 10000);
        }
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat getContratByEmployeId(Long employeId) {
        // Return latest contract for employee
        return contratRepository.findByEmployeId(employeId).stream()
                .findFirst() // In real app, sort by date desc
                .orElse(null);
    }

    private String generateModelHtml(Contrat contrat, Employe employe) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateAujourdhui = LocalDate.now().format(formatter);
        String dateDebut = contrat.getDateDebut() != null ? contrat.getDateDebut().format(formatter) : "_________";
        String dateFin = contrat.getDateFin() != null ? contrat.getDateFin().format(formatter) : "Indéterminée";
        String salaire = contrat.getSalaireBase() != null ? String.format("%.2f", contrat.getSalaireBase())
                : "_________";
        String dateNaissance = employe != null && employe.getDateNaissance() != null
                ? employe.getDateNaissance().format(formatter)
                : "01/01/1990";
        String lieu = "Casablanca";
        String reference = contrat.getReferenceContrat() != null ? contrat.getReferenceContrat() : "Non définie";

        // Safety checks for employee
        String nom = employe != null ? employe.getNom() : "________________";
        String prenom = employe != null ? employe.getPrenom() : "________________";
        String adresse = (employe != null && employe.getAdresse() != null) ? employe.getAdresse() : "________________";
        String poste = contrat.getPoste() != null ? contrat.getPoste()
                : (employe != null ? employe.getPoste().name() : "Collaborateur");
        String typeContrat = contrat.getTypeContrat() != null ? contrat.getTypeContrat() : "CDI";
        String dureeEssai = contrat.getDureeEssai() != null ? contrat.getDureeEssai().toString() : "90";
        String heures = contrat.getHeuresHebdomadaires() != null ? contrat.getHeuresHebdomadaires().toString() : "44";
        String avantages = contrat.getAvantages() != null && !contrat.getAvantages().isEmpty() ? contrat.getAvantages()
                : "Aucun avantage particulier mentionné";

        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Helvetica', 'Arial', sans-serif; line-height: 1.6; color: #333; padding: 40px; background: #fff; }
                        .header { text-align: center; border-bottom: 2px solid #2c3e50; padding-bottom: 20px; margin-bottom: 30px; }
                        .company-name { font-size: 20pt; font-weight: bold; color: #2c3e50; }
                        .company-info { font-size: 9pt; color: #7f8c8d; }
                        .contract-title { text-align: center; font-size: 18pt; font-weight: bold; margin: 30px 0; color: #2c3e50; text-transform: uppercase; border: 2px solid #2c3e50; padding: 10px; }
                        .doc-confidential { text-align: center; font-size: 10pt; color: #e74c3c; font-weight: bold; margin-bottom: 20px; }
                        .section-title { font-size: 14pt; font-weight: bold; color: #2c3e50; margin-top: 25px; border-bottom: 1px solid #eee; padding-bottom: 5px; }
                        .highlight { background-color: #f9f9f9; padding: 2px 5px; font-weight: bold; }
                        .footer { margin-top: 50px; font-size: 8pt; color: #95a5a6; border-top: 1px solid #eee; padding-top: 10px; text-align: center; }
                        .signature-table { width: 100%%; margin-top: 50px; border-collapse: collapse; }
                        .signature-table td { width: 50%%; vertical-align: top; padding: 20px; height: 150px; border: 1px solid #eee; }
                        .data-row { display: flex; margin-bottom: 5px; }
                        .data-label { font-weight: bold; min-width: 150px; }
                        .important-box { background-color: #f8f9fa; border: 1px solid #dee2e6; padding: 15px; margin: 20px 0; font-size: 10pt; }
                        ul { padding-left: 20px; }
                        li { margin-bottom: 5px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <div class="company-name">TECHSOLUTIONS SARL</div>
                        <div class="company-info">
                            123 Boulevard Zerktouni, Casablanca 20000<br>
                            Tél: +212 5 22 XX XX XX | Email: contact@techsolutions.ma<br>
                            RC: 123456 | ICE: 002123456000000 | Patente: 12345678
                        </div>
                    </div>

                    <div style="float: right; text-align: right;">
                        <strong>Référence:</strong> %s<br>
                        <strong>Date d'émission:</strong> %s
                    </div>
                    <div style="clear: both;"></div>

                    <div class="contract-title">CONTRAT DE TRAVAIL À DURÉE INDÉTERMINÉE</div>
                    <div class="doc-confidential">DOCUMENT CONFIDENTIEL</div>

                    <div class="section-title">Article 1 - Les Parties Contractantes</div>
                    <p><strong>L'EMPLOYEUR :</strong><br>
                    <strong>TECHSOLUTIONS SARL</strong>, Société à Responsabilité Limitée au capital de 100.000,00 MAD,
                    dont le siège social est situé au 123 Boulevard Zerktouni, Casablanca,
                    inscrite au RC n° 123456 - ICE : 002123456000000,
                    représentée par M. Directeur Général, en sa qualité de Gérant.</p>

                    <p><strong>LE SALARIÉ :</strong><br>
                    M./Mme <span class="highlight">%s %s</span><br>
                    Né(e) le : <span class="highlight">%s</span><br>
                    Adresse : <span class="highlight">%s</span><br>
                    CIN n° : <span class="highlight">_______________</span></p>
                    <p>Ci-après dénommé(e) "le Salarié"</p>

                    <div class="important-box">
                        <strong>IMPORTANT :</strong> La déclaration préalable à l'embauche de <span class="highlight">%s %s</span>
                        a été effectuée auprès de la Caisse Nationale de Sécurité Sociale (CNSS) conformément aux dispositions du Code du Travail.
                        Le salarié pourra exercer son droit d'accès et de rectification auprès de cet organisme.
                    </div>

                    <div class="section-title">Article 2 - Objet du Contrat</div>
                    <p>Le présent contrat a pour objet de définir les modalités et conditions d'emploi du salarié au sein de la société.</p>
                    <div class="data-row"><span class="data-label">Fonction :</span> <span>%s</span></div>
                    <div class="data-row"><span class="data-label">Type de contrat :</span> <span>%s</span></div>
                    <p>Le salarié s'engage à exercer ses fonctions avec diligence et professionnalisme, conformément aux directives de sa hiérarchie et au règlement intérieur de l'entreprise.</p>

                    <div class="section-title">Article 3 - Durée du Contrat et Période d'Essai</div>
                    <div class="data-row"><span class="data-label">Date d'effet :</span> <span>%s</span></div>
                    <div class="data-row"><span class="data-label">Date de fin :</span> <span>%s</span></div>
                    <div class="data-row"><span class="data-label">Période d'essai :</span> <span>%s jours</span></div>
                    <div class="data-row"><span class="data-label">Temps de travail :</span> <span>%s heures/semaine</span></div>
                    <p>Durant la période d'essai, chacune des parties pourra rompre le contrat sans préavis ni indemnité, conformément aux dispositions légales en vigueur.</p>

                    <div class="section-title">Article 4 - Rémunération et Avantages</div>
                    <div class="data-row"><span class="data-label">Salaire mensuel brut :</span> <span class="highlight">%s MAD</span></div>
                    <p>Ce salaire s'entend brut, avant déduction des cotisations sociales et fiscales obligatoires. Le salaire sera versé mensuellement par virement bancaire avant le 5 de chaque mois.</p>
                    <p><strong>Avantages complémentaires :</strong><br>
                    %s</p>

                    <div class="section-title">Article 5 - Congés et Repos</div>
                    <p>Le salarié bénéficie des congés légaux suivants :</p>
                    <ul>
                        <li><strong>Congés payés :</strong> 1,5 jour ouvrable par mois de travail effectif (18 jours par an)</li>
                        <li><strong>Jours fériés :</strong> Conformément au calendrier officiel marocain</li>
                        <li><strong>Repos hebdomadaire :</strong> Généralement le week-end</li>
                        <li><strong>Autres congés :</strong> Conformément aux dispositions du Code du Travail (mariage, naissance, décès, etc.)</li>
                    </ul>

                    <div class="section-title">Article 6 - Obligations du Salarié</div>
                    <ul>
                        <li>Respecter le règlement intérieur de l'entreprise dont un exemplaire lui a été remis</li>
                        <li>Exécuter ses missions avec professionnalisme et ponctualité</li>
                        <li>Respecter les procédures et directives établies par l'employeur</li>
                    </ul>

                    <div class="section-title">Article 7 - Confidentialité</div>
                    <p>Le salarié s'engage à observer la plus stricte confidentialité concernant les informations techniques, commerciales et financières de l'entreprise. Cette obligation demeure valable pendant et après la cessation du contrat.</p>

                    <div class="section-title">Article 8 - Rupture du Contrat</div>
                    <p>Le présent contrat pourra être rompu dans les conditions prévues par le Code du Travail marocain (Démission, Licenciement, Rupture conventionnelle).</p>

                    <div class="section-title">Article 9 - Dispositions Finales</div>
                    <p>Le présent contrat est régi par les dispositions du Code du Travail marocain (Loi n° 65-99). Fait à Casablanca, le %s.</p>

                    <table class="signature-table">
                        <tr>
                            <td>
                                <strong>L'EMPLOYEUR</strong><br>
                                <small>Signature et cachet de l'entreprise</small>
                            </td>
                            <td>
                                <strong>LE SALARIÉ</strong><br>
                                <small>Précédée de la mention manuscrite "Lu et approuvé"</small>
                            </td>
                        </tr>
                    </table>

                    <div class="footer">
                        TECHSOLUTIONS SARL - RC: 123456 - ICE: 002123456000000<br>
                        Ce document est confidentiel et ne peut être reproduit sans autorisation.<br>
                        Généré automatiquement le %s
                    </div>
                </body>
                </html>
                """
                .formatted(
                        reference, dateAujourdhui,
                        prenom, nom, dateNaissance, adresse,
                        prenom, nom,
                        poste, typeContrat,
                        dateDebut, dateFin, dureeEssai, heures,
                        salaire, avantages,
                        dateAujourdhui, dateAujourdhui);
    }

    @Override
    public List<Contrat> getAllActiveContrats() {
        return contratRepository.findAll().stream()
                .filter(c -> c.getStatut() == Contrat.StatutContrat.SIGNE)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getContratStats() {
        long total = contratRepository.count();
        long active = contratRepository.findAll().stream()
                .filter(c -> c.getStatut() == Contrat.StatutContrat.SIGNE)
                .count();
        long archives = contratRepository.findAll().stream()
                .filter(c -> c.getStatut() == Contrat.StatutContrat.RESILIE
                        || c.getStatut() == Contrat.StatutContrat.EXPIRE)
                .count();

        long expirant = getContratsExpirantBientot(30).size();
        long enAttente = contratRepository.countByStatut(Contrat.StatutContrat.EN_ATTENTE_SIGNATURE);

        return Map.of(
                "total", total,
                "actif", active,
                "archive", archives,
                "expirant", expirant,
                "en_attente", enAttente);
    }

    @Override
    public Optional<Contrat> getContratById(Long id) {
        return contratRepository.findById(id);
    }

    @Override
    public void archiverContrat(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));
        contrat.setStatut(Contrat.StatutContrat.RESILIE);
        contratRepository.save(contrat);
    }

    @Override
    public void validerContrat(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

        contrat.setStatut(Contrat.StatutContrat.EN_ATTENTE_SIGNATURE);

        // Auto-send email upon validation
        envoyerContrat(id);

        contratRepository.save(contrat);
    }

    @Override
    public void envoyerContrat(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

        Employe employe = contrat.getEmploye();
        if (employe == null || employe.getEmail() == null) {
            throw new RuntimeException("L'employé n'a pas d'adresse email configurée");
        }

        // Generate PDF
        byte[] pdfContent = pdfContratService.genererContratPDF(contrat, employe);
        String sujet = "Votre contrat de travail - " + contrat.getReferenceContrat();
        String contenu = genererEmailContenu(contrat, employe);

        log.info("Envoi du contrat {} à l'employé {} ({})",
                contrat.getReferenceContrat(),
                employe.getNom() + " " + employe.getPrenom(),
                employe.getEmail());

        emailService.envoyerContratParEmail(employe.getEmail(), sujet, contenu,
                pdfContent,
                "contrat_" + contrat.getReferenceContrat().replace("/", "_") + ".pdf");
    }

    @Override
    public void updateContratContent(Long id, String contentHtml) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));
        contrat.setContentHtml(contentHtml);
        contratRepository.save(contrat);
    }

    /**
     * Génère le contenu HTML de l'email pour l'envoi du contrat
     */
    private String genererEmailContenu(Contrat contrat, Employe employe) {
        return String.format(
                """
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }
                                .content { padding: 20px; }
                                .highlight { color: #3498db; font-weight: bold; }
                                .footer { background-color: #ecf0f1; padding: 15px; text-align: center; font-size: 12px; color: #7f8c8d; }
                                .button { background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 15px 0; }
                            </style>
                        </head>
                        <body>
                            <div class="header">
                                <h1>TECHSOLUTIONS</h1>
                                <p>Votre Contrat de Travail</p>
                            </div>
                            <div class="content">
                                <p>Bonjour <strong>%s %s</strong>,</p>

                                <p>Nous avons le plaisir de vous faire parvenir votre contrat de travail en pièce jointe.</p>

                                <p><strong>Détails du contrat :</strong></p>
                                <ul>
                                    <li>Référence : <span class="highlight">%s</span></li>
                                    <li>Type : <span class="highlight">%s</span></li>
                                    <li>Poste : <span class="highlight">%s</span></li>
                                    <li>Date de début : <span class="highlight">%s</span></li>
                                </ul>

                                <p>Veuillez lire attentivement ce document et, si vous acceptez les termes du contrat,
                                nous vous remercions de nous le retourner signé dans les meilleurs délais.</p>

                                <p>Si vous avez des questions concernant ce contrat, n'hésitez pas à contacter le service RH.</p>

                                <p>Cordialement,<br>
                                <strong>Le Service des Ressources Humaines</strong><br>
                                TECHSOLUTIONS</p>
                            </div>
                            <div class="footer">
                                <p>© 2024 TECHSOLUTIONS - Tous droits réservés</p>
                                <p>123 Boulevard Zerktouni, Casablanca | Tel: +212 5XX-XXXXXX | Email: rh@techsolutions.ma</p>
                            </div>
                        </body>
                        </html>
                        """,
                employe.getPrenom(),
                employe.getNom(),
                contrat.getReferenceContrat(),
                contrat.getTypeContrat(),
                contrat.getPoste() != null ? contrat.getPoste() : "Non spécifié",
                contrat.getDateDebut() != null
                        ? contrat.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "Non spécifiée");
    }

    // --- Implementation of Requested Refactoring Methods ---

    @Override
    public Contrat creerContrat(Contrat contrat, Long employeId, Long rhId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
        contrat.setEmploye(employe);

        if (contrat.getPoste() == null) {
            String posteNom = "Indéfini";
            if (employe.getPoste() != null) {
                posteNom = employe.getPoste().name();
            }
            contrat.setPoste(posteNom);
        }

        if (rhId != null) {
            Employe rh = employeRepository.findById(rhId).orElse(null);
            contrat.setCreateurRH(rh);
        }

        // Generate reference if null or empty (form can bind empty string)
        if (contrat.getReferenceContrat() == null || contrat.getReferenceContrat().trim().isEmpty()) {
            contrat.setReferenceContrat(genererReferenceContrat());
        }
        if (contrat.getStatut() == null) {
            contrat.setStatut(Contrat.StatutContrat.BROUILLON);
        }

        return contratRepository.save(contrat);
    }

    // ... skipped methods ...

    @Override
    public Contrat updateContrat(Long id, Contrat contratDetails) {
        Contrat existing = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé avec id: " + id));

        // Update fields
        existing.setDateDebut(contratDetails.getDateDebut());
        existing.setDateFin(contratDetails.getDateFin());
        existing.setSalaireBase(contratDetails.getSalaireBase());
        existing.setTypeContrat(contratDetails.getTypeContrat());
        existing.setPoste(contratDetails.getPoste());
        existing.setDureeEssai(contratDetails.getDureeEssai());
        existing.setHeuresHebdomadaires(contratDetails.getHeuresHebdomadaires());

        return contratRepository.save(existing);
    }

    @Override
    public void deleteContrat(Long id) {
        contratRepository.deleteById(id);
    }

    @Override
    public List<Contrat> getAllContrats() {
        return contratRepository.findAll();
    }

    @Override
    public Contrat signerContrat(Long contratId, Long signataireId) {
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

        if (signataireId != null) {
            Employe signataire = employeRepository.findById(signataireId)
                    .orElseThrow(() -> new RuntimeException("Signataire non trouvé"));
            contrat.setSignataire(signataire);
        }

        contrat.setStatut(Contrat.StatutContrat.SIGNE);
        contrat.setDateSignature(LocalDate.now());

        return contratRepository.save(contrat);
    }

    @Override
    public void envoyerContratParEmail(Long contratId) {
        // Reuse existing logic
        envoyerContrat(contratId);
    }

    @Override
    public List<Contrat> getContratsExpirantSoon() {
        // Using existing logic for expiring soon (e.g. 30 days)
        return getContratsExpirantBientot(30);
    }

    @Override
    public boolean validerContrat(Contrat contrat) {
        // Basic validation rules
        if (contrat.getSalaireBase() == null || contrat.getSalaireBase() < 0)
            return false;
        if (contrat.getDateDebut() == null)
            return false;
        if (contrat.getEmploye() == null)
            return false;
        return true;
    }

    @Override
    public String genererReferenceContrat() {
        return "CTR-" + LocalDate.now().getYear() + "-"
                + String.format("%02d", LocalDate.now().getMonthValue()) + "-" + System.currentTimeMillis() % 10000;
    }

    @Override
    public String generateContractHtml(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

        // Use the new dynamic template system
        ContratTemplate template = contrat.getTemplate();
        if (template == null) {
            template = findDefaultTemplate();
        }

        if (template != null) {
            return contractGenerationService.generateHtml(template, contrat, contrat.getEmploye());
        }

        // Fallback to the hardcoded professional model
        return generateModelHtml(contrat, contrat.getEmploye());
    }

    private ContratTemplate findDefaultTemplate() {
        return contratTemplateRepository.findAll().stream()
                .filter(ContratTemplate::isDefault)
                .findFirst()
                .orElse(null);
    }
}
