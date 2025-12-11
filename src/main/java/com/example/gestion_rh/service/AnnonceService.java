package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Annonce;

import java.util.List;

public interface AnnonceService {
    public abstract Annonce createAnnonce(Annonce annonce, Long createurId);

    public abstract Annonce updateAnnonce(Long id, Annonce annonce);

    public abstract void deleteAnnonce(Long id);

    public abstract Annonce getAnnonceById(Long id);

    public abstract List<Annonce> getAllAnnonces();

    public abstract List<Annonce> getAnnoncesByDepartement(Long departementId);

    public abstract List<Annonce> getAnnoncesByCreateur(Long createurId);

    public abstract List<Annonce> getAnnoncesByType(Annonce.TypeAnnonce type);

    public abstract List<Annonce> getAnnoncesForEmploye(Long employeId);

    public abstract List<Annonce> getImportantAnnonces();

    public abstract void notifyUsersAboutAnnonce(Annonce annonce);
}
