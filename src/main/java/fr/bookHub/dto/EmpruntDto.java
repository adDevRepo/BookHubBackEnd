package fr.bookHub.dto;

import fr.bookHub.bo.Emprunt;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EmpruntDto(
        Integer id,

        // sortie only (générée par le back)
        LocalDate dateEmprunt,
        LocalDate dateRetourPrevue,
        LocalDate dateRetourReel,
        String statut,

        @NotNull(message = "L'utilisateur est obligatoire")
        Integer utilisateurId,

        // sortie only
        String utilisateurNom,

        @NotNull(message = "Le livre est obligatoire")
        Integer livreId,

        // sortie only
        String livreTitre
) {

    public static EmpruntDto fromEntity(Emprunt e) {
        if (e == null) return null;

        return new EmpruntDto(
                e.getId(),
                e.getDateEmprunt(),
                e.getDateRetourPrevue(),
                e.getDateRetourReel(),
                e.getStatut() != null ? e.getStatut().name() : null,
                e.getUtilisateur() != null ? e.getUtilisateur().getId() : null,
                e.getUtilisateur() != null ? e.getUtilisateur().getNom() + " " + e.getUtilisateur().getPrenom() : null,
                e.getLivre() != null ? e.getLivre().getId() : null,
                e.getLivre() != null ? e.getLivre().getTitre() : null
        );
    }
}
