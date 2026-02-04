package fr.bookHub.bo;

import jakarta.persistence.*;
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
@Table(name = "BOOKHUB_REVIEW")
public class Avis implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer id;

    @Column(name = "rating", nullable = false)
    private Integer note; // De 1 à 5

    // Texte long supporté par SQL Server
    @Column(name = "comment", columnDefinition = "VARCHAR(MAX)")
    private String commentaire;

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime datePublication;

    // --- RELATIONS ---

    @ManyToOne(fetch = FetchType.LAZY) // Lazy ici : On n'a pas toujours besoin de tout l'user pour afficher un avis
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude // Evite boucle infinie si on affiche l'avis
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @ToString.Exclude
    private Livre livre;

    @PrePersist
    public void prePersist() {
        if (this.datePublication == null) {
            this.datePublication = LocalDateTime.now();
        }
    }
}