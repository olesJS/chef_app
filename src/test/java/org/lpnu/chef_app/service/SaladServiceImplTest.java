package org.lpnu.chef_app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lpnu.chef_app.model.Ingredient;
import org.lpnu.chef_app.model.Product;
import org.lpnu.chef_app.model.RootVegetable;
import org.lpnu.chef_app.model.Salad;
import org.lpnu.chef_app.model.enums.ProcessingState;
import org.lpnu.chef_app.repository.SaladRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SaladServiceImplTest {

    private SaladRepository saladRepository;
    private SaladServiceImpl saladService;
    private Product testProduct;

    @BeforeEach
    public void setUp() {
        saladRepository = mock(SaladRepository.class);
        saladService = new SaladServiceImpl(saladRepository);
        testProduct = new RootVegetable(1L, "Огірок", 15.0, 0.8, 0.1, 2.8, 0.0);
    }

    @Test
    public void testGetAllSalads_Success() {
        Salad s1 = new Salad("Цезар");
        Salad s2 = new Salad("Грецький");
        when(saladRepository.findAll()).thenReturn(Arrays.asList(s1, s2));

        List<Salad> result = saladService.getAllSalads();

        assertEquals(2, result.size());
        verify(saladRepository, times(1)).findAll();
    }

    @Test
    public void testSaveSalad_NewValidSalad_CallsSave() {
        Salad salad = new Salad("Вітамінний");
        salad.addIngredient(new Ingredient(testProduct, 150.0, ProcessingState.RAW));

        assertDoesNotThrow(() -> saladService.saveSalad(salad));

        verify(saladRepository, times(1)).save(salad);
        verify(saladRepository, never()).update(any());
    }

    @Test
    public void testSaveSalad_ExistingValidSalad_CallsUpdate() {
        Salad salad = new Salad("Олів'є");
        salad.setId(10L);
        salad.addIngredient(new Ingredient(testProduct, 200.0, ProcessingState.RAW));

        assertDoesNotThrow(() -> saladService.saveSalad(salad));

        verify(saladRepository, times(1)).update(salad);
        verify(saladRepository, never()).save(any());
    }

    @Test
    public void testSaveSalad_NullSalad_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            saladService.saveSalad(null);
        });

        assertEquals("Об'єкт салату не може бути null", exception.getMessage());
        verifyNoInteractions(saladRepository);
    }

    @Test
    public void testSaveSalad_EmptyOrBlankName_ThrowsException() {
        Salad salad = new Salad("   ");
        salad.addIngredient(new Ingredient(testProduct, 100.0, ProcessingState.RAW));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            saladService.saveSalad(salad);
        });

        assertTrue(exception.getMessage().contains("Назва салату не може бути порожньою!"));
        verifyNoInteractions(saladRepository);
    }

    @Test
    public void testSaveSalad_NoIngredients_ThrowsException() {
        Salad salad = new Salad("Порожній Салат");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            saladService.saveSalad(salad);
        });

        assertTrue(exception.getMessage().contains("Рецепт салату повинен містити щонайменше один інгредієнт!"));
        verifyNoInteractions(saladRepository);
    }

    @Test
    public void testSaveSalad_NegativeOrZeroIngredientWeight_ThrowsException() {
        Salad salad = new Salad("Тестовий");
        salad.addIngredient(new Ingredient(testProduct, -50.0, ProcessingState.RAW));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            saladService.saveSalad(salad);
        });

        assertTrue(exception.getMessage().contains("має некоректну вагу (менше або рівно 0 г)!"));
        verifyNoInteractions(saladRepository);
    }

    @Test
    public void testDeleteSalad_ValidId_CallsDelete() {
        assertDoesNotThrow(() -> saladService.deleteSalad(1L));

        verify(saladRepository, times(1)).delete(1L);
    }

    @Test
    public void testDeleteSalad_NullId_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            saladService.deleteSalad(null);
        });

        assertEquals("ID салату не може бути null для видалення", exception.getMessage());
        verify(saladRepository, never()).delete(any());
    }

    @Test
    public void testDeleteSalad_RepositoryThrowsException_ThrowsRuntimeException() {
        doThrow(new RuntimeException("Database error")).when(saladRepository).delete(100L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            saladService.deleteSalad(100L);
        });

        assertTrue(exception.getMessage().contains("Не вдалося видалити салат: Database error"));
        verify(saladRepository, times(1)).delete(100L);
    }
}