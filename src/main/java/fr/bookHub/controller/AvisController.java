package fr.bookHub.controller;

import fr.bookHub.bo.Avis;
import fr.bookHub.bll.AvisService;
import fr.bookHub.dto.AvisRequestDTO;
import fr.bookHub.dto.AvisResponseDTO;
import fr.bookHub.mapper.AvisMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AvisController {

    private final AvisService avisService;

    public AvisController(AvisService avisService) {
        this.avisService = avisService;
    }

    /**
     * POST /api/books/{id}/ratings   Ajouter un avis sur un livre
     */
    @PostMapping("/books/{livreId}/ratings")
    public ResponseEntity<AvisResponseDTO> createAvis(
            @PathVariable Integer livreId,
            @Valid @RequestBody AvisRequestDTO dto
    ) {

        System.out.println("COMMENTAIRE DTO = " + dto.getCommentaire());
        Avis avis = avisService.saveAvis(
                livreId,
                dto.getUtilisateurId(),
                dto.getNote(),
                dto.getCommentaire()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AvisMapper.toDto(avis));
    }

    /**
     * PUT /api/ratings/{id}  Modifier un avis
     */
    @PutMapping("/ratings/{avisId}")
    public ResponseEntity<AvisResponseDTO> updateAvis(
            @PathVariable Integer avisId,
            @Valid @RequestBody AvisRequestDTO dto
    ) {
        Avis avis = avisService.updateAvis(
                avisId,
                dto.getNote(),
                dto.getCommentaire()
        );

        return ResponseEntity.ok(AvisMapper.toDto(avis));
    }

    /**
     * DELETE /api/ratings/{id} Supprimer un avis ( Réservé au bibliothécaire )
     */
    @DeleteMapping("/ratings/{avisId}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Void> deleteAvis(@PathVariable Integer avisId) {
        avisService.deleteAvisById(avisId);
        return ResponseEntity.noContent().build();
    }


    /**
     *  GET /api/books/{id}/ratings  Afficher les avis d'un livre
     */


    @GetMapping("/books/{id}/ratings")
    public List<AvisResponseDTO> getAvisByLivre(@PathVariable Integer id) {
        return avisService.getAvisByLivre(id)
                .stream()
                .map(avis -> AvisMapper.toDto(avis))
                .toList();
    }
/**
 *  GET /api/users/{id}/ratings  Afficher les avis d'un utilisateur
 */


    @GetMapping("/users/{id}/ratings")
    public List<AvisResponseDTO> getAvisByUtilisateur(@PathVariable Integer id) {
        return avisService.getAvisByUtilisateur(id)
                .stream()
                .map(avis -> AvisMapper.toDto(avis))
                .toList();
    }
}
