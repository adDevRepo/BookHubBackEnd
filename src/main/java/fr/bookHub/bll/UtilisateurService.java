package fr.bookHub.bll;

import fr.bookHub.bo.Utilisateur;

import java.util.List;

public interface UtilisateurService {

    /**
     * Inscrit un nouvel utilisateur.
     * Gère le hachage du mot de passe et l'unicité de l'email.
     */
    Utilisateur creerUtilisateur(Utilisateur utilisateur);

    /**
     * Récupère un utilisateur par son email (pour le login).
     */
    Utilisateur consulterParEmail(String email);

    /**
     * Récupère tous les utilisateurs.
     */
    List<Utilisateur> consulterTous();

    /**
     * Supprime un utilisateur (vérifier les règles métier avant).
     */
    void supprimerUtilisateur(Integer id);
}