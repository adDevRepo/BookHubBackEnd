package fr.bookHub.bll;

import fr.bookHub.bo.Utilisateur;
import fr.bookHub.bo.enums.NomRole;
import fr.bookHub.dal.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder; // Injection de l'encodeur

    @Override
    @Transactional // Important : Si une étape échoue, on rollback tout
    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        // 1. Règle métier : Email unique
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new RuntimeException("Erreur : Cet email est déjà utilisé.");
        }

        // 2. Sécurité : Hachage du mot de passe
        String passwordHache = passwordEncoder.encode(utilisateur.getPassword());
        utilisateur.setPassword(passwordHache);

        // 3. Règle métier : Rôle par défaut
        // Si l'utilisateur n'a pas de rôle, on lui met READER par défaut
        if (utilisateur.getRole() == null) {
            utilisateur.setRole(roleService.consulterParNom(NomRole.READER));
        }

        // 4. Sauvegarde
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public Utilisateur consulterParEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'email : " + email));
    }

    @Override
    public List<Utilisateur> consulterTous() {
        return utilisateurRepository.findAll();
    }

    @Override
    @Transactional
    public void supprimerUtilisateur(Integer id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer : Utilisateur introuvable.");
        }
        utilisateurRepository.deleteById(id);
    }
}