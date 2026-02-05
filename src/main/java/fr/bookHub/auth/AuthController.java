package fr.bookHub.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {

        // ✅ TEST SIMPLE (à remplacer par DB plus tard)
        if (!"user".equals(req.username()) || !"pass".equals(req.password())) {
            return ResponseEntity.status(401).body("Bad credentials");
        }

        String token = jwtService.generateToken(req.username());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "expiresInSeconds", jwtService.getExpirationSeconds()
        ));
    }
}
