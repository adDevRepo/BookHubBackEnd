package fr.bookHub.bo;

import jakarta.persistence.*;
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
    private String nom;

    // Le code technique stable (ex: "SCIFI")
    // Utile pour le front-end (icônes) ou les URLs
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;


    /**
     * ASTUCE : Normalisation des données.
     * Avant de sauvegarder, on transforme le code en MAJUSCULES
     * et on supprime les espaces inutiles.
     * Ex: " sci fi " devient "SCI_FI" ou "SCIFI" selon votre logique.
     */
    @PrePersist
    @PreUpdate
    public void normalizeCode() {
        if (this.code != null) {
            this.code = this.code.trim().toUpperCase();
        }
    }
}