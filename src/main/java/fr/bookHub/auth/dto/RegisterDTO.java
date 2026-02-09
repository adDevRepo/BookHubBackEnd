package fr.bookHub.auth.dto;

import fr.bookHub.bo.Utilisateur;
import fr.bookHub.util.AppConstants;
import jakarta.validation.constraints.*;

public record RegisterDTO(

        @NotBlank(message = "L'email est obligatoire")
        @Email(regexp = AppConstants.REGEX_EMAIL, message = "Format email invalide")
        @Size(max = 255, message = "L'email est trop long")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Pattern(regexp = AppConstants.REGEX_PASSWORD, message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial")
        String password, // EN CLAIR (sera hashé par le service)

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
        String prenom,

        // Le téléphone est optionnel dans l'entité (nullable = true),
        // mais S'IL est renseigné, il doit respecter le format.
        @Pattern(regexp = AppConstants.REGEX_PHONE, message = "Le numéro doit contenir 10 chiffres (ex: 0612345678)")
        String numTelephone
) {
    // Méthode helper pour transformer en Entité
    public Utilisateur toEntity() {
        return Utilisateur.builder()
                .email(this.email)
                .password(this.password) // Le service se chargera du hashage
                .nom(this.nom)
                .prenom(this.prenom)
                .numTelephone(this.numTelephone)
                // Le rôle et la date de création sont gérés automatiquement
                .build();
    }
}