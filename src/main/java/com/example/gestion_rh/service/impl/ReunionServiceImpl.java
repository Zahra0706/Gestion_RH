package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Reunion;
import com.example.gestion_rh.repository.ReunionRepository;
import com.example.gestion_rh.service.ReunionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReunionServiceImpl implements ReunionService {

    private final ReunionRepository reunionRepository;

    @Override
    public List<Reunion> getReunionsVisiblesPourEmploye(Long employeId, Long departementId) {

        List<Reunion> result = new ArrayList<>();

        // 1) Réunions où l'employé est explicitement participant
        if (employeId != null) {
            result.addAll(reunionRepository.findByParticipantId(employeId));
        }

        // 2) Réunions du département de l'employé
        if (departementId != null) {
            result.addAll(reunionRepository.findByDepartementId(departementId));
        }

        // 3) Réunions générales (sans département)
        result.addAll(reunionRepository.findByDepartementIsNullOrderByDateHeureDebutDesc());

        // Déduplication + tri par date de début (plus proche en premier)
        Map<Long, Reunion> unique = new LinkedHashMap<>();
        for (Reunion r : result) {
            if (r != null && r.getId() != null) {
                unique.put(r.getId(), r);
            }
        }

        return unique.values().stream()
                .sorted(Comparator.comparing(Reunion::getDateHeureDebut, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }
}
