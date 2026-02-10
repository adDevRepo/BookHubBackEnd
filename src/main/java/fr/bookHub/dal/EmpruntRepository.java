package fr.bookHub.dal;

import fr.bookHub.bo.Emprunt;
import fr.bookHub.bo.enums.StatutEmprunt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmpruntRepository extends JpaRepository<Emprunt, Integer> {

    // Récupérer l'historique complet via l'ID (plus performant que passer l'entité User)
    List<Emprunt> findByUtilisateurId(Integer userId);

    // Récupérer les emprunts selon un statut précis (ex: EN_COURS)
    List<Emprunt> findByUtilisateurIdAndStatut(Integer userId, StatutEmprunt statut);

    // Compter les emprunts actifs (En cours ou En retard)
    // Utile pour la règle "Max 3 livres"
    @Query("SELECT COUNT(e) FROM Emprunt e WHERE e.utilisateur.id = :userId AND e.statut IN :statuts")
    long countEmpruntsActifs(@Param("userId") Integer userId, @Param("statuts") List<StatutEmprunt> statuts);

    // Vérifier s'il y a au moins un emprunt en retard (Bloquant)
    boolean existsByUtilisateurIdAndStatut(Integer userId, StatutEmprunt statut);

    boolean existsByUtilisateurIdAndLivreIdAndStatutIn(Integer userId, Integer livreId, List<StatutEmprunt> statuts);

    // OPTIMISATION : Trouver les retards directement en SQL
    // Critère : Date retour réelle est NULL ET Date prévue < Aujourd'hui
    @Query("SELECT e FROM Emprunt e WHERE e.dateRetourReel IS NULL AND e.dateRetourPrevue < :today")
    List<Emprunt> findEmpruntsEnRetard(@Param("today") LocalDate today);

}