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
@Table(name = "BOOKHUB_BOOK")
public class Livre implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer id;

    @Column(name = "title", nullable = false)
    private String titre;

    @Column(name = "author", nullable = false)
    private String auteur;

    @Column(name = "isbn", unique = true, length = 20)
    private String isbn;

    @Column(name = "cover_url", length = 500)
    private String urlCouverture;

    // IMPORTANT : Pour SQL Server, cela crée un VARCHAR(MAX)
    // Permet de stocker un résumé très long sans limite de 255 caractères
    @Column(name = "description", columnDefinition = "VARCHAR(MAX)")
    private String description;

    // Valeur par défaut 1, mais sera écrasée si le front envoie 10
    @Builder.Default
    @Column(name = "total_copies", nullable = false)
    private Integer exemplairesTotal = 1;

    // À la création, le livre n'est pas encore emprunté, donc 1 dispo
    @Builder.Default
    @Column(name = "available_copies", nullable = false)
    private Integer exemplairesDispo = 1;

    // On initialise à 1 par sécurité, mais la méthode ci-dessous va le corriger
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean actif = true; // Par défaut, un livre créé est actif

    // --- RELATIONS ---

    // Plusieurs livres peuvent avoir la même catégorie (Many-To-One)
    // FetchType.EAGER : On veut afficher la catégorie direct sur la fiche livre
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
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