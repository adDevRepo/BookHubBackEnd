package fr.bookHub.dal;

import fr.bookHub.bo.Reservation;
import fr.bookHub.bo.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    // Pour l'utilisateur : "Mes réservations"
    List<Reservation> findByUtilisateurId(Integer userId);

    // Pour le système : Trouver la file d'attente d'un livre spécifique
    // Trié par ordre de priorité (1, 2, 3...)
    List<Reservation> findByLivreIdAndStatutOrderByRangPrioriteAsc(Integer livreId, StatutReservation statut);

    // Pour calculer le rang d'une nouvelle réservation
    // "Combien de gens attendent déjà ce livre ?"
    long countByLivreIdAndStatut(Integer livreId, StatutReservation statut);
}