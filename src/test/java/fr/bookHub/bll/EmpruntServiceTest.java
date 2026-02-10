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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpruntServiceImplTest {

    @Mock
    private EmpruntRepository empruntRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private LivreRepository livreRepository;
    @Mock
    private ReservationService reservationService; // Mock du service tiers

    @InjectMocks
    private EmpruntServiceImpl empruntService;

    // Objets de test
    private Utilisateur utilisateur;
    private Livre livre;
    private Emprunt empruntEnCours;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1);
        utilisateur.setNom("Dupont");

        livre = new Livre();
        livre.setId(10);
        livre.setTitre("Java pour les Nuls");
        livre.setExemplairesTotal(5);
        livre.setExemplairesDispo(5); // Stock OK par défaut

        empruntEnCours = Emprunt.builder()
                .id(100)
                .utilisateur(utilisateur)
                .livre(livre)
                .statut(StatutEmprunt.EN_COURS)
                .dateEmprunt(LocalDate.now().minusDays(5))
                .dateRetourPrevue(LocalDate.now().plusDays(9))
                .build();
    }

    // ========================================================================
    // TESTS : emprunterLivre
    // ========================================================================

    @Test
    void emprunterLivre_CasNominal_Success() {
        // GIVEN
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(livreRepository.findById(10)).thenReturn(Optional.of(livre));
        // Pas de retard
        when(empruntRepository.existsByUtilisateurIdAndStatut(1, StatutEmprunt.EN_RETARD)).thenReturn(false);
        // Quota OK (0 emprunt actuel)
        when(empruntRepository.countEmpruntsActifs(eq(1), anyList())).thenReturn(0L);
        // Simulation sauvegarde
        when(empruntRepository.save(any(Emprunt.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Emprunt result = empruntService.emprunterLivre(1, 10);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getStatut()).isEqualTo(StatutEmprunt.EN_COURS);
        assertThat(result.getDateRetourPrevue()).isEqualTo(LocalDate.now().plusDays(14));

        // Vérification Stock Décrémenté : 5 -> 4
        assertThat(livre.getExemplairesDispo()).isEqualTo(4);
        verify(livreRepository).save(livre);
    }

    @Test
    void emprunterLivre_UtilisateurIntrouvable_ThrowException() {
        when(utilisateurRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> empruntService.emprunterLivre(99, 10))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }

    @Test
    void emprunterLivre_AvecRetard_ThrowException() {
        // GIVEN
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(livreRepository.findById(10)).thenReturn(Optional.of(livre));

        // Simulation : L'utilisateur a un retard bloquant
        when(empruntRepository.existsByUtilisateurIdAndStatut(1, StatutEmprunt.EN_RETARD)).thenReturn(true);

        // WHEN / THEN
        assertThatThrownBy(() -> empruntService.emprunterLivre(1, 10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Vous avez des livres en retard");

        verify(empruntRepository, never()).save(any());
    }

    @Test
    void emprunterLivre_QuotaAtteint_ThrowException() {
        // GIVEN
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(livreRepository.findById(10)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByUtilisateurIdAndStatut(1, StatutEmprunt.EN_RETARD)).thenReturn(false);

        // Simulation : L'utilisateur a déjà 3 livres
        when(empruntRepository.countEmpruntsActifs(eq(1), anyList())).thenReturn(3L);

        // WHEN / THEN
        assertThatThrownBy(() -> empruntService.emprunterLivre(1, 10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Quota atteint");
    }

    @Test
    void emprunterLivre_StockEpuise_ThrowException() {
        // GIVEN
        livre.setExemplairesDispo(0); // Plus de stock

        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(livreRepository.findById(10)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByUtilisateurIdAndStatut(1, StatutEmprunt.EN_RETARD)).thenReturn(false);
        when(empruntRepository.countEmpruntsActifs(eq(1), anyList())).thenReturn(0L);

        // WHEN / THEN
        assertThatThrownBy(() -> empruntService.emprunterLivre(1, 10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock épuisé");
    }

    // ========================================================================
    // TESTS : retournerLivre
    // ========================================================================

    @Test
    void retournerLivre_CasNominal_SansReservation_IncrementeStock() {
        // GIVEN
        // Stock initial à 4 (1 exemplaire dehors)
        livre.setExemplairesDispo(4);
        empruntEnCours.setLivre(livre);

        when(empruntRepository.findById(100)).thenReturn(Optional.of(empruntEnCours));

        // Mock : Personne n'attend ce livre
        when(reservationService.notifierRetourLivre(livre.getId())).thenReturn(null);

        // WHEN
        Emprunt result = empruntService.retournerLivre(100);

        // THEN
        assertThat(result.getStatut()).isEqualTo(StatutEmprunt.RETOURNE);
        assertThat(result.getDateRetourReel()).isNotNull();

        // Le stock doit remonter à 5
        assertThat(livre.getExemplairesDispo()).isEqualTo(5);
        verify(livreRepository).save(livre);
    }

    @Test
    void retournerLivre_AvecReservation_NeChangePasLeStock() {
        // GIVEN
        // Stock initial à 0 (Tout emprunté)
        livre.setExemplairesDispo(0);
        empruntEnCours.setLivre(livre);

        when(empruntRepository.findById(100)).thenReturn(Optional.of(empruntEnCours));

        // Mock : Une réservation est trouvée et activée !
        Reservation reservation = new Reservation();
        reservation.setId(50);
        when(reservationService.notifierRetourLivre(livre.getId())).thenReturn(reservation);

        // WHEN
        Emprunt result = empruntService.retournerLivre(100);

        // THEN
        assertThat(result.getStatut()).isEqualTo(StatutEmprunt.RETOURNE);

        // CRUCIAL : Le stock doit rester à 0 car le livre est mis de côté pour la réservation
        assertThat(livre.getExemplairesDispo()).isEqualTo(0);

        // On ne sauve PAS le livre avec +1, donc verify save n'est pas appelé ou appelé avec 0
        // (Dans ton code, tu as un bloc if/else, si réservation -> pas de save(livre))
        verify(livreRepository, never()).save(livre);
    }

    @Test
    void retournerLivre_DejaRetourne_ThrowException() {
        // GIVEN
        empruntEnCours.setStatut(StatutEmprunt.RETOURNE);
        when(empruntRepository.findById(100)).thenReturn(Optional.of(empruntEnCours));

        // WHEN / THEN
        assertThatThrownBy(() -> empruntService.retournerLivre(100))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà retourné");
    }

    // ========================================================================
    // TESTS : Lectures
    // ========================================================================

    @Test
    void getEmpruntsUtilisateur_Success() {
        when(utilisateurRepository.existsById(1)).thenReturn(true);
        when(empruntRepository.findByUtilisateurId(1)).thenReturn(List.of(empruntEnCours));

        List<Emprunt> res = empruntService.getEmpruntsUtilisateur(1);

        assertThat(res).hasSize(1);
    }

    @Test
    void getEmpruntsUtilisateur_UserInexistant_ThrowException() {
        when(utilisateurRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> empruntService.getEmpruntsUtilisateur(99))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getEmpruntsEnRetard_AppelleRepository() {
        // On vérifie juste la délégation au repository
        empruntService.getEmpruntsEnRetard();
        verify(empruntRepository).findEmpruntsEnRetard(any(LocalDate.class));
    }
}