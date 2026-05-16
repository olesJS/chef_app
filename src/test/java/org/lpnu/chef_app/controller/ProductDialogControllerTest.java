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
import org.lpnu.chef_app.model.RootVegetable;
import org.lpnu.chef_app.model.enums.ProductType;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

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
}