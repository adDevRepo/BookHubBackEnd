package fr.bookHub.bll;

import fr.bookHub.bo.Categorie;
import fr.bookHub.bo.Livre;
import fr.bookHub.dal.CategorieRepository;
import fr.bookHub.dal.LivreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivreServiceTest {

    @Mock
    private LivreRepository livreRepository;

    @Mock
    private CategorieRepository categorieRepository;

    @InjectMocks
    private LivreServiceImpl livreService;

    // Objets utilitaires pour les tests
    private Livre livreTest;
    private Categorie categorieTest;

    @BeforeEach
    void setUp() {
        categorieTest = new Categorie();
        categorieTest.setId(10);
        categorieTest.setNom("Science-Fiction");

        livreTest = new Livre();
        livreTest.setId(1);
        livreTest.setTitre("Dune");
        livreTest.setAuteur("Frank Herbert");
        livreTest.setIsbn("123456789");
        livreTest.setExemplairesTotal(5);
        livreTest.setExemplairesDispo(5);
        livreTest.setCategorie(categorieTest);
    }

    // ========================================================================
    // TESTS : creerLivre
    // ========================================================================

    @Test
    void creerLivre_CasNominal_Success() {
        // GIVEN
        when(livreRepository.existsByIsbn("123456789")).thenReturn(false);
        when(categorieRepository.getOrThrow(10)).thenReturn(categorieTest);
        when(livreRepository.save(any(Livre.class))).thenReturn(livreTest);

        // WHEN
        Livre result = livreService.creerLivre(livreTest);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getTitre()).isEqualTo("Dune");
        verify(livreRepository).save(livreTest);
    }

    @Test
    void creerLivre_IsbnManquant_ThrowException() {
        // GIVEN
        livreTest.setIsbn(null);

        // WHEN / THEN
        assertThatThrownBy(() -> livreService.creerLivre(livreTest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ISBN est obligatoire");

        verify(livreRepository, never()).save(any());
    }

    @Test
    void creerLivre_IsbnDejaExistant_ThrowException() {
        // GIVEN
        when(livreRepository.existsByIsbn(livreTest.getIsbn())).thenReturn(true);

        // WHEN / THEN
        assertThatThrownBy(() -> livreService.creerLivre(livreTest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ISBN existe déjà");

        verify(livreRepository, never()).save(any());
    }

    @Test
    void creerLivre_CategorieManquante_ThrowException() {
        // GIVEN
        livreTest.setCategorie(null);
        // On mock quand même l'ISBN pour passer le premier check
        when(livreRepository.existsByIsbn(any())).thenReturn(false);

        // WHEN / THEN
        assertThatThrownBy(() -> livreService.creerLivre(livreTest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("catégorie est obligatoire");
    }

    // ========================================================================
    // TESTS : modifierLivre
    // ========================================================================

    @Test
    void modifierLivre_CasNominal_Success() {
        // GIVEN
        Livre modifs = new Livre();
        modifs.setTitre("Dune Messiah"); // Changement de titre
        modifs.setIsbn("123456789"); // Même ISBN
        modifs.setExemplairesTotal(10); // Augmentation stock
        modifs.setCategorie(categorieTest);

        when(livreRepository.findById(1)).thenReturn(Optional.of(livreTest));
        when(categorieRepository.getOrThrow(10)).thenReturn(categorieTest);
        when(livreRepository.save(any(Livre.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Livre result = livreService.modifierLivre(1, modifs);

        // THEN
        assertThat(result.getTitre()).isEqualTo("Dune Messiah");
        assertThat(result.getExemplairesTotal()).isEqualTo(10);
        assertThat(result.getExemplairesDispo()).isEqualTo(10); // 10 - 0 empruntés
        verify(livreRepository).save(livreTest);
    }

    @Test
    void modifierLivre_ChangementIsbn_Doublon_ThrowException() {
        // GIVEN
        Livre modifs = new Livre();
        modifs.setIsbn("999999999"); // Nouvel ISBN
        modifs.setCategorie(categorieTest);

        when(livreRepository.findById(1)).thenReturn(Optional.of(livreTest));
        // On simule que cet ISBN existe déjà sur un AUTRE livre
        when(livreRepository.existsByIsbnAndIdNot("999999999", 1)).thenReturn(true);

        // WHEN / THEN
        assertThatThrownBy(() -> livreService.modifierLivre(1, modifs))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Un autre livre possède déjà cet ISBN");
    }

    @Test
    void modifierLivre_ReductionStockImpossible_ThrowException() {
        // GIVEN
        // Situation actuelle : Total 5, Dispo 2 => Donc 3 empruntés
        livreTest.setExemplairesTotal(5);
        livreTest.setExemplairesDispo(2);

        // Tentative : Passer le total à 2
        Livre modifs = new Livre();
        modifs.setExemplairesTotal(2);
        modifs.setIsbn(livreTest.getIsbn());
        modifs.setCategorie(categorieTest);

        when(livreRepository.findById(1)).thenReturn(Optional.of(livreTest));
        when(categorieRepository.getOrThrow(10)).thenReturn(categorieTest);

        // WHEN / THEN
        // 2 (nouveau total) < 3 (empruntés) => Erreur attendue
        assertThatThrownBy(() -> livreService.modifierLivre(1, modifs))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Impossible de réduire le stock");
    }

    @Test
    void modifierLivre_ReductionStockValide_Success() {
        // GIVEN
        // Situation actuelle : Total 10, Dispo 8 => Donc 2 empruntés
        livreTest.setExemplairesTotal(10);
        livreTest.setExemplairesDispo(8);

        // Tentative : Passer le total à 5
        Livre modifs = new Livre();
        modifs.setExemplairesTotal(5);
        modifs.setIsbn(livreTest.getIsbn());
        modifs.setCategorie(categorieTest);

        when(livreRepository.findById(1)).thenReturn(Optional.of(livreTest));
        when(categorieRepository.getOrThrow(10)).thenReturn(categorieTest);
        when(livreRepository.save(any(Livre.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Livre result = livreService.modifierLivre(1, modifs);

        // THEN
        // Nouveau total : 5
        // Empruntés : 2 (fixe)
        // Nouveau dispo : 5 - 2 = 3
        assertThat(result.getExemplairesTotal()).isEqualTo(5);
        assertThat(result.getExemplairesDispo()).isEqualTo(3);
    }

    // ========================================================================
    // TESTS : supprimerLivre
    // ========================================================================

    @Test
    void supprimerLivre_CasNominal_Success() {
        // GIVEN
        // Tous les exemplaires sont dispo (5/5)
        when(livreRepository.findById(1)).thenReturn(Optional.of(livreTest));

        // WHEN
        livreService.supprimerLivre(1);

        // THEN
        verify(livreRepository).delete(livreTest);
    }

    @Test
    void supprimerLivre_ExemplairesEmpruntes_ThrowException() {
        // GIVEN
        // 4 dispos sur 5 total => 1 emprunté
        livreTest.setExemplairesDispo(4);
        when(livreRepository.findById(1)).thenReturn(Optional.of(livreTest));

        // WHEN / THEN
        assertThatThrownBy(() -> livreService.supprimerLivre(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("des exemplaires sont actuellement empruntés");

        verify(livreRepository, never()).delete(any());
    }

    @Test
    void supprimerLivre_Introuvable_ThrowException() {
        // GIVEN
        when(livreRepository.findById(99)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> livreService.supprimerLivre(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Livre introuvable");
    }

    // ========================================================================
    // TESTS : Lecture (Passthroughs)
    // ========================================================================

    @Test
    void consulterParId_Success() {
        when(livreRepository.findById(1)).thenReturn(Optional.of(livreTest));

        Livre result = livreService.consulterParId(1);

        assertThat(result).isEqualTo(livreTest);
    }

    @Test
    void rechercher_AvecMotCle_AppelleMethodeRecherche() {
        Pageable pageable = Pageable.unpaged();
        Page<Livre> page = new PageImpl<>(Collections.singletonList(livreTest));

        when(livreRepository.rechercherLivres("Dune", pageable)).thenReturn(page);

        Page<Livre> result = livreService.rechercher("Dune", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(livreRepository).rechercherLivres("Dune", pageable);
    }

    @Test
    void rechercher_SansMotCle_AppelleFindAll() {
        Pageable pageable = Pageable.unpaged();
        when(livreRepository.findAll(pageable)).thenReturn(Page.empty());

        livreService.rechercher("", pageable);

        verify(livreRepository).findAll(pageable); // Doit basculer sur findAll
        verify(livreRepository, never()).rechercherLivres(anyString(), any());
    }
}