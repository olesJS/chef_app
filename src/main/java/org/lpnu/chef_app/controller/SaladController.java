package org.lpnu.chef_app.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.lpnu.chef_app.model.*;
import org.lpnu.chef_app.repository.*;
import java.io.IOException;
import java.util.List;

public class SaladController {

    @FXML private TextField saladNameField;
    @FXML private TextField searchProductField;

    // Products table
    @FXML private TableView<Product> availableProductsTable;
    @FXML private TableColumn<Product, Long> colProdId;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, String> colProdType;
    @FXML private TableColumn<Product, Double> colProdKcal;

    // Ingredients table
    @FXML private TableView<Ingredient> saladIngredientsTable;
    @FXML private TableColumn<Ingredient, String> colIngName;
    @FXML private TableColumn<Ingredient, Double> colIngWeight;
    @FXML private TableColumn<Ingredient, Double> colIngFinalWeight;
    @FXML private TableColumn<Ingredient, String> colIngState;
    @FXML private TableColumn<Ingredient, Double> colIngKcal;

    @FXML private Label lblTotalWeight;
    @FXML private Label lblTotalKcal;
    @FXML private Label lblMacros;

    @FXML private Button saveButton;

    private final ProductRepository productRepository = new JdbcProductRepository();
    private final SaladRepository saladRepository = new JdbcSaladRepository();

    private FilteredList<Product> filteredProducts;
    private ObservableList<Product> availableProducts = FXCollections.observableArrayList();
    private ObservableList<Ingredient> currentIngredients = FXCollections.observableArrayList();

    private Long editingSaladId = null;

    @FXML
    public void initialize() {
        setupLeftTable();
        setupRightTable();
        loadAvailableProducts();
        setupSearch();

        // Listener for updating stats when adding/deleting ingredients
        currentIngredients.addListener((javafx.collections.ListChangeListener.Change<? extends Ingredient> c) -> {
            updateStatistics();
        });

        saveButton.disableProperty().bind(
                saladNameField.textProperty().isEmpty()
                        .or(javafx.beans.binding.Bindings.isEmpty(currentIngredients))
        );
    }

    private void setupSearch() {
        filteredProducts = new FilteredList<>(availableProducts, p -> true);

        searchProductField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredProducts.setPredicate(product -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase().trim();

                // Searching by name
                boolean matchesName = product.getName().toLowerCase().contains(lowerCaseFilter);
                // Searching by ID
                boolean matchesId = String.valueOf(product.getID()).contains(lowerCaseFilter);

                return matchesName || matchesId;
            });
        });

        availableProductsTable.setItems(filteredProducts);
    }

    private void setupLeftTable() {
        colProdId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleLongProperty(cellData.getValue().getID()).asObject());
        colProdName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        colProdType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType().toString()));
        colProdKcal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getKcalPer100g()).asObject());

        availableProductsTable.setItems(filteredProducts);
    }

    private void setupRightTable() {
        colIngName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        colIngWeight.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getWeight()).asObject());
        colIngFinalWeight.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getFinalWeight()).asObject());
        colIngState.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getState().getDisplayName()));

        // Calculating kcals by ingredient final weight
        colIngKcal.setCellValueFactory(cellData -> {
            double kcal = cellData.getValue().calculateCalories();
            return new SimpleDoubleProperty(Math.round(kcal * 100.0) / 100.0).asObject();
        });

        // Displaying preparation tips
        colIngName.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);

                if (empty || name == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(name);

                    Ingredient ing = getTableRow().getItem();

                    if (ing != null) {
                        ing.getCookingTip().ifPresent(tip -> {
                            Tooltip tooltip = new Tooltip(tip);
                            tooltip.setShowDelay(javafx.util.Duration.millis(300));
                            setTooltip(tooltip);
                        });
                    }
                }
            }
        });

        colIngFinalWeight.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        saladIngredientsTable.setItems(currentIngredients);
    }

    public void loadAvailableProducts() {
        try {
            availableProducts.setAll(productRepository.findAll());
        } catch (RuntimeException e) {
            showErrorAlert("Помилка завантаження", "Не вдалося отримати список продуктів: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddIngredient() {
        Product selectedProduct = availableProductsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/lpnu/chef_app/dialogs/add-ingredient-dialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Параметри інгредієнта");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(page));

            AddIngredientDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage, selectedProduct);

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                currentIngredients.add(controller.getResultIngredient());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRemoveIngredient() {
        Ingredient selected = saladIngredientsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentIngredients.remove(selected);
        }
    }

    private void updateStatistics() {
        double totalWeight = 0, finalSaladWeight = 0, totalKcal = 0, totalP = 0, totalF = 0, totalC = 0;

        for (Ingredient ing : currentIngredients) {
            totalWeight += ing.getWeight();
            finalSaladWeight += ing.getFinalWeight();
            totalKcal += ing.calculateCalories();
            totalP += ing.calculateProteins();
            totalF += ing.calculateFats();
            totalC += ing.calculateCarbs();
        }

        lblTotalWeight.setText(String.format("%.0f г", finalSaladWeight));
        lblTotalKcal.setText(String.format("%.1f", totalKcal));
        lblMacros.setText(String.format("%.1f / %.1f / %.1f", totalP, totalF, totalC));
    }

    @FXML
    private void handleClearSalad() {
        currentIngredients.clear();
        saladNameField.clear();
    }

    public void loadSaladForEditing(Salad salad) {
        this.editingSaladId = salad.getId();
        saladNameField.setText(salad.getName());
        currentIngredients.setAll(salad.getIngredients());
        saveButton.setText("Оновити рецепт");
    }

    @FXML
    private void handleSaveSalad() {
        String saladName = saladNameField.getText().trim();

        Salad salad = new Salad(saladName);
        currentIngredients.forEach(salad::addIngredient);

        try {
            if (editingSaladId != null) {
                saladRepository.update(salad, editingSaladId);
                showAlert("Оновлено", "Зміни в салаті збережено!");
            } else {
                saladRepository.save(salad);
                showAlert("Збережено", "Новий салат створено!");
            }

            handleClearSalad();
            editingSaladId = null;
            saveButton.setText("Зберегти рецепт");

        } catch (RuntimeException e) {
            showErrorAlert("Помилка бази даних", "Не вдалося зберегти салат: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Сталася помилка при роботі з БД");
        alert.setContentText(content);
        alert.showAndWait();
    }

}