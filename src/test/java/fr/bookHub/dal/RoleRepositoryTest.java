package fr.bookHub.dal;

import fr.bookHub.bo.Role;
import fr.bookHub.bo.Utilisateur;
import fr.bookHub.bo.enums.NomRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    /**
     * Test 1 : Vérifier qu'on peut créer et retrouver un rôle
     */
    @Test
    public void testCreateAndFindRole() {
        // 1. Arrange : On crée le rôle "LIBRARIAN"
        Role roleLibrarian = Role.builder()
                .nom(NomRole.LIBRARIAN)
                .build();

        roleRepository.save(roleLibrarian);

        // 2. Act : On essaie de le récupérer par son Enum
        Optional<Role> foundRole = roleRepository.findByNom(NomRole.LIBRARIAN);

        // 3. Assert
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getNom()).isEqualTo(NomRole.LIBRARIAN);
    }

    /**
     * Test 2 : Vérifier l'Intégrité Référentielle
     * On ne doit PAS pouvoir supprimer un rôle si un utilisateur l'utilise.
     */
    @Test
    public void testDeleteRole_LinkedToUser_ShouldFail() {
        // --- 1. ARRANGE (Préparation avec TestEntityManager) ---

        // A. On persiste le Rôle
        Role roleReader = Role.builder().nom(NomRole.READER).build();

        // persistAndFlush : Sauvegarde et envoie tout de suite le SQL (INSERT)
        // Il retourne l'entité "gérée" (avec son ID)
        Role savedRole = entityManager.persistAndFlush(roleReader);

        // B. On persiste l'Utilisateur lié
        Utilisateur user = Utilisateur.builder()
                .nom("Doe")
                .prenom("John")
                .email("john.doe@integrity.com")
                .password("SecurePass1!")
                .role(savedRole) // Liaison avec l'entité gérée
                .build();

        entityManager.persistAndFlush(user);

        // C. Nettoyage du contexte de persistance (Cache)
        // C'est toujours nécessaire ici !
        // On veut que le test simule une suppression "à froid", comme si on arrivait
        // dans une nouvelle transaction, pour forcer Hibernate à parler à la BDD.
        entityManager.clear();

        // --- 2. ACT & ASSERT (Test du Repository) ---

        // On récupère l'ID (car l'objet savedRole est maintenant détaché suite au clear)
        Integer roleId = savedRole.getId();

        assertThatThrownBy(() -> {
            roleRepository.deleteById(roleId);
            roleRepository.flush(); // Indispensable pour déclencher l'erreur SQL
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}