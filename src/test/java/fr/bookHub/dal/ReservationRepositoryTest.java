package fr.bookHub.dal;

import fr.bookHub.bo.*;
import fr.bookHub.bo.enums.NomRole;
import fr.bookHub.bo.enums.StatutReservation;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du ReservationRepository (Spring Data JPA).
 *
 * Objectif :
 * - Vérifier les requêtes dérivées Spring Data (find/count)
 * - Garantir l'ordre de la file d'attente (rangPriorite ASC)
 * - Vérifier la cohérence des statuts
 * - Tester la logique @PrePersist (dateDemande et statut par défaut)
 */
@DataJpaTest
@ActiveProfiles("test")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EntityManager em;

    /**
     * Vérifie que findByUtilisateurId(userId) retourne uniquement
     * les réservations appartenant à cet utilisateur.
     */
    @Test
    void findByUtilisateurId_retourne_les_reservations_de_l_utilisateur() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);
        Utilisateur u2 = persistUtilisateur(uniqueEmail("u2"), role);

        Categorie cat = persistCategorie("Roman", uniqueCode("roman"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN"), cat);
        Livre l2 = persistLivre("Livre 2", "Auteur 2", uniqueIsbn("ISBN"), cat);

        // u1 a 2 réservations, u2 en a 1
        persistReservation(u1, l1, StatutReservation.EN_ATTENTE, 1);
        persistReservation(u1, l2, StatutReservation.DISPONIBLE, 1);
        persistReservation(u2, l1, StatutReservation.EN_ATTENTE, 2);

        em.flush();
        em.clear();

        List<Reservation> reservationsU1 = reservationRepository.findByUtilisateurId(u1.getId());

        assertThat(reservationsU1).hasSize(2);
        assertThat(reservationsU1)
                .allMatch(r -> r.getUtilisateur().getId().equals(u1.getId()));
    }

    /**
     * Vérifie que findByLivreIdAndStatutOrderByRangPrioriteAsc(...)
     * retourne uniquement les réservations du livre + statut demandé,
     * triées par rangPriorite croissant (1,2,3...).
     *
     * Cas métier : file d'attente d'un livre.
     */
    @Test
    void findByLivreIdAndStatutOrderByRangPrioriteAsc_retourne_file_attente_triee() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);
        Utilisateur u2 = persistUtilisateur(uniqueEmail("u2"), role);
        Utilisateur u3 = persistUtilisateur(uniqueEmail("u3"), role);

        Categorie cat = persistCategorie("SF", uniqueCode("sf"));
        Livre dune = persistLivre("Dune", "Frank Herbert", uniqueIsbn("ISBN"), cat);

        // On crée volontairement dans le désordre : rang 3, puis 1, puis 2
        persistReservation(u1, dune, StatutReservation.EN_ATTENTE, 3);
        persistReservation(u2, dune, StatutReservation.EN_ATTENTE, 1);
        persistReservation(u3, dune, StatutReservation.EN_ATTENTE, 2);

        // Une réservation du même livre mais autre statut : doit être ignorée
        persistReservation(u1, dune, StatutReservation.TERMINEE, null);

        em.flush();
        em.clear();

        List<Reservation> file =
                reservationRepository.findByLivreIdAndStatutOrderByRangPrioriteAsc(
                        dune.getId(), StatutReservation.EN_ATTENTE
                );

        assertThat(file).hasSize(3);
        assertThat(file).extracting(Reservation::getRangPriorite).containsExactly(1, 2, 3);
        assertThat(file).allMatch(r -> r.getLivre().getId().equals(dune.getId()));
        assertThat(file).allMatch(r -> r.getStatut() == StatutReservation.EN_ATTENTE);
    }

    /**
     * Vérifie que countByLivreIdAndStatut(livreId, statut)
     * compte correctement le nombre de réservations en attente.
     *
     * Cas métier : calculer le rang (combien de personnes attendent déjà).
     */
    @Test
    void countByLivreIdAndStatut_compte_correctement() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);
        Utilisateur u2 = persistUtilisateur(uniqueEmail("u2"), role);

        Categorie cat = persistCategorie("Roman", uniqueCode("roman"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN"), cat);

        persistReservation(u1, l1, StatutReservation.EN_ATTENTE, 1);
        persistReservation(u2, l1, StatutReservation.EN_ATTENTE, 2);
        persistReservation(u1, l1, StatutReservation.DISPONIBLE, 1); // pas comptée

        em.flush();
        em.clear();

        long count =
                reservationRepository.countByLivreIdAndStatut(l1.getId(), StatutReservation.EN_ATTENTE);

        assertThat(count).isEqualTo(2);
    }

    /**
     * Vérifie la logique @PrePersist de Reservation :
     * - si dateDemande est null => auto-initialisée
     * - si statut est null => EN_ATTENTE par défaut
     */
    @Test
    void prePersist_initialise_dateDemande_et_statut_par_defaut() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);

        Categorie cat = persistCategorie("Roman", uniqueCode("roman"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN"), cat);

        Reservation r = new Reservation();
        r.setUtilisateur(u1);
        r.setLivre(l1);
        r.setStatut(null); // <- pour tester le default
        r.setDateDemande(null); // <- pour tester le default
        r.setRangPriorite(1);

        em.persist(r);
        em.flush();
        em.clear();

        Reservation reloaded = em.find(Reservation.class, r.getId());

        assertThat(reloaded.getDateDemande()).isNotNull();
        assertThat(reloaded.getStatut()).isEqualTo(StatutReservation.EN_ATTENTE);
    }

    // ---------------- Helpers (création d'entités valides) ----------------

    private Role persistRole(NomRole nomRole) {
        Role r = new Role();
        r.setNom(nomRole);
        em.persist(r);
        return r;
    }

    private Utilisateur persistUtilisateur(String email, Role role) {
        Utilisateur u = new Utilisateur();
        u.setEmail(email);
        u.setPassword("Password123!");
        u.setNom("NomTest");
        u.setPrenom("PrenomTest");
        u.setRole(role);
        u.setNumTelephone("0612345678");
        em.persist(u);
        return u;
    }

    private Categorie persistCategorie(String nomCategorie, String code) {
        Categorie c = new Categorie();
        c.setNom(nomCategorie);
        c.setCode(code);
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

    private void persistReservation(Utilisateur u, Livre livre, StatutReservation statut, Integer rang) {
        Reservation r = new Reservation();
        r.setUtilisateur(u);
        r.setLivre(livre);
        r.setStatut(statut);
        r.setRangPriorite(rang);

        // dateDemande peut être null (PrePersist), mais on peut aussi la fixer
        r.setDateDemande(LocalDateTime.now());

        // les dates optionnelles restent null (disponibilité / clôture)
        em.persist(r);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "") + "@test.com";
    }

    private String uniqueCode(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String uniqueIsbn(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
