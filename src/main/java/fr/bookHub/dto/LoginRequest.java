package fr.bookHub.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "L'email est obligatoire")
        String username,
        @NotBlank(message = "Le mot de passe est obligatoire")
        String password) {}
