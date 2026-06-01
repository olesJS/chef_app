package org.lpnu.chef_app.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lpnu.chef_app.model.Product;
import org.lpnu.chef_app.model.*;
import org.lpnu.chef_app.model.enums.ProductType;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class ProductDialogControllerTest extends ApplicationTest {

    private ProductDialogController controller;
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        Locale.setDefault(Locale.US);
        this.stage = stage;

        FXMLLoader loader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("org/lpnu/chef_app/dialogs/product-dialog.fxml"));
        Scene scene = new Scene(loader.load());

        controller = loader.getController();
        controller.setDialogStage(stage);

        stage.setScene(scene);
        stage.show();
    }

    @Test
    @DisplayName("Зміна ProductType динамічно змінює поля інтерфейсу")
    void testDynamicFieldsUpdate() {
        ComboBox<ProductType> typeBox = lookup("#typeComboBox").queryComboBox();
        Label extraLabel = lookup("#extraLabel").queryAs(Label.class);

        interact(() -> typeBox.getSelectionModel().select(ProductType.ROOT_VEGETABLE));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Цукор (%):", extraLabel.getText());

        interact(() -> typeBox.getSelectionModel().select(ProductType.LEAFY_VEGETABLE));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Клітковина (%):", extraLabel.getText());
    }

    @Test
    @DisplayName("Успішне створення нового продукту")
    void testSuccessfulProductCreation() {
        clickOn("#nameField").write("Картопля");
        clickOn("#kcalField").write("80");
        clickOn("#pField").write("2.0");
        clickOn("#fField").write("0.4");
        clickOn("#cField").write("18.1");

        ComboBox<ProductType> typeBox = lookup("#typeComboBox").queryComboBox();
        interact(() -> typeBox.getSelectionModel().select(ProductType.TUBER_VEGETABLE));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#extraField").write("15.0");

        try {
            clickOn("Зберегти");
        } catch (Exception e) {
            // If JavaFX didn't find the button
            interact(() -> {
                try {
                    java.lang.reflect.Method method = ProductDialogController.class.getDeclaredMethod("handleSave");
                    method.setAccessible(true);
                    method.invoke(controller);
                } catch (Exception ex) { throw new RuntimeException(ex); }
            });
        }
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(controller.isSaveClicked(), "Прапорець збереження має бути true");

        Product result = controller.getProduct();
        assertNotNull(result);
        assertEquals("Картопля", result.getName());
        assertEquals(80.0, result.getKcalPer100g());
        assertEquals(ProductType.TUBER_VEGETABLE, result.getType());
    }

    @Test
    @DisplayName("Завантаження існуючого продукту для редагування")
    void testLoadExistingProduct() {
        Product existingProduct = new RootVegetable(10L, "Буряк", 43.0, 1.5, 0.1, 8.8, 6.0);

        interact(() -> controller.setProduct(existingProduct));
        WaitForAsyncUtils.waitForFxEvents();

        TextField nameField = lookup("#nameField").queryAs(TextField.class);
        TextField extraField = lookup("#extraField").queryAs(TextField.class);

        assertEquals("Буряк", nameField.getText());
        assertEquals("6.0", extraField.getText(), "Специфічне поле (цукор) має заповнитися");
    }

    @Test
    @DisplayName("Перевірка switch при заповненні форми діалогу для всіх типів продуктів (Редагування)")
    void testSwitchFormPopulationForAllProductTypes() {
        Product root = new RootVegetable(1L, "Морква", 41.0, 1.0, 0.0, 9.0, 5.5);
        Product tuber = new TuberVegetable(2L, "Картопля", 77.0, 2.0, 0.4, 16.0, 15.0);
        Product leafy = new LeafyVegetable(3L, "Шпинат", 23.0, 2.9, 0.4, 3.6, 2.2);
        Product fruiting = new FruitingVegetable(4L, "Томат", 18.0, 0.9, 0.2, 3.9, 94.5);
        Product dressing = new Dressing(5L, "Майонез", 680.0, 1.0, 75.0, 2.6, true);
        Product topping = new Topping(6L, "Горіх", 654.0, 15.2, 65.2, 13.7, org.lpnu.chef_app.model.enums.Allergen.NUTS, true);

        List<Product> testProducts = List.of(root, tuber, leafy, fruiting, dressing, topping);

        for (Product prod : testProducts) {
            interact(() -> {
                controller.setProduct(prod);
            });
            WaitForAsyncUtils.waitForFxEvents();

            // Checking every switch case
            switch (prod.getType()) {
                case ROOT_VEGETABLE ->
                        assertEquals("5.5", lookup("#extraField").queryAs(TextField.class).getText());
                case TUBER_VEGETABLE ->
                        assertEquals("15.0", lookup("#extraField").queryAs(TextField.class).getText());
                case LEAFY_VEGETABLE ->
                        assertEquals("2.2", lookup("#extraField").queryAs(TextField.class).getText());
                case FRUITING_VEGETABLE ->
                        assertEquals("94.5", lookup("#extraField").queryAs(TextField.class).getText());
                case DRESSING ->
                        assertTrue(lookup("#fatBasedCheckBox").queryAs(javafx.scene.control.CheckBox.class).isSelected());
                case TOPPING -> {
                    ComboBox<org.lpnu.chef_app.model.enums.Allergen> allergenBox = lookup("#allergenComboBox").queryComboBox();
                    javafx.scene.control.CheckBox crunchyBox = lookup("#crunchyCheckBox").queryAs(javafx.scene.control.CheckBox.class);
                    assertEquals(org.lpnu.chef_app.model.enums.Allergen.NUTS, allergenBox.getValue());
                    assertTrue(crunchyBox.isSelected());
                }
            }
        }
    }

    @Test
    @DisplayName("Перевірка switch при збереженні форми для створення нових типів продуктів")
    void testSwitchProductCreationOnSave() {
        // Testing creating Topping
        try {
            clickOn("#addProductButton");
        } catch (Exception ignored) {}
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#nameField").write("Кунжут");
        clickOn("#kcalField").write("573");
        clickOn("#pField").write("18");
        clickOn("#fField").write("50");
        clickOn("#cField").write("23");

        ComboBox<ProductType> typeBox = lookup("#typeComboBox").queryComboBox();
        interact(() -> typeBox.getSelectionModel().select(ProductType.TOPPING));
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<org.lpnu.chef_app.model.enums.Allergen> allergenBox = lookup("#allergenComboBox").queryComboBox();
        interact(() -> allergenBox.getSelectionModel().select(org.lpnu.chef_app.model.enums.Allergen.NONE));
        clickOn("#crunchyCheckBox"); // true
        WaitForAsyncUtils.waitForFxEvents();

        // Save button triggers the switch (type)
        clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        Product createdProduct = controller.getProduct();
        assertNotNull(createdProduct);
        assertEquals(ProductType.TOPPING, createdProduct.getType());
        assertTrue(((Topping) createdProduct).getIsCrunchy());
        assertEquals(org.lpnu.chef_app.model.enums.Allergen.NONE, ((Topping) createdProduct).getAllergen());
    }

    @Test
    @DisplayName("showErrorAlert() відображає вікно помилки при некоректно заповнених полях")
    void testShowErrorAlertOnInvalidFields() {
        // Invalid form
        try {
            clickOn("#addProductButton");
        } catch (Exception ignored) {}
        WaitForAsyncUtils.waitForFxEvents();

        javafx.application.Platform.runLater(() -> clickOn("Зберегти"));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("OK");
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(controller.isSaveClicked(), "Прапорець збереження має залишатися false через помилку");
    }

    @Test
    @DisplayName("handleCancel() успішно закриває вікно діалогу без збереження змін")
    void testHandleCancelClosesDialogStage() {
        try {
            clickOn("#addProductButton");
        } catch (Exception ignored) {}
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Скасувати");
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(controller.isSaveClicked(), "isSaveClicked має бути false після скасування");
    }
}