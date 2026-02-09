package fr.bookHub.security;

import fr.bookHub.auth.JwtAuthFilter;
import fr.bookHub.dal.UtilisateurRepository; // ✅ Import nécessaire
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // C'est ça qui active @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UtilisateurRepository utilisateurRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth ->
                    auth
                        // Tout ce qui est AUTH (Login + Register) est PUBLIC
                        .requestMatchers("/api/auth/**").permitAll()

                        // Autoriser Swagger si besoin
                        //.requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        // TOUT LE RESTE nécessite juste d'être CONNECTÉ
                        // On ne gère pas les rôles ici, on le fera dans les contrôleurs !
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // --- Pour connecter Spring à la base de données ---

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // 1. On lui dit comment trouver l'utilisateur (UserDetailsService)
        authProvider.setUserDetailsService(userDetailsService());

        // 2. On lui dit comment vérifier le mot de passe
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Permet d'injecter l'AuthenticationManager ailleurs si besoin (ex: AuthController)
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * C'est ce Bean qui fait le lien entre "Spring Security" et "UtilisateurRepository".
     * Il convertit l'entité Utilisateur en un objet que Spring comprend (UserDetails).
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> utilisateurRepository.findByEmail(email)
                .map(u -> User.builder()
                        .username(u.getEmail())
                        .password(u.getPassword())
                        .roles(u.getRole().getNom().name()) // Convertit l'Enum en Rôle Spring
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable avec l'email : " + email));
    }

    /*
        Spring Security va donc :
        Appeler ce Bean avec l'email.
        le repo cherche l'user.
        Si trouvé, on le transforme en User technique de Spring (avec username, password et roles).
        Si pas trouvé, on lance l'exception standard.
    */
}