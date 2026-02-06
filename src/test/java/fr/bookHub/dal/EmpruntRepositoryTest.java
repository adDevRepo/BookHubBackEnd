package fr.bookHub.dal;

import fr.bookHub.bo.*;
import fr.bookHub.bo.enums.NomRole;
import fr.bookHub.bo.enums.StatutEmprunt;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du repository EmpruntRepository.
 *
 * Objectif :
 * - Vérifier que les méthodes Spring Data JPA fonctionnent correctement
 * - Garantir la cohérence des requêtes sur les emprunts
 * - Sécuriser les règles métier liées aux emprunts (statut, utilisateur)
 */
@DataJpaTest
@ActiveProfiles("test")
class EmpruntRepositoryTest {

    @Autowired
    private EmpruntRepository empruntRepository;

    @Autowired
    private EntityManager em;

    /**
     * Vérifie que la méthode findByUtilisateur(...)
     * retourne UNIQUEMENT les emprunts liés à l'utilisateur donné.
     */
    @Test
    void findByUtilisateur_retourneTousLesEmpruntsDeCetUtilisateur() {
        Role role = persistRole(NomRole.READER);

        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);
        Utilisateur u2 = persistUtilisateur(uniqueEmail("u2"), role);

        Categorie cat = persistCategorie("Roman", uniqueCode("ROMAN"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN-1"), cat);
        Livre l2 = persistLivre("Livre 2", "Auteur 2", uniqueIsbn("ISBN-2"), cat);

        // u1 a 2 emprunts, u2 en a 1
        persistEmprunt(u1, l1, StatutEmprunt.EN_COURS);
        persistEmprunt(u1, l2, StatutEmprunt.RETOURNE);
        persistEmprunt(u2, l1, StatutEmprunt.EN_COURS);

        em.flush();
        em.clear();

        List<Emprunt> empruntsU1 = empruntRepository.findByUtilisateur(u1);

        // On attend uniquement les emprunts de u1
        assertThat(empruntsU1).hasSize(2);
        assertThat(empruntsU1)
                .allMatch(e -> e.getUtilisateur().getId().equals(u1.getId()));
    }

    /**
     * Vérifie que la méthode findByUtilisateurIdAndStatut(...)
     * retourne seulement les emprunts EN_COURS pour un utilisateur donné.
     */
    @Test
    void findByUtilisateurIdAndStatut_retourneEmpruntsEnCours() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);

        Categorie cat = persistCategorie("Roman", uniqueCode("ROMAN"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN-1"), cat);
        Livre l2 = persistLivre("Livre 2", "Auteur 2", uniqueIsbn("ISBN-2"), cat);

        // 2 emprunts en cours, 1 retourné
        persistEmprunt(u1, l1, StatutEmprunt.EN_COURS);
        persistEmprunt(u1, l2, StatutEmprunt.EN_COURS);
        persistEmprunt(u1, l1, StatutEmprunt.RETOURNE);

        em.flush();
        em.clear();

        List<Emprunt> enCours =
                empruntRepository.findByUtilisateurIdAndStatut(
                        u1.getId(), StatutEmprunt.EN_COURS
                );

        // Seuls les emprunts EN_COURS doivent être retournés
        assertThat(enCours).hasSize(2);
        assertThat(enCours)
                .allMatch(e -> e.getStatut() == StatutEmprunt.EN_COURS);
    }

    /**
     * Vérifie que countByUtilisateurIdAndStatut(...)
     * compte correctement le nombre d'emprunts EN_COURS.
     *
     * Cas métier :
     * - règle des "maximum 3 emprunts en cours"
     */
    @Test
    void countByUtilisateurIdAndStatut_compteCorrectement() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);

        Categorie cat = persistCategorie("Roman", uniqueCode("ROMAN"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN-1"), cat);
        Livre l2 = persistLivre("Livre 2", "Auteur 2", uniqueIsbn("ISBN-2"), cat);

        persistEmprunt(u1, l1, StatutEmprunt.EN_COURS);
        persistEmprunt(u1, l2, StatutEmprunt.EN_COURS);
        persistEmprunt(u1, l1, StatutEmprunt.RETOURNE);

        em.flush();
        em.clear();

        long count =
                empruntRepository.countByUtilisateurIdAndStatut(
                        u1.getId(), StatutEmprunt.EN_COURS
                );

        assertThat(count).isEqualTo(2);
    }

    /**
     * Vérifie que existsByUtilisateurIdAndStatut(...)
     * retourne TRUE si l'utilisateur a au moins un emprunt EN_RETARD.
     *
     * Cas métier :
     * - un utilisateur avec retard est bloqué
     */
    @Test
    void existsByUtilisateurIdAndStatut_retourneVraiSiAuMoinsUn() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);

        Categorie cat = persistCategorie("Roman", uniqueCode("ROMAN"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN-1"), cat);

        persistEmprunt(u1, l1, StatutEmprunt.EN_RETARD);

        em.flush();
        em.clear();

        boolean hasRetard =
                empruntRepository.existsByUtilisateurIdAndStatut(
                        u1.getId(), StatutEmprunt.EN_RETARD
                );

        assertThat(hasRetard).isTrue();
    }

    /**
     * Vérifie que existsByUtilisateurIdAndStatut(...)
     * retourne FALSE si l'utilisateur n'a aucun emprunt EN_RETARD.
     */
    @Test
    void existsByUtilisateurIdAndStatut_retourneFauxSiAucun() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);

        Categorie cat = persistCategorie("Roman", uniqueCode("ROMAN"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN-1"), cat);

        persistEmprunt(u1, l1, StatutEmprunt.RETOURNE);

        em.flush();
        em.clear();

        boolean hasRetard =
                empruntRepository.existsByUtilisateurIdAndStatut(
                        u1.getId(), StatutEmprunt.EN_RETARD
                );

        assertThat(hasRetard).isFalse();
    }

    // ---------------- Helpers (création d'entités valides) ----------------

    private Role persistRole(NomRole nomRole) {
        Role r = new Role();
        r.setNom(nomRole);
        em.persist(r);
        return r;
    }

    private Categorie persistCategorie(String nomCategorie, String code) {
        Categorie c = new Categorie();
        c.setNom(nomCategorie);
        c.setCode(code);
        em.persist(c);
        return c;
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

    private void persistEmprunt(Utilisateur u, Livre livre, StatutEmprunt statut) {
        Emprunt e = new Emprunt();
        e.setUtilisateur(u);
        e.setLivre(livre);
        e.setStatut(statut);

        LocalDate now = LocalDate.now();
        e.setDateEmprunt(now);
        e.setDateRetourPrevue(now.plusDays(14));

        em.persist(e);
    }

    // Générateurs pour éviter les collisions de contraintes UNIQUE
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
