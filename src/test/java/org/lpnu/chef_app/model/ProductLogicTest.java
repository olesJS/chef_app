package org.lpnu.chef_app.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lpnu.chef_app.model.enums.Allergen;
import org.lpnu.chef_app.model.enums.ProductType;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class ProductLogicTest {

    @Test
    @DisplayName("Тест FruitingVegetable: порада при високому вмісті води")
    void testFruitingVegetableTip() {
        // 95% water content -> there must be a tip
        Product cucumber = new FruitingVegetable(1L, "Огірок", 15.0, 0.8, 0.1, 2.8, 95.0);
        assertTrue(cucumber.getPreparationTip().isPresent());
        assertEquals("Додавайте безпосередньо перед подачею, щоб уникнути надмірного виділення соку через високий вміст води.",
                cucumber.getPreparationTip().get());

        // 85% water content -> no tip
        Product tomato = new FruitingVegetable(2L, "Томат", 20.0, 1.1, 0.2, 3.9, 85.0);
        assertFalse(tomato.getPreparationTip().isPresent());
    }

    @Test
    @DisplayName("Тест LeafyVegetable: поради залежно від клітковини")
    void testLeafyVegetableTips() {
        //  > 5.0
        Product kale = new LeafyVegetable(3L, "Кейл", 49.0, 4.3, 0.9, 8.8, 6.0);
        assertEquals("Тонко нашаткуйте листя для кращого розкриття смаку та легшого засвоєння.",
                kale.getPreparationTip().get());

        // <= 5.0
        Product spinach = new LeafyVegetable(4L, "Шпинат", 23.0, 2.9, 0.4, 3.6, 2.2);
        assertEquals("Рвіть листя руками замість різання ножем, щоб запобігти передчасному окисленню та в'яненню.",
                spinach.getPreparationTip().get());
    }

    @Test
    @DisplayName("Тест TuberVegetable: рівні крохмалю")
    void testTuberVegetableStarch() {
        // > 15.0
        TuberVegetable potato = new TuberVegetable(5L, "Картопля", 77.0, 2.0, 0.4, 17.0, 16.5);
        assertTrue(potato.getPreparationTip().get().contains("схильний до втрати форми"));

        // between 5.0 and 15.0
        TuberVegetable youngPotato = new TuberVegetable(6L, "Молода картопля", 60.0, 1.5, 0.1, 12.0, 8.0);
        assertEquals("Середній рівень крохмалю забезпечує стабільну структуру та гарне утримання форми в салаті.",
                youngPotato.getPreparationTip().get());
    }

    @Test
    @DisplayName("Тест Topping: алергени та хрусткість")
    void testToppingLogic() {
        // Allergen + Crunchy
        Topping walnuts = new Topping(7L, "Волоський горіх", 654.0, 15.0, 65.0, 14.0, Allergen.NUTS, true);
        String tip = walnuts.getPreparationTip().get();

        assertTrue(tip.contains("Містить алерген"));
        assertTrue(tip.contains("хрустку текстуру"));

        // No allergens and isn't crunchy -> Optional.empty
        Topping simpleTopping = new Topping(8L, "Звичайний топінг", 100.0, 1.0, 1.0, 1.0, Allergen.NONE, false);
        assertFalse(simpleTopping.getPreparationTip().isPresent());
    }

    @Test
    @DisplayName("Перевірка геттерів базового класу Product")
    void testProductBaseGetters() {
        Product p = new Dressing(9L, "Олія", 884.0, 0.0, 100.0, 0.0, true);
        assertAll("Перевірка полів",
                () -> assertEquals(9L, p.getID()),
                () -> assertEquals("Олія", p.getName()),
                () -> assertEquals(884.0, p.getKcalPer100g()),
                () -> assertEquals(100.0, p.getFats())
        );
    }

    @Test
    @DisplayName("Тест Dressing: перевірка типу, жирної основи та порад")
    void testDressingCompletion() {
        Dressing dressing = new Dressing(1L, "Олія", 800, 0, 99, 0, true);
        assertEquals(ProductType.DRESSING, dressing.getType());
        assertTrue(dressing.getIsFatBased());

        Dressing lightDressing = new Dressing(2L, "Яблучний оцет", 21.0, 0.0, 0.0, 0.9, false);
        assertFalse(lightDressing.getIsFatBased());
        Optional<String> tip = lightDressing.getPreparationTip();
        assertFalse(tip.isPresent(), "Поради не має бути для нежирної заправки");
    }

    @Test
    @DisplayName("Тест RootVegetable: перевірка типу, вмісту цукру та логіки порад")
    void testRootVegetableCompletion() {
        RootVegetable carrot = new RootVegetable(3L, "Морква", 41, 0.9, 0.1, 9.6, 12.0);
        assertEquals(ProductType.ROOT_VEGETABLE, carrot.getType());
        assertEquals(12.0, carrot.getSugarContent());
        assertTrue(carrot.getPreparationTip().isPresent());

        // sugar content <= 10
        RootVegetable radish = new RootVegetable(4L, "Редис", 16, 0.7, 0.1, 3.4, 3.0);
        assertFalse(radish.getPreparationTip().isPresent());
    }

    @Test
    @DisplayName("Тест Fruiting та Leafy: перевірка типів та специфічних полів")
    void testFruitingAndLeafyCompletion() {
        FruitingVegetable fv = new FruitingVegetable(5L, "Перець", 26, 1, 0, 6, 92);
        assertEquals(ProductType.FRUITING_VEGETABLE, fv.getType());
        assertEquals(92.0, fv.getWaterContentPercent());

        LeafyVegetable lv = new LeafyVegetable(6L, "Салат", 15, 1.4, 0.2, 2.9, 1.2);
        assertEquals(ProductType.LEAFY_VEGETABLE, lv.getType());
        assertEquals(1.2, lv.getFiberContent());
    }

    @Test
    @DisplayName("Тест Tuber та Topping: перевірка крохмалю, алергенів та хрусткості")
    void testTuberAndToppingCompletion() {
        TuberVegetable tv = new TuberVegetable(7L, "Буряк", 43, 1.6, 0.1, 10, 4.0);
        assertEquals(ProductType.TUBER_VEGETABLE, tv.getType());
        assertEquals(4.0, tv.getStarchContent());
        // <= 5
        assertFalse(tv.getPreparationTip().isPresent());

        Topping topping = new Topping(8L, "Кунжут", 573, 18, 50, 23, null, false);
        assertEquals(ProductType.TOPPING, topping.getType());
        assertNull(topping.getAllergen());
        assertFalse(topping.getIsCrunchy());
    }
}