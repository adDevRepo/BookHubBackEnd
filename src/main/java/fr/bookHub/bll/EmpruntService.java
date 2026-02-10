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

    // Historique complet (fermés + en cours)
    List<Emprunt> getEmpruntsUtilisateur(Integer utilisateurId);

    // Seulement ceux en cours
    List<Emprunt> getEmpruntsEnCours(Integer utilisateurId);

    // Liste globale des retards (pour l'admin/bibliothécaire)
    List<Emprunt> getEmpruntsEnRetard();

    /**
     * Récupère uniquement les emprunts terminés (Statut RETOURNE).
     * C'est l'historique passif.
     */
    List<Emprunt> getEmpruntsTermines(Integer utilisateurId);
}