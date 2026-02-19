package fr.bookHub.dto;

import fr.bookHub.bo.Utilisateur;

import java.time.LocalDateTime;

// Record : Idéal pour les objets de transport de données (immutable)
public record UtilisateurDTO(
        Integer id,
        String email,
        String nom,
        String prenom,
        String role, // On renvoie juste le String (ex: "ADMIN"), pas l'objet Role complet
        LocalDateTime dateCreation,
        String numTelephone
) {
    /**
     * Méthode statique de conversion (Mapping).
     * Elle transforme l'Entité (Lourde/Secrète) en DTO (Léger/Public).
     */
    public static UtilisateurDTO fromEntity(Utilisateur user) {
        // Sécurité anti-NullPointer
        if (user == null) {
            return null;
        }

        return new UtilisateurDTO(
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                // Gestion null-safe du rôle
                (user.getRole() != null && user.getRole().getNom() != null)
                        ? user.getRole().getNom().name()
                        : null,
                user.getDateCreation(),
                user.getNumTelephone()
        );
    }
}