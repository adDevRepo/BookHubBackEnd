package fr.bookHub.auth;

import fr.bookHub.bll.UtilisateurService;
import fr.bookHub.bo.Utilisateur;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UtilisateurService utilisateurService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest req) {

        Utilisateur user;

        // 1. Récupération de l'utilisateur via le Service
        try {
            // On utilise req.email()
            user = utilisateurService.consulterParEmail(req.email());
        } catch (RuntimeException e) {
            // SÉCURITÉ : Si l'email n'existe pas, on renvoie 401 (Unauthorized)
            // On ne renvoie pas 404 pour ne pas indiquer aux pirates quels emails existent.
            return ResponseEntity.status(401).build();
        }

        // 2. Vérification du mot de passe
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            return ResponseEntity.status(401).build();
        }

        // 3. Préparation des données pour le Token (Claims)
        String roleName = user.getRole().getNom().name();

        Map<String, Object> extraClaims = Map.of(
                "id", user.getId(),
                "role", roleName
        );

        // 4. Génération du token
        String token = jwtService.generateToken(user.getEmail(), extraClaims);

        // 5. Construction de la réponse propre
        AuthResponse response = new AuthResponse(
                token,
                jwtService.getExpirationSeconds(),
                user.getId(),
                roleName // On renvoie aussi le rôle en clair pour le front
        );

        return ResponseEntity.ok(response);
    }
}