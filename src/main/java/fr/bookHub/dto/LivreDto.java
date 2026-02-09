package fr.bookHub.dto;

import fr.bookHub.bo.Livre;
import fr.bookHub.bo.Categorie;

public record LivreDto(
        Integer id,
        String titre,
        String auteur,
        String isbn,
        String description,
        String urlCouverture,
        Integer exemplairesTotal,
        Integer exemplairesDispo,
        Boolean actif,
        Integer categorieId,
        String categorieNom
) {

    /* =======================
       ENTITY -> DTO (SORTIE)
       ======================= */
    public static LivreDto fromEntity(Livre livre) {
        if (livre == null) return null;

        return new LivreDto(
                livre.getId(),
                livre.getTitre(),
                livre.getAuteur(),
                livre.getIsbn(),
                livre.getDescription(),
                livre.getUrlCouverture(),
                livre.getExemplairesTotal(),
                livre.getExemplairesDispo(),
                livre.getActif(),
                livre.getCategorie() != null ? livre.getCategorie().getId() : null,
                livre.getCategorie() != null ? livre.getCategorie().getNom() : null
        );
    }

    /* =======================
       DTO -> ENTITY (ENTRÉE)
       ======================= */
    public Livre toEntity() {
        Livre livre = new Livre();

        livre.setId(this.id);
        livre.setTitre(this.titre);
        livre.setAuteur(this.auteur);
        livre.setIsbn(this.isbn);
        livre.setDescription(this.description);
        livre.setUrlCouverture(this.urlCouverture);
        livre.setExemplairesTotal(this.exemplairesTotal);
        livre.setExemplairesDispo(this.exemplairesDispo);
        livre.setActif(this.actif);

        // ⚠ On ne charge PAS la catégorie ici
        // On prépare juste l'ID pour le service
        if (this.categorieId != null) {
            Categorie categorie = new Categorie();
            categorie.setId(this.categorieId);
            livre.setCategorie(categorie);
        }

        return livre;
    }
}
