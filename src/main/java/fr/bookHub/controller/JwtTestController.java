package fr.bookHub.controller;

import fr.bookHub.auth.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwtTestController {

    private final JwtService jwtService;

    public JwtTestController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/public/test-jwt")
    public String testJwt() {
        return jwtService.generateToken("test-user");
    }

    @GetMapping("/private/me")
    public String me() {
        return "OK tu es authentifié";
    }

}
