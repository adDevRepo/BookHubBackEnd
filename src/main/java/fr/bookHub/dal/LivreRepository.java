package fr.bookHub.dal;

import fr.bookHub.bo.Livre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LivreRepository extends JpaRepository<Livre, Integer> {

    // US-BOOK-03 : Recherche simple par Titre ou Auteur
    // Ex: findByTitreContainingIgnoreCase("harry") -> Trouve "Harry Potter"
    List<Livre> findByTitreContainingIgnoreCase(String titre);

    // US-BOOK-03 : Filtrer par catégorie
    // Spring navigue dans la relation : Livre -> Categorie -> Id
    Page<Livre> findByCategorieId(Integer categoryId, Pageable pageable);

    // RECHERCHE AVANCÉE (Optionnelle mais puissante)
    // Si l'utilisateur tape un mot clé, on cherche dans Titre OU Auteur OU ISBN
    @Query("SELECT l FROM Livre l WHERE " +
            "LOWER(l.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(l.auteur) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "l.isbn LIKE CONCAT('%', :keyword, '%')")
    Page<Livre> rechercherLivres(@Param("keyword") String keyword, Pageable pageable);
}