package fr.bookHub.dal;

import fr.bookHub.bo.Categorie;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategorieRepositoryTest {

    @Autowired
    private CategorieRepository categorieRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void existsByCodeIgnoreCase() {

        // création d'une catégorie
        entityManager.persist(
                Categorie.builder()
                        .nom("Science-Fiction")
                        .code("SCI_FI")
                        .build()
        );

        // sauvegarde
        entityManager.flush();
        entityManager.clear();

        // test si la catégorie existe par le code
        boolean exists =
                categorieRepository.existsByCodeIgnoreCase("sci_fi");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNomIgnoreCase() {

        // création d'une catégorie
        entityManager.persist(
                Categorie.builder()
                        .nom("Fantastique")
                        .code("FANTASY")
                        .build()
        );

        // sauvegarde
        entityManager.flush();
        entityManager.clear();

        // test si la catégorie existe par le nom
        boolean exists = categorieRepository.existsByNomIgnoreCase("fantastique");

        assertThat(exists).isTrue();
    }

    @Test
    void findByCodeIgnoreCase() {
        // création d'une catégorie
        Categorie categorie = Categorie.builder()
                .nom("Policier")
                .code("POLAR")
                .build();

        // sauvegarde
        entityManager.persist(categorie);
        entityManager.flush();
        entityManager.clear();

        // recherche de la categorie par code
        Optional<Categorie> result =
                categorieRepository.findByCodeIgnoreCase("polar");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(categorie.getId());
    }

    @Test
    void findAllByOrderByNomAscSortedCategories() {
        // création de 2 catégories
        entityManager.persist(
                Categorie.builder().nom("Science fiction").code("SCI_FI").build()
        );
        entityManager.persist(
                Categorie.builder().nom("Policier").code("POLAR").build()
        );

        //sauvegarde
        entityManager.flush();
        entityManager.clear();

        // Liste des catégories par order alphabétique
        List<Categorie> result =
                categorieRepository.findAllByOrderByNomAsc();


        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNom()).isEqualTo("Policier");
        assertThat(result.get(1).getNom()).isEqualTo("Science fiction");
    }

}
