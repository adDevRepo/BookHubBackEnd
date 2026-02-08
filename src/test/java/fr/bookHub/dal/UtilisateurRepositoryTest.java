package fr.bookHub.dal;

import fr.bookHub.bo.Role;
import fr.bookHub.bo.Utilisateur;
import fr.bookHub.bo.enums.NomRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UtilisateurRepositoryTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    // Variables de classe pour réutilisation dans les tests
    private Role roleReader;
    private Role roleLibrarian;
    private Utilisateur alice; // On garde une référence vers Alice pour le test de suppression

    /**
     * SETUP : S'exécute AVANT chaque méthode @Test.
     * Prépare : 2 Rôles et 3 Utilisateurs (Alice, Bob, Charlie)
     */
    @BeforeEach
    public void setUp() {
        // 1. Création des Rôles
        roleReader = Role.builder().nom(NomRole.READER).build();
        roleLibrarian = Role.builder().nom(NomRole.LIBRARIAN).build();

        roleRepository.save(roleReader);
        roleRepository.save(roleLibrarian);

        // 2. Création des Utilisateurs
        alice = Utilisateur.builder()
                .nom("Alice")
                .prenom("Merveille")
                .email("alice@test.com")
                .password("Pass1234!")
                .role(roleReader)
                .build();

        Utilisateur bob = Utilisateur.builder()
                .nom("Bob")
                .prenom("Bricoleur")
                .email("bob@test.com")
                .password("Pass1234!")
                .role(roleReader)
                .build();

        Utilisateur charlie = Utilisateur.builder()
                .nom("Charlie")
                .prenom("Oleg")
                .email("charlie@test.com")
                .password("Pass1234!")
                .role(roleLibrarian)
                .build();

        // 3. Sauvegarde
        utilisateurRepository.saveAll(List.of(alice, bob, charlie));
    }

    /**
     * Ici, on vérifie qu'on peut AJOUTER un nouvel utilisateur en plus de ceux du setUp.
     */
    @Test
    public void testCreateNewUser() {
        // Arrange
        Utilisateur denis = Utilisateur.builder()
                .nom("Denis")
                .prenom("La Malice")
                .email("denis@test.com")
                .password("Pass1234!")
                .numTelephone("0612345678")
                .role(roleReader) // On réutilise le rôle existant
                .build();

        // Act
        Utilisateur savedDenis = utilisateurRepository.save(denis);

        // Assert
        assertThat(savedDenis.getId()).isNotNull();
        assertThat(utilisateurRepository.count()).isEqualTo(4); // 3 du setup + 1 Denis
        assertThat(utilisateurRepository.existsByEmail("denis@test.com")).isTrue();
    }

    /**
     * On supprime Alice (créée dans le setUp) et on vérifie le Rôle.
     */
    @Test
    public void testDeleteUser_KeepRole() {
        // Arrange : Récupérer l'ID d'Alice et de son rôle
        Integer aliceId = alice.getId();
        Integer roleId = roleReader.getId();

        // Act : Supprimer Alice
        utilisateurRepository.deleteById(aliceId);

        // Assert

        // 1. Alice ne doit plus exister
        Optional<Utilisateur> deletedAlice = utilisateurRepository.findById(aliceId);
        assertThat(deletedAlice).isEmpty();

        // 2. Le rôle READER doit toujours être là
        Optional<Role> survivingRole = roleRepository.findById(roleId);
        assertThat(survivingRole).isPresent();
        assertThat(survivingRole.get().getNom()).isEqualTo(NomRole.READER);
    }

    @Test
    public void testFindAllUsers() {
        List<Utilisateur> allUsers = utilisateurRepository.findAll();
        assertThat(allUsers).hasSize(3);
    }

    @Test
    public void testFindByEmail() {
        // --- Cas A : L'email existe (Alice a été créée dans le setUp) ---
        Optional<Utilisateur> foundUser = utilisateurRepository.findByEmail("alice@test.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getNom()).isEqualTo("Alice");
        // Petite vérif supplémentaire pour être sûr de la cohérence
        assertThat(foundUser.get().getRole().getNom()).isEqualTo(NomRole.READER);

        // --- Cas B : L'email n'existe pas ---
        Optional<Utilisateur> unknownUser = utilisateurRepository.findByEmail("fantome@inconnu.com");

        assertThat(unknownUser).isEmpty(); // Vérifie que c'est Optional.empty()
    }

    @Test
    public void testFindUsersByRole() {
        // Chercher les LECTEURS (Alice et Bob)
        List<Utilisateur> readers = utilisateurRepository.findByRole(roleReader);
        assertThat(readers).hasSize(2);

        // Chercher les BIBLIOTHÉCAIRES (Charlie)
        List<Utilisateur> librarians = utilisateurRepository.findByRole(roleLibrarian);
        assertThat(librarians).hasSize(1);
    }
}