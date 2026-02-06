package fr.bookHub.dal;

import fr.bookHub.bo.*;
import fr.bookHub.bo.enums.NomRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AvisRepositoryTest {

    @Autowired
    private AvisRepository avisRepository;

    @Autowired
    private EntityManager entityManager;

    private Utilisateur paul;
    private Utilisateur julie;
    private Livre seigneurDesAnneaux;
    private Livre universCoquilleDeNoix;

    @BeforeEach
    void setUp() {

        Role role = Role.builder()
                .nom(NomRole.READER)
                .build();
        entityManager.persist(role);

        Categorie categorie = Categorie.builder()
                .nom("Science Fiction" )
                .code("SCI_FI" )
                .build();
        entityManager.persist(categorie);

        paul = Utilisateur.builder()
                .nom("Durand" )
                .prenom("Paul" )
                .email("paul@test.fr" )
                .password("12345" )
                .role(role)
                .build();
        entityManager.persist(paul);

        julie = Utilisateur.builder()
                .nom("Martin" )
                .prenom("Julie" )
                .email("julie@test.fr" )
                .password("54321" )
                .role(role)
                .build();
        entityManager.persist(julie);

        seigneurDesAnneaux = Livre.builder()
                .titre("Le seigneur des anneaux" )
                .auteur("Tolkien" )
                .isbn("ISBN123" )
                .description("Un anneau unique" )
                .categorie(categorie)
                .build();
        entityManager.persist(seigneurDesAnneaux);

        universCoquilleDeNoix = Livre.builder()
                .titre("L'Univers dans une coquille de noix" )
                .auteur("Hawking" )
                .isbn("ISBN456" )
                .description("La théorie du tout" )
                .categorie(categorie)
                .build();
        entityManager.persist(universCoquilleDeNoix);

        entityManager.flush();
        entityManager.clear();
    }


    @Test
    void findAverageNoteByLivreId() {

        // création de 2 avis
        entityManager.persist(
                Avis.builder()
                        .livre(seigneurDesAnneaux)
                        .utilisateur(paul)
                        .note(4)
                        .build()
        );

        entityManager.persist(
                Avis.builder()
                        .livre(seigneurDesAnneaux)
                        .utilisateur(julie)
                        .note(2)
                        .build()
        );
        // sauvegarde
        entityManager.flush();
        entityManager.clear();

        // calcul de la note moyenne
        Double average = avisRepository.findAverageNoteByLivreId(seigneurDesAnneaux.getId());

        assertThat(average).isEqualTo(3.0);
    }

    @Test
    void countByLivreId() {
        // création de 2 avis
        entityManager.persist(
                Avis.builder()
                        .livre(seigneurDesAnneaux)
                        .utilisateur(paul)
                        .note(5)
                        .build()
        );

        entityManager.persist(
                Avis.builder()
                        .livre(seigneurDesAnneaux)
                        .utilisateur(julie)
                        .note(4)
                        .build()
        );

        entityManager.flush();
        entityManager.clear();

        // comptage des avis par Id
        long count = avisRepository.countByLivreId(seigneurDesAnneaux.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void findByUtilisateurIdOrderByDatePublicationDesc() {

        // création de 2 avis
        entityManager.persist(
                Avis.builder()
                        .livre(seigneurDesAnneaux)
                        .utilisateur(paul)
                        .note(2)
                        .datePublication(LocalDateTime.now().minusDays(2))
                        .build()
        );

        entityManager.persist(
                Avis.builder()
                        .livre(universCoquilleDeNoix)
                        .utilisateur(paul)
                        .note(5)
                        .datePublication(LocalDateTime.now())
                        .build()
        );

        // sauvegarde
        entityManager.flush();
        entityManager.clear();

        // listing des avis d'un même utilisateur
        List<Avis> avisList = avisRepository.findByUtilisateurIdOrderByDatePublicationDesc(paul.getId());

        assertThat(avisList).hasSize(2);
        assertThat(avisList.get(0).getNote()).isEqualTo(5);
        assertThat(avisList.get(1).getNote()).isEqualTo(2);
    }
}