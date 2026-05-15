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

import java.io.IOException;

public class ProductController {

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

    // Sorting order names
    private static final String SORT_BY_NAME = "Назва (А-Я)";
    private static final String SORT_BY_ID = "ID (за зростанням)";

    private final ProductRepository productRepository = new JdbcProductRepository();
    private ObservableList<Product> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();

        sortComboBox.setItems(FXCollections.observableArrayList(SORT_BY_NAME, SORT_BY_ID));
        sortComboBox.setValue(SORT_BY_ID);
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applySorting(newVal);
            }
        });

        loadProducts();
    }

    private void loadProducts() {
        try {
            masterData.clear();
            masterData.addAll(productRepository.findAll());
            applySorting(sortComboBox.getValue());
        } catch (RuntimeException e) {
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

            boolean matchesCategory = selectedCategory.equals("Всі") ||
                    product.getType().getDisplayName().equals(selectedCategory);

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
                    } else {
                        productRepository.update(controller.getProduct());
                    }
                    loadProducts();
                } catch (RuntimeException e) {
                    showErrorAlert("Помилка збереження", e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Помилка завантаження", "Не вдалося відкрити вікно діалогу.");
        }
    }

    @FXML
    private void handleAddAction() {
        showProductDialog(null);
    }

    @FXML
    private void handleEditAction() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showProductDialog(selected);
        } else {
            showAlert("Помилка", "Будь ласка, виберіть продукт для редагування");
        }
    }

    @FXML
    private void handleDeleteAction() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                productRepository.delete(selected.getID());
                loadProducts();
            } catch (RuntimeException e) {
                showErrorAlert("Помилка видалення", "Не вдалося видалити продукт: " + e.getMessage());
            }
        } else {
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