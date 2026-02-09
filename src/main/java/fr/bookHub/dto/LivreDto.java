package fr.bookHub.dto;

import fr.bookHub.bo.Livre;

public record LivreDto(
        Integer id,
        String titre,
        String auteur,
        String isbn,
        Integer exemplairesTotal,
        Integer exemplairesDispo,
        Boolean actif,
        String categorie
) {

    public static LivreDto fromEntity(Livre livre) {
        if (livre == null) return null;

        return new LivreDto(
                livre.getId(),
                livre.getTitre(),
                livre.getAuteur(),
                livre.getIsbn(),
                livre.getExemplairesTotal(),
                livre.getExemplairesDispo(),
                livre.getActif(),
                livre.getCategorie() != null ? livre.getCategorie().getNom() : null
        );
    }
}
