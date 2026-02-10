package fr.bookHub.controller;

import fr.bookHub.bll.EmpruntService;
import fr.bookHub.bo.Emprunt;
import fr.bookHub.dto.EmpruntDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class EmpruntController {

    private final EmpruntService empruntService;

    public EmpruntController(EmpruntService empruntService) {
        this.empruntService = empruntService;
    }

    /**
     * ✅ Créer un emprunt
     * POST /api/loans
     * Body: { "utilisateurId": 1, "livreId": 5 }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpruntDto emprunter(@Valid @RequestBody EmpruntDto dto) {
        Emprunt emprunt = empruntService.emprunterLivre(dto.utilisateurId(), dto.livreId());
        return EmpruntDto.fromEntity(emprunt);
    }

    /**
     * ✅ Retourner un emprunt
     * POST /api/loans/{empruntId}/return
     */
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @PostMapping("/{empruntId}/return")
    public EmpruntDto retourner(@PathVariable Integer empruntId) {
        Emprunt emprunt = empruntService.retournerLivre(empruntId);
        return EmpruntDto.fromEntity(emprunt);
    }

    /**
     * ✅ Emprunts EN COURS d’un utilisateur
     * GET /api/loans/{utilisateurId}/ongoing
     */
    @GetMapping("/{utilisateurId}/ongoing")
    public List<EmpruntDto> empruntsEnCours(@PathVariable Integer utilisateurId) {
        return empruntService.consulterEmpruntsEnCours(utilisateurId)
                .stream()
                .map(EmpruntDto::fromEntity)
                .toList();
    }

    /**
     * ✅ Historique complet d’un utilisateur
     * GET /api/loans/{utilisateurId}/history
     */

    @GetMapping("/{utilisateurId}/history")
    public List<EmpruntDto> historique(@PathVariable Integer utilisateurId) {
        return empruntService.consulterHistoriqueUtilisateur(utilisateurId)
                .stream()
                .map(EmpruntDto::fromEntity)
                .toList();
    }

    /**
     * ✅ Tous les emprunts en retard (bibliothécaire)
     * GET /api/loans/overdue
     */

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public List<EmpruntDto> enRetard() {
        return empruntService.consulterEmpruntsEnRetard()
                .stream()
                .map(EmpruntDto::fromEntity)
                .toList();
    }
}
