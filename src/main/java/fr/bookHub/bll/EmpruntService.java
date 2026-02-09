package fr.bookHub.bll;

import fr.bookHub.bo.Emprunt;

import java.util.List;

public interface EmpruntService {

    /**
     * Enregistre un nouvel emprunt pour un utilisateur.
     * Règles à vérifier :
     * 1. L'utilisateur existe.
     * 2. Le livre est disponible (stock > 0).
     * 3. L'utilisateur n'a pas dépassé son quota (ex: max 3 livres).
     */
    Emprunt emprunterLivre(Integer utilisateurId, Integer livreId);

    /**
     * Valide le retour d'un livre.
     * Met à jour la date de retour effective et libère du stock.
     */
    Emprunt retournerLivre(Integer empruntId);

    /**
     * Liste tous les emprunts EN COURS d'un utilisateur.
     * (Ceux qui n'ont pas encore été rendus).
     */
    List<Emprunt> consulterEmpruntsEnCours(Integer utilisateurId);

    /**
     * Historique complet des emprunts d'un utilisateur (Passés et présents).
     */
    List<Emprunt> consulterHistoriqueUtilisateur(Integer utilisateurId);

    /**
     * Récupère tous les emprunts actuellement en retard.
     * (Date de retour prévue < Date du jour ET Date retour effective est null).
     * Utile pour les bibliothécaires.
     */
    List<Emprunt> consulterEmpruntsEnRetard();
}