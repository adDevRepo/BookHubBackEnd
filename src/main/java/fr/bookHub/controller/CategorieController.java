package fr.bookHub.controller;

import fr.bookHub.bll.CategorieService;
import fr.bookHub.bo.Categorie;
import fr.bookHub.dto.CategorieDto;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:4200")
public class CategorieController {

    private final CategorieService service;

    public CategorieController(CategorieService service) {
        this.service = service;
    }

    /**
     * Méthode Get, Toutes les catégories (triées nom)
     */

    @GetMapping
    public List<CategorieDto.Response> getAll() {
        return CategorieDto.Response.fromEntityList(service.findAllByOrderByNomAsc());
    }

    /**
     * Méthode Get, afficher une catégorie par ID
     */

    @GetMapping("/{id}")
    public ResponseEntity<CategorieDto.Response> getById(@PathVariable Integer id) {
        return ResponseEntity.of(
                service.findById(id).map(CategorieDto.Response::fromEntity)
        );
    }

    /**
     * Méthode Post, création d'une catégorie
     */

    @PostMapping
    public ResponseEntity<CategorieDto.Response> create(@Valid @RequestBody CategorieDto.Request dto) {
        Categorie saved = service.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategorieDto.Response.fromEntity(saved));
    }

    /**
     * Méthode Put, modifier une catégorie (Controle rôle Bibliothécaire)
     */

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<CategorieDto.Response> update(
            @PathVariable Integer id,
            @Valid @RequestBody CategorieDto.Update dto) {
        try {
            Categorie updated = service.update(id, dto);
            return ResponseEntity.ok(CategorieDto.Response.fromEntity(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Méthode Delete, supprimer une catégorie (Controle rôle Bibliothécaire)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
