package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.entity.Employe;

/**
 * Service for generating professional PDF contracts using iText 7
 */
public interface PdfContratService {

    /**
     * Generate a professional PDF contract
     * 
     * @param contrat Contract entity
     * @param employe Employee entity
     * @return PDF content as byte array
     */
    byte[] genererContratPDF(Contrat contrat, Employe employe);

    /**
     * Generate and save PDF contract to file system
     * 
     * @param contrat Contract entity
     * @param employe Employee entity
     * @return File path where PDF was saved
     */
    String genererEtSauvegarderPDF(Contrat contrat, Employe employe);
}
