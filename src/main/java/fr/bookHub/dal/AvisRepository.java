package fr.bookHub.dal;

import fr.bookHub.bo.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AvisRepository extends JpaRepository<Avis, Integer> {
    /**
     * Vérifie si un utilisateur a déjà laissé un avis sur un livre
     * création ou modification
     */
    Optional<Avis> findByLivreIdAndUtilisateurId(Integer livreId, Integer utilisateurId);

    /**
     * Récupère tous les avis d'un livre
     */
    List<Avis> findByLivreIdOrderByDatePublicationDesc(Integer livreId);

    /**
     * Calcule la note moyenne d'un livre
     */
    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.livre.id = :livreId")
    Double findAverageNoteByLivreId(Integer livreId);

    /**
     * Nombre d'avis pour un livre
     */
    long countByLivreId(Integer livreId);

    /**
     * Tous les avis d'un utilisateur
     */
    List<Avis> findByUtilisateurIdOrderByDatePublicationDesc(Integer utilisateurId);

    /**
     * Suppression de l'avis par l'id du livre er de l'utilisateur
     */
    void deleteByLivreIdAndUtilisateurId(Integer livreId, Integer utilisateurId);


    /**
     * L'avis existe-t-il par l'id du livre de l'utilisateur
     */
    boolean existsByLivreIdAndUtilisateurId(Integer livreId, Integer utilisateurId);
}

