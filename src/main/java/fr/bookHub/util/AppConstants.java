package fr.bookHub.util;

public class AppConstants {
    // 1. Email (Caractères spéciaux interdits)
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    // 2. Téléphone (10 chiffres exacts)
    public static final String REGEX_PHONE = "^\\d{10}$";

    // 3. Mot de passe fort (Au moins 8 car., 1 Maj, 1 min, 1 chiffre, 1 spécial)
    // OWASP Standard
    public static final String REGEX_PASSWORD = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!.*])(?=\\S+$).{8,}$";

    private AppConstants() {} // Constructeur privé pour empêcher l'instanciation
}