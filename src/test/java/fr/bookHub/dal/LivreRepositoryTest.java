package fr.bookHub.dal;

import fr.bookHub.bo.Categorie;
import fr.bookHub.bo.Livre;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du LivreRepository (Spring Data JPA).
 *
 * Objectif :
 * - Valider les méthodes de recherche (simple + filtre catégorie + recherche avancée)
 * - Vérifier que la pagination fonctionne
 * - S'assurer que nos entités (Livre, Categorie) respectent les contraintes JPA/Validation
 */
@DataJpaTest
@ActiveProfiles("test")
class LivreRepositoryTest {

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private EntityManager em;

    /**
     * Test US-BOOK-03 (recherche simple):
     * Vérifie que findByTitreContainingIgnoreCase(...) retourne les livres
     * dont le titre contient un mot-clé, sans tenir compte de la casse.
     */
    @Test
    void findByTitreContainingIgnoreCase_recherche_insensible_a_la_casse() {
        Categorie cat = persistCategorie("Roman", uniqueCode("roman"));

        Livre l1 = persistLivre("Harry Potter et la coupe de feu", "J.K. Rowling", uniqueIsbn("ISBN"), cat);
        Livre l2 = persistLivre("HARRY POTTER et le prisonnier d'Azkaban", "J.K. Rowling", uniqueIsbn("ISBN"), cat);
        persistLivre("Dune", "Frank Herbert", uniqueIsbn("ISBN"), cat);

        em.flush();
        em.clear();

        List<Livre> result = livreRepository.findByTitreContainingIgnoreCase("harry potter");

        // On doit retrouver les 2 Harry Potter, et pas Dune
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Livre::getId).contains(l1.getId(), l2.getId());
    }

    /**
     * Test US-BOOK-03 (filtre par catégorie + pagination):
     * Vérifie que findByCategorieId(...) retourne uniquement les livres de la catégorie demandée.
     */
    @Test
    void findByCategorieId_retourne_uniquement_les_livres_de_la_categorie() {
        Categorie roman = persistCategorie("Roman", uniqueCode("roman"));
        Categorie sf = persistCategorie("Science-Fiction", uniqueCode("sci fi"));

        // 3 livres en SF, 1 en Roman
        persistLivre("Dune", "Frank Herbert", uniqueIsbn("ISBN"), sf);
        persistLivre("Fondation", "Isaac Asimov", uniqueIsbn("ISBN"), sf);
        persistLivre("Neuromancer", "William Gibson", uniqueIsbn("ISBN"), sf);
        persistLivre("Madame Bovary", "Gustave Flaubert", uniqueIsbn("ISBN"), roman);

        em.flush();
        em.clear();

        // Page 0, taille 2 (pagination)
        Page<Livre> page0 = livreRepository.findByCategorieId(sf.getId(), PageRequest.of(0, 2));

        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getContent())
                .allMatch(l -> l.getCategorie().getId().equals(sf.getId()));
    }

    /**
     * Test recherche avancée (Query JPQL):
     * Vérifie que rechercherLivres(...) trouve un livre
     * si le mot-clé est présent dans :
     * - le titre, ou
     * - l'auteur, ou
     * - l'ISBN
     */
    @Test
    void rechercherLivres_trouve_par_titre_ou_auteur_ou_isbn() {
        Categorie cat = persistCategorie("Roman", uniqueCode("roman"));

        Livre dune = persistLivre("Dune", "Frank Herbert", "9780441013593", cat);
        Livre hp = persistLivre("Harry Potter", "J.K. Rowling", "9782070584628", cat);
        persistLivre("Le Rouge et le Noir", "Stendhal", "9780140447646", cat);

        em.flush();
        em.clear();

        // 1) Recherche par titre
        Page<Livre> byTitle = livreRepository.rechercherLivres("dune", PageRequest.of(0, 10));
        assertThat(byTitle.getContent()).extracting(Livre::getId).contains(dune.getId());

        // 2) Recherche par auteur (insensible à la casse)
        Page<Livre> byAuthor = livreRepository.rechercherLivres("rowling", PageRequest.of(0, 10));
        assertThat(byAuthor.getContent()).extracting(Livre::getId).contains(hp.getId());

        // 3) Recherche par ISBN (contient)
        Page<Livre> byIsbn = livreRepository.rechercherLivres("978207", PageRequest.of(0, 10));
        assertThat(byIsbn.getContent()).extracting(Livre::getId).contains(hp.getId());
    }

    /**
     * Test de non-résultat :
     * Vérifie que la recherche avancée retourne une page vide si aucun match.
     */
    @Test
    void rechercherLivres_retourne_vide_si_aucun_match() {
        Categorie cat = persistCategorie("Roman", uniqueCode("roman"));
        persistLivre("Dune", "Frank Herbert", uniqueIsbn("ISBN"), cat);

        em.flush();
        em.clear();

        Page<Livre> result = livreRepository.rechercherLivres("mot-cle-inexistant", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    // ---------------- Helpers (création d'entités valides) ----------------

    private Categorie persistCategorie(String nom, String code) {
        Categorie c = new Categorie();
        c.setNom(nom);
        c.setCode(code); // obligatoire + unique (et normalisé via @PrePersist)
        em.persist(c);
        return c;
    }

    private Livre persistLivre(String titre, String auteur, String isbn, Categorie categorie) {
        Livre l = new Livre();
        l.setTitre(titre);
        l.setAuteur(auteur);
        l.setIsbn(isbn);
        l.setDescription("Description de test");
        l.setUrlCouverture("https://example.com/cover.png");
        l.setExemplairesTotal(3);
        l.setExemplairesDispo(3);
        l.setActif(true);
        l.setCategorie(categorie);
        em.persist(l);
        return l;
    }

    private String uniqueCode(String prefix) {
        // Evite collisions + la catégorie normalise (trim, espaces->underscore, majuscules)
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String uniqueIsbn(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
