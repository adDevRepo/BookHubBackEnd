package fr.bookHub.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    // redirection page Catalogue
    @GetMapping("/")
    public String home() {
        return "Bienvenue sur BookHub ";
    }

    // Endpoint  vérifier que l’app tourne
    @GetMapping("/public/ping")
    public String ping() {
        return "BookHub backend is running";
    }

    //  Endpoint pour récupérer le CSRF
    @GetMapping("/csrf")
    public String csrf(CsrfToken token) {
        return token.getToken();
    }

    @GetMapping("/books")
    public String booksGet() {
        return "Books page (GET)  - utilise POST /books pour tester CSRF";
    }

    //Endpoint POST pour tester CSRF
    @PostMapping("/books")
    public String createBook() {
        return "Book created  (CSRF OK)";
    }
}
