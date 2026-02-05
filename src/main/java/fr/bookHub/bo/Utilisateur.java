package fr.bookHub.bo;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(of= { "id" })
@ToString

@Entity
@Table(name = "BOOKHUB_USER")
public class Utilisateur implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    @NotBlank(message = "L'email ne peut pas être vide")
    @Email(
            regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Format invalide (Caractères spéciaux interdits)"
    )
    private String email;

    @ToString.Exclude
    @Column(name = "password", nullable = false, length = 255)
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @NotNull(message = "L'utilisateur doit avoir un rôle assigné")
    private Role role;

    @Column(name = "phone_number", nullable = true, length = 20)
    @Pattern(
            regexp = "^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$",
            message = "Format de téléphone invalide (Ex: 0612345678 ou +336...)"
    )
    private String numTelephone;


    /**
     * Méthode exécutée automatiquement par JPA avant l'insertion en base.
     * Permet d'initialiser la date de création sans y penser.
     */
    @PrePersist
    public void prePersist() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
    }

}
