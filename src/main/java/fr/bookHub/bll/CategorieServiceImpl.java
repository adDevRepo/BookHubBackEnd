package fr.bookHub.bll;

import fr.bookHub.bo.Categorie;
import fr.bookHub.dal.CategorieRepository;
import org.springframework.stereotype.Service;

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
}
