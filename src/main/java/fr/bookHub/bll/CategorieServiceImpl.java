package fr.bookHub.bll;

import fr.bookHub.bo.Categorie;
import fr.bookHub.dal.CategorieRepository;
import fr.bookHub.dto.CategorieDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategorieServiceImpl implements CategorieService {

    private final CategorieRepository categorieRepository;

    public CategorieServiceImpl(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    @Override
    public Optional<Categorie> findByCodeIgnoreCase(String code) {
        return  categorieRepository.findByCodeIgnoreCase(code);
    }

    @Override
    public List<Categorie> findAllByOrderByNomAsc() {
        return  categorieRepository.findAllByOrderByNomAsc();
    }

    @Override
    public boolean existsByNomIgnoreCase(String nom) {
        return categorieRepository.existsByNomIgnoreCase(nom);
    }

    @Override
    public boolean existsByCodeIgnoreCase(String code) {
        return categorieRepository.existsByCodeIgnoreCase(code);
    }


    @Override
    @Transactional
    public Categorie save(CategorieDto.Request dto) {
        if (existsByNomIgnoreCase(dto.nom()))
            throw new IllegalArgumentException("Nom existe déjà");
        if (existsByCodeIgnoreCase(dto.code()))
            throw new IllegalArgumentException("Code existe déjà");
        // mapping DTO
        Categorie entity = CategorieDto.Request.toEntity(dto);
        return categorieRepository.save(entity);
    }

    @Override
    @Transactional
    public Optional<Categorie> findById(Integer id) {
        return categorieRepository.findById(id);
    }

    @Transactional
    @Override
    public Categorie update(Integer id, CategorieDto.Update dto) {
        Categorie c = findById(id).orElseThrow(() ->
                new IllegalArgumentException("Catégorie non trouvée"));
        // mapping DTO
        CategorieDto.Update.applyToEntity(dto, c);
        return categorieRepository.save(c);
    }

    @Transactional
    @Override
    public void deleteById(Integer id) {
        if (!categorieRepository.existsById(id))
            throw new IllegalArgumentException("Catégorie non trouvée");
        categorieRepository.deleteById(id);
    }

}
