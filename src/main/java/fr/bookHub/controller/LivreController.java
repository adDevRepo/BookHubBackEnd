package fr.bookHub.controller;

import fr.bookHub.bll.LivreService;
import fr.bookHub.bo.Livre;
import fr.bookHub.dto.LivreDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/livres")
public class LivreController {

    private final LivreService livreService;

    public LivreController(LivreService livreService) {
        this.livreService = livreService;
    }

    // GET /api/livres?page=0&size=10&sort=titre,asc
    @GetMapping
    public Page<LivreDto> getAll(Pageable pageable) {
        return livreService.consulterTous(pageable)
                .map(LivreDto::fromEntity);
    }

    // GET /api/livres/5
    @GetMapping("/{id}")
    public LivreDto getById(@PathVariable Integer id) {
        return LivreDto.fromEntity(livreService.consulterParId(id));
    }

    // GET /api/livres/search?motCle=dune&page=0&size=10
    @GetMapping("/search")
    public Page<LivreDto> search(@RequestParam(required = false) String motCle, Pageable pageable) {
        return livreService.rechercher(motCle, pageable)
                .map(LivreDto::fromEntity);
    }

    // GET /api/livres/categorie/3?page=0&size=10
    @GetMapping("/categorie/{categorieId}")
    public Page<LivreDto> getByCategorie(@PathVariable Integer categorieId, Pageable pageable) {
        return livreService.consulterParCategorie(categorieId, pageable)
                .map(LivreDto::fromEntity);
    }

    // POST /api/livres
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivreDto create(@Valid @RequestBody Livre livre) {
        return LivreDto.fromEntity(livreService.creerLivre(livre));
    }

    // PUT /api/livres/5
    @PutMapping("/{id}")
    public LivreDto update(@PathVariable Integer id, @Valid @RequestBody Livre livre) {
        return LivreDto.fromEntity(livreService.modifierLivre(id, livre));
    }

    // DELETE /api/livres/5
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        livreService.supprimerLivre(id);
    }
}
