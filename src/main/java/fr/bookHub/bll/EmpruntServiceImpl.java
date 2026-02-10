package fr.bookHub.bll;

import fr.bookHub.bo.Emprunt;
import fr.bookHub.bo.Livre;
import fr.bookHub.bo.Reservation;
import fr.bookHub.bo.Utilisateur;
import fr.bookHub.bo.enums.StatutEmprunt;
import fr.bookHub.dal.EmpruntRepository;
import fr.bookHub.dal.LivreRepository;
import fr.bookHub.dal.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor // Remplace le constructeur manuel (Lombok)
public class EmpruntServiceImpl implements EmpruntService {

    private static final int MAX_EMPRUNTS_SIMULTANES = 3;
    private static final int DUREE_EMPRUNT_JOURS = 14;

    private final EmpruntRepository empruntRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final LivreRepository livreRepository;

    @Lazy
    private final ReservationService reservationService;

    @Override
    public Emprunt emprunterLivre(Integer utilisateurId, Integer livreId) {
        // 1. Chargement des entités
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : " + utilisateurId));

        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new EntityNotFoundException("Livre introuvable : " + livreId));

        // 2. Vérification Bloquante : Aucun retard autorisé
        boolean aDesRetards = empruntRepository.existsByUtilisateurIdAndStatut(utilisateurId, StatutEmprunt.EN_RETARD);
        if (aDesRetards) {
            throw new IllegalStateException("Impossible d'emprunter : Vous avez des livres en retard.");
        }

        // 3. Vérification Quota : On compte EN_COURS et EN_RETARD (tout ce qui est chez lui)
        long nbActuels = empruntRepository.countEmpruntsActifs(
                utilisateurId,
                List.of(StatutEmprunt.EN_COURS, StatutEmprunt.EN_RETARD)
        );

        if (nbActuels >= MAX_EMPRUNTS_SIMULTANES) {
            throw new IllegalStateException("Quota atteint : Vous avez déjà " + nbActuels + " livres en votre possession.");
        }

        // 4. Vérification Stock
        if (livre.getExemplairesDispo() <= 0) {
            throw new IllegalStateException("Livre indisponible (Stock épuisé).");
        }

        // 5. Création de l'emprunt
        Emprunt emprunt = Emprunt.builder()
                .utilisateur(utilisateur)
                .livre(livre)
                .dateEmprunt(LocalDate.now())
                .dateRetourPrevue(LocalDate.now().plusDays(DUREE_EMPRUNT_JOURS))
                .statut(StatutEmprunt.EN_COURS)
                .build();

        // 6. Mise à jour du stock
        livre.setExemplairesDispo(livre.getExemplairesDispo() - 1);
        livreRepository.save(livre); // Optionnel si transactionnel, mais explicite c'est bien

        return empruntRepository.save(emprunt);
    }

    @Override
    public Emprunt retournerLivre(Integer empruntId) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new EntityNotFoundException("Emprunt introuvable : " + empruntId));

        if (emprunt.getStatut() == StatutEmprunt.RETOURNE || emprunt.getDateRetourReel() != null) {
            throw new IllegalStateException("Ce livre est déjà retourné.");
        }

        // 1. Clôture de l'emprunt
        emprunt.setDateRetourReel(LocalDate.now());
        emprunt.setStatut(StatutEmprunt.RETOURNE);

        // Sauvegarde intermédiaire de l'emprunt
        empruntRepository.save(emprunt);

        Livre livre = emprunt.getLivre();

        // 2. Vérification des réservations
        // On demande au service de réservation : "Y a-t-il quelqu'un qui attend ce livre ?"
        Reservation reservationActivee = reservationService.notifierRetourLivre(livre.getId());

        if (reservationActivee != null) {
            // CAS A : Quelqu'un attendait le livre.
            // On NE remet PAS le livre en stock général.
            // Le livre est maintenant "réservé" (logiquement bloqué).
            // Le stock reste à 0 (ou sa valeur actuelle) pour empêcher un emprunt "sauvage".
            System.out.println("Livre mis de côté pour la réservation ID: " + reservationActivee.getId());
        } else {
            // CAS B : Personne n'attend.
            // On remet le livre en rayon.
            livre.setExemplairesDispo(livre.getExemplairesDispo() + 1);
            livreRepository.save(livre);
        }

        return emprunt;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emprunt> getEmpruntsUtilisateur(Integer utilisateurId) {
        // Vérifier existence user seulement si nécessaire, sinon le repo renvoie liste vide
        if (!utilisateurRepository.existsById(utilisateurId)) {
            throw new EntityNotFoundException("Utilisateur introuvable");
        }
        return empruntRepository.findByUtilisateurId(utilisateurId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emprunt> getEmpruntsEnCours(Integer utilisateurId) {
        return empruntRepository.findByUtilisateurIdAndStatut(utilisateurId, StatutEmprunt.EN_COURS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emprunt> getEmpruntsTermines(Integer utilisateurId) {
        return empruntRepository.findByUtilisateurIdAndStatut(utilisateurId, StatutEmprunt.RETOURNE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emprunt> getEmpruntsEnRetard() {
        return empruntRepository.findEmpruntsEnRetard(LocalDate.now());
    }
}