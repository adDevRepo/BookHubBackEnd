
package fr.bookHub.bll;

import fr.bookHub.bo.Reservation;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {

    Reservation creerReservation(Integer userId, Integer livreId);

    List<Reservation> getReservationsUtilisateur(Integer userId);

    List<Reservation> getFileAttenteLivre(Integer livreId);

    Reservation annulerReservation(Integer reservationId, Integer userId);

    /**
     * Appelé quand un exemplaire redevient disponible :
     * - passe la 1ère réservation EN_ATTENTE -> DISPONIBLE
     * - fixe dateDisponibilite = now
     */
    Reservation notifierRetourLivre(Integer livreId);

    /**
     * Confirme le retrait par l'utilisateur (réservation DISPONIBLE -> TERMINEE)
     */
    Reservation terminerReservation(Integer reservationId, Integer userId);

    /**
     * Permet de vérifier si la réservation est expirée (dateDisponibilite + 48h).
     */
    boolean estExpiree(Integer reservationId, LocalDateTime now);

    /**
     * Job/cron possible : annule les DISPONIBLE expirées et active la suivante.
     * Retourne le nombre de réservations traitées.
     */
    int traiterReservationsExpirees(Integer livreId);

    void supprimerDefinitivement(Integer reservationId);
}
