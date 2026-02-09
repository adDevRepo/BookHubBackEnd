package fr.bookHub.controller;

import fr.bookHub.bll.ReservationService;
import fr.bookHub.bll.UtilisateurService;
import fr.bookHub.bo.Reservation;
import fr.bookHub.bo.Utilisateur;
import fr.bookHub.dto.ReservationDTO;
import fr.bookHub.dto.ReservationRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UtilisateurService utilisateurService;

    // --- ACCESSIBLE À TOUS LES UTILISATEURS CONNECTÉS ---

    /**
     * 1. CRÉER UNE RÉSERVATION
     * POST /api/reservations
     */
    @PostMapping
    public ResponseEntity<ReservationDTO> creerReservation(@RequestBody @Valid ReservationRequest request) {
        Utilisateur currentUser = getUtilisateurConnecte();
        try {
            Reservation r = reservationService.creerReservation(currentUser.getId(), request.livreId());
            return ResponseEntity.status(HttpStatus.CREATED).body(ReservationDTO.fromEntity(r));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /**
     * 2. VOIR MES RÉSERVATIONS (Utilisateur connecté)
     * GET /api/reservations/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<ReservationDTO>> getMesReservations() {
        Utilisateur currentUser = getUtilisateurConnecte();
        return ResponseEntity.ok(
                reservationService.getReservationsUtilisateur(currentUser.getId())
                        .stream().map(ReservationDTO::fromEntity).toList()
        );
    }

    /**
     * 4. ANNULER MA RÉSERVATION
     * PATCH /api/reservations/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationDTO> annulerMaReservation(@PathVariable Integer id) {
        Utilisateur currentUser = getUtilisateurConnecte();
        try {
            // Note: Le service vérifie déjà que c'est bien MA réservation
            Reservation r = reservationService.annulerReservation(id, currentUser.getId());
            return ResponseEntity.ok(ReservationDTO.fromEntity(r));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    // --- RÉSERVÉ AUX ADMINS ---

    /**
     * SUPPRESSION DÉFINITIVE (Hard Delete)
     * Attention : Supprime la ligne de la BDD. Pas d'historique.
     */
    @PreAuthorize("hasRole('ADMIN')") // SÉCURITÉ ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerReservation(@PathVariable Integer id) {
        try {
            reservationService.supprimerDefinitivement(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation introuvable");
        }
    }

    /**
     * TRAITEMENT DES EXPIRATIONS
     * Tâche de maintenance
     */
    @PreAuthorize("hasRole('ADMIN')") // SÉCURITÉ ADMIN
    @PostMapping("/process-expired/{livreId}")
    public ResponseEntity<String> traiterExpirations(@PathVariable Integer livreId) {
        int count = reservationService.traiterReservationsExpirees(livreId);
        return ResponseEntity.ok(count + " réservations traitées.");
    }

    /**
     * VOIR TOUTES LES RÉSERVATIONS (Global)
     * Optionnel : Si l'admin veut voir tout ce qui se passe
     */
    @PreAuthorize("hasRole('ADMIN')") // SÉCURITÉ ADMIN
    @GetMapping("/all")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        // Tu devras ajouter findAll() dans ton service si tu veux cette méthode
        // return ...
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * VOIR LA FILE D'ATTENTE (ADMIN SEULEMENT)
     * Permet de gérer les priorités ou de contacter les gens.
     */
    @PreAuthorize("hasRole('ADMIN')") // SÉCURITÉ ADMIN
    @GetMapping("/book/{livreId}")
    public ResponseEntity<List<ReservationDTO>> getFileAttente(@PathVariable Integer livreId) {
        List<Reservation> list = reservationService.getFileAttenteLivre(livreId);

        return ResponseEntity.ok(list.stream()
                .map(ReservationDTO::fromEntity)
                .toList());
    }


    // --- Helper ---
    private Utilisateur getUtilisateurConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return utilisateurService.consulterParEmail(auth.getName());
    }
}