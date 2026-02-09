package fr.bookHub.bll;

import fr.bookHub.bo.Categorie;
import fr.bookHub.bo.Emprunt;
import fr.bookHub.bo.Livre;
import fr.bookHub.bo.Role;
import fr.bookHub.bo.Utilisateur;
import fr.bookHub.bo.enums.NomRole;
import fr.bookHub.bo.enums.StatutEmprunt;
import fr.bookHub.dal.CategorieRepository;
import fr.bookHub.dal.EmpruntRepository;
import fr.bookHub.dal.LivreRepository;
import fr.bookHub.dal.RoleRepository;
import fr.bookHub.dal.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class EmpruntServiceIT {

    @Autowired EmpruntService empruntService;

    @Autowired EmpruntRepository empruntRepository;
    @Autowired LivreRepository livreRepository;
    @Autowired UtilisateurRepository utilisateurRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired CategorieRepository categorieRepository;

    private Role creerRoleReader() {
        // Si plusieurs tests passent, on évite les doublons en le cherchant d'abord
        return roleRepository.findAll().stream()
                .filter(r -> r.getNom() == NomRole.READER)   // <- si ton getter s'appelle autrement, voir note plus bas
                .findFirst()
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setNom(NomRole.READER);            // <- idem: setter peut s'appeler setName / setNom
                    return roleRepository.save(role);
                });
    }

    private Utilisateur creerUtilisateur() {
        Role role = creerRoleReader();

        Utilisateur u = new Utilisateur();
        u.setEmail("u" + System.nanoTime() + "@test.com");
        u.setPassword("pwd");
        u.setNom("Nom");
        u.setPrenom("Prenom");
        u.setRole(role);
        u.setNumTelephone("0612345678");
        return utilisateurRepository.save(u);
    }

    private Categorie creerCategorie() {
        Categorie c = new Categorie();
        c.setCode("ROM" + System.nanoTime());
        c.setNom("Romans");
        return categorieRepository.save(c);
    }

    private Livre creerLivreDispo(int dispo) {
        Categorie c = creerCategorie();

        Livre livre = Livre.builder()
                .titre("Livre")
                .auteur("Auteur")
                .isbn("ISBN-" + System.nanoTime())
                .description("desc")
                .categorie(c)
                .exemplairesTotal(Math.max(1, dispo))
                .exemplairesDispo(dispo)
                .actif(true)
                .build();

        return livreRepository.save(livre);
    }

    @Test
    void emprunterLivre_ok_decremente_stock() {
        Utilisateur u = creerUtilisateur();
        Livre l = creerLivreDispo(2);

        Emprunt e = empruntService.emprunterLivre(u.getId(), l.getId());

        assertThat(e.getId()).isNotNull();
        assertThat(e.getStatut()).isEqualTo(StatutEmprunt.EN_COURS);

        Livre reloaded = livreRepository.findById(l.getId()).orElseThrow();
        assertThat(reloaded.getExemplairesDispo()).isEqualTo(1);
    }

    @Test
    void retournerLivre_ok_incremente_stock_et_change_statut() {
        Utilisateur u = creerUtilisateur();
        Livre l = creerLivreDispo(1);

        Emprunt e = empruntService.emprunterLivre(u.getId(), l.getId());

        Emprunt retour = empruntService.retournerLivre(e.getId());

        assertThat(retour.getStatut()).isEqualTo(StatutEmprunt.RETOURNE);
        assertThat(retour.getDateRetourReel()).isNotNull();

        Livre reloaded = livreRepository.findById(l.getId()).orElseThrow();
        assertThat(reloaded.getExemplairesDispo()).isEqualTo(1);
    }

    @Test
    void emprunterLivre_refuse_si_quota_3_depasse() {
        Utilisateur u = creerUtilisateur();

        Livre l1 = creerLivreDispo(1);
        Livre l2 = creerLivreDispo(1);
        Livre l3 = creerLivreDispo(1);
        Livre l4 = creerLivreDispo(1);

        empruntService.emprunterLivre(u.getId(), l1.getId());
        empruntService.emprunterLivre(u.getId(), l2.getId());
        empruntService.emprunterLivre(u.getId(), l3.getId());

        assertThatThrownBy(() -> empruntService.emprunterLivre(u.getId(), l4.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void consulterEmpruntsEnRetard_detecte_un_retard() {
        Utilisateur u = creerUtilisateur();
        Livre l = creerLivreDispo(1);

        Emprunt e = Emprunt.builder()
                .utilisateur(u)
                .livre(l)
                .dateEmprunt(LocalDate.now().minusDays(20))
                .dateRetourPrevue(LocalDate.now().minusDays(5))
                .dateRetourReel(null)
                .statut(StatutEmprunt.EN_COURS) // ton service calcule le retard via dates
                .build();

        empruntRepository.save(e);

        var retards = empruntService.consulterEmpruntsEnRetard();
        assertThat(retards).isNotEmpty();
        assertThat(retards.stream().anyMatch(x -> x.getId().equals(e.getId()))).isTrue();
    }
}
