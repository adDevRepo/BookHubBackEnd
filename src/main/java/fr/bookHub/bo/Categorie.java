package fr.bookHub.bo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = { "id" })
@ToString

@Entity
@Table(name = "BOOKHUB_CATEGORY")
public class Categorie implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer id;

    // Le nom affiché à l'utilisateur (ex: "Science-Fiction")
    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    // Le code technique stable (ex: "SCIFI")
    // Utile pour le front-end (icônes) ou les URLs
    @Column(name = "code", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Le code catégorie est obligatoire")
    @Size(max = 50, message = "Le code ne doit pas dépasser 50 caractères")
    // Regex : Lettres (min/maj), chiffres et underscore uniquement.
    // Pas d'espaces, pas de caractères spéciaux bizarres.
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "Le code ne doit contenir que des lettres, des chiffres ou des underscores (ex: SCI_FI)"
    )
    private String code;


    /**
     * Normalisation des données.
     * NETTOYAGE AUTOMATIQUE
     * Avant de sauvegarder, on transforme le code en MAJUSCULES
     * et on supprime les espaces inutiles.
     * Ex: Transforme "sci fi" ou "Sci_Fi" en "SCI_FI" avant l'enregistrement.
     */
    @PrePersist
    @PreUpdate
    public void normalizeCode() {
        if (this.code != null) {
            // 1. On enlève les espaces autour
            // 2. On remplace les espaces internes par des underscores
            // 3. On met tout en majuscules
            this.code = this.code.trim().replace(" ", "_").toUpperCase();
        }
    }
}