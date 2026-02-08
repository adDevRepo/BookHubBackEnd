package fr.bookHub.auth;

import fr.bookHub.bo.Utilisateur;
import fr.bookHub.dal.UtilisateurRepository;
import fr.bookHub.dto.LoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {

        // 1. On cherche l'utilisateur par son EMAIL (puisque LoginRequest.username recevra l'email)
        Utilisateur user = utilisateurRepository.findByEmail(req.username())
                .orElse(null);

        // 2. Vérification :
        // - Si l'utilisateur est null (email inconnu)
        // - OU si le mot de passe ne correspond pas au hash en base
        if (user == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            return ResponseEntity.status(401).body("Email ou mot de passe incorrect");
        }

        // 3. Préparation des données supplémentaires (Claims)
        // On récupère le nom de l'enum (ex: "ADMIN", "READER") via .name()
        Map<String, Object> extraClaims = Map.of(
                "id", user.getId(),
                "role", user.getRole().getNom().name()
        );

        // 4. Génération du token avec les claims
        String token = jwtService.generateToken(user.getEmail(), extraClaims);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "expiresInSeconds", jwtService.getExpirationSeconds()
        ));
    }
}