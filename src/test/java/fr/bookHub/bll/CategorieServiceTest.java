package fr.bookHub.bll;


import fr.bookHub.dal.CategorieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategorieServiceTest {

    @Mock
    private CategorieRepository categorieRepository;

    @InjectMocks
    private CategorieServiceImpl categorieService;

    @Test
    void findByCodeTest() {
        categorieService.findByCodeIgnoreCase("polar");
        verify(categorieRepository).findByCodeIgnoreCase("polar");
    }

    @Test
    void findAllByOrderByNomAsc() {
        categorieService.findAllByOrderByNomAsc();
        verify(categorieRepository).findAllByOrderByNomAsc();
    }

    @Test
    void existsByCodeTest() {
        when(categorieRepository.existsByCodeIgnoreCase("polar")).thenReturn(true);

        boolean exists = categorieService.existsByCodeIgnoreCase("polar");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameTest() {
        when(categorieRepository.existsByCodeIgnoreCase("Polar")).thenReturn(true);

        boolean exists = categorieService.existsByCodeIgnoreCase("Polar");
        assertThat(exists).isTrue();
    }

}
