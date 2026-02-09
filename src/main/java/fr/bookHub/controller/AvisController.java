package fr.bookHub.controller;

import fr.bookHub.bll.AvisService;
import fr.bookHub.bo.Avis;
import fr.bookHub.dto.AvisDto;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class AvisController {

    private final AvisService avisService;

    public AvisController(AvisService avisService) {
        this.avisService = avisService;
    }

    /**
     * Méthode Post, création d'un avis
     */

    @PostMapping("/books/{livreId}/ratings")
    public ResponseEntity<AvisDto.Response> createAvis(
            @PathVariable Integer livreId,
            @Valid @RequestBody AvisDto.Request dto
    ) {
        System.out.println("COMMENTAIRE DTO = " + dto.commentaire());
        Avis avis = avisService.saveAvis(
                livreId,
                dto.utilisateurId(),
                dto.note(),
                dto.commentaire()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AvisDto.Response.fromEntity(avis));
    }

    /**
     * Méthode Put, modification d'un avis
     */

    @PutMapping("/ratings/{avisId}")
    public ResponseEntity<AvisDto.Response> updateAvis(
            @PathVariable Integer avisId,
            @Valid @RequestBody AvisDto.Request dto
    ) {
        Avis avis = avisService.updateAvis(avisId, dto.note(), dto.commentaire());
        return ResponseEntity.ok(AvisDto.Response.fromEntity(avis));
    }

    /**
     * Méthode Delete, suppression d'un avis (controle role bibliothécaire
     */
    @DeleteMapping("/ratings/{avisId}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Void> deleteAvis(@PathVariable Integer avisId) {
        avisService.deleteAvisById(avisId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Méthode Get, afficher avis par Id Livre
     */
    @GetMapping("/books/{id}/ratings")
    public List<AvisDto.Response> getAvisByLivre(@PathVariable Integer id) {
        return AvisDto.Response.fromEntityList(avisService.getAvisByLivre(id));
    }

    /**
     * Méthode Get, afficher avis par Id Utilisateur
     */

    @GetMapping("/users/{id}/ratings")
    public List<AvisDto.Response> getAvisByUtilisateur(@PathVariable Integer id) {
        return AvisDto.Response.fromEntityList(avisService.getAvisByUtilisateur(id));
    }
}
