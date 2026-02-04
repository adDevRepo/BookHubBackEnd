package fr.bookHub.bo;

import fr.bookHub.bo.enums.StatutReservation;
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
@Table(name = "BOOKHUB_RESERVATION")
public class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Integer id;

    // 1. Début : Quand l'utilisateur a cliqué sur "Réserver"
    @Column(name = "request_date", nullable = false)
    private LocalDateTime dateDemande;

    // 2. Milieu : Quand le livre est revenu en rayon (Statut passe à DISPONIBLE)
    // Permet de calculer : "Vous avez jusqu'au [Date + 48h] pour venir chercher le livre"
    @Column(name = "availability_date")
    private LocalDateTime dateDisponibilite;

    // 3. Fin : Quand la réservation est terminée (Statut passe à ANNULEE ou TERMINEE)
    // Utile pour l'historique et les statistiques
    @Column(name = "closing_date")
    private LocalDateTime dateCloture;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatutReservation statut;

    @Column(name = "priority_rank")
    private Integer rangPriorite;

    // --- RELATIONS ---

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Livre livre;

    @PrePersist
    public void prePersist() {
        if (this.dateDemande == null) {
            this.dateDemande = LocalDateTime.now();
        }
        if (this.statut == null) {
            this.statut = StatutReservation.EN_ATTENTE;
        }
    }
}