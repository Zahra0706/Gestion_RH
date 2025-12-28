package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Contrat;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContratEmployeService {

    List<Contrat> getContratsEmploye(Long employeId);

    Contrat getContratPourEmploye(Long contratId, Long employeId);

    Contrat getContratByToken(String token);

    Contrat signerContrat(String token, MultipartFile fichierSignature);
}
