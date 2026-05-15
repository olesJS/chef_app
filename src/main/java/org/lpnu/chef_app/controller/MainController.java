package org.lpnu.chef_app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import org.lpnu.chef_app.model.Salad;

public class MainController {
    @FXML private TabPane mainTabPane;
    @FXML private SaladController saladController;
    @FXML private RecipeBookController recipeBookController;

    @FXML
    public void initialize() {
        if (recipeBookController != null) {
            recipeBookController.setMainController(this);
        }

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            System.out.println("Ви перейшли на вкладку: " + newTab.getText());
            int selectedIndex = mainTabPane.getSelectionModel().getSelectedIndex();

            if (selectedIndex == 1) {
                if (saladController != null) {
                    System.out.println("Оновлюємо список продуктів у конструкторі...");
                    saladController.loadAvailableProducts();
                }
            }

            else if (selectedIndex == 2) {
                if (recipeBookController != null) {
                    recipeBookController.refresh();
                }
            }
        });
    }

    public void switchToEditMode(Salad salad) {
        mainTabPane.getSelectionModel().select(1);
        saladController.loadSaladForEditing(salad);
    }
}