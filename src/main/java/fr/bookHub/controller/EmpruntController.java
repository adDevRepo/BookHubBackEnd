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
@RequestMapping("/api/emprunts")
public class EmpruntController {

    private final EmpruntService empruntService;

    public EmpruntController(EmpruntService empruntService) {
        this.empruntService = empruntService;
    }

    /**
     * ✅ Créer un emprunt
     * POST /api/emprunts
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
     * POST /api/emprunts/{empruntId}/retour
     */
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @PostMapping("/{empruntId}/retour")
    public EmpruntDto retourner(@PathVariable Integer empruntId) {
        Emprunt emprunt = empruntService.retournerLivre(empruntId);
        return EmpruntDto.fromEntity(emprunt);
    }

    /**
     * ✅ Emprunts EN COURS d’un utilisateur
     * GET /api/emprunts/utilisateur/{utilisateurId}/en-cours
     */
    @GetMapping("/utilisateur/{utilisateurId}/en-cours")
    public List<EmpruntDto> empruntsEnCours(@PathVariable Integer utilisateurId) {
        return empruntService.consulterEmpruntsEnCours(utilisateurId)
                .stream()
                .map(EmpruntDto::fromEntity)
                .toList();
    }

    /**
     * ✅ Historique complet d’un utilisateur
     * GET /api/emprunts/utilisateur/{utilisateurId}/historique
     */

    @GetMapping("/utilisateur/{utilisateurId}/historique")
    public List<EmpruntDto> historique(@PathVariable Integer utilisateurId) {
        return empruntService.consulterHistoriqueUtilisateur(utilisateurId)
                .stream()
                .map(EmpruntDto::fromEntity)
                .toList();
    }

    /**
     * ✅ Tous les emprunts en retard (bibliothécaire)
     * GET /api/emprunts/en-retard
     */

    @GetMapping("/en-retard")
    public List<EmpruntDto> enRetard() {
        return empruntService.consulterEmpruntsEnRetard()
                .stream()
                .map(EmpruntDto::fromEntity)
                .toList();
    }
}
