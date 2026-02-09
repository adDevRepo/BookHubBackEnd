package fr.bookHub.dto;

import fr.bookHub.bo.Emprunt;

import java.time.LocalDate;

public record EmpruntDto(
        Integer id,
        LocalDate dateEmprunt,
        LocalDate dateRetourPrevue,
        LocalDate dateRetourReel,
        String statut,
        Integer utilisateurId,
        String utilisateurNom,
        Integer livreId,
        String livreTitre
) {

    public static EmpruntDto fromEntity(Emprunt e) {
        if (e == null) return null;

        return new EmpruntDto(
                e.getId(),
                e.getDateEmprunt(),
                e.getDateRetourPrevue(),
                e.getDateRetourReel(),
                e.getStatut().name(),
                e.getUtilisateur().getId(),
                e.getUtilisateur().getNom() + " " + e.getUtilisateur().getPrenom(),
                e.getLivre().getId(),
                e.getLivre().getTitre()
        );
    }
}
