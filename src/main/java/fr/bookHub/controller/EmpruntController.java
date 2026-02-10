package fr.bookHub.controller;

import fr.bookHub.bll.EmpruntService;
import fr.bookHub.bll.UtilisateurService;
import fr.bookHub.bo.Emprunt;
import fr.bookHub.bo.Utilisateur;
import fr.bookHub.dto.EmpruntDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class EmpruntController {

    private final EmpruntService empruntService;
    private final UtilisateurService utilisateurService;

    /**
     * 1. CRÉER UN EMPRUNT (Action Bibliothécaire)
     * POST /api/loans
     * Accès : ADMIN ou LIBRARIAN
     */
    /**
     * 1. CRÉER UN EMPRUNT (Self-service ou Bibliothécaire)
     * POST /api/loans
     * Accès : Tout utilisateur connecté (READER, ADMIN, LIBRARIAN)
     */
    // On enlève le @PreAuthorize restrictif pour laisser passer les READER
    // (L'accès reste protégé par SecurityConfig .anyRequest().authenticated())
    @PostMapping
    public ResponseEntity<EmpruntDto.Response> emprunter(@Valid @RequestBody EmpruntDto.Request request) {
        try {
            // A. On récupère l'utilisateur qui fait la requête
            Utilisateur connectedUser = getUtilisateurConnecte();

            // B. On détermine QUI va être l'emprunteur final
            Integer emprunteurId = connectedUser.getId(); // Par défaut : Soi-même

            // Si c'est un membre du staff, il a le droit d'emprunter pour quelqu'un d'autre
            boolean isStaff = connectedUser.getRole().getNom().name().equals("ADMIN")
                    || connectedUser.getRole().getNom().name().equals("LIBRARIAN");

            if (isStaff && request.utilisateurId() != null) {
                // Le staff a précisé un ID cible, on prend celui-là
                emprunteurId = request.utilisateurId();
            }
            // Sinon (READER ou Staff sans ID précisé) -> emprunteurId reste connectedUser.getId()

            // C. Appel du service
            Emprunt emprunt = empruntService.emprunterLivre(emprunteurId, request.livreId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(EmpruntDto.Response.fromEntity(emprunt));

        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            // Règles métier (Quota, Retard, Stock...) -> 409 Conflict
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /**
     * 2. RETOURNER UN LIVRE (Action Bibliothécaire)
     * PATCH /api/loans/{empruntId}/return
     * Accès : ADMIN ou LIBRARIAN
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PatchMapping("/{empruntId}/return")
    public ResponseEntity<EmpruntDto.Response> retourner(@PathVariable Integer empruntId) {
        try {
            Emprunt emprunt = empruntService.retournerLivre(empruntId);
            return ResponseEntity.ok(EmpruntDto.Response.fromEntity(emprunt));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 3. MES EMPRUNTS EN COURS (Utilisateur Connecté)
     * GET /api/loans/me/ongoing
     * Accès : Tout utilisateur connecté
     */
    @GetMapping("/me/ongoing")
    public ResponseEntity<List<EmpruntDto.Response>> getMesEmpruntsEnCours() {
        Utilisateur user = getUtilisateurConnecte();

        List<Emprunt> emprunts = empruntService.getEmpruntsEnCours(user.getId());

        return ResponseEntity.ok(emprunts.stream()
                .map(EmpruntDto.Response::fromEntity)
                .toList());
    }

    /**
     * 4. MON HISTORIQUE PASSÉ (Utilisateur Connecté)
     * GET /api/loans/me/history
     * Affiche SEULEMENT les livres rendus.
     */
    @GetMapping("/me/history")
    public ResponseEntity<List<EmpruntDto.Response>> getMonHistorique() {
        Utilisateur user = getUtilisateurConnecte();

        // ✅ Utilisation de la nouvelle méthode filtrée
        List<Emprunt> emprunts = empruntService.getEmpruntsTermines(user.getId());

        return ResponseEntity.ok(emprunts.stream()
                .map(EmpruntDto.Response::fromEntity)
                .toList());
    }

    /**
     * 5. HISTORIQUE D'UN UTILISATEUR SPÉCIFIQUE (Vue Bibliothécaire)
     * GET /api/loans/user/{userId}
     * Accès : ADMIN ou LIBRARIAN
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmpruntDto.Response>> getEmpruntsUser(@PathVariable Integer userId) {
        try {
            List<Emprunt> emprunts = empruntService.getEmpruntsUtilisateur(userId);
            return ResponseEntity.ok(emprunts.stream()
                    .map(EmpruntDto.Response::fromEntity)
                    .toList());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
        }
    }

    /**
     * 6. LISTE DES RETARDS (Dashboard)
     * GET /api/loans/overdue
     * Accès : ADMIN ou LIBRARIAN
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @GetMapping("/overdue")
    public ResponseEntity<List<EmpruntDto.Response>> getRetards() {
        List<Emprunt> retards = empruntService.getEmpruntsEnRetard();

        return ResponseEntity.ok(retards.stream()
                .map(EmpruntDto.Response::fromEntity)
                .toList());
    }

    // --- Helper ---
    private Utilisateur getUtilisateurConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return utilisateurService.consulterParEmail(auth.getName());
    }
}