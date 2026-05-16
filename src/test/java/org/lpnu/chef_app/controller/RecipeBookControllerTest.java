package org.lpnu.chef_app.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lpnu.chef_app.model.*;
import org.lpnu.chef_app.model.enums.ProcessingState;
import org.lpnu.chef_app.repository.SaladRepository;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RecipeBookControllerTest extends ApplicationTest {

    private RecipeBookController controller;
    private SaladRepository mockSaladRepo;
    private MainController mockMainController;
    private Salad testSalad;

    @Override
    public void start(Stage stage) throws Exception {
        Locale.setDefault(Locale.US); // Double format (0.0 instead of 0,0)

        mockSaladRepo = Mockito.mock(SaladRepository.class);
        mockMainController = Mockito.mock(MainController.class);

        testSalad = new Salad("Цезар");
        testSalad.setId(101L);

        Product chicken = new RootVegetable(1L, "Курка", 120.0, 20.0, 4.0, 0.0, 0.0);
        Product saladLeaves = new RootVegetable(2L, "Салат", 15.0, 1.0, 0.0, 2.0, 0.0);

        testSalad.addIngredient(new Ingredient(chicken, 100.0, ProcessingState.RAW)); // 120 kcal
        testSalad.addIngredient(new Ingredient(saladLeaves, 200.0, ProcessingState.RAW)); // 30 kcal

        when(mockSaladRepo.findAll()).thenReturn(List.of(testSalad));

        FXMLLoader loader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("org/lpnu/chef_app/views/recipe-book-view.fxml"));
        Scene scene = new Scene(loader.load());

        controller = loader.getController();
        controller.setSaladRepository(mockSaladRepo);
        controller.setMainController(mockMainController);

        interact(() -> controller.refresh());

        stage.setScene(scene);
        stage.show();
    }

    @Test
    @DisplayName("Список салатів успішно завантажується та форматує текст комірки")
    void testSaladListViewLoadsCorrectly() {
        ListView<Salad> listView = lookup("#saladListView").queryListView();
        assertEquals(1, listView.getItems().size());

        interact(() -> listView.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        Label detailsTitle = lookup("#detailsTitleLabel").queryAs(Label.class);
        assertEquals("Рецепт \"Цезар\"", detailsTitle.getText());
    }

    @Test
    @DisplayName("Вибір салату відображає правильні сумарні калорії, макроси та розблоковує експорт")
    void testShowSaladDetails() {
        ListView<Salad> listView = lookup("#saladListView").queryListView();
        interact(() -> listView.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        Label lblTotalKcal = lookup("#lblTotalKcal").queryAs(Label.class);
        Label lblMacros = lookup("#lblMacros").queryAs(Label.class);
        Button exportBtn = lookup("#exportButton").queryButton();

        // Kcal: 120 + 30 = 150
        assertTrue(lblTotalKcal.getText().contains("150.0 ккал"));
        // P = 20+2=22, F = 4+0=4, C = 0+4=4 -> "22.0 / 4.0 / 4.0"
        assertEquals("22.0 / 4.0 / 4.0", lblMacros.getText());
        assertFalse(exportBtn.isDisabled(), "Кнопка експорту має стати активною");
    }

    @Test
    @DisplayName("Пошук відфільтровує салати за назвою")
    void testSaladSearch() {
        ListView<Salad> listView = lookup("#saladListView").queryListView();
        TextField searchField = lookup("#searchSaladField").queryAs(TextField.class);

        assertEquals(1, listView.getItems().size());

        clickOn(searchField).write("Олів'є");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(0, listView.getItems().size(), "Салат 'Цезар' має зникнути");

        doubleClickOn(searchField).write("");
        clickOn(searchField).write("Цезар");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(1, listView.getItems().size());
    }

    @Test
    @DisplayName("Фільтр калорійності інгредієнтів (handleFilterIngredients) працює коректно")
    void testIngredientKcalFiltering() {
        ListView<Salad> listView = lookup("#saladListView").queryListView();
        interact(() -> listView.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        TableView<Ingredient> detailsTable = lookup("#detailsTable").queryTableView();
        assertEquals(2, detailsTable.getItems().size(), "Спочатку відображаються обидва інгредієнти");

        clickOn(controller.minKcalField).write("50");
        clickOn(controller.maxKcalField).write("200");

        interact(() -> controller.handleFilterIngredients());
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(1, detailsTable.getItems().size());
        assertEquals("Курка", detailsTable.getItems().get(0).getProduct().getName());

        // Resetting filter
        interact(() -> controller.handleResetFilter());
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(2, detailsTable.getItems().size(), "Після скидання знову має бути 2 елементи");
    }

    @Test
    @DisplayName("Сортування інгредієнтів у таблиці через ComboBox змінює порядок")
    void testSortingIngredients() {
        ListView<Salad> listView = lookup("#saladListView").queryListView();
        interact(() -> listView.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<String> sortBox = lookup("#sortComboBox").queryComboBox();
        TableView<Ingredient> detailsTable = lookup("#detailsTable").queryTableView();

        // Калорійність ↑
        interact(() -> sortBox.getSelectionModel().select("Калорійністю ↑"));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Курка", detailsTable.getItems().get(0).getProduct().getName());

        // Калорійність ↓
        interact(() -> sortBox.getSelectionModel().select("Калорійністю ↓"));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Салат", detailsTable.getItems().get(0).getProduct().getName());
    }

    @Test
    @DisplayName("handleEditSalad() успішно перемикає додаток у режим редагування через MainController")
    void testEditSaladNavigation() {
        ListView<Salad> listView = lookup("#saladListView").queryListView();
        interact(() -> listView.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> controller.handleEditSalad());
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockMainController, times(1)).switchToEditMode(testSalad);
    }

    @Test
    @DisplayName("handleDeleteSalad() ігнорує виклик, якщо салат не обрано")
    void testDeleteActionWithNoSelection() {
        ListView<Salad> listView = lookup("#saladListView").queryListView();
        interact(() -> listView.getSelectionModel().clearSelection());

        assertDoesNotThrow(() -> interact(() -> controller.handleDeleteSalad()));
        verify(mockSaladRepo, never()).delete(anyLong());
    }
}