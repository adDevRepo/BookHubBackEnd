package fr.bookHub.bo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
@Table(name = "BOOKHUB_BOOK")
public class Livre implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer id;

    @Column(name = "title", nullable = false)
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne doit pas dépasser 255 caractères")
    private String titre;

    @Column(name = "author", nullable = false)
    @NotBlank(message = "L'auteur est obligatoire")
    @Size(max = 255, message = "Le nom de l'auteur ne doit pas dépasser 255 caractères")
    private String auteur;

    @Column(name = "isbn", unique = true, length = 20)
    @NotBlank(message = "L'ISBN est obligatoire")
    // Regex : Chiffres, tirets, et éventuellement 'X' à la fin (ISBN-10)
    /*@Pattern(
            regexp = "^[0-9-X]{10,20}$",
            message = "Format ISBN invalide (chiffres, tirets et 'X' uniquement)"
    )*/
    private String isbn;

    @Column(name = "cover_url", length = 500)
    @Size(max = 500, message = "L'URL ne doit pas dépasser 500 caractères")
    private String urlCouverture;

    // IMPORTANT : Pour SQL Server, cela crée un VARCHAR(MAX)
    // Permet de stocker un résumé très long sans limite de 255 caractères
    @Column(name = "description", columnDefinition = "VARCHAR(MAX)")
    @NotBlank(message = "La description est obligatoire")
    private String description;

    // Valeur par défaut 1, mais sera écrasée si le front envoie 10
    @Builder.Default
    @Column(name = "total_copies", nullable = false)
    @NotNull(message = "Le nombre d'exemplaires est obligatoire")
    @Min(value = 1, message = "Il faut au moins 1 exemplaire pour créer le livre")
    private Integer exemplairesTotal = 1;

    // À la création, le livre n'est pas encore emprunté, donc 1 disponible
    @Builder.Default
    @Column(name = "available_copies", nullable = false)
    @NotNull(message = "Le nombre d'exemplaires disponibles est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif") // 0 est autorisé (rupture de stock)
    private Integer exemplairesDispo = 1;

    // On initialise à 1 par sécurité, mais la méthode ci-dessous va le corriger
    @Builder.Default
    @Column(name = "active", nullable = false)
    @NotNull
    private Boolean actif = true; // Par défaut, un livre créé est actif

    // --- RELATIONS ---

    // Plusieurs livres peuvent avoir la même catégorie (Many-To-One)
    // FetchType.EAGER : On veut afficher la catégorie direct sur la fiche livre
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "La catégorie est obligatoire")
    private Categorie categorie;

    /**
     * LOGIQUE DE SYNCHRONISATION :
     * Avant d'insérer un NOUVEAU livre en base (Persist),
     * on s'assure que le nombre de dispo est égal au total.
     */
    @PrePersist
    public void prePersist() {
        // Si on crée un livre avec 10 exemplaires, on a forcément 10 dispos au départ.
        // Cette logique s'applique si dispo n'a pas été forcé manuellement à autre chose.
        if (this.exemplairesDispo == null || this.exemplairesDispo.equals(1)) {
            this.exemplairesDispo = this.exemplairesTotal;
        }
    }
}