package fr.bookHub.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {


    @GetMapping("/")
    public String home() {
        return " BookHub ";
    }

    @GetMapping("/public/ping")
    public String ping() {
        return "BookHub backend";
    }

}
