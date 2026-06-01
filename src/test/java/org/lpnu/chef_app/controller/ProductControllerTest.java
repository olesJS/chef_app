package org.lpnu.chef_app.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lpnu.chef_app.model.Dressing;
import org.lpnu.chef_app.model.Product;
import org.lpnu.chef_app.model.RootVegetable;
import org.lpnu.chef_app.repository.ProductRepository;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductControllerTest extends ApplicationTest {

    private ProductController controller;
    private ProductRepository mockRepo;
    private Product p1, p2;

    @Override
    public void start(Stage stage) throws Exception {
        Locale.setDefault(Locale.US); // For doubles (0.0 instead of 0,0)

        mockRepo = Mockito.mock(ProductRepository.class);

        p1 = new RootVegetable(1L, "Морква", 41.0, 1.0, 0.0, 9.0, 5.0);
        p2 = new Dressing(2L, "Оливкова олія", 899.0, 0.0, 99.0, 0.0, true);

        when(mockRepo.findAll()).thenReturn(List.of(p1, p2));

        FXMLLoader loader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("org/lpnu/chef_app/views/products-view.fxml"));
        Scene scene = new Scene(loader.load());

        controller = loader.getController();
        controller.setProductRepository(mockRepo);

        interact(() -> controller.initialize());

        stage.setScene(scene);
        stage.show();
    }

    @Test
    @DisplayName("Таблиця успішно завантажує та відображає продукти")
    void testTableLoadsProducts() {
        TableView<Product> table = lookup("#productTable").queryTableView();
        assertEquals(2, table.getItems().size(), "В таблиці має бути 2 продукти");
    }

    @Test
    @DisplayName("Фільтр пошуку за назвою працює коректно")
    void testSearchFilter() {
        TableView<Product> table = lookup("#productTable").queryTableView();
        TextField searchField = lookup("#searchField").queryAs(TextField.class);

        clickOn(searchField).write("Морква");
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(1, table.getItems().size());
        assertEquals("Морква", table.getItems().get(0).getName());
    }

    @Test
    @DisplayName("Фільтр за категорією залишає лише відповідні продукти")
    void testCategoryFilter() {
        TableView<Product> table = lookup("#productTable").queryTableView();
        ComboBox<String> categoryBox = lookup("#categoryFilter").queryComboBox();

        String dressingDisplayName = org.lpnu.chef_app.model.enums.ProductType.DRESSING.getDisplayName();
        interact(() -> categoryBox.getSelectionModel().select(dressingDisplayName));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(1, table.getItems().size(), "В таблиці має залишитися рівно 1 продукт");
        assertEquals("Оливкова олія", table.getItems().get(0).getName());
    }

    @Test
    @DisplayName("Видалення продукту викликає метод репозиторію delete()")
    void testDeleteAction() {
        TableView<Product> table = lookup("#productTable").queryTableView();

        interact(() -> table.getSelectionModel().select(p1));
        interact(() -> controller.handleDeleteAction());

        WaitForAsyncUtils.waitForFxEvents();
        verify(mockRepo, times(1)).delete(1L);
    }

    @Test
    @DisplayName("handleEditAction() відкриває діалог з даними існуючого продукту та викликає update()")
    void testEditProductWorkflow() {
        TableView<Product> table = lookup("#productTable").queryTableView();

        interact(() -> table.getSelectionModel().select(p1));

        try {
            clickOn("#editProductButton");
        } catch (Exception e) {
            clickOn("Редагувати");
        }
        WaitForAsyncUtils.waitForFxEvents();

        TextField nameField = lookup("#nameField").queryAs(TextField.class);
        assertEquals("Морква", nameField.getText(), "Ім'я в діалозі має збігатися з обраним продуктом");

        doubleClickOn(nameField).write("Морква Свіжа");
        clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockRepo, times(1)).update(any(Product.class));
    }

    @Test
    @DisplayName("showProductDialog перехоплює RuntimeException при збереженні і показує помилку")
    void testProductDialogDatabaseExceptionHandling() {
        doThrow(new RuntimeException("Помилка зв'язку з Postgres")).when(mockRepo).save(any(Product.class));

        try {
            clickOn("#addProductButton");
        } catch (Exception e) {
            clickOn("#btnAdd");
        }
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#nameField").write("Збійний Овоч");
        clickOn("#kcalField").write("25");
        clickOn("#pField").write("1");
        clickOn("#fField").write("0");
        clickOn("#cField").write("5");

        ComboBox<org.lpnu.chef_app.model.enums.ProductType> typeBox = lookup("#typeComboBox").queryComboBox();
        interact(() -> typeBox.getSelectionModel().select(org.lpnu.chef_app.model.enums.ProductType.ROOT_VEGETABLE));
        clickOn("#extraField").write("4");

        assertDoesNotThrow(() -> {
            clickOn("Зберегти");
            WaitForAsyncUtils.waitForFxEvents();
        });
    }

    @Test
    @DisplayName("Сортування продуктів за назвою та ID змінює порядок елементів у таблиці")
    void testApplySortingLogic() {
        TableView<Product> table = lookup("#productTable").queryTableView();
        ComboBox<String> sortBox = lookup("#sortComboBox").queryComboBox();

        // Sorting by name
        interact(() -> sortBox.getSelectionModel().select("Назва (А-Я)"));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Морква", table.getItems().get(0).getName());
        assertEquals("Оливкова олія", table.getItems().get(1).getName());

        Product p3 = new RootVegetable(3L, "Авокадо", 160.0, 2.0, 14.6, 8.5, 0.5);
        when(mockRepo.findAll()).thenReturn(List.of(p1, p2, p3));

        interact(() -> controller.initialize());
        WaitForAsyncUtils.waitForFxEvents();

        // "Авокадо" must be first after sorting
        interact(() -> sortBox.getSelectionModel().select("Назва (А-Я)"));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Авокадо", table.getItems().get(0).getName());

        interact(() -> sortBox.getSelectionModel().select("ID (за зростанням)"));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(1L, table.getItems().get(0).getID());
        assertEquals(2L, table.getItems().get(1).getID());
        assertEquals(3L, table.getItems().get(2).getID());
    }

    @Test
    @DisplayName("handleDeleteAction() та handleEditAction() показують Warning Alert, якщо продукт не вибрано (блок else)")
    void testActionsWithoutSelectionTriggersShowAlert() {
        TableView<Product> table = lookup("#productTable").queryTableView();

        interact(() -> table.getSelectionModel().clearSelection());
        WaitForAsyncUtils.waitForFxEvents();

        // Testing 'else' branch for deleting
        javafx.application.Platform.runLater(() -> controller.handleDeleteAction());
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("OK");
        WaitForAsyncUtils.waitForFxEvents();

        // Testing 'else' branch for editing
        javafx.application.Platform.runLater(() -> controller.handleEditAction());
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("OK");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockRepo, never()).delete(anyLong());
    }

    @Test
    @DisplayName("Відображення специфічних деталей (getSpecificDetails) для різних підкласів Product")
    void testGetSpecificDetailsFormatting() {
        Product tuber = new org.lpnu.chef_app.model.TuberVegetable(3L, "Картопля", 77.0, 2.0, 0.4, 16.0, 15.0);
        Product fruiting = new org.lpnu.chef_app.model.FruitingVegetable(4L, "Томат", 18.0, 0.9, 0.2, 3.9, 94.5);
        Product leafy = new org.lpnu.chef_app.model.LeafyVegetable(5L, "Шпинат", 23.0, 2.9, 0.4, 3.6, 2.2);
        Product topping = new org.lpnu.chef_app.model.Topping(6L, "Волоський горіх", 654.0, 15.2, 65.2, 13.7, org.lpnu.chef_app.model.enums.Allergen.NUTS, true);
        Product lightDressing = new Dressing(7L, "Лимонний сік", 22.0, 0.4, 0.2, 6.9, false);

        when(mockRepo.findAll()).thenReturn(List.of(p1, p2, tuber, fruiting, leafy, topping, lightDressing));

        interact(() -> {
            try {
                java.lang.reflect.Method loadMethod = ProductController.class.getDeclaredMethod("loadProducts");
                loadMethod.setAccessible(true);
                loadMethod.invoke(controller);
            } catch (Exception e) {
                controller.initialize();
            }
        });
        WaitForAsyncUtils.waitForFxEvents();

        TableView<Product> table = lookup("#productTable").queryTableView();

        TableColumn<Product, ?> detailsColumn = table.getColumns().stream()
                .filter(col -> col.getText() != null && (col.getText().equalsIgnoreCase("Деталі") || col.getText().contains("деталі")))
                .findFirst()
                .orElse((TableColumn<Product, ?>) table.getColumns().get(table.getColumns().size() - 1));

        assertEquals("Цукор: 5.0%", detailsColumn.getCellData(0));
        assertEquals("На жирній основі", detailsColumn.getCellData(1));
        assertEquals("Крохмаль: 15.0%", detailsColumn.getCellData(2));
        assertEquals("Вода: 94.5%", detailsColumn.getCellData(3));
        assertEquals("Клітковина: 2.2%", detailsColumn.getCellData(4));
        assertEquals("Алерген: Горіхи", detailsColumn.getCellData(5));
        assertEquals("Легка заправка", detailsColumn.getCellData(6));
    }

    @Test
    @DisplayName("handleDeleteAction() перехоплює RuntimeException від бази даних (блок catch)")
    void testDeleteActionDatabaseExceptionHandling() {
        TableView<Product> table = lookup("#productTable").queryTableView();

        doThrow(new RuntimeException("Помилка зв'язку з БД при видаленні")).when(mockRepo).delete(1L);

        interact(() -> table.getSelectionModel().select(p1));

        javafx.application.Platform.runLater(() -> controller.handleDeleteAction());
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockRepo, times(1)).delete(1L);

        clickOn("OK");
        WaitForAsyncUtils.waitForFxEvents();
    }
}