package fr.bookHub.bll;

import fr.bookHub.bo.Role;
import fr.bookHub.bo.Utilisateur;
import fr.bookHub.bo.enums.NomRole;
import fr.bookHub.dal.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private RoleService roleService; // On mock le service, pas le repo ici

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;


    // --- Test Création (Succès) ---

    @Test
    void creerUtilisateur_CasNominal_AvecRoleParDefaut() {
        // ARRANGE
        Utilisateur userEntree = Utilisateur.builder()
                .email("test@bookhub.fr")
                .password("monMotDePasseSecret")
                .nom("Test")
                .prenom("User")
                .role(null) // Pas de rôle fourni -> doit devenir READER
                .build();

        // 1. Email n'existe pas
        when(utilisateurRepository.existsByEmail(userEntree.getEmail())).thenReturn(false);

        // 2. Mock encodage password
        when(passwordEncoder.encode("monMotDePasseSecret")).thenReturn("HASHED_XYZ_123");

        // 3. Mock récupération rôle par défaut
        Role roleReader = Role.builder().id(1).nom(NomRole.READER).build();
        when(roleService.consulterParNom(NomRole.READER)).thenReturn(roleReader);

        // 4. Mock sauvegarde
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(i -> {
            Utilisateur u = i.getArgument(0);
            u.setId(10);
            return u;
        });

        // ACT
        Utilisateur resultat = utilisateurService.creerUtilisateur(userEntree);

        // ASSERT
        assertNotNull(resultat.getId());

        // Vérification Sécurité : Le mot de passe stocké doit être le HASH
        assertEquals("HASHED_XYZ_123", resultat.getPassword());

        // Vérification Métier : Le rôle doit être READER
        assertNotNull(resultat.getRole());
        assertEquals(NomRole.READER, resultat.getRole().getNom());

        // Vérifications des appels
        verify(passwordEncoder).encode("monMotDePasseSecret");
        verify(roleService).consulterParNom(NomRole.READER);
        verify(utilisateurRepository).save(any());
    }

    @Test
    void creerUtilisateur_NeDoitPasEcraserRole_SiFourni() {
        // ARRANGE
        Role roleAdmin = Role.builder().id(2).nom(NomRole.ADMIN).build();
        Utilisateur userAdmin = Utilisateur.builder()
                .email("admin@bookhub.fr")
                .password("pass")
                .role(roleAdmin) // Rôle fourni !
                .build();

        when(utilisateurRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(utilisateurRepository.save(any())).thenReturn(userAdmin);

        // ACT
        utilisateurService.creerUtilisateur(userAdmin);

        // ASSERT
        // On vérifie que le service n'a PAS cherché à mettre le rôle READER
        verify(roleService, never()).consulterParNom(any());
    }


    // --- Test Création (Erreur) ---

    @Test
    void creerUtilisateur_DoitEchouer_SiEmailExisteDeja() {
        // ARRANGE
        Utilisateur user = Utilisateur.builder().email("doublon@test.fr").build();
        when(utilisateurRepository.existsByEmail("doublon@test.fr")).thenReturn(true);

        // ACT & ASSERT
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> utilisateurService.creerUtilisateur(user));

        assertEquals("Erreur : Cet email est déjà utilisé.", ex.getMessage());

        // On vérifie que rien n'a été sauvegardé
        verify(utilisateurRepository, never()).save(any());
    }


    // --- Autres méthodes ---

    @Test
    void consulterParEmail_DoitRetournerUtilisateur() {
        // ARRANGE
        String email = "existe@test.fr";
        Utilisateur u = Utilisateur.builder().email(email).build();
        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(u));

        // ACT
        Utilisateur res = utilisateurService.consulterParEmail(email);

        // ASSERT
        assertEquals(email, res.getEmail());
    }

    @Test
    void consulterParEmail_DoitEchouer_SiIntrouvable() {
        // ARRANGE
        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class,
                () -> utilisateurService.consulterParEmail("inconnu@test.fr"));
    }

    @Test
    void supprimerUtilisateur_DoitEchouer_SiIdInconnu() {
        // ARRANGE
        Integer idInconnu = 999;
        when(utilisateurRepository.existsById(idInconnu)).thenReturn(false);

        // ACT & ASSERT
        assertThrows(RuntimeException.class,
                () -> utilisateurService.supprimerUtilisateur(idInconnu));

        // Vérifie qu'on n'a pas appelé delete
        verify(utilisateurRepository, never()).deleteById(anyInt());
    }
}