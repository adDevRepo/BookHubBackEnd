package fr.bookHub.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");

        // pas de token -> on laisse passer (Spring décidera si c'est protégé ou non)
        if (auth == null || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = auth.substring(7);

        // token invalide -> on laisse Spring refuser plus loin si endpoint protégé
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractUsername(token);

        // Si pas déjà authentifié
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 1. On extrait toutes les infos du token
            Claims claims = jwtService.extractAllClaims(token);

            // 2. On récupère le rôle (ex: "ADMIN")
            String roleName = claims.get("role", String.class); // "role" est la clé qu'on a mise dans l'AuthController

            // 3. On crée l'autorité Spring Security
            // Convention Spring : ajouter le préfixe "ROLE_" devant
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);

            var authToken = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(authority) // On donne la vraie autorité ici
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
