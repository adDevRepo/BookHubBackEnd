package fr.bookHub.dal;

import fr.bookHub.bo.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Integer> {

    /**
     * Vérifie si une catégorie existe déjà avec ce code
     * utilisé pour création et modification
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Vérifie si une catégorie existe déjà avec ce nom
     */
    boolean existsByNomIgnoreCase(String nom);

    /**
     * Recherche d'une catégorie par son code
     */
    Optional<Categorie> findByCodeIgnoreCase(String code);

    /**
     * Liste toutes les catégories triées par nom
     * utilisé pour les recherches par filtres
     */
    List<Categorie> findAllByOrderByNomAsc();

    /**
     * Utilitaire : récupère une catégorie par ID ou lève une exception claire
     */
    default Categorie getOrThrow(Integer id) {
        return findById(id).orElseThrow(() ->
                new IllegalArgumentException("Catégorie introuvable : " + id)
        );
    }
}
