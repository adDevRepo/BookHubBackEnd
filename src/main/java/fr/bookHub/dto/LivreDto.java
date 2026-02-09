package fr.bookHub.dto;

import fr.bookHub.bo.Categorie;
import fr.bookHub.bo.Livre;
import jakarta.validation.constraints.*;

public record LivreDto(

        Integer id,

        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 255, message = "Le titre ne doit pas dépasser 255 caractères")
        String titre,

        @NotBlank(message = "L'auteur est obligatoire")
        @Size(max = 255, message = "Le nom de l'auteur ne doit pas dépasser 255 caractères")
        String auteur,

        @NotBlank(message = "L'ISBN est obligatoire")
        @Size(max = 20, message = "L'ISBN ne doit pas dépasser 20 caractères")
        String isbn,

        @NotBlank(message = "La description est obligatoire")
        String description,

        @Size(max = 500, message = "L'URL de la couverture ne doit pas dépasser 500 caractères")
        String urlCouverture,

        @NotNull(message = "Le nombre total d'exemplaires est obligatoire")
        @Min(value = 1, message = "Il faut au moins 1 exemplaire")
        Integer exemplairesTotal,

        @Min(value = 0, message = "Le nombre d'exemplaires disponibles ne peut pas être négatif")
        Integer exemplairesDispo,

        @NotNull(message = "Le statut actif est obligatoire")
        Boolean actif,

        @NotNull(message = "La catégorie est obligatoire")
        Integer categorieId,

        // champ de sortie uniquement → pas de validation
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
        // Le service se charge de résoudre l'entité
        if (this.categorieId != null) {
            Categorie categorie = new Categorie();
            categorie.setId(this.categorieId);
            livre.setCategorie(categorie);
        }

        return livre;
    }
}
