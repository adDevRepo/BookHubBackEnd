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

        // ARRANGE
        when(categorieRepository.existsByCodeIgnoreCase("polar")).thenReturn(true);

        // ACT
        boolean exists = categorieService.existsByCodeIgnoreCase("polar");

        // ASSERT
        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameTest() {
        // ARRANGE
        when(categorieRepository.existsByCodeIgnoreCase("Polar")).thenReturn(true);

        // ACT
        boolean exists = categorieService.existsByCodeIgnoreCase("Polar");

        // ASSERT
        assertThat(exists).isTrue();
    }

}
