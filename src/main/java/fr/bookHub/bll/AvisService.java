package fr.bookHub.bll;

import fr.bookHub.bo.Avis;
import fr.bookHub.bo.Reservation;
import fr.bookHub.bo.Utilisateur;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AvisService {

    /**
     * Ajouter ou modifier un avis pour un livre
     */


    Avis saveAvis(Integer livreId, Integer utilisateurId, int note, String commentaire);


    Avis updateAvis(Integer avisId, int note, String commentaire);


    void deleteAvisById(Integer avisId);

    /**
     * Moyenne des notes d'un livre
     */
    Double getAverageNoteByLivre(Integer livreId);


    /**
     * Nombre d'avis pour un livre
     */
    long countAvisByLivre(Integer livreId);

    /**
     * Tous les avis d'un utilisateur (triés par date)
     */
    List<Avis> getAvisByUtilisateur(Integer utilisateurId);

    /**
     * Tous les avis d'un livre (triés par date)
     */
    List<Avis> getAvisByLivre(Integer livreId);

}
