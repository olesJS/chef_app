package org.lpnu.chef_app.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lpnu.chef_app.model.Salad;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MainControllerTest extends ApplicationTest {

    private MainController mainController;
    private SaladController mockSaladController;
    private RecipeBookController mockRecipeBookController;

    @Override
    public void start(Stage stage) throws Exception {
        Locale.setDefault(Locale.US);

        mockSaladController = Mockito.mock(SaladController.class);
        mockRecipeBookController = Mockito.mock(RecipeBookController.class);

        FXMLLoader loader = new FXMLLoader(Thread.currentThread().getContextClassLoader()
                .getResource("org/lpnu/chef_app/views/main-view.fxml"));
        Scene scene = new Scene(loader.load());

        mainController = loader.getController();

        setPrivateField(mainController, "saladController", mockSaladController);
        setPrivateField(mainController, "recipeBookController", mockRecipeBookController);

        stage.setScene(scene);
        stage.show();
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("switchToEditMode успішно перемикає вкладку на індекс 1 та викликає завантаження салату")
    void testSwitchToEditMode() {
        TabPane tabPane = lookup("#mainTabPane").queryAs(TabPane.class);
        Salad dummySalad = new Salad("Цезар");
        dummySalad.setId(1L);

        interact(() -> mainController.switchToEditMode(dummySalad));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(1, tabPane.getSelectionModel().getSelectedIndex());
        verify(mockSaladController, times(1)).loadSaladForEditing(dummySalad);
    }

    @Test
    @DisplayName("Перехід на вкладку 2 викликає метод refresh() у RecipeBookController")
    void testTabSelectionListener() {
        TabPane tabPane = lookup("#mainTabPane").queryAs(TabPane.class);

        interact(() -> tabPane.getSelectionModel().select(2));
        WaitForAsyncUtils.waitForFxEvents();
        verify(mockRecipeBookController, times(1)).refresh();
    }
}