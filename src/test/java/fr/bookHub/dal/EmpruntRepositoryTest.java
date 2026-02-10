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

@DataJpaTest
@ActiveProfiles("test")
class EmpruntRepositoryTest {

    @Autowired
    private EmpruntRepository empruntRepository;

    @Autowired
    private EntityManager em;

    /**
     * Teste findByUtilisateurId(...)
     * Doit retourner tous les emprunts (historique complet) via l'ID user.
     */
    @Test
    void findByUtilisateurId_retourneHistoriqueComplet() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);
        Categorie cat = persistCategorie("Roman", uniqueCode("ROMAN"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN-1"), cat);

        persistEmprunt(u1, l1, StatutEmprunt.EN_COURS);
        persistEmprunt(u1, l1, StatutEmprunt.RETOURNE);

        em.flush();
        em.clear();

        List<Emprunt> result = empruntRepository.findByUtilisateurId(u1.getId());

        assertThat(result).hasSize(2);
    }

    /**
     * Teste findByUtilisateurIdAndStatut(...)
     * Cas classique : récupérer uniquement les emprunts EN_COURS.
     */
    @Test
    void findByUtilisateurIdAndStatut_filtreCorrectement() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);
        Categorie cat = persistCategorie("Roman", uniqueCode("ROMAN"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN-1"), cat);

        persistEmprunt(u1, l1, StatutEmprunt.EN_COURS);
        persistEmprunt(u1, l1, StatutEmprunt.RETOURNE);

        em.flush();
        em.clear();

        List<Emprunt> result = empruntRepository.findByUtilisateurIdAndStatut(
                u1.getId(), StatutEmprunt.EN_COURS
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatut()).isEqualTo(StatutEmprunt.EN_COURS);
    }

    /**
     * Teste countEmpruntsActifs(...)
     * Vérifie que la méthode compte à la fois EN_COURS et EN_RETARD.
     * C'est crucial pour la règle des "3 livres max".
     */
    @Test
    void countEmpruntsActifs_compteEnCoursEtEnRetard() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);
        Categorie cat = persistCategorie("Roman", uniqueCode("ROMAN"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN-1"), cat);
        Livre l2 = persistLivre("Livre 2", "Auteur 2", uniqueIsbn("ISBN-2"), cat);
        Livre l3 = persistLivre("Livre 3", "Auteur 3", uniqueIsbn("ISBN-3"), cat);

        // 1 En cours
        persistEmprunt(u1, l1, StatutEmprunt.EN_COURS);
        // 1 En retard
        persistEmprunt(u1, l2, StatutEmprunt.EN_RETARD);
        // 1 Retourné (ne doit pas être compté)
        persistEmprunt(u1, l3, StatutEmprunt.RETOURNE);

        em.flush();
        em.clear();

        long count = empruntRepository.countEmpruntsActifs(
                u1.getId(),
                List.of(StatutEmprunt.EN_COURS, StatutEmprunt.EN_RETARD)
        );

        assertThat(count).isEqualTo(2); // 1 En cours + 1 Retard
    }

    /**
     * Teste findEmpruntsEnRetard(...)
     * Vérifie la requête JPQL optimisée.
     */
    @Test
    void findEmpruntsEnRetard_detecteLesRetards() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);
        Categorie cat = persistCategorie("Roman", uniqueCode("ROMAN"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN-1"), cat);

        LocalDate today = LocalDate.now();

        // Cas 1 : En retard (Date prévue hier, retour réel null)
        Emprunt retard = new Emprunt();
        retard.setUtilisateur(u1);
        retard.setLivre(l1);
        retard.setDateEmprunt(today.minusDays(20));
        retard.setDateRetourPrevue(today.minusDays(1)); // Hier !
        retard.setDateRetourReel(null);
        retard.setStatut(StatutEmprunt.EN_COURS); // Statut technique encore EN_COURS, mais date dépassée
        em.persist(retard);

        // Cas 2 : Pas en retard (Date prévue demain)
        Emprunt aLheure = new Emprunt();
        aLheure.setUtilisateur(u1);
        aLheure.setLivre(l1);
        aLheure.setDateEmprunt(today);
        aLheure.setDateRetourPrevue(today.plusDays(1)); // Demain
        aLheure.setDateRetourReel(null);
        aLheure.setStatut(StatutEmprunt.EN_COURS);
        em.persist(aLheure);

        // Cas 3 : Déjà rendu (même si c'était en retard, c'est fini)
        Emprunt rendu = new Emprunt();
        rendu.setUtilisateur(u1);
        rendu.setLivre(l1);
        rendu.setDateEmprunt(today.minusDays(20));
        rendu.setDateRetourPrevue(today.minusDays(5));
        rendu.setDateRetourReel(today); // Rendu !
        rendu.setStatut(StatutEmprunt.RETOURNE);
        em.persist(rendu);

        em.flush();
        em.clear();

        // Action : On cherche les retards par rapport à "Aujourd'hui"
        List<Emprunt> resultats = empruntRepository.findEmpruntsEnRetard(today);

        // Assert : Seul le Cas 1 doit remonter
        assertThat(resultats).hasSize(1);
        assertThat(resultats.get(0).getId()).isEqualTo(retard.getId());
    }

    /**
     * Teste existsByUtilisateurIdAndStatut(...)
     * Bloquant pour emprunter si true.
     */
    @Test
    void existsByUtilisateurIdAndStatut_fonctionne() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);
        Categorie cat = persistCategorie("Roman", uniqueCode("ROMAN"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN-1"), cat);

        persistEmprunt(u1, l1, StatutEmprunt.EN_RETARD);

        em.flush();
        em.clear();

        boolean exists = empruntRepository.existsByUtilisateurIdAndStatut(u1.getId(), StatutEmprunt.EN_RETARD);
        assertThat(exists).isTrue();
    }

    /**
     * Teste existsByUtilisateurIdAndLivreIdAndStatutIn(...)
     * Utilisé pour empêcher la double réservation si on a déjà le livre.
     */
    @Test
    void existsByUtilisateurIdAndLivreIdAndStatutIn_detectePossession() {
        Role role = persistRole(NomRole.READER);
        Utilisateur u1 = persistUtilisateur(uniqueEmail("u1"), role);
        Categorie cat = persistCategorie("Roman", uniqueCode("ROMAN"));
        Livre l1 = persistLivre("Livre 1", "Auteur 1", uniqueIsbn("ISBN-1"), cat);

        // L'utilisateur a le livre l1 EN_COURS
        persistEmprunt(u1, l1, StatutEmprunt.EN_COURS);

        em.flush();
        em.clear();

        boolean possedeDeja = empruntRepository.existsByUtilisateurIdAndLivreIdAndStatutIn(
                u1.getId(),
                l1.getId(),
                List.of(StatutEmprunt.EN_COURS, StatutEmprunt.EN_RETARD)
        );

        assertThat(possedeDeja).isTrue();
    }


    // ---------------- Helpers ----------------

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
        u.setNom("Test");
        u.setPrenom("User");
        u.setRole(role);
        u.setNumTelephone("0600000000");
        em.persist(u);
        return u;
    }

    private Livre persistLivre(String titre, String auteur, String isbn, Categorie categorie) {
        Livre l = new Livre();
        l.setTitre(titre);
        l.setAuteur(auteur);
        l.setIsbn(isbn);
        l.setDescription("Desc");
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

    private String uniqueEmail(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "") + "@test.com";
    }
    private String uniqueCode(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    private String uniqueIsbn(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}