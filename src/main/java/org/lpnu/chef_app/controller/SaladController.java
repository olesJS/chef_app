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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SaladController {
    private static final Logger log = LoggerFactory.getLogger(SaladController.class);

    @FXML private TextField saladNameField;
    @FXML private TextField searchProductField;

    @FXML private TableView<Product> availableProductsTable;
    @FXML private TableColumn<Product, Long> colProdId;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, String> colProdType;
    @FXML private TableColumn<Product, Double> colProdKcal;

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

    private ProductRepository productRepository = new JdbcProductRepository();
    private SaladRepository saladRepository = new JdbcSaladRepository();

    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    public void setSaladRepository(SaladRepository saladRepository) {
        this.saladRepository = saladRepository;
    }

    private FilteredList<Product> filteredProducts;
    private ObservableList<Product> availableProducts = FXCollections.observableArrayList();
    private ObservableList<Ingredient> currentIngredients = FXCollections.observableArrayList();

    private Long editingSaladId = null;

    @FXML
    public void initialize() {
        log.info("Ініціалізація конструктора салатів.");
        setupLeftTable();
        setupRightTable();
        loadAvailableProducts();
        setupSearch();

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
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase().trim();
                boolean matchesName = product.getName().toLowerCase().contains(lowerCaseFilter);
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
        colIngWeight.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getWeight()).asObject());
        colIngFinalWeight.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFinalWeight()).asObject());
        colIngState.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getState().getDisplayName()));

        colIngKcal.setCellValueFactory(cellData -> {
            double kcal = cellData.getValue().calculateCalories();
            return new SimpleDoubleProperty(Math.round(kcal * 100.0) / 100.0).asObject();
        });

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
            log.debug("Оновлено список доступних продуктів для конструктора.");
        } catch (RuntimeException e) {
            log.error("Помилка завантаження доступних продуктів", e);
            showErrorAlert("Помилка завантаження", "Не вдалося отримати список продуктів: " + e.getMessage());
        }
    }

    @FXML
    void handleAddIngredient() {
        Product selectedProduct = availableProductsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            log.warn("Спроба додати інгредієнт без виділення продукту в таблиці.");
            return;
        }

        log.info("Відкриття діалогу додавання інгредієнта для продукту: {}", selectedProduct.getName());

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
                Ingredient newIngredient = controller.getResultIngredient();
                currentIngredients.add(newIngredient);
                log.info("До салату додано інгредієнт: {}, вага: {} г", newIngredient.getProduct().getName(), newIngredient.getWeight());
            }
        } catch (IOException e) {
            log.error("Помилка завантаження FXML файлу для діалогу інгредієнта", e);
        }
    }

    @FXML
    void handleRemoveIngredient() {
        Ingredient selected = saladIngredientsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentIngredients.remove(selected);
            log.info("З салату видалено інгредієнт: {}", selected.getProduct().getName());
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
    void handleClearSalad() {
        currentIngredients.clear();
        saladNameField.clear();
        log.info("Форму конструктора салатів було повністю очищено.");
    }

    public void loadSaladForEditing(Salad salad) {
        this.editingSaladId = salad.getId();
        saladNameField.setText(salad.getName());
        currentIngredients.setAll(salad.getIngredients());
        saveButton.setText("Оновити рецепт");
        log.info("Салат '{}' (ID: {}) успішно завантажено в конструктор для редагування.", salad.getName(), salad.getId());
    }

    @FXML
    void handleSaveSalad() {
        String saladName = saladNameField.getText().trim();
        log.info("Користувач натиснув 'Зберегти' для салату: '{}'", saladName);

        Salad salad = new Salad(saladName);
        currentIngredients.forEach(salad::addIngredient);

        try {
            if (editingSaladId != null) {
                saladRepository.update(salad, editingSaladId);
                showAlert("Оновлено", "Зміни в салаті збережено!");
                log.info("Успішно оновлено існуючий салат '{}' (ID: {})", saladName, editingSaladId);
            } else {
                saladRepository.save(salad);
                showAlert("Збережено", "Новий салат створено!");
                log.info("Успішно створено новий салат '{}'", saladName);
            }

            handleClearSalad();
            editingSaladId = null;
            saveButton.setText("Зберегти рецепт");

        } catch (RuntimeException e) {
            log.error("Не вдалося зберегти/оновити салат '{}' через помилку БД.", saladName, e);
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