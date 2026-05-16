package org.lpnu.chef_app.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lpnu.chef_app.model.*;
import org.lpnu.chef_app.model.enums.ProcessingState;
import org.lpnu.chef_app.repository.ProductRepository;
import org.lpnu.chef_app.repository.SaladRepository;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SaladControllerTest extends ApplicationTest {

    private SaladController controller;
    private ProductRepository mockProductRepo;
    private SaladRepository mockSaladRepo;
    private Product dummyProduct;

    @Override
    public void start(Stage stage) throws Exception {
        java.util.Locale.setDefault(java.util.Locale.US);

        mockProductRepo = Mockito.mock(ProductRepository.class);
        mockSaladRepo = Mockito.mock(SaladRepository.class);

        dummyProduct = new RootVegetable(1L, "Морква", 40.0, 1.0, 0.0, 9.0, 5.0);
        when(mockProductRepo.findAll()).thenReturn(List.of(dummyProduct));

        FXMLLoader loader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("org/lpnu/chef_app/views/salads-view.fxml"));
        Scene scene = new Scene(loader.load());

        controller = loader.getController();
        controller.setProductRepository(mockProductRepo);
        controller.setSaladRepository(mockSaladRepo);
        controller.loadAvailableProducts();

        stage.setScene(scene);
        stage.show();
    }

    @Test
    @DisplayName("Зміна імені та додавання інгредієнтів керує станом кнопки збереження")
    void testSaveButtonBindingFullCycle() {
        Button saveBtn = lookup("#saveButton").queryButton();
        assertTrue(saveBtn.isDisabled());

        clickOn("#saladNameField").write("Весняний");
        assertTrue(saveBtn.isDisabled());

        interact(() -> {
            TableView<Ingredient> ingredientsTable = lookup("#saladIngredientsTable").queryTableView();
            ingredientsTable.getItems().add(new Ingredient(dummyProduct, 100, ProcessingState.RAW));
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(saveBtn.isDisabled());
    }

    @Test
    @DisplayName("Пошук відфільтровує продукти за ім'ям або ID")
    void testSearchFilteringLogic() {
        TableView<Product> table = lookup("#availableProductsTable").queryTableView();
        assertEquals(1, table.getItems().size());

        // Searching non-existent
        clickOn("#searchProductField").write("Буряк");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(0, table.getItems().size());

        // Reset searching field
        doubleClickOn("#searchProductField").write("");
        // Searching by valid ID
        clickOn("#searchProductField").write("1");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(1, table.getItems().size());
    }

    @Test
    @DisplayName("updateStatistics() коректно рахує Ккал та БЖВ при додаванні елементів")
    void testStatisticsCalculation() {
        Label lblTotalWeight = lookup("#lblTotalWeight").query();
        Label lblTotalKcal = lookup("#lblTotalKcal").query();
        Label lblMacros = lookup("#lblMacros").query();

        interact(() -> {
            TableView<Ingredient> ingredientsTable = lookup("#saladIngredientsTable").queryTableView();
            // + 200g of carrot (40 kcal on 100g -> must be 80 kcal)
            ingredientsTable.getItems().add(new Ingredient(dummyProduct, 200, ProcessingState.RAW));
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("200 г", lblTotalWeight.getText());
        assertEquals("80.0", lblTotalKcal.getText());
        assertEquals("2.0 / 0.0 / 18.0", lblMacros.getText());
    }

    @Test
    @DisplayName("handleAddIngredient() успішно відкриває діалог, заповнює вагу та додає інгредієнт")
    void testAddIngredientViaDialog() {
        TableView<Product> productsTable = lookup("#availableProductsTable").queryTableView();
        TableView<Ingredient> ingredientsTable = lookup("#saladIngredientsTable").queryTableView();

        assertEquals(0, ingredientsTable.getItems().size());

        // Select Carrot
        interact(() -> productsTable.getSelectionModel().select(0));

        clickOn("#addIngredientButton");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#weightField").write("150");
        clickOn("Додати");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(1, ingredientsTable.getItems().size(), "Інгредієнт мав додатися до таблиці салату");

        Ingredient added = ingredientsTable.getItems().get(0);
        assertEquals("Морква", added.getProduct().getName());
        assertEquals(150.0, added.getWeight(), "Вага має збігатися з введеною в діалозі");
        assertEquals(org.lpnu.chef_app.model.enums.ProcessingState.RAW, added.getState());
    }

    @Test
    @DisplayName("handleRemoveIngredient() успішно видаляє обраний рядок")
    void testRemoveIngredientAction() {
        TableView<Ingredient> ingredientsTable = lookup("#saladIngredientsTable").queryTableView();

        interact(() -> {
            ingredientsTable.getItems().add(new Ingredient(dummyProduct, 100, ProcessingState.RAW));
        });
        assertEquals(1, ingredientsTable.getItems().size());

        interact(() -> ingredientsTable.getSelectionModel().select(0));
        interact(() -> controller.handleRemoveIngredient());
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(0, ingredientsTable.getItems().size());
    }

    @Test
    @DisplayName("handleClearSalad() повністю скидає форму конструктора")
    void testClearForm() {
        TextField nameField = lookup("#saladNameField").queryAs(TextField.class);
        TableView<Ingredient> ingredientsTable = lookup("#saladIngredientsTable").queryTableView();

        clickOn(nameField).write("Тест Очищення");
        interact(() -> ingredientsTable.getItems().add(new Ingredient(dummyProduct, 100, ProcessingState.RAW)));

        interact(() -> controller.handleClearSalad());

        assertTrue(nameField.getText().isEmpty());
        assertTrue(ingredientsTable.getItems().isEmpty());
    }

    @Test
    @DisplayName("loadSaladForEditing() правильно заповнює поля даними існуючого салату")
    void testLoadSaladForEditing() {
        Salad mockSalad = new Salad("Цезар");
        mockSalad.setId(42L);
        mockSalad.addIngredient(new Ingredient(dummyProduct, 150, ProcessingState.BOILED));

        interact(() -> controller.loadSaladForEditing(mockSalad));
        WaitForAsyncUtils.waitForFxEvents();

        TextField nameField = lookup("#saladNameField").queryAs(TextField.class);
        Button saveBtn = lookup("#saveButton").queryButton();

        assertEquals("Цезар", nameField.getText());
        assertEquals("Оновити рецепт", saveBtn.getText());
    }

    @Test
    @DisplayName("handleSaveSalad() викликає потрібний метод репозиторію (Новий / Редагування)")
    void testSaveAndUpdateRepositoryCalls() {
        clickOn("#saladNameField").write("Грецький");

        TableView<Ingredient> ingredientsTable = lookup("#saladIngredientsTable").queryTableView();
        interact(() -> ingredientsTable.getItems().add(new Ingredient(dummyProduct, 100, ProcessingState.RAW)));

        // SAVE
        try {
            interact(() -> controller.handleSaveSalad());
        } catch (Exception ignored) { }
        verify(mockSaladRepo, times(1)).save(any(Salad.class));

        // UPDATE
        Salad editSalad = new Salad("Старий");
        editSalad.setId(99L);
        interact(() -> controller.loadSaladForEditing(editSalad));

        try {
            interact(() -> controller.handleSaveSalad());
        } catch (Exception ignored) {}

        verify(mockSaladRepo, times(1)).update(any(Salad.class), eq(99L));
    }

    @Test
    @DisplayName("handleSaveSalad() перехоплює RuntimeException від бази і не ламає UI")
    void testSaveSaladDatabaseExceptionHandling() {
        clickOn("#saladNameField").write("Збійний Салат");
        interact(() -> {
            TableView<Ingredient> table = lookup("#saladIngredientsTable").queryTableView();
            table.getItems().add(new Ingredient(dummyProduct, 100, ProcessingState.RAW));
        });

        doThrow(new RuntimeException("Connection lost")).when(mockSaladRepo).save(any(Salad.class));
        assertDoesNotThrow(() -> interact(() -> controller.handleSaveSalad()));
    }

}