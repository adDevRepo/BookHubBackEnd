package fr.bookHub.dal;

import fr.bookHub.bo.Reservation;
import fr.bookHub.bo.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    // 1. Récupérer les réservations d'un user
    List<Reservation> findByUtilisateurId(Integer userId);

    // 2. Récupérer la file d'attente (triée)
    List<Reservation> findByLivreIdAndStatutOrderByRangPrioriteAsc(Integer livreId, StatutReservation statut);

    // 3. Compter combien de personnes attendent (pour le rang)
    long countByLivreIdAndStatut(Integer livreId, StatutReservation statut);

    // --- NOUVELLES MÉTHODES OPTIMISÉES ---

    // 4. Vérifier si un user a déjà une réservation ACTIVE (En attente ou Disponible) pour ce livre
    // Cela évite de charger l'objet, on veut juste un boolean.
    boolean existsByUtilisateurIdAndLivreIdAndStatutIn(Integer userId, Integer livreId, List<StatutReservation> statuts);

    // 5. Trouver directement les réservations expirées via SQL
    // On cherche les réservations DISPONIBLE dont la date limite (dateDispo + 48h) est dépassée par 'now'
    // Mathématiquement : dateDispo < now - 48h
    @Query("SELECT r FROM Reservation r WHERE r.livre.id = :livreId AND r.statut = 'DISPONIBLE' AND r.dateDisponibilite < :dateLimite")
    List<Reservation> findExpiredReservations(@Param("livreId") Integer livreId, @Param("dateLimite") LocalDateTime dateLimite);
}