package org.lpnu.chef_app.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lpnu.chef_app.model.enums.ProcessingState;
import static org.junit.jupiter.api.Assertions.*;

class SaladTest {

    @Test
    @DisplayName("Покриття логіки ID та імені")
    void testIdAndName() {
        Salad salad = new Salad("Цезар");
        salad.setId(10L);

        assertEquals(10L, salad.getId());
        assertEquals("Цезар", salad.getName());

        salad.setName("Грецький");
        assertEquals("Грецький", salad.getName());
    }

    @Test
    @DisplayName("Складний розрахунок салату з декількох інгредієнтів")
    void testComplexSaladCalculation() {
        Salad salad = new Salad("Мікс");
        Product p1 = new FruitingVegetable(1L, "Томат", 20.0, 1.0, 0.2, 4.0, 95.0);
        Product p2 = new Dressing(2L, "Олія", 800.0, 0.0, 90.0, 0.0, true);

        // 100g of raw tomatoes = 20 kcal
        salad.addIngredient(new Ingredient(p1, 100.0, ProcessingState.RAW));
        // 10g of fried oil (* 1.5) = (800 * 10 / 100) * 1.5 = 80 * 1.5 = 120 kcal
        salad.addIngredient(new Ingredient(p2, 10.0, ProcessingState.FRIED));

        assertEquals(140.0, salad.getTotalCalories(), 0.001);
        assertEquals(2, salad.getIngredients().size());
    }

    @Test
    @DisplayName("Перевірка ініціалізації списку інгредієнтів")
    void testIngredientsListNotNull() {
        Salad salad = new Salad("Тест");
        assertNotNull(salad.getIngredients());
        assertEquals(0, salad.getIngredients().size());
    }
}