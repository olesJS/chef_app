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

            // Refreshing list of salads when opening Recipe Book Tab
            if (mainTabPane.getSelectionModel().getSelectedIndex() == 2) {
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