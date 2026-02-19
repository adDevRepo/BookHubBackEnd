package fr.bookHub.dto;

import fr.bookHub.bo.Reservation;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ReservationDTO {

    public record Response(
        Integer id,
        String statut,         // Ex: "EN_ATTENTE"
        Integer rangPriorite,  // Ex: 1, 2... ou null
        LocalDateTime dateDemande,
        LocalDateTime dateDisponibilite,
        LocalDateTime dateCloture,

        // Infos simplifiées du Livre (pas besoin de tout l'objet Livre)
        Integer livreId,
        String livreTitre,

        // Infos simplifiées de l'Utilisateur (utile pour l'admin)
        Integer utilisateurId,
        String utilisateurNomComplet
    ) {
        public static Response fromEntity(Reservation r) {
            if (r == null) return null;

            return new Response(
                    r.getId(),
                    r.getStatut().name(),
                    r.getRangPriorite(),
                    r.getDateDemande(),
                    r.getDateDisponibilite(),
                    r.getDateCloture(),

                    // Gestion null-safe pour le livre
                    (r.getLivre() != null) ? r.getLivre().getId() : null,
                    (r.getLivre() != null) ? r.getLivre().getTitre() : "Livre inconnu",

                    // Gestion null-safe pour l'utilisateur
                    (r.getUtilisateur() != null) ? r.getUtilisateur().getId() : null,
                    (r.getUtilisateur() != null) ? r.getUtilisateur().getPrenom() + " " + r.getUtilisateur().getNom() : "Inconnu"
            );
        }
    }

    public record Request(
            @NotNull(message = "L'ID du livre est obligatoire")
            Integer livreId
    ) {}
}