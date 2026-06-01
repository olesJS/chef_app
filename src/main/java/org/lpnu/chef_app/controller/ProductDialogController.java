package org.lpnu.chef_app.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.lpnu.chef_app.model.*;
import org.lpnu.chef_app.model.enums.Allergen;
import org.lpnu.chef_app.model.enums.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductDialogController {
    private static final Logger log = LoggerFactory.getLogger(ProductDialogController.class);

    @FXML private TextField nameField, kcalField, pField, fField, cField, extraField;
    @FXML private ComboBox<ProductType> typeComboBox;
    @FXML private ComboBox<Allergen> allergenComboBox;
    @FXML private CheckBox fatBasedCheckBox, crunchyCheckBox;
    @FXML private VBox vegetableExtraBox, dressingExtraBox, toppingExtraBox;
    @FXML private Label extraLabel;

    private Product product;
    private Stage dialogStage;
    private boolean saveClicked = false;

    @FXML
    private void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList(ProductType.values()));
        allergenComboBox.setItems(FXCollections.observableArrayList(Allergen.values()));

        typeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                log.debug("Змінено категорію продукту в діалозі на: {}", newVal);
                updateDynamicFields(newVal);
            }
        });
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            log.info("Діалог відкрито для редагування продукту: '{}'", product.getName());
            nameField.setText(product.getName());
            kcalField.setText(String.valueOf(product.getKcalPer100g()));
            pField.setText(String.valueOf(product.getProteins()));
            fField.setText(String.valueOf(product.getFats()));
            cField.setText(String.valueOf(product.getCarbs()));

            typeComboBox.setValue(product.getType());

            switch (product.getType()) {
                case ROOT_VEGETABLE -> extraField.setText(String.valueOf(((RootVegetable) product).getSugarContent()));
                case TUBER_VEGETABLE -> extraField.setText(String.valueOf(((TuberVegetable) product).getStarchContent()));
                case LEAFY_VEGETABLE -> extraField.setText(String.valueOf(((LeafyVegetable) product).getFiberContent()));
                case FRUITING_VEGETABLE -> extraField.setText(String.valueOf(((FruitingVegetable) product).getWaterContentPercent()));
                case DRESSING -> fatBasedCheckBox.setSelected(((Dressing) product).getIsFatBased());
                case TOPPING -> {
                    Topping t = (Topping) product;
                    allergenComboBox.setValue(t.getAllergen());
                    crunchyCheckBox.setSelected(t.getIsCrunchy());
                }
            }
        } else {
            log.info("Діалог відкрито для створення нового продукту.");
        }
    }

    private void updateDynamicFields(ProductType type) {
        vegetableExtraBox.setVisible(false);
        dressingExtraBox.setVisible(false);
        toppingExtraBox.setVisible(false);

        switch (type) {
            case ROOT_VEGETABLE -> { vegetableExtraBox.setVisible(true); extraLabel.setText("Цукор (%):"); }
            case TUBER_VEGETABLE -> { vegetableExtraBox.setVisible(true); extraLabel.setText("Крохмаль (%):"); }
            case LEAFY_VEGETABLE -> { vegetableExtraBox.setVisible(true); extraLabel.setText("Клітковина (%):"); }
            case FRUITING_VEGETABLE -> { vegetableExtraBox.setVisible(true); extraLabel.setText("Вода (%):"); }
            case DRESSING -> dressingExtraBox.setVisible(true);
            case TOPPING -> toppingExtraBox.setVisible(true);
        }
    }

    public void setDialogStage(Stage dialogStage) { this.dialogStage = dialogStage; }
    public boolean isSaveClicked() { return saveClicked; }
    public Product getProduct() { return product; }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            ProductType type = typeComboBox.getValue();
            String name = nameField.getText();
            double kcal = Double.parseDouble(kcalField.getText());
            double p = Double.parseDouble(pField.getText());
            double f = Double.parseDouble(fField.getText());
            double c = Double.parseDouble(cField.getText());
            double extraValue = extraField.getText().isEmpty() ? 0 : Double.parseDouble(extraField.getText());

            Long id = (product != null) ? product.getID() : null;

            product = switch (type) {
                case ROOT_VEGETABLE -> new RootVegetable(id, name, kcal, p, f, c, extraValue);
                case TUBER_VEGETABLE -> new TuberVegetable(id, name, kcal, p, f, c, extraValue);
                case LEAFY_VEGETABLE -> new LeafyVegetable(id, name, kcal, p, f, c, extraValue);
                case FRUITING_VEGETABLE -> new FruitingVegetable(id, name, kcal, p, f, c, extraValue);
                case DRESSING -> new Dressing(id, name, kcal, p, f, c, fatBasedCheckBox.isSelected());
                case TOPPING -> new Topping(id, name, kcal, p, f, c, allergenComboBox.getValue(), crunchyCheckBox.isSelected());
            };

            saveClicked = true;
            log.info("Форму продукту '{}' успішно заповнено. Передаємо об'єкт вищому рівню.", name);
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        log.info("Користувач скасував редагування/створення продукту.");
        dialogStage.close();
    }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (nameField.getText() == null || nameField.getText().isBlank()) {
            errorMessage.append("Не вказано назву продукту!\n");
        }

        ProductType type = typeComboBox.getValue();
        if (type == null) {
            errorMessage.append("Не обрано категорію!\n");
        }

        errorMessage.append(validateDouble(kcalField.getText(), "калорій"));
        errorMessage.append(validateDouble(pField.getText(), "білків"));
        errorMessage.append(validateDouble(fField.getText(), "жирів"));
        errorMessage.append(validateDouble(cField.getText(), "вуглеводів"));

        if (type != null && type != ProductType.DRESSING && type != ProductType.TOPPING) {
            errorMessage.append(validateDouble(extraField.getText(), "додаткового показника"));
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            log.warn("Помилка первинної валідації FX-полів продукту: \n{}", errorMessage.toString().trim());
            showErrorAlert(errorMessage.toString());
            return false;
        }
    }

    private String validateDouble(String value, String fieldName) {
        if (value == null || value.isBlank()) return "Поле " + fieldName + " порожнє!\n";
        try {
            Double.parseDouble(value);
            return "";
        } catch (NumberFormatException e) {
            return "Поле " + fieldName + " має бути числом!\n";
        }
    }

    private void showErrorAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialogStage);
        alert.setTitle("Некоректні поля");
        alert.setHeaderText("Будь ласка, виправте наступні помилки:");
        alert.setContentText(content);
        alert.showAndWait();
    }
}