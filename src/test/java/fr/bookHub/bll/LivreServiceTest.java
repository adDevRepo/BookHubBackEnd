package fr.bookHub.bll;

import fr.bookHub.bo.Categorie;
import fr.bookHub.bo.Livre;
import fr.bookHub.dal.CategorieRepository;
import fr.bookHub.dal.LivreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class LivreServiceIT {

    @Autowired LivreService livreService;
    @Autowired LivreRepository livreRepository;
    @Autowired CategorieRepository categorieRepository;

    private Categorie creerCategorie() {
        Categorie c = new Categorie();
        c.setCode("ROM");
        // IMPORTANT : chez toi c’est probablement "nom" (pas "name")
        c.setNom("Romans");
        return categorieRepository.save(c);
    }

    @Test
    void creerLivre_ok() {
        Categorie cat = creerCategorie();

        Livre livre = Livre.builder()
                .titre("Dune")
                .auteur("Frank Herbert")
                .isbn("ISBN-1")
                .description("desc")
                .categorie(cat)
                .exemplairesTotal(3)
                .exemplairesDispo(3)
                .actif(true)
                .build();

        Livre saved = livreService.creerLivre(livre);

        assertThat(saved.getId()).isNotNull();
        assertThat(livreRepository.count()).isEqualTo(1);
    }

    @Test
    void creerLivre_isbn_duplique_refuse() {
        Categorie cat = creerCategorie();

        livreRepository.save(Livre.builder()
                .titre("A")
                .auteur("B")
                .isbn("DUP")
                .description("desc")
                .categorie(cat)
                .exemplairesTotal(1)
                .exemplairesDispo(1)
                .actif(true)
                .build());

        Livre livre2 = Livre.builder()
                .titre("C")
                .auteur("D")
                .isbn("DUP")
                .description("desc")
                .categorie(cat)
                .exemplairesTotal(1)
                .exemplairesDispo(1)
                .actif(true)
                .build();

        assertThatThrownBy(() -> livreService.creerLivre(livre2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rechercher_retourne_1_resultat() {
        Categorie cat = creerCategorie();

        livreRepository.save(Livre.builder()
                .titre("Harry Potter")
                .auteur("Rowling")
                .isbn("HP-1")
                .description("desc")
                .categorie(cat)
                .exemplairesTotal(1)
                .exemplairesDispo(1)
                .actif(true)
                .build());

        var page = livreService.rechercher("harry", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void supprimerLivre_refuse_si_exemplaire_emprunte() {
        Categorie cat = creerCategorie();

        Livre livre = livreRepository.save(Livre.builder()
                .titre("Dune")
                .auteur("Frank Herbert")
                .isbn("ISBN-2")
                .description("desc")
                .categorie(cat)
                .exemplairesTotal(3)
                .exemplairesDispo(2) // 1 emprunté
                .actif(true)
                .build());

        assertThatThrownBy(() -> livreService.supprimerLivre(livre.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
