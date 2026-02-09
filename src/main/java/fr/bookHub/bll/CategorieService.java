package fr.bookHub.bll;

import fr.bookHub.bo.Categorie;
import fr.bookHub.dto.CategorieDto;

import java.util.List;
import java.util.Optional;

public interface CategorieService {

    /**
     * Recherche d'une catégorie par son code
     */
    Optional<Categorie> findByCodeIgnoreCase(String code);


    /**
     * Liste toutes les catégories triées par nom
     * utilisé pour les recherches par filtres
     */
    List<Categorie> findAllByOrderByNomAsc();

    /**
     * Vérifie si une catégorie existe déjà avec ce nom
     */
    boolean existsByNomIgnoreCase(String nom);


    /**
     * Vérifie si une catégorie existe déjà avec ce code
     * utilisé pour création et modification
     */
    boolean existsByCodeIgnoreCase(String code);


    Categorie save(CategorieDto.Request dto);

    Optional<Categorie> findById(Integer id);

    Categorie update(Integer id, CategorieDto.Update dto);

    void deleteById(Integer id);

}