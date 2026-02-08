package fr.bookHub.dal;

import fr.bookHub.bo.Emprunt;
import fr.bookHub.bo.Utilisateur;
import fr.bookHub.bo.enums.StatutEmprunt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmpruntRepository extends JpaRepository<Emprunt, Integer> {

    // US-LOAN-02 : Tous les emprunts d'un utilisateur
    List<Emprunt> findByUtilisateur(Utilisateur utilisateur);

    // US-LOAN-02 (Raffiné) : Emprunts en cours d'un utilisateur
    // SELECT * FROM loan WHERE user_id = ? AND status = 'EN_COURS'
    List<Emprunt> findByUtilisateurIdAndStatut(Integer userId, StatutEmprunt statut);

    // US-LOAN-01 : Compter combien d'emprunts en cours a cet utilisateur
    // (Pour vérifier la règle des "Max 3 emprunts")
    long countByUtilisateurIdAndStatut(Integer userId, StatutEmprunt statut);

    // US-LOAN-01 : Vérifier s'il a des retards (Bloquant)
    boolean existsByUtilisateurIdAndStatut(Integer userId, StatutEmprunt statut);
}