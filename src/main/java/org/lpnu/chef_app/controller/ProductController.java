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
import org.lpnu.chef_app.repository.JdbcProductRepository;
import org.lpnu.chef_app.repository.ProductRepository;
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

    private ProductRepository productRepository = new JdbcProductRepository();
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private ObservableList<Product> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        log.info("Ініціалізація контролера продуктів.");
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
            masterData.addAll(productRepository.findAll());
            applySorting(sortComboBox.getValue());
            log.info("Успішно завантажено {} продуктів з бази даних.", masterData.size());
        } catch (RuntimeException e) {
            log.error("Не вдалося завантажити список продуктів", e);
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
            Product p = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(getSpecificDetails(p));
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

    private String getSpecificDetails(Product p) {
        if (p instanceof RootVegetable rv) return "Цукор: " + rv.getSugarContent() + "%";
        if (p instanceof TuberVegetable tv) return "Крохмаль: " + tv.getStarchContent() + "%";
        if (p instanceof FruitingVegetable fv) return "Вода: " + fv.getWaterContentPercent() + "%";
        if (p instanceof LeafyVegetable lv) return "Клітковина: " + lv.getFiberContent() + "%";
        if (p instanceof Dressing d) return d.getIsFatBased() ? "На жирній основі" : "Легка заправка";
        if (p instanceof Topping t) return "Алерген: " + t.getAllergen().getDisplayName();
        return "-";
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
                    if (product == null) {
                        productRepository.save(controller.getProduct());
                        log.info("Новий продукт успішно створено.");
                    } else {
                        productRepository.update(controller.getProduct());
                        log.info("Продукт '{}' успішно оновлено.", controller.getProduct().getName());
                    }
                    loadProducts();
                } catch (RuntimeException e) {
                    log.error("Помилка при збереженні продукту з діалогу.", e);
                    showErrorAlert("Помилка збереження", e.getMessage());
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
                productRepository.delete(selected.getID());
                loadProducts();
            } catch (RuntimeException e) {
                log.error("Помилка при видаленні продукту через UI", e);
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
        alert.setHeaderText("Помилка при роботі з базою даних");
        alert.setContentText(content);
        alert.showAndWait();
    }
}