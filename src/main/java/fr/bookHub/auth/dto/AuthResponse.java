package fr.bookHub.auth.dto;

public record AuthResponse(
        String token,
        long expiresInSeconds,
        Integer id,
        String role
) {}