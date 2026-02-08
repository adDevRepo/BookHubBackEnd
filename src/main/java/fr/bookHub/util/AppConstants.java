package fr.bookHub.util;

public class AppConstants {
    // On définit la regex une seule fois ici pour tout le projet
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private AppConstants() {} // Constructeur privé pour empêcher l'instanciation
}