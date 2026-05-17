package org.lpnu.chef_app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import org.lpnu.chef_app.model.Salad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private TabPane mainTabPane;
    @FXML private SaladController saladController;
    @FXML private RecipeBookController recipeBookController;

    @FXML
    public void initialize() {
        if (recipeBookController != null) {
            recipeBookController.setMainController(this);
        }

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            log.info("Користувач перейшов на вкладку: {}", newTab.getText());
            int selectedIndex = mainTabPane.getSelectionModel().getSelectedIndex();

            if (selectedIndex == 1) {
                if (saladController != null) {
                    log.debug("Оновлюємо список доступних продуктів у конструкторі салатів...");
                    saladController.loadAvailableProducts();
                }
            } else if (selectedIndex == 2) {
                if (recipeBookController != null) {
                    log.debug("Оновлюємо книгу рецептів...");
                    recipeBookController.refresh();
                }
            }
        });
    }

    public void switchToEditMode(Salad salad) {
        log.info("Перемикання додатку в режим редагування салату: '{}'", salad.getName());
        mainTabPane.getSelectionModel().select(1);
        saladController.loadSaladForEditing(salad);
    }
}