package fr.bookHub.bll;

import fr.bookHub.bo.Categorie;
import fr.bookHub.bo.Livre;
import fr.bookHub.dal.CategorieRepository;
import fr.bookHub.dal.LivreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LivreServiceImpl implements LivreService {

    private final LivreRepository livreRepository;
    private final CategorieRepository categorieRepository;

    public LivreServiceImpl(LivreRepository livreRepository, CategorieRepository categorieRepository) {
        this.livreRepository = livreRepository;
        this.categorieRepository = categorieRepository;
    }

    @Override
    public Livre creerLivre(Livre livre) {
        if (livre == null) throw new IllegalArgumentException("Le livre est obligatoire.");
        if (livre.getIsbn() == null || livre.getIsbn().isBlank())
            throw new IllegalArgumentException("L'ISBN est obligatoire.");

        // ✅ Vérification unicité ISBN (performant)
        if (livreRepository.existsByIsbn(livre.getIsbn())) {
            throw new IllegalStateException("Un livre avec cet ISBN existe déjà.");
        }

        // ✅ Résoudre la catégorie (pour FK + categorieNom non null)
        if (livre.getCategorie() == null || livre.getCategorie().getId() == null) {
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        }
        Categorie categorie = categorieRepository.getOrThrow(livre.getCategorie().getId());
        livre.setCategorie(categorie);

        return livreRepository.save(livre);
    }

    @Override
    public Livre modifierLivre(Integer id, Livre livre) {
        if (id == null) throw new IllegalArgumentException("L'id du livre est obligatoire.");
        if (livre == null) throw new IllegalArgumentException("Le livre est obligatoire.");

        Livre existant = livreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable : " + id));

        // ✅ Unicité ISBN si changement
        if (livre.getIsbn() != null && !livre.getIsbn().equals(existant.getIsbn())) {
            if (livreRepository.existsByIsbnAndIdNot(livre.getIsbn(), id)) {
                throw new IllegalStateException("Un autre livre possède déjà cet ISBN.");
            }
        }

        // ✅ Résoudre la catégorie
        if (livre.getCategorie() == null || livre.getCategorie().getId() == null) {
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        }
        Categorie categorie = categorieRepository.getOrThrow(livre.getCategorie().getId());

        // Mise à jour des champs modifiables
        existant.setTitre(livre.getTitre());
        existant.setAuteur(livre.getAuteur());
        existant.setIsbn(livre.getIsbn());
        existant.setUrlCouverture(livre.getUrlCouverture());
        existant.setDescription(livre.getDescription());
        existant.setCategorie(categorie);
        existant.setActif(livre.getActif());

        // Gestion du stock (ta logique est bonne)
        if (livre.getExemplairesTotal() != null) {
            int totalActuel = existant.getExemplairesTotal() == null ? 0 : existant.getExemplairesTotal();
            int dispoActuel = existant.getExemplairesDispo() == null ? 0 : existant.getExemplairesDispo();
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
        if (id == null) throw new IllegalArgumentException("L'id du livre est obligatoire.");

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
        if (categorieId == null) throw new IllegalArgumentException("L'id de la catégorie est obligatoire.");
        return livreRepository.findByCategorieId(categorieId, pageable);
    }

    @Override
    public void supprimerLivre(Integer id) {
        if (id == null) throw new IllegalArgumentException("L'id du livre est obligatoire.");

        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable : " + id));

        if (livre.getExemplairesDispo() < livre.getExemplairesTotal()) {
            throw new IllegalStateException(
                    "Impossible de supprimer le livre : des exemplaires sont actuellement empruntés."
            );
        }

        livreRepository.delete(livre);
    }
}
