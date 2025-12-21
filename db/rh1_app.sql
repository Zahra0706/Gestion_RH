-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3307
-- Généré le : dim. 21 déc. 2025 à 21:57
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `rh1_app`
--

-- --------------------------------------------------------

--
-- Structure de la table `annonces`
--

CREATE TABLE `annonces` (
  `id` bigint(20) NOT NULL,
  `contenu` varchar(2000) NOT NULL,
  `date_publication` datetime(6) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `type` enum('DEPARTEMENT','GENERALE','IMPORTANTE','INFORMATION') NOT NULL,
  `createur_id` bigint(20) NOT NULL,
  `departement_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `annonces`
--

INSERT INTO `annonces` (`id`, `contenu`, `date_publication`, `titre`, `type`, `createur_id`, `departement_id`) VALUES
(1, 'Bienvenue à Narine dans l’équipe 🎉', '2025-12-19 23:36:15.000000', 'Nouvel employé', 'INFORMATION', 4, 1),
(3, 'Ceci est une annonce réservée au département 2.', '2025-12-19 23:47:47.000000', 'Annonce Dept 2', 'DEPARTEMENT', 3, 2),
(12, 'yjukikk', '2025-12-21 13:56:06.000000', 'fdbgdsd', 'IMPORTANTE', 4, 1);

-- --------------------------------------------------------

--
-- Structure de la table `conges`
--

CREATE TABLE `conges` (
  `id` bigint(20) NOT NULL,
  `commentaire_manager` varchar(500) DEFAULT NULL,
  `commentairerh` varchar(500) DEFAULT NULL,
  `date_debut` date NOT NULL,
  `date_demande` datetime(6) DEFAULT NULL,
  `date_fin` date NOT NULL,
  `date_refus` datetime(6) DEFAULT NULL,
  `date_validation_manager` datetime(6) DEFAULT NULL,
  `motif` varchar(255) DEFAULT NULL,
  `motif_refus` varchar(500) DEFAULT NULL,
  `statut` enum('ANNULE','EN_ATTENTE_MANAGER','REFUSE_MANAGER','REFUSE_RH','VALIDE_MANAGER','VALIDE_RH') NOT NULL,
  `type_conge` enum('CONGE_MALADIE','CONGE_MATERNITE','CONGE_PATERNITE','CONGE_PAYE','CONGE_SANS_SOLDE','PAYE') NOT NULL,
  `employe_id` bigint(20) NOT NULL,
  `manager_validateur_id` bigint(20) DEFAULT NULL,
  `rh_validateur_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `conges`
--

INSERT INTO `conges` (`id`, `commentaire_manager`, `commentairerh`, `date_debut`, `date_demande`, `date_fin`, `date_refus`, `date_validation_manager`, `motif`, `motif_refus`, `statut`, `type_conge`, `employe_id`, `manager_validateur_id`, `rh_validateur_id`) VALUES
(2001, NULL, NULL, '2025-12-22', '2025-12-18 22:34:00.225604', '2025-12-24', NULL, NULL, 'Voyage', NULL, 'EN_ATTENTE_MANAGER', 'CONGE_PAYE', 3, NULL, NULL),
(2002, 'OK', NULL, '2025-12-10', '2025-12-18 22:34:00.225604', '2025-12-11', NULL, '2025-12-18 22:34:00.225604', 'RDV médical', NULL, 'VALIDE_MANAGER', 'CONGE_MALADIE', 4, 5, NULL),
(2003, 'Non', NULL, '2025-11-02', '2025-12-18 22:34:00.225604', '2025-11-04', '2025-12-18 22:34:00.225604', NULL, 'Famille', 'Période chargée', 'REFUSE_MANAGER', 'CONGE_PAYE', 4, 5, NULL),
(2004, 'OK', 'Validé RH', '2025-10-05', '2025-12-18 22:34:00.225604', '2025-10-06', NULL, '2025-12-18 22:34:00.225604', 'Congé', NULL, 'VALIDE_RH', 'CONGE_PAYE', 4, 5, 2),
(2005, 'OK', 'Refus RH', '2025-09-15', '2025-12-18 22:34:00.225604', '2025-09-16', '2025-12-18 22:34:00.225604', '2025-12-18 22:34:00.225604', 'Congé', 'Dossier incomplet', 'REFUSE_RH', 'CONGE_SANS_SOLDE', 4, 5, 2),
(2006, NULL, NULL, '2025-12-23', '2025-12-18 22:53:00.000000', '2025-12-24', NULL, '2025-12-20 20:52:13.000000', 'Voyage', NULL, 'VALIDE_MANAGER', 'CONGE_PAYE', 6, 4, NULL),
(2007, NULL, NULL, '2025-12-26', '2025-12-18 22:53:00.000000', '2025-12-27', '2025-12-20 21:07:57.000000', NULL, 'Famille', 'Car  kaygheyeb bzzzzf wmadarch la tache dyalo', 'REFUSE_MANAGER', 'CONGE_SANS_SOLDE', 7, 4, NULL),
(2008, NULL, NULL, '2025-12-30', '2025-12-18 22:53:00.000000', '2025-12-31', NULL, NULL, 'Maladie', NULL, 'EN_ATTENTE_MANAGER', 'CONGE_MALADIE', 8, NULL, NULL),
(2009, 'Deuxieme absence Ayatt LILI meme jour (test)', 'Validé RH', '2025-12-24', '2025-12-21 16:26:42.000000', '2025-12-24', NULL, '2025-12-21 16:26:42.000000', 'Test 2', NULL, 'VALIDE_RH', 'CONGE_PAYE', 6, 4, 2),
(2010, 'Conge Narine meme jour que Ayatt (test)', 'Validé RH', '2025-12-24', '2025-12-21 16:37:22.000000', '2025-12-24', NULL, '2025-12-21 16:37:22.000000', 'Test Narine', NULL, 'VALIDE_RH', 'CONGE_PAYE', 15, 4, 2);

-- --------------------------------------------------------

--
-- Structure de la table `contrats`
--

CREATE TABLE `contrats` (
  `id` bigint(20) NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date DEFAULT NULL,
  `heures_hebdomadaires` double DEFAULT NULL,
  `salaire_base` double NOT NULL,
  `type_contrat` varchar(255) NOT NULL,
  `employe_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `contrats`
--

INSERT INTO `contrats` (`id`, `date_debut`, `date_fin`, `heures_hebdomadaires`, `salaire_base`, `type_contrat`, `employe_id`) VALUES
(1, '2024-01-01', NULL, 44, 12000, 'CDI', 4),
(2, '2024-02-01', NULL, 44, 6000, 'CDI', 5),
(3, '2024-02-01', NULL, 44, 6000, 'CDI', 6),
(4, '2024-02-01', NULL, 44, 6000, 'CDI', 7),
(5, '2024-02-01', NULL, 44, 6000, 'CDI', 8),
(6, '2024-02-01', NULL, 44, 6000, 'CDI', 9),
(7, '2024-02-01', NULL, 44, 6000, 'CDI', 10),
(8, '2024-02-01', NULL, 44, 6000, 'CDI', 11),
(9, '2024-02-01', NULL, 44, 6000, 'CDI', 12),
(10, '2024-02-01', NULL, 44, 6000, 'CDI', 13),
(11, '2024-02-01', NULL, 44, 6000, 'CDI', 14),
(12, '2024-02-01', NULL, 44, 6000, 'CDI', 15);

-- --------------------------------------------------------

--
-- Structure de la table `departements`
--

CREATE TABLE `departements` (
  `id` bigint(20) NOT NULL,
  `code` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_actif` bit(1) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `manager_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `departements`
--

INSERT INTO `departements` (`id`, `code`, `description`, `is_actif`, `nom`, `manager_id`) VALUES
(1, 'IT', 'Département informatique', b'1', 'Technologie de l\'Information', 4),
(2, 'RH', 'Département des ressources humaines', b'1', 'Ressources Humaines', NULL),
(3, 'FIN', 'Département financier', b'1', 'Finance', NULL),
(4, 'MKT', 'Département marketing', b'1', 'Marketing', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `employes`
--

CREATE TABLE `employes` (
  `id` bigint(20) NOT NULL,
  `adresse` varchar(255) NOT NULL,
  `date_embauche` date NOT NULL,
  `date_naissance` date DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `is_compte_actif` bit(1) NOT NULL,
  `matricule` varchar(255) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `poste` enum('ADMIN','ANALYSTE','CHEF_PROJET','DESIGNER','DEVELOPPEUR','MANAGER','RH') NOT NULL,
  `premiere_connexion` bit(1) NOT NULL,
  `prenom` varchar(255) NOT NULL,
  `role` enum('ADMIN','EMPLOYE','MANAGER','RH','SUPER_ADMIN') NOT NULL,
  `telephone` varchar(255) DEFAULT NULL,
  `departement_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `employes`
--

INSERT INTO `employes` (`id`, `adresse`, `date_embauche`, `date_naissance`, `email`, `is_compte_actif`, `matricule`, `nom`, `password`, `poste`, `premiere_connexion`, `prenom`, `role`, `telephone`, `departement_id`) VALUES
(1, 'Siège social', '2025-12-18', NULL, 'superadmin@hr.com', b'1', 'SADM001', 'Super', '$2a$10$62G3VY4mZUb90sWY.TcKG.KKLXpQlItOmJRkDXxjZm5aeVZ0YhEoe', 'ADMIN', b'0', 'Admin', 'SUPER_ADMIN', NULL, NULL),
(2, 'Siège social', '2025-12-18', NULL, 'admin@hr.com', b'1', 'ADM001', 'Admin', '$2a$10$X6y1RAMn/67XdRhFELdq7eM5MqIHCse9sZBGvGe9SJntZkyUV9nq2', 'ADMIN', b'0', 'System', 'ADMIN', NULL, NULL),
(3, '456 Avenue RH', '2025-12-18', NULL, 'rh@test.com', b'1', 'RH001', 'CHOKRI', '$2a$10$QJd0AdlWeI1OptVaRfvIWemIDDdCkdQnEUgHsLBnJ8Oe45uyDiHbC', 'RH', b'1', 'Zahra', 'RH', NULL, 2),
(4, '789 Boulevard Manager', '2025-12-18', NULL, 'manager@test.com', b'1', 'MGR001', 'ES-SAID', '$2a$10$vfEg0XL8hAevbIvLMIS/tuXnm0Ccr/VN0LxV/TIBuz8H9jWnUkPiu', 'MANAGER', b'1', 'SALMA', 'MANAGER', NULL, 1),
(5, '123 Rue Test', '2025-12-18', NULL, 'employe@test.com', b'1', 'EMP001', 'RAZZOUK', '$2a$10$329gNWpaBiLW37zqjCdIOuAFLipY.IUZ.doJDnHsG8OKUE.8DB/au', 'DEVELOPPEUR', b'1', 'MAJDA', 'EMPLOYE', NULL, 1),
(6, 'Dept 1', '2025-12-18', NULL, 'ayatt@test.com', b'1', 'EMP002', 'LILI', '$2a$10$testtesttesttesttesttesttesttesttest', 'DEVELOPPEUR', b'1', 'Ayatt', 'EMPLOYE', NULL, 1),
(7, 'Dept 1', '2025-12-18', NULL, 'abir@test.com', b'1', 'EMP003', 'MOSSAIF', '$2a$10$testtesttesttesttesttesttesttesttest', 'DEVELOPPEUR', b'1', 'Abir', 'EMPLOYE', NULL, 1),
(8, 'Dept 1', '2025-12-18', NULL, 'hanane@test.com', b'1', 'EMP004', 'OUCHEN', '$2a$10$testtesttesttesttesttesttesttesttest', 'DEVELOPPEUR', b'1', 'Hanane', 'EMPLOYE', NULL, 1),
(9, 'Dept 1', '2025-12-18', NULL, 'mohamed@test.com', b'1', 'EMP005', 'MILI', '$2a$10$testtesttesttesttesttesttesttesttest', 'DEVELOPPEUR', b'1', 'Zakaria', 'EMPLOYE', NULL, 1),
(10, 'Dept 1', '2025-12-18', NULL, 'mustapha@test.com', b'1', 'EMP006', 'NATIF', '$2a$10$testtesttesttesttesttesttesttesttest', 'DEVELOPPEUR', b'1', 'Isam', 'EMPLOYE', NULL, 1),
(11, 'Dept 1', '2025-12-18', NULL, 'zineb@test.com', b'1', 'EMP007', 'ELHALA', '$2a$10$testtesttesttesttesttesttesttesttest', 'DEVELOPPEUR', b'1', 'Zineb', 'EMPLOYE', NULL, 1),
(12, 'Dept 1', '2025-12-18', NULL, 'hassnae@test.com', b'1', 'EMP008', 'BELAASRI', '$2a$10$testtesttesttesttesttesttesttesttest', 'DEVELOPPEUR', b'1', 'Hassnae', 'EMPLOYE', NULL, 1),
(13, 'Dept 1', '2025-12-18', NULL, 'sanae@test.com', b'1', 'EMP009', 'AMAZIGH', '$2a$10$testtesttesttesttesttesttesttesttest', 'DEVELOPPEUR', b'1', 'Sanae', 'EMPLOYE', NULL, 1),
(14, 'Dept 1', '2025-12-18', NULL, 'ali@test.com', b'1', 'EMP010', 'BATI', '$2a$10$testtesttesttesttesttesttesttesttest', 'DEVELOPPEUR', b'1', 'Ali', 'EMPLOYE', NULL, 1),
(15, 'Adresse test', '2025-12-19', NULL, 'narine@test.com', b'1', 'EMP010', 'NARINE', '$2a$10$329gNWpaBiLW37zqjCdIOuAFLipY.IUZ.doJDnHsG8OKUE.8DB/au', 'DEVELOPPEUR', b'1', 'Narine', 'EMPLOYE', NULL, 1);

-- --------------------------------------------------------

--
-- Structure de la table `evaluations`
--

CREATE TABLE `evaluations` (
  `id` bigint(20) NOT NULL,
  `annee` int(11) NOT NULL,
  `commentaires_manager` varchar(1000) DEFAULT NULL,
  `commentairesrh` varchar(1000) DEFAULT NULL,
  `date_completion` date DEFAULT NULL,
  `date_creation` date NOT NULL,
  `score_competences` double NOT NULL,
  `score_global` double NOT NULL,
  `score_performance` double NOT NULL,
  `statut` enum('ARCHIVEE','COMPLETEE','EN_ATTENTE','EN_COURS') NOT NULL,
  `employe_id` bigint(20) NOT NULL,
  `manager_id` bigint(20) DEFAULT NULL,
  `rh_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `evaluations`
--

INSERT INTO `evaluations` (`id`, `annee`, `commentaires_manager`, `commentairesrh`, `date_completion`, `date_creation`, `score_competences`, `score_global`, `score_performance`, `statut`, `employe_id`, `manager_id`, `rh_id`) VALUES
(4, 2025, NULL, NULL, NULL, '2025-12-21', 0, 0, 0, 'EN_ATTENTE', 1, 2, NULL),
(5, 2025, NULL, NULL, NULL, '2025-12-21', 0, 0, 0, 'EN_ATTENTE', 1, 4, NULL),
(6, 2025, 'Great', NULL, '2025-12-21', '2025-12-21', 19, 16, 13, 'COMPLETEE', 6, 4, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `reunions`
--

CREATE TABLE `reunions` (
  `id` bigint(20) NOT NULL,
  `date_heure_debut` datetime(6) NOT NULL,
  `date_heure_fin` datetime(6) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `lieu` varchar(255) DEFAULT NULL,
  `titre` varchar(255) NOT NULL,
  `createur_id` bigint(20) NOT NULL,
  `departement_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `reunions`
--

INSERT INTO `reunions` (`id`, `date_heure_debut`, `date_heure_fin`, `description`, `lieu`, `titre`, `createur_id`, `departement_id`) VALUES
(1, '2025-12-20 10:00:00.000000', '2025-12-20 11:00:00.000000', 'Réunion hebdomadaire', 'Salle A', 'Weekly meeting', 4, 1),
(5, '2025-12-25 21:33:00.000000', '2025-12-25 22:33:00.000000', 'azadezawqwqsxsx', 'Salle C', 'zdcsqx', 4, 1),
(6, '2025-12-19 18:11:00.000000', '2025-12-19 19:12:00.000000', 'qddqd', 'https://meet.google.com/eno-zota-cig', 'zdcsqx', 4, 1);

-- --------------------------------------------------------

--
-- Structure de la table `reunion_participants`
--

CREATE TABLE `reunion_participants` (
  `reunion_id` bigint(20) NOT NULL,
  `employe_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `reunion_participants`
--

INSERT INTO `reunion_participants` (`reunion_id`, `employe_id`) VALUES
(5, 4),
(5, 5),
(5, 6),
(5, 7),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(6, 4),
(6, 5),
(6, 7);

-- --------------------------------------------------------

--
-- Structure de la table `salaires`
--

CREATE TABLE `salaires` (
  `id` bigint(20) NOT NULL,
  `chemin_fichierpdf` varchar(255) DEFAULT NULL,
  `date_paiement` date NOT NULL,
  `mois_annee` varchar(255) NOT NULL,
  `primes` double NOT NULL,
  `retenues` double NOT NULL,
  `salaire_brut` double NOT NULL,
  `salaire_net` double NOT NULL,
  `employe_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `salaires`
--

INSERT INTO `salaires` (`id`, `chemin_fichierpdf`, `date_paiement`, `mois_annee`, `primes`, `retenues`, `salaire_brut`, `salaire_net`, `employe_id`) VALUES
(1, '/uploads/salaire_emp1_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 1),
(2, '/uploads/salaire_ayatt_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 1),
(4, '/uploads/salaire_emp1_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 5),
(5, '/uploads/salaire_emp2_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 6),
(6, '/uploads/salaire_emp3_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 7),
(7, '/uploads/salaire_emp4_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 8),
(8, '/uploads/salaire_emp5_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 9),
(9, '/uploads/salaire_emp6_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 10),
(10, '/uploads/salaire_emp7_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 11),
(11, '/uploads/salaire_emp8_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 12),
(12, '/uploads/salaire_emp9_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 13),
(13, '/uploads/salaire_emp10_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 14),
(14, '/uploads/salaire_emp11_dec2025.pdf', '2025-12-20', '12-2025', 500, 100, 6000, 5400, 15),
(15, '/uploads/salaire_manager_dec2025.pdf', '2025-12-20', '12-2025', 1000, 200, 8000, 7800, 4);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `annonces`
--
ALTER TABLE `annonces`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKppstv162rkabrqsl74wv4eu36` (`createur_id`),
  ADD KEY `FKr7qx8h78k89oks2njxp7asxbc` (`departement_id`);

--
-- Index pour la table `conges`
--
ALTER TABLE `conges`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKfcmv18ndb20r5k4e9jajd929i` (`employe_id`),
  ADD KEY `FKs84dspmivhl3jvaftx5m0rwlr` (`manager_validateur_id`),
  ADD KEY `FKkvn3x82aoix69iwu6kmd5iu9i` (`rh_validateur_id`);

--
-- Index pour la table `contrats`
--
ALTER TABLE `contrats`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK5ul4t1udft41nmxxe9r83s2if` (`employe_id`);

--
-- Index pour la table `departements`
--
ALTER TABLE `departements`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKcggyqk74g1naphihefb9vj1b7` (`code`),
  ADD KEY `FKklakp5d6eb08nllqgyiwg0rh` (`manager_id`);

--
-- Index pour la table `employes`
--
ALTER TABLE `employes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK3v0uyo0bds0i1s553pjkiewvv` (`email`),
  ADD KEY `FKg6y4s69ena7cto8y7egtpqsi` (`departement_id`);

--
-- Index pour la table `evaluations`
--
ALTER TABLE `evaluations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKemf4pw9nsi3jlgv90xaijw7a0` (`employe_id`),
  ADD KEY `FK8956a4uhfb6yp24vv2c8ssfqw` (`manager_id`),
  ADD KEY `FKivgechqybkgn4wl37e09qdl0r` (`rh_id`);

--
-- Index pour la table `reunions`
--
ALTER TABLE `reunions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKg6mj15rjm3jl462uomjyt6bqr` (`createur_id`),
  ADD KEY `FKbk4pkfmtwwfjyk0ixan5ttre8` (`departement_id`);

--
-- Index pour la table `reunion_participants`
--
ALTER TABLE `reunion_participants`
  ADD KEY `FK2fwt6srj2rtebjw5hjduw5f6i` (`employe_id`),
  ADD KEY `FKp1it4jkqn29ar7ywhd9u95lwq` (`reunion_id`);

--
-- Index pour la table `salaires`
--
ALTER TABLE `salaires`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKhjlqweom7a2rgjfu9o0i46rkx` (`employe_id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `annonces`
--
ALTER TABLE `annonces`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT pour la table `conges`
--
ALTER TABLE `conges`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2011;

--
-- AUTO_INCREMENT pour la table `contrats`
--
ALTER TABLE `contrats`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT pour la table `departements`
--
ALTER TABLE `departements`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `employes`
--
ALTER TABLE `employes`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT pour la table `evaluations`
--
ALTER TABLE `evaluations`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `reunions`
--
ALTER TABLE `reunions`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `salaires`
--
ALTER TABLE `salaires`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `annonces`
--
ALTER TABLE `annonces`
  ADD CONSTRAINT `FKppstv162rkabrqsl74wv4eu36` FOREIGN KEY (`createur_id`) REFERENCES `employes` (`id`),
  ADD CONSTRAINT `FKr7qx8h78k89oks2njxp7asxbc` FOREIGN KEY (`departement_id`) REFERENCES `departements` (`id`);

--
-- Contraintes pour la table `conges`
--
ALTER TABLE `conges`
  ADD CONSTRAINT `FKfcmv18ndb20r5k4e9jajd929i` FOREIGN KEY (`employe_id`) REFERENCES `employes` (`id`),
  ADD CONSTRAINT `FKkvn3x82aoix69iwu6kmd5iu9i` FOREIGN KEY (`rh_validateur_id`) REFERENCES `employes` (`id`),
  ADD CONSTRAINT `FKs84dspmivhl3jvaftx5m0rwlr` FOREIGN KEY (`manager_validateur_id`) REFERENCES `employes` (`id`);

--
-- Contraintes pour la table `contrats`
--
ALTER TABLE `contrats`
  ADD CONSTRAINT `FKar44fligg64n20sqs2cjhyyn0` FOREIGN KEY (`employe_id`) REFERENCES `employes` (`id`);

--
-- Contraintes pour la table `departements`
--
ALTER TABLE `departements`
  ADD CONSTRAINT `FKklakp5d6eb08nllqgyiwg0rh` FOREIGN KEY (`manager_id`) REFERENCES `employes` (`id`);

--
-- Contraintes pour la table `employes`
--
ALTER TABLE `employes`
  ADD CONSTRAINT `FKg6y4s69ena7cto8y7egtpqsi` FOREIGN KEY (`departement_id`) REFERENCES `departements` (`id`);

--
-- Contraintes pour la table `evaluations`
--
ALTER TABLE `evaluations`
  ADD CONSTRAINT `FK8956a4uhfb6yp24vv2c8ssfqw` FOREIGN KEY (`manager_id`) REFERENCES `employes` (`id`),
  ADD CONSTRAINT `FKemf4pw9nsi3jlgv90xaijw7a0` FOREIGN KEY (`employe_id`) REFERENCES `employes` (`id`),
  ADD CONSTRAINT `FKivgechqybkgn4wl37e09qdl0r` FOREIGN KEY (`rh_id`) REFERENCES `employes` (`id`);

--
-- Contraintes pour la table `reunions`
--
ALTER TABLE `reunions`
  ADD CONSTRAINT `FKbk4pkfmtwwfjyk0ixan5ttre8` FOREIGN KEY (`departement_id`) REFERENCES `departements` (`id`),
  ADD CONSTRAINT `FKg6mj15rjm3jl462uomjyt6bqr` FOREIGN KEY (`createur_id`) REFERENCES `employes` (`id`);

--
-- Contraintes pour la table `reunion_participants`
--
ALTER TABLE `reunion_participants`
  ADD CONSTRAINT `FK2fwt6srj2rtebjw5hjduw5f6i` FOREIGN KEY (`employe_id`) REFERENCES `employes` (`id`),
  ADD CONSTRAINT `FKp1it4jkqn29ar7ywhd9u95lwq` FOREIGN KEY (`reunion_id`) REFERENCES `reunions` (`id`);

--
-- Contraintes pour la table `salaires`
--
ALTER TABLE `salaires`
  ADD CONSTRAINT `FKhjlqweom7a2rgjfu9o0i46rkx` FOREIGN KEY (`employe_id`) REFERENCES `employes` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
