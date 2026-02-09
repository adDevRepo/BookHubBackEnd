package fr.bookHub.bll;

import fr.bookHub.bll.EmpruntService;
import fr.bookHub.bo.Emprunt;
import fr.bookHub.bo.Livre;
import fr.bookHub.bo.Utilisateur;
import fr.bookHub.bo.enums.StatutEmprunt;
import fr.bookHub.dal.EmpruntRepository;
import fr.bookHub.dal.LivreRepository;
import fr.bookHub.dal.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class EmpruntServiceImpl implements EmpruntService {

    private static final int MAX_EMPRUNTS_EN_COURS = 3;
    private static final int DUREE_EMPRUNT_JOURS = 14;

    private final EmpruntRepository empruntRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final LivreRepository livreRepository;

    public EmpruntServiceImpl(EmpruntRepository empruntRepository,
                              UtilisateurRepository utilisateurRepository,
                              LivreRepository livreRepository) {
        this.empruntRepository = empruntRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.livreRepository = livreRepository;
    }

    @Override
    public Emprunt emprunterLivre(Integer utilisateurId, Integer livreId) {
        if (utilisateurId == null || livreId == null) {
            throw new IllegalArgumentException("utilisateurId et livreId sont obligatoires.");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable : " + utilisateurId));

        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable : " + livreId));

        // Bloquant : retards existants
        if (empruntRepository.existsByUtilisateurIdAndStatut(utilisateurId, StatutEmprunt.EN_RETARD)) {
            throw new IllegalStateException("Emprunt refusé : l'utilisateur a au moins un emprunt en retard.");
        }

        // Quota max (EN_COURS)
        long nbEnCours = empruntRepository.countByUtilisateurIdAndStatut(utilisateurId, StatutEmprunt.EN_COURS);
        if (nbEnCours >= MAX_EMPRUNTS_EN_COURS) {
            throw new IllegalStateException("Emprunt refusé : quota maximum atteint (" + MAX_EMPRUNTS_EN_COURS + ").");
        }

        // Stock
        Integer dispo = livre.getExemplairesDispo();
        if (dispo == null || dispo <= 0) {
            throw new IllegalStateException("Emprunt refusé : aucune copie disponible.");
        }

        // Créer l'emprunt
        LocalDate today = LocalDate.now();

        Emprunt emprunt = Emprunt.builder()
                .utilisateur(utilisateur)
                .livre(livre)
                .dateEmprunt(today) // (ton @PrePersist le ferait aussi)
                .dateRetourPrevue(today.plusDays(DUREE_EMPRUNT_JOURS))
                .dateRetourReel(null)
                .statut(StatutEmprunt.EN_COURS)
                .build();

        // Décrémenter stock
        livre.setExemplairesDispo(dispo - 1);
        livreRepository.save(livre);

        return empruntRepository.save(emprunt);
    }

    @Override
    public Emprunt retournerLivre(Integer empruntId) {
        if (empruntId == null) {
            throw new IllegalArgumentException("empruntId est obligatoire.");
        }

        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new IllegalArgumentException("Emprunt introuvable : " + empruntId));

        if (emprunt.getDateRetourReel() != null || emprunt.getStatut() == StatutEmprunt.RETOURNE) {
            throw new IllegalStateException("Cet emprunt est déjà retourné.");
        }

        // Mettre à jour emprunt
        emprunt.setDateRetourReel(LocalDate.now());
        emprunt.setStatut(StatutEmprunt.RETOURNE);

        // Libérer stock
        Livre livre = emprunt.getLivre();
        Integer dispo = livre.getExemplairesDispo();
        livre.setExemplairesDispo((dispo == null ? 0 : dispo) + 1);
        livreRepository.save(livre);

        return empruntRepository.save(emprunt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emprunt> consulterEmpruntsEnCours(Integer utilisateurId) {
        if (utilisateurId == null) {
            throw new IllegalArgumentException("utilisateurId est obligatoire.");
        }

        if (!utilisateurRepository.existsById(utilisateurId)) {
            throw new IllegalArgumentException("Utilisateur introuvable : " + utilisateurId);
        }

        return empruntRepository.findByUtilisateurIdAndStatut(utilisateurId, StatutEmprunt.EN_COURS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emprunt> consulterHistoriqueUtilisateur(Integer utilisateurId) {
        if (utilisateurId == null) {
            throw new IllegalArgumentException("utilisateurId est obligatoire.");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable : " + utilisateurId));

        return empruntRepository.findByUtilisateur(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emprunt> consulterEmpruntsEnRetard() {
        // Option B : calcul en mémoire (pas besoin de méthode repo en plus)
        LocalDate today = LocalDate.now();

        return empruntRepository.findAll().stream()
                .filter(e -> e.getDateRetourReel() == null)
                .filter(e -> e.getDateRetourPrevue() != null && e.getDateRetourPrevue().isBefore(today))
                .toList();
    }
}
