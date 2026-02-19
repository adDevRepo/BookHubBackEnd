package fr.bookHub.controller;

import fr.bookHub.bll.UtilisateurService;
import fr.bookHub.bo.Utilisateur;
import fr.bookHub.dto.UtilisateurDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    /**
     * 1. VOIR MON PROFIL
     * Endpoint : GET /api/users/me
     * Accès : Utilisateur connecté (n'importe quel rôle)
     */
    @GetMapping("/me")
    public ResponseEntity<UtilisateurDTO> getMonProfil() {
        // A. On récupère l'email de l'utilisateur actuellement connecté via le contexte de sécurité
        // (C'est le JwtAuthFilter qui a rempli ça)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailConnecte = auth.getName();

        // B. On cherche les infos complètes en BDD
        Utilisateur user = utilisateurService.consulterParEmail(emailConnecte);

        // C. On convertit en DTO pour ne pas renvoyer le mot de passe
        return ResponseEntity.ok(UtilisateurDTO.fromEntity(user));
    }

    /**
     * 2. LISTER TOUS LES UTILISATEURS
     * Endpoint : GET /api/users
     * Accès : RÉSERVÉ AUX ADMINS
     * Note : @PreAuthorize fonctionne car on a mis @EnableMethodSecurity dans SecurityConfig
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> getAllUsers() {
        // A. Récupération de la liste brute (Entités)
        List<Utilisateur> users = utilisateurService.consulterTous();

        // B. Conversion de la liste en DTOs (Stream)
        List<UtilisateurDTO> usersDtos = users.stream()
                .map(UtilisateurDTO::fromEntity) // Appel de la méthode statique
                .toList();

        return ResponseEntity.ok(usersDtos);
    }
}