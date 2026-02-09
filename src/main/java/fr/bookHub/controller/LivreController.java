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

    @GetMapping
    public Page<LivreDto> getAll(Pageable pageable) {
        return livreService.consulterTous(pageable).map(LivreDto::fromEntity);
    }

    @GetMapping("/{id}")
    public LivreDto getById(@PathVariable Integer id) {
        return LivreDto.fromEntity(livreService.consulterParId(id));
    }

    @GetMapping("/search")
    public Page<LivreDto> search(@RequestParam(required = false) String motCle, Pageable pageable) {
        return livreService.rechercher(motCle, pageable).map(LivreDto::fromEntity);
    }

    @GetMapping("/categorie/{categorieId}")
    public Page<LivreDto> getByCategorie(@PathVariable Integer categorieId, Pageable pageable) {
        return livreService.consulterParCategorie(categorieId, pageable).map(LivreDto::fromEntity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivreDto create(@Valid @RequestBody LivreDto dto) {
        if (dto.categorieId() == null) {
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        }

        Livre livre = dto.toEntity();
        livre.setId(null); // on force la création (pas d'id imposé par le front)

        return LivreDto.fromEntity(livreService.creerLivre(livre));
    }

    @PutMapping("/{id}")
    public LivreDto update(@PathVariable Integer id, @Valid @RequestBody LivreDto dto) {
        if (dto.categorieId() == null) {
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        }

        Livre livre = dto.toEntity();
        livre.setId(id); // l'id du path fait foi

        return LivreDto.fromEntity(livreService.modifierLivre(id, livre));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        livreService.supprimerLivre(id);
    }
}
