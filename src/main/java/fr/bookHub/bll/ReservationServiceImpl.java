package fr.bookHub.bll;

import fr.bookHub.bo.Reservation;
import fr.bookHub.bo.Utilisateur;
import fr.bookHub.bo.Livre;
import fr.bookHub.bo.enums.StatutEmprunt;
import fr.bookHub.bo.enums.StatutReservation;
import fr.bookHub.dal.EmpruntRepository;
import fr.bookHub.dal.LivreRepository;
import fr.bookHub.dal.ReservationRepository;
import fr.bookHub.dal.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j // Pour les logs
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private static final Duration DELAI_RETRAIT = Duration.ofHours(48);

    private final ReservationRepository reservationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final LivreRepository livreRepository;
    private final EmpruntRepository empruntRepository;


    @Override
    public Reservation creerReservation(Integer userId, Integer livreId) {
        // 1. Vérifications préalables (User et Livre existent)
        Utilisateur utilisateur = getUtilisateurOrThrow(userId);
        Livre livre = getLivreOrThrow(livreId);

        // --- NOUVELLES RÈGLES MÉTIER ---

        // Règle A : On ne réserve pas un livre qui est disponible en rayon
        if (livre.getExemplairesDispo() > 0) {
            throw new IllegalStateException("Le livre est disponible en rayon. Vous pouvez l'emprunter directement.");
        }

        // Règle B : On ne réserve pas un livre qu'on a déjà emprunté (En cours ou En retard)
        boolean aDejaLeLivre = empruntRepository.existsByUtilisateurIdAndLivreIdAndStatutIn(
                userId,
                livreId,
                List.of(StatutEmprunt.EN_COURS, StatutEmprunt.EN_RETARD)
        );
        if (aDejaLeLivre) {
            throw new IllegalStateException("Vous empruntez déjà ce livre actuellement.");
        }

        // -------------------------------

        // 2. Règle doublon réservation
        verifierPasDeDoublon(userId, livreId);

        long nbEnAttente = reservationRepository.countByLivreIdAndStatut(livreId, StatutReservation.EN_ATTENTE);

        Reservation reservation = Reservation.builder()
                .utilisateur(utilisateur)
                .livre(livre)
                .statut(StatutReservation.EN_ATTENTE)
                .dateDemande(LocalDateTime.now())
                .rangPriorite((int) nbEnAttente + 1)
                .build();

        log.info("Réservation créée...");
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsUtilisateur(Integer userId) {
        return reservationRepository.findByUtilisateurId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getFileAttenteLivre(Integer livreId) {
        return reservationRepository.findByLivreIdAndStatutOrderByRangPrioriteAsc(
                livreId,
                StatutReservation.EN_ATTENTE
        );
    }

    @Override
    public Reservation annulerReservation(Integer reservationId, Integer userId) {
        Reservation r = getReservationOrThrow(reservationId);

        // Sécurité : Vérifier l'appartenance
        if (!r.getUtilisateur().getId().equals(userId)) {
            throw new IllegalStateException("Impossible d'annuler une réservation qui ne vous appartient pas.");
        }

        // Idempotence : Si déjà fini/annulé, on ne fait rien
        if (estTermineeOuAnnulee(r)) {
            return r;
        }

        boolean etaitDisponible = (r.getStatut() == StatutReservation.DISPONIBLE);
        Integer livreId = r.getLivre().getId();

        // Action : Clôturer
        cloturerReservation(r, StatutReservation.ANNULEE);

        // Si elle était dans la file d'attente, on doit recaler les autres
        if (!etaitDisponible) {
            reindexerFileAttente(livreId);
        } else {
            // Si elle était disponible, le livre se libère pour le suivant
            notifierRetourLivre(livreId);
        }

        return r;
    }

    @Override
    public Reservation notifierRetourLivre(Integer livreId) {
        // 1. Qui est le prochain ?
        List<Reservation> file = reservationRepository.findByLivreIdAndStatutOrderByRangPrioriteAsc(
                livreId, StatutReservation.EN_ATTENTE
        );

        if (file.isEmpty()) {
            log.info("Livre {} rendu, mais personne n'attend.", livreId);
            return null;
        }

        // 2. On prend le premier et on l'active
        Reservation heureuseElue = file.get(0);
        activerReservationPourSuivant(heureuseElue);

        // 3. On décale tous les autres (ex: le 2 devient 1, le 3 devient 2...)
        // Note: Comme le premier est passé DISPONIBLE, il sort de la liste lors du prochain appel SQL.
        // On doit re-indexer ceux qui restent EN_ATTENTE.
        reindexerFileAttente(livreId);

        return heureuseElue;
    }

    @Override
    public Reservation terminerReservation(Integer reservationId, Integer userId) {
        Reservation r = getReservationOrThrow(reservationId);

        if (!r.getUtilisateur().getId().equals(userId)) {
            throw new IllegalStateException("Erreur de sécurité : Utilisateur incorrect.");
        }

        if (r.getStatut() != StatutReservation.DISPONIBLE) {
            throw new IllegalStateException("La réservation n'est pas disponible (Statut actuel : " + r.getStatut() + ")");
        }

        cloturerReservation(r, StatutReservation.TERMINEE);

        // Note : Ici, le livre sort physiquement de la bibliothèque.
        // La prochaine réservation ne sera activée que lorsque le livre reviendra (retour d'emprunt).
        // Donc on n'appelle PAS notifierRetourLivre() ici, sauf si ton métier dit le contraire.

        return r;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estExpiree(Integer reservationId, LocalDateTime now) {
        Reservation r = getReservationOrThrow(reservationId);
        return estReservationExpiree(r, now);
    }

    @Override
    public int traiterReservationsExpirees(Integer livreId) {
        // 1. Calcul de la date limite : Tout ce qui était dispo AVANT cette date est périmé.
        LocalDateTime dateLimite = LocalDateTime.now().minus(DELAI_RETRAIT);

        // 2. Récupération optimisée (SQL)
        List<Reservation> expirees = reservationRepository.findExpiredReservations(livreId, dateLimite);

        if (expirees.isEmpty()) {
            return 0;
        }

        // 3. Traitement
        for (Reservation r : expirees) {
            log.info("Expiration de la réservation {}", r.getId());
            cloturerReservation(r, StatutReservation.ANNULEE);
        }

        // 4. Puisqu'on a annulé des réservations "DISPONIBLE", le livre est libre pour les suivants dans la file
        // On active le suivant (autant de fois qu'on a annulé)
        // Simplification : on appelle notifier une fois, si le livre est unique.
        notifierRetourLivre(livreId);

        return expirees.size();
    }

    @Override
    public void supprimerDefinitivement(Integer reservationId) {
        if (!reservationRepository.existsById(reservationId)) {
            throw new EntityNotFoundException("Réservation introuvable pour suppression");
        }
        reservationRepository.deleteById(reservationId);
    }

    // =========================================================================
    // MÉTHODES PRIVÉES (Helpers) - Pour alléger le code principal
    // =========================================================================

    private void verifierPasDeDoublon(Integer userId, Integer livreId) {
        boolean existeDeja = reservationRepository.existsByUtilisateurIdAndLivreIdAndStatutIn(
                userId,
                livreId,
                List.of(StatutReservation.EN_ATTENTE, StatutReservation.DISPONIBLE)
        );

        if (existeDeja) {
            throw new IllegalStateException("L'utilisateur a déjà une réservation active pour ce livre.");
        }
    }

    private void activerReservationPourSuivant(Reservation r) {
        r.setStatut(StatutReservation.DISPONIBLE);
        r.setDateDisponibilite(LocalDateTime.now());
        r.setRangPriorite(null); // On sort de la file d'attente numérotée
        reservationRepository.save(r);
        log.info("Réservation {} passée en DISPONIBLE. Email envoyé à l'utilisateur.", r.getId());
        // TODO: Envoyer mail ici
    }

    private void cloturerReservation(Reservation r, StatutReservation statutFinal) {
        r.setStatut(statutFinal);
        r.setDateCloture(LocalDateTime.now());
        r.setRangPriorite(null); // Plus de rang
        reservationRepository.save(r);
    }

    private void reindexerFileAttente(Integer livreId) {
        // On ne recharge que ceux qui sont EN_ATTENTE
        List<Reservation> file = reservationRepository.findByLivreIdAndStatutOrderByRangPrioriteAsc(
                livreId, StatutReservation.EN_ATTENTE
        );

        int rang = 1;
        for (Reservation r : file) {
            if (r.getRangPriorite() == null || r.getRangPriorite() != rang) {
                r.setRangPriorite(rang);
                // Pas besoin d'appel explicite à save() si on est dans une transaction @Transactional,
                // mais saveAll est une bonne pratique explicite.
            }
            rang++;
        }
        reservationRepository.saveAll(file);
    }

    private boolean estTermineeOuAnnulee(Reservation r) {
        return r.getStatut() == StatutReservation.ANNULEE || r.getStatut() == StatutReservation.TERMINEE;
    }

    private boolean estReservationExpiree(Reservation r, LocalDateTime now) {
        if (r.getStatut() != StatutReservation.DISPONIBLE) return false;
        if (r.getDateDisponibilite() == null) return false;
        return r.getDateDisponibilite().plus(DELAI_RETRAIT).isBefore(now);
    }

    private Reservation getReservationOrThrow(Integer id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Réservation introuvable : " + id));
    }

    private Utilisateur getUtilisateurOrThrow(Integer id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : " + id));
    }

    private Livre getLivreOrThrow(Integer id) {
        return livreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livre introuvable : " + id));
    }
}