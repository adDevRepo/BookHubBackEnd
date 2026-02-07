package fr.bookHub.bll;

import fr.bookHub.bo.Role;
import fr.bookHub.bo.enums.NomRole;
import fr.bookHub.dal.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    // --- Tests de consulterParNom ---

    @Test
    void consulterParNom_DoitRetournerLeRole_QuandIlExiste() {
        // ARRANGE
        NomRole nom = NomRole.ADMIN;
        Role roleAttendu = Role.builder().id(1).nom(nom).build();
        when(roleRepository.findByNom(nom)).thenReturn(Optional.of(roleAttendu));

        // ACT
        Role resultat = roleService.consulterParNom(nom);

        // ASSERT
        assertEquals(nom, resultat.getNom());
    }

    @Test
    void consulterParNom_DoitLancerException_QuandIlNexistePas() {
        // ARRANGE
        when(roleRepository.findByNom(NomRole.READER)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> roleService.consulterParNom(NomRole.READER));

        assertTrue(ex.getMessage().contains("introuvable"));
    }

    // --- Tests de creer ---

    @Test
    void creer_DoitSauvegarderNouveauRole_SiInexistant() {
        // ARRANGE
        NomRole nom = NomRole.LIBRARIAN;

        // 1. existe() appelle findByNom -> on simule qu'il ne trouve rien (empty)
        when(roleRepository.findByNom(nom)).thenReturn(Optional.empty());

        // 2. save() va être appelé -> on simule le retour d'un rôle avec ID
        when(roleRepository.save(any(Role.class))).thenAnswer(invoc -> {
            Role r = invoc.getArgument(0);
            r.setId(5); // ID généré
            return r;
        });

        // ACT
        Role resultat = roleService.creer(nom);

        // ASSERT
        assertNotNull(resultat);
        assertEquals(5, resultat.getId());
        assertEquals(nom, resultat.getNom());

        verify(roleRepository).save(any(Role.class)); // Vérifie qu'on a bien sauvegardé
    }

    @Test
    void creer_DoitRetournerRoleExistant_SansSauvegarder_SiExisteDeja() {
        // ARRANGE
        NomRole nom = NomRole.READER;
        Role roleExistant = Role.builder().id(1).nom(nom).build();

        // existe() appelle findByNom -> on retourne un Optional plein
        // consulterParNom() le rappelle ensuite
        when(roleRepository.findByNom(nom)).thenReturn(Optional.of(roleExistant));

        // ACT
        Role resultat = roleService.creer(nom);

        // ASSERT
        assertEquals(1, resultat.getId());

        // IMPORTANT : On vérifie que save() n'a JAMAIS été appelé
        verify(roleRepository, never()).save(any());
    }
}