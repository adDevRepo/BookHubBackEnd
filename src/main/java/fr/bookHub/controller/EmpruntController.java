package fr.bookHub.controller;

import fr.bookHub.bll.EmpruntService;
import fr.bookHub.bo.Emprunt;
import fr.bookHub.dto.EmpruntDto;
import org.springframework.http.HttpStatus;
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
     * POST /api/emprunts?utilisateurId=1&livreId=5
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpruntDto emprunter(@RequestParam Integer utilisateurId,
                                @RequestParam Integer livreId) {

        Emprunt emprunt = empruntService.emprunterLivre(utilisateurId, livreId);
        return EmpruntDto.fromEntity(emprunt);
    }

    /**
     * ✅ Retourner un emprunt
     * POST /api/emprunts/{empruntId}/retour
     */
    @PostMapping("/{empruntId}/retour")
    public EmpruntDto retourner(@PathVariable Integer empruntId) {

        Emprunt emprunt = empruntService.retournerLivre(empruntId);
        return EmpruntDto.fromEntity(emprunt);
    }

    /**
     * ✅ Emprunts EN COURS d’un utilisateur
     * GET /api/emprunts/utilisateur/1/en-cours
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
     * GET /api/emprunts/utilisateur/1/historique
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
