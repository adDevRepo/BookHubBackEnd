package fr.bookHub.dal;

import fr.bookHub.bo.Role;
import fr.bookHub.bo.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    // Pour le Login : SELECT * FROM bookhub_user WHERE email = ?
    Optional<Utilisateur> findByEmail(String email);


    /*
     * Exists : Pour vérifier si un email est pris,
     * C'est beaucoup plus rapide que findByEmail (le SGBD renvoie juste "True/False" sans charger les données).
    */
    // Pour l'Inscription : SELECT count(*) FROM bookhub_user WHERE email = ?
    boolean existsByEmail(String email);


    // Trouver tous les utilisateurs ayant un rôle précis
    List<Utilisateur> findByRole(Role role);
}