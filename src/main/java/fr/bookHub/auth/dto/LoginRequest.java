package fr.bookHub.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "L'email est obligatoire")
        String email,
        @NotBlank(message = "Le mot de passe est obligatoire")
        String password
) {

        // SÉCURITÉ : On écrase le toString() par défaut
        // pour éviter que le mot de passe n'apparaisse dans les logs serveur
        @Override
        public String toString() {
                return "LoginRequest{" +
                        "email='" + email + '\'' +
                        ", password='*** PROTECTED ***'" + // On masque ici
                        '}';
        }
}
