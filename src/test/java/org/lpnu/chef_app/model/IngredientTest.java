package org.lpnu.chef_app.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lpnu.chef_app.model.enums.ProcessingState;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {
    private Product potato;

    @BeforeEach
    void setUp() {
        potato = new TuberVegetable(1L, "Картопля", 77.0, 2.0, 0.4, 17.0, 16.0);
    }

    @Test
    @DisplayName("Перевірка обчислення КБЖВ")
    void testAllCalculations() {
        // 2g of fried potatoes (kcalFactor = 1.5)
        Ingredient ing = new Ingredient(potato, 200.0, ProcessingState.FRIED);
        // Kcal: (77 * 200 / 100) * 1.5 = 154 * 1.5 = 231
        assertEquals(231.0, ing.calculateCalories(), 0.001);
        // Proteins: 2.0 * 200 / 100 = 4.0 (doesn't depend)
        assertEquals(4.0, ing.calculateProteins(), 0.001);
        // Fats: (0.4 * 200 / 100) * 1.5 = 0.8 * 1.5 = 1.2
        assertEquals(1.2, ing.calculateFats(), 0.001);
        // Carbs: (17 * 200 / 100) * 1.5 = 34 * 1.5 = 51
        assertEquals(51.0, ing.calculateCarbs(), 0.001);
    }

    @Test
    @DisplayName("Перевірка геттерів та зв'язку з порадами")
    void testGettersAndTips() {
        Ingredient ing = new Ingredient(potato, 150.0, ProcessingState.BOILED);

        assertEquals(potato, ing.getProduct());
        assertEquals(150.0, ing.getWeight());
        assertEquals(ProcessingState.BOILED, ing.getState());

        Optional<String> tip = ing.getCookingTip();
        assertTrue(tip.isPresent());
        assertTrue(tip.get().contains("крохмалю"));
    }

    @Test
    @DisplayName("Граничне значення: нульова вага")
    void testZeroWeight() {
        Ingredient ing = new Ingredient(potato, 0.0, ProcessingState.RAW);
        assertEquals(0.0, ing.calculateCalories());
        assertEquals(0.0, ing.getFinalWeight());
    }
}