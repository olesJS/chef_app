package org.lpnu.chef_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.stage.FileChooser;
import org.lpnu.chef_app.model.*;
import org.lpnu.chef_app.service.SaladService;
import org.lpnu.chef_app.service.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class RecipeBookController {
    private static final Logger log = LoggerFactory.getLogger(RecipeBookController.class);

    @FXML private ListView<Salad> saladListView;
    @FXML private TextField searchSaladField;
    @FXML private Label detailsTitleLabel;
    @FXML private TableView<Ingredient> detailsTable;
    @FXML private TableColumn<Ingredient, String> colDetailName;
    @FXML private TableColumn<Ingredient, Double> colDetailWeight;
    @FXML private TableColumn<Ingredient, Double> colDetailFinalWeight;
    @FXML private TableColumn<Ingredient, String> colDetailState;
    @FXML private TableColumn<Ingredient, Double> colDetailKcal;
    @FXML private Label lblTotalKcal;
    @FXML private Label lblMacros;
    @FXML private TextArea tipTextArea;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button exportButton;

    private SaladService saladService = ServiceFactory.getSaladService();

    // For tests
    public void setSaladService(SaladService saladService) {
        this.saladService = saladService;
    }

    private final ObservableList<Salad> allSalads = FXCollections.observableArrayList();
    private FilteredList<Salad> filteredSalads;
    private FilteredList<Ingredient> filteredIngredients;

    @FXML public TextField minKcalField;
    @FXML public TextField maxKcalField;

    private MainController mainController;

    @FXML
    public void initialize() {
        log.info("Ініціалізація Книги рецептів через сервісний шар.");
        this.filteredSalads = new FilteredList<>(allSalads, s -> true);
        setupTable();
        loadSalads();
        setupSelectionListener();
        setupSearch();
        setupSortOptions();
        saladListView.setItems(filteredSalads);
    }

    private void setupTable() {
        colDetailName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduct().getName()));
        colDetailWeight.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getWeight()).asObject());

        colDetailFinalWeight.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getFinalWeight()).asObject());
        colDetailFinalWeight.setCellFactory(column -> new TableCell<>() {
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

        colDetailState.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getState().getDisplayName()));
        colDetailKcal.setCellValueFactory(d -> new SimpleDoubleProperty(
                Math.round(d.getValue().calculateCalories() * 100.0) / 100.0).asObject());

        saladListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Salad item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("[%d] %s", item.getId(), item.getName()));
                }
            }
        });
    }

    void loadSalads() {
        try {
            allSalads.setAll(saladService.getAllSalads());
            log.debug("Завантажено {} салатів у Книгу рецептів через сервіс.", allSalads.size());
        } catch (RuntimeException e) {
            log.error("Помилка завантаження книги рецептів через сервіс", e);
            showAlert("Помилка", "Не вдалося завантажити книгу рецептів: " + e.getMessage());
        }
    }

    public void refresh() {
        loadSalads();
        exportButton.setDisable(true);
    }

    private void setupSelectionListener() {
        saladListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showSaladDetails(newVal);
            }
        });

        detailsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String productName = newVal.getProduct().getName();
                String tip = newVal.getCookingTip().orElse("поради немає.");
                tipTextArea.setText(productName + ": " + tip);
            } else {
                tipTextArea.setText("");
            }
        });
    }

    private void showSaladDetails(Salad salad) {
        detailsTitleLabel.setText("Рецепт \"" + salad.getName() + "\"");

        ObservableList<Ingredient> masterList = FXCollections.observableArrayList(salad.getIngredients());
        this.filteredIngredients = new FilteredList<>(masterList, p -> true);
        detailsTable.setItems(this.filteredIngredients);

        double totalNetto = salad.getIngredients().stream().mapToDouble(Ingredient::getWeight).sum();
        double totalExit = salad.getIngredients().stream().mapToDouble(Ingredient::getFinalWeight).sum();

        lblTotalKcal.setText(String.format("%.1f ккал (Вага: %.0f г / Вихід: %.0f г)",
                salad.getTotalCalories(), totalNetto, totalExit));

        double p = salad.getIngredients().stream().mapToDouble(Ingredient::calculateProteins).sum();
        double f = salad.getIngredients().stream().mapToDouble(Ingredient::calculateFats).sum();
        double c = salad.getIngredients().stream().mapToDouble(Ingredient::calculateCarbs).sum();

        lblMacros.setText(String.format("%.1f / %.1f / %.1f", p, f, c));
        exportButton.setDisable(false);
    }

    @FXML
    void handleFilterIngredients() {
        if (filteredIngredients == null) return;

        try {
            double min = minKcalField.getText().isEmpty() ? 0 : Double.parseDouble(minKcalField.getText());
            double max = maxKcalField.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxKcalField.getText());

            filteredIngredients.setPredicate(ing -> {
                double kcal = ing.calculateCalories();
                return kcal >= min && kcal <= max;
            });
            log.info("Застосовано фільтр калорійності: від {} до {}", min, max);
        } catch (NumberFormatException e) {
            log.warn("Некоректне введення діапазону калорійності у фільтрі.");
        }
    }

    @FXML
    void handleResetFilter() {
        minKcalField.clear();
        maxKcalField.clear();
        if (filteredIngredients != null) {
            filteredIngredients.setPredicate(p -> true);
        }
        log.info("Скинуто фільтр калорійності інгредієнтів.");
    }

    private void setupSearch() {
        searchSaladField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredSalads.setPredicate(salad -> {
                if (newVal == null || newVal.trim().isEmpty()) return true;
                String filter = newVal.toLowerCase().trim();
                return salad.getName().toLowerCase().contains(filter) || String.valueOf(salad.getId()).contains(filter);
            });
        });
    }

    private void setupSortOptions() {
        sortComboBox.setItems(FXCollections.observableArrayList(
                "Назвою (А-Я)", "Калорійністю ↓", "Калорійністю ↑", "Вагою виходу ↓", "Вагою виходу ↑"
        ));

        sortComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && filteredIngredients != null) {
                sortIngredients(newVal);
                log.debug("Сортування інгредієнтів змінено на: {}", newVal);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void sortIngredients(String criteria) {
        ObservableList<Ingredient> sourceList = (ObservableList<Ingredient>) filteredIngredients.getSource();
        switch (criteria) {
            case "Назвою (А-Я)": sourceList.sort(Comparator.comparing(ing -> ing.getProduct().getName())); break;
            case "Калорійністю ↓": sourceList.sort(Comparator.comparingDouble(Ingredient::calculateCalories)); break;
            case "Калорійністю ↑": sourceList.sort((a, b) -> Double.compare(b.calculateCalories(), a.calculateCalories())); break;
            case "Вагою виходу ↓": sourceList.sort(Comparator.comparingDouble(Ingredient::getFinalWeight)); break;
            case "Вагою виходу ↑": sourceList.sort((a, b) -> Double.compare(b.getFinalWeight(), a.getFinalWeight()));
        }
    }

    @FXML
    void handleDeleteSalad() {
        Salad selected = saladListView.getSelectionModel().getSelectedItem();

        if (selected == null) return;

        if (selected.getId() == null) {
            log.warn("Спроба видалення салату '{}' без ID.", selected.getName());
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Видалення рецепту");
        alert.setHeaderText("Ви впевнені, що хочете видалити салат: " + selected.getName() + "?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            log.info("Користувач підтвердив видалення салату '{}' (ID: {})", selected.getName(), selected.getId());
            try {
                saladService.deleteSalad(selected.getId());

                loadSalads();
                resetDetailsView();
            } catch (IllegalArgumentException e) {
                log.warn("Сервіс відхилив видалення салату: {}", e.getMessage());
                showAlert("Помилка валідації", e.getMessage());
            } catch (RuntimeException e) {
                log.error("Помилка виконання бізнес-операції видалення салату", e);
                showAlert("Помилка видалення", "Не вдалося видалити салат через помилку системи: " + e.getMessage());
            }
        } else {
            log.info("Користувач скасував видалення салату '{}'", selected.getName());
        }
    }

    private void resetDetailsView() {
        detailsTable.setItems(FXCollections.observableArrayList());
        detailsTitleLabel.setText("Оберіть салат для перегляду");
        lblTotalKcal.setText("0.0");
        lblMacros.setText("0.0 / 0.0 / 0.0");
        tipTextArea.clear();
        exportButton.setDisable(true);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    void handleEditSalad() {
        Salad selected = saladListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (mainController != null) {
                log.info("Ініційовано редагування салату '{}' (ID: {}). Передача управління MainController.", selected.getName(), selected.getId());
                mainController.switchToEditMode(selected);
            } else {
                log.error("Критична архітектурна помилка: MainController не встановлено у RecipeBookController!");
            }
        }
    }

    @FXML
    void handleExportToTxt() {
        Salad selected = saladListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Помилка", "Будь ласка, оберіть салат для експорту.");
            return;
        }

        log.info("Користувач ініціював експорт салату '{}' в TXT файл.", selected.getName());

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Зберегти рецепт");
        fileChooser.setInitialFileName(selected.getName() + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        java.io.File file = fileChooser.showSaveDialog(saladListView.getScene().getWindow());

        if (file != null) {
            saveToFile(selected, file);
        } else {
            log.info("Експорт скасовано користувачем.");
        }
    }

    private void saveToFile(Salad salad, java.io.File file) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            writer.println("\tРецепт салату: \"" + salad.getName() + "\"");
            writer.println("==========================================================================");
            writer.println();

            writer.printf("%-20s | %-12s | %-10s | %-10s | %-6s\n",
                    "Інгредієнт", "Обробка", "Брутто(г)", "Нетто(г)", "Ккал");
            writer.println("--------------------------------------------------------------------------");

            for (Ingredient ing : salad.getIngredients()) {
                writer.printf("%-20s | %-12s | %-10.1f | %-10.1f | %-6.1f\n",
                        ing.getProduct().getName(),
                        ing.getState().getDisplayName(),
                        ing.getWeight(),
                        ing.getFinalWeight(),
                        ing.calculateCalories());
            }

            writer.println("--------------------------------------------------------------------------");

            double p = salad.getIngredients().stream().mapToDouble(Ingredient::calculateProteins).sum();
            double f = salad.getIngredients().stream().mapToDouble(Ingredient::calculateFats).sum();
            double c = salad.getIngredients().stream().mapToDouble(Ingredient::calculateCarbs).sum();

            writer.printf("Загальна енергетична цінність: %.1f ккал\n", salad.getTotalCalories());
            writer.printf("Білки: %.1f г | Жири: %.1f г | Вуглеводи: %.1f г\n", p, f, c);
            writer.println();
            writer.println("--------------------------------------------------------------------------");

            writer.println("Поради щодо приготування:");
            for (Ingredient ing : salad.getIngredients()) {
                ing.getCookingTip().ifPresent(tip ->
                        writer.println("- " + ing.getProduct().getName() + ": " + tip)
                );
            }

            writer.println();
            log.info("Рецепт салату '{}' успішно експортовано до файлу: {}", salad.getName(), file.getAbsolutePath());
            showAlert("Успіх", "Рецепт успішно збережено у файл: " + file.getName());

        } catch (java.io.IOException e) {
            log.error("Помилка запису файлу експорту для салату '{}'", salad.getName(), e);
            showAlert("Помилка", "Не вдалося зберегти файл: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}