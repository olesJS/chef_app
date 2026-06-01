package org.lpnu.chef_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.lpnu.chef_app.model.*;
import org.lpnu.chef_app.model.enums.ProductType;
import org.lpnu.chef_app.service.ProductService;
import org.lpnu.chef_app.service.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Long> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colType;
    @FXML private TableColumn<Product, Double> colKcal;
    @FXML private TableColumn<Product, Double> colProteins;
    @FXML private TableColumn<Product, Double> colFats;
    @FXML private TableColumn<Product, Double> colCarbs;
    @FXML private TableColumn<Product, String> colDetails;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private ComboBox<String> categoryFilter;

    private static final String SORT_BY_NAME = "Назва (А-Я)";
    private static final String SORT_BY_ID = "ID (за зростанням)";

    private ProductService productService = ServiceFactory.getProductService();

    // For Mockito
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    private final ObservableList<Product> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        log.info("Ініціалізація контролера продуктів через сервісний шар.");
        setupTableColumns();
        setupFilters();

        sortComboBox.setItems(FXCollections.observableArrayList(SORT_BY_NAME, SORT_BY_ID));
        sortComboBox.setValue(SORT_BY_ID);
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applySorting(newVal);
                log.debug("Змінено сортування продуктів на: {}", newVal);
            }
        });

        loadProducts();
    }

    private void loadProducts() {
        try {
            masterData.clear();
            masterData.addAll(productService.getAllProducts());
            applySorting(sortComboBox.getValue());
            log.info("Успішно завантажено {} продуктів через сервісний шар.", masterData.size());
        } catch (RuntimeException e) {
            log.error("Не вдалося завантажити список продуктів через сервіс", e);
            showErrorAlert("Помилка завантаження", "Не вдалося отримати список продуктів: " + e.getMessage());
        }
    }

    private void applySorting(String sortType) {
        if (sortType == null) return;
        if (sortType.equals(SORT_BY_NAME)) {
            masterData.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        } else if (sortType.equals(SORT_BY_ID)) {
            masterData.sort((p1, p2) -> p1.getID().compareTo(p2.getID()));
        }
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("ID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colKcal.setCellValueFactory(new PropertyValueFactory<>("kcalPer100g"));
        colProteins.setCellValueFactory(new PropertyValueFactory<>("proteins"));
        colFats.setCellValueFactory(new PropertyValueFactory<>("fats"));
        colCarbs.setCellValueFactory(new PropertyValueFactory<>("carbs"));

        colType.setCellValueFactory(cellData -> {
            ProductType type = cellData.getValue().getType();
            return new javafx.beans.property.SimpleStringProperty(
                    type != null ? type.getDisplayName() : "Невідомо"
            );
        });

        colDetails.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    product != null ? product.getSpecificDetails() : "-"
            );
        });
    }

    private void setupFilters() {
        categoryFilter.getItems().add("Всі");
        for (ProductType type : ProductType.values()) {
            categoryFilter.getItems().add(type.getDisplayName());
        }
        categoryFilter.setValue("Всі");

        FilteredList<Product> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilter(filteredData));
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter(filteredData));

        productTable.setItems(filteredData);
    }

    private void updateFilter(FilteredList<Product> filteredData) {
        filteredData.setPredicate(product -> {
            String searchText = searchField.getText().toLowerCase();
            String selectedCategory = categoryFilter.getValue();
            boolean matchesSearch = product.getName().toLowerCase().contains(searchText);
            boolean matchesCategory = selectedCategory.equals("Всі") || product.getType().getDisplayName().equals(selectedCategory);
            return matchesSearch && matchesCategory;
        });
    }

    private void showProductDialog(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/lpnu/chef_app/dialogs/product-dialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(product == null ? "Додати продукт" : "Редагувати продукт");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(productTable.getScene().getWindow());
            dialogStage.setScene(new Scene(page));

            ProductDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setProduct(product);

            dialogStage.showAndWait();

            if (controller.isSaveClicked() && controller.getProduct() != null) {
                try {
                    productService.saveProduct(controller.getProduct());
                    log.info("Операцію збереження/оновлення продукту '{}' успішно виконано сервісом.", controller.getProduct().getName());
                    loadProducts();
                } catch (IllegalArgumentException e) {
                    log.warn("Помилка бізнес-валідації при збереженні продукту.", e);
                    showErrorAlert("Помилка валідації даних", e.getMessage());
                } catch (RuntimeException e) {
                    log.error("Помилка під час збереження продукту через сервісний шар.", e);
                    showErrorAlert("Помилка збереження", "Не вдалося виконати операцію: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Критична помилка завантаження FXML файлу діалогу продукту.", e);
            showAlert("Помилка завантаження", "Не вдалося відкрити вікно діалогу.");
        }
    }

    @FXML
    void handleAddAction() {
        log.info("Користувач натиснув 'Додати продукт'.");
        showProductDialog(null);
    }

    @FXML
    void handleEditAction() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Користувач ініціював редагування продукту: {}", selected.getName());
            showProductDialog(selected);
        } else {
            log.warn("Спроба редагувати продукт без виділення у таблиці.");
            showAlert("Помилка", "Будь ласка, виберіть продукт для редагування");
        }
    }

    @FXML
    void handleDeleteAction() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Користувач ініціював видалення продукту: {} (ID: {})", selected.getName(), selected.getID());
            try {
                productService.deleteProduct(selected.getID());
                loadProducts();
            } catch (IllegalArgumentException e) {
                log.warn("Сервіс відхилив видалення продукту через некоректні параметри.", e);
                showErrorAlert("Помилка видалення", e.getMessage());
            } catch (RuntimeException e) {
                log.error("Помилка при видаленні продукту через сервісний шар", e);
                showErrorAlert("Помилка видалення", "Не вдалося видалити продукт: " + e.getMessage());
            }
        } else {
            log.warn("Спроба видалити продукт без виділення у таблиці.");
            showAlert("Помилка", "Будь ласка, виберіть продукт для видалення");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Помилка обробки бізнес-операції");
        alert.setContentText(content);
        alert.showAndWait();
    }
}