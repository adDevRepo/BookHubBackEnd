package fr.bookHub.bo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = { "id" })
@ToString

@Entity
@Table(name = "BOOKHUB_REVIEW", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "book_id"}) // contrainte d'une unicité en BDD pour assurer un seul avis/livre/utilisateur
})
public class Avis implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer id;

    @Column(name = "rating", nullable = false)
    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimum est 1")
    @Max(value = 5, message = "La note maximum est 5")
    private Integer note; // De 1 à 5

    // Texte long supporté par SQL Server
    @Column(name = "comment", columnDefinition = "VARCHAR(MAX)")
    @Size(max = 2000, message = "Le commentaire est trop long (max 2000 caractères)")
    private String commentaire;

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime datePublication;

    // --- RELATIONS ---

    @ManyToOne(fetch = FetchType.LAZY) // Lazy ici : On n'a pas toujours besoin de tout l'user pour afficher un avis
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "L'avis doit être lié à un utilisateur")
    @ToString.Exclude // Evite boucle infinie si on affiche l'avis
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @NotNull(message = "L'avis doit être lié à un livre")
    @ToString.Exclude
    private Livre livre;

    @PrePersist
    public void prePersist() {
        if (this.datePublication == null) {
            this.datePublication = LocalDateTime.now();
        }
    }
}