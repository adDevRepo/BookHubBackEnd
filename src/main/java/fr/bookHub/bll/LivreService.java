package fr.bookHub.bll;

import fr.bookHub.bo.Livre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LivreService {

    /**
     * Ajoute un nouveau livre au catalogue.
     * Doit vérifier si l'ISBN n'existe pas déjà.
     */
    Livre creerLivre(Livre livre);

    /**
     * Met à jour les infos d'un livre existant.
     */
    Livre modifierLivre(Integer id, Livre livre);

    /**
     * Récupère un livre par son ID.
     * Lance une exception si non trouvé.
     */
    Livre consulterParId(Integer id);

    /**
     * Liste tous les livres actifs avec pagination.
     * Exemple : page 1, taille 20, trié par titre.
     */
    Page<Livre> consulterTousActif(Pageable pageable);

    /**
     * Liste tous les livres avec pagination.
     * Exemple : page 1, taille 20, trié par titre.
     */
    Page<Livre> consulterTous(Pageable pageable);

    /**
     * Recherche des livres par mot-clé (Titre, Auteur, ISBN).
     * Utilise la méthode personnalisée du Repository.
     */
    Page<Livre> rechercher(String motCle, Pageable pageable);

    /**
     * Filtre les livres par catégorie.
     */
    Page<Livre> consulterParCategorie(Integer categorieId, Pageable pageable);

    /**
     * Supprime un livre (Admin seulement).
     * Doit vérifier qu'aucun exemplaire n'est actuellement emprunté.
     */
    void supprimerLivre(Integer id);
}