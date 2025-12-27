package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Contrat;
import java.util.List;

public interface ExportService {
    byte[] generateCongesExcel(List<Conge> conges);

    byte[] generateCongesPdf(List<Conge> conges);

    byte[] generateContratPdf(Contrat contrat, byte[] qrCodeImage);
}
