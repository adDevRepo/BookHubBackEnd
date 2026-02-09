package fr.bookHub.bll;

import fr.bookHub.bll.LivreService;
import fr.bookHub.bo.Livre;
import fr.bookHub.dal.LivreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LivreServiceImpl implements LivreService {

    private final LivreRepository livreRepository;

    public LivreServiceImpl(LivreRepository livreRepository) {
        this.livreRepository = livreRepository;
    }

    @Override
    public Livre creerLivre(Livre livre) {
        if (livre == null) {
            throw new IllegalArgumentException("Le livre est obligatoire.");
        }

        if (livre.getIsbn() == null || livre.getIsbn().isBlank()) {
            throw new IllegalArgumentException("L'ISBN est obligatoire.");
        }

        // Vérification unicité ISBN
        boolean isbnExiste = livreRepository.findAll().stream()
                .anyMatch(l -> l.getIsbn().equals(livre.getIsbn()));

        if (isbnExiste) {
            throw new IllegalStateException("Un livre avec cet ISBN existe déjà.");
        }

        return livreRepository.save(livre);
    }

    @Override
    public Livre modifierLivre(Integer id, Livre livre) {
        if (id == null) {
            throw new IllegalArgumentException("L'id du livre est obligatoire.");
        }

        if (livre == null) {
            throw new IllegalArgumentException("Le livre est obligatoire.");
        }

        Livre existant = livreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable : " + id));

        // Mise à jour des champs modifiables
        existant.setTitre(livre.getTitre());
        existant.setAuteur(livre.getAuteur());
        existant.setIsbn(livre.getIsbn());
        existant.setUrlCouverture(livre.getUrlCouverture());
        existant.setDescription(livre.getDescription());
        existant.setCategorie(livre.getCategorie());
        existant.setActif(livre.getActif());

        // Gestion du stock
        if (livre.getExemplairesTotal() != null) {
            int totalActuel = existant.getExemplairesTotal();
            int dispoActuel = existant.getExemplairesDispo();
            int empruntes = totalActuel - dispoActuel;

            if (livre.getExemplairesTotal() < empruntes) {
                throw new IllegalStateException(
                        "Impossible de réduire le stock en dessous du nombre d'exemplaires empruntés."
                );
            }

            existant.setExemplairesTotal(livre.getExemplairesTotal());
            existant.setExemplairesDispo(livre.getExemplairesTotal() - empruntes);
        }

        return livreRepository.save(existant);
    }

    @Override
    @Transactional(readOnly = true)
    public Livre consulterParId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'id du livre est obligatoire.");
        }

        return livreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Livre> consulterTous(Pageable pageable) {
        return livreRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Livre> rechercher(String motCle, Pageable pageable) {
        if (motCle == null || motCle.isBlank()) {
            return livreRepository.findAll(pageable);
        }

        return livreRepository.rechercherLivres(motCle, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Livre> consulterParCategorie(Integer categorieId, Pageable pageable) {
        if (categorieId == null) {
            throw new IllegalArgumentException("L'id de la catégorie est obligatoire.");
        }

        return livreRepository.findByCategorieId(categorieId, pageable);
    }

    @Override
    public void supprimerLivre(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'id du livre est obligatoire.");
        }

        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable : " + id));

        // Règle métier : aucun exemplaire emprunté
        if (livre.getExemplairesDispo() < livre.getExemplairesTotal()) {
            throw new IllegalStateException(
                    "Impossible de supprimer le livre : des exemplaires sont actuellement empruntés."
            );
        }

        livreRepository.delete(livre);
    }
}
