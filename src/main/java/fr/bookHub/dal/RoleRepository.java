package fr.bookHub.dal;

import fr.bookHub.bo.Role;
import fr.bookHub.bo.enums.NomRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Utile lors de l'inscription pour récupérer l'objet Role "READER"
    Optional<Role> findByNom(NomRole nom);
}