package fr.bookHub.auth;

public record AuthResponse(
        String token,
        long expiresInSeconds,
        Integer id,
        String role
) {}