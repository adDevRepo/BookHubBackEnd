package fr.bookHub.dal;

import fr.bookHub.bo.Livre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LivreRepository extends JpaRepository<Livre, Integer> {

    // ✅ Vérification unicité ISBN (création)
    boolean existsByIsbn(String isbn);

    // ✅ Vérification unicité ISBN (modification : exclut le livre courant)
    boolean existsByIsbnAndIdNot(String isbn, Integer id);

    // US-BOOK-03 : Recherche simple par Titre
    List<Livre> findByTitreContainingIgnoreCase(String titre);

    // US-BOOK-03 : Filtrer par catégorie
    Page<Livre> findByCategorieId(Integer categoryId, Pageable pageable);

    // Recherche avancée : Titre OU Auteur OU ISBN
    @Query("SELECT l FROM Livre l WHERE " +
            "LOWER(l.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(l.auteur) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "l.isbn LIKE CONCAT('%', :keyword, '%')")
    Page<Livre> rechercherLivres(@Param("keyword") String keyword, Pageable pageable);
}
