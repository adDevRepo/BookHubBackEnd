package fr.bookHub.bll;

import fr.bookHub.bo.Role;
import fr.bookHub.bo.enums.NomRole;
import fr.bookHub.dal.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // Génère le constructeur avec les arguments 'final'
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role consulterParNom(NomRole nomRole) {
        return roleRepository.findByNom(nomRole)
                .orElseThrow(() -> new RuntimeException("Erreur: Le rôle " + nomRole + " est introuvable en base."));
    }

    @Override
    public boolean existe(NomRole nomRole) {
        return roleRepository.findByNom(nomRole).isPresent();
    }

    @Override
    public Role creer(NomRole nomRole) {
        if (existe(nomRole)) {
            return consulterParNom(nomRole);
        }
        Role role = Role.builder().nom(nomRole).build();
        return roleRepository.save(role);
    }
}