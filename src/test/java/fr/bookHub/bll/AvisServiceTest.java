package fr.bookHub.bll;

import fr.bookHub.bo.*;
import fr.bookHub.bo.enums.NomRole;
import fr.bookHub.dal.*;
import org.aspectj.bridge.IMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvisServiceTest {

    @Mock
    private AvisRepository avisRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private LivreRepository livreRepository;

    @InjectMocks
    private AvisServiceImpl avisService;

    @Test
    void saveAvisTest() {

        Role role = Role.builder()
                .nom(NomRole.READER)
                .build();

        Categorie categorie = Categorie.builder()
                .nom("Science Fiction")
                .code("SCI_FI")
                .build();

        Utilisateur utilisateur = Utilisateur.builder()
                .id(2)
                .nom("Durand")
                .prenom("Paul")
                .email("paul@test.fr")
                .password("12345")
                .role(role)
                .build();

        Livre livre = Livre.builder()
                .id(1)
                .titre("Le seigneur des anneaux")
                .auteur("Tolkien")
                .isbn("ISBN123")
                .description("Un anneau unique")
                .categorie(categorie)
                .build();

        Avis avis = Avis.builder()
                .id(10)
                .livre(livre)
                .utilisateur(utilisateur)
                .note(4)
                .commentaire("Super film")
                .build();


        // ARRANGE
        when(livreRepository.findById(1)).thenReturn(Optional.of(livre));
        when(utilisateurRepository.findById(2)).thenReturn(Optional.of(utilisateur));
        when(avisRepository.findByLivreIdAndUtilisateurId(1, 2)).thenReturn(Optional.empty());
        when(avisRepository.save(any(Avis.class))).thenReturn(avis);

        // ACT
        Avis avisResult =
                avisService.saveAvis(1, 2, 4);

        // ASSERT
        assertThat(avisResult).isNotNull();
        assertThat(avisResult.getNote()).isEqualTo(4);
        assertThat(avisResult.getLivre().getId()).isEqualTo(1);
        assertThat(avisResult.getUtilisateur().getId()).isEqualTo(2);


    }

    @Test
    void getAverageNoteByLivreTest() {

        // ARRANGE
        Integer livreId = 1;
        when(avisRepository.findAverageNoteByLivreId(livreId)).thenReturn(3.5);

        // ACT
        Double averageNote = avisService.getAverageNoteByLivre(livreId);

        // ASSERT
        assertThat(averageNote).isEqualTo(3.5);
        verify(avisRepository).findAverageNoteByLivreId(livreId);
    }



    @Test
    void saveAvisThrowsExceptionAvisAllReadyExist() {
        when(livreRepository.findById(1)).thenReturn(Optional.of(new Livre()));
        when(utilisateurRepository.findById(2)).thenReturn(Optional.of(new Utilisateur()));
        when(avisRepository.findByLivreIdAndUtilisateurId(1, 2)).thenReturn(Optional.of(new Avis()));


        assertThatThrownBy(() -> avisService.saveAvis(1, 2, 4))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Un utilisateur ne peut poster qu’un seul avis par livre");
    }



    @Test
    void countAvisByLivreTest() {
        // ARRANGE
        Integer livreId = 1;
        when(avisRepository.countByLivreId(livreId)).thenReturn(3L);

        // ACT
        long count = avisService.countAvisByLivre(livreId);

        // ASSERT
        assertThat(count).isEqualTo(3L);
        verify(avisRepository).countByLivreId(livreId);
    }


    @Test
    void getAvisByUtilisateurTest() {

        // ARRANGE
        Integer utilisateurId = 5;

        Avis avis1 = Avis.builder().note(4).commentaire("Super film").build();
        Avis avis2 = Avis.builder().note(2).commentaire("Film moyen").build();


        when(avisRepository.findByUtilisateurIdOrderByDatePublicationDesc(utilisateurId)).thenReturn(List.of(avis1, avis2));

        // ACT
        List<Avis> listAvis = avisService.getAvisByUtilisateur(utilisateurId);

        // ASSERT
        assertThat(listAvis).isNotEmpty();
        assertThat(listAvis).hasSize(2);
        assertThat(listAvis.get(0).getNote()).isEqualTo(4);
        assertThat(listAvis.get(1).getNote()).isEqualTo(2);

        verify(avisRepository).findByUtilisateurIdOrderByDatePublicationDesc(utilisateurId);
    }


    @Test
    void getAvisByLivreTest() {

        // ARRANGE
        Integer livreId = 10;

        Avis avis1 = Avis.builder().note(5).commentaire("Super film").build();
        Avis avis2 = Avis.builder().note(1).commentaire("Film moyen").build();


        when(avisRepository.findByLivreIdOrderByDatePublicationDesc(livreId)).thenReturn(List.of(avis1, avis2));
        // ACT
        List<Avis> listAvis = avisService.getAvisByLivre(livreId);

        // ASSERT
        assertThat(listAvis).isNotEmpty();
        assertThat(listAvis).hasSize(2);
        assertThat(listAvis.get(0).getNote()).isEqualTo(5);
        assertThat(listAvis.get(1).getNote()).isEqualTo(1);

        verify(avisRepository).findByLivreIdOrderByDatePublicationDesc(livreId);
    }


}
