package fr.bookHub.bo;

import fr.bookHub.bo.enums.StatutEmprunt;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = { "id" })
@ToString

@Entity
@Table(name = "BOOKHUB_LOAN")
public class Emprunt implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Integer id;

    @Column(name = "loan_date", nullable = false)
    private LocalDate dateEmprunt;

    // Date limite théorique (Calculée : Date emprunt + 14 jours)
    @Column(name = "expected_return_date", nullable = false)
    private LocalDate dateRetourPrevue;

    // Date réelle du retour (Null tant que le livre n'est pas rendu)
    @Column(name = "actual_return_date")
    private LocalDate dateRetourReel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Le statut de l'emprunt est obligatoire")
    private StatutEmprunt statut;

    // --- RELATIONS ---

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "L'emprunt doit être lié à un utilisateur")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    @NotNull(message = "L'emprunt doit concerner un livre")
    private Livre livre;

    /**
     * Automatisation à la création :
     * 1. La date d'emprunt est aujourd'hui.
     * 2. Le statut est EN_COURS.
     * (La date de retour prévue sera gérée par le Service pour respecter la règle des 14 jours)
     */
    @PrePersist
    public void prePersist() {
        if (this.dateEmprunt == null) {
            this.dateEmprunt = LocalDate.now();
        }
        if (this.statut == null) {
            this.statut = StatutEmprunt.EN_COURS;
        }
        // Calcul automatique de la date de retour (J+14) si non fournie
        if (this.dateRetourPrevue == null && this.dateEmprunt != null) {
            this.dateRetourPrevue = this.dateEmprunt.plusDays(14);
        }
    }
}