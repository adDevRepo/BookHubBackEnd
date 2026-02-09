package fr.bookHub.controller;

import fr.bookHub.bll.LivreService;
import fr.bookHub.bo.Livre;
import fr.bookHub.dto.LivreDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // 🔒 Gestion du catalogue (staff)
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivreDto create(@Valid @RequestBody LivreDto dto) {
        Livre livre = dto.toEntity();
        livre.setId(null); // création forcée
        return LivreDto.fromEntity(livreService.creerLivre(livre));
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @PutMapping("/{id}")
    public LivreDto update(@PathVariable Integer id, @Valid @RequestBody LivreDto dto) {
        Livre livre = dto.toEntity();
        livre.setId(id); // l'id du path fait foi
        return LivreDto.fromEntity(livreService.modifierLivre(id, livre));
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        livreService.supprimerLivre(id);
    }
}
