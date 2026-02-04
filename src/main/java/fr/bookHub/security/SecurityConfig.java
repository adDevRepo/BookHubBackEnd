package fr.bookHub.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //  CSRF activé + token exploitable côté client (cookie lisible)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )

                //  Autorisations
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/public/**",
                                "/login",
                                "/error",
                                "/csrf"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                //  Form login + redirection forcée vers "/" après login
                .formLogin(form -> form
                        .defaultSuccessUrl("/", true)
                )

                .logout(Customizer.withDefaults());

        return http.build();
    }
}
