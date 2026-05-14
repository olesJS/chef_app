package org.lpnu.chef_app.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.lpnu.chef_app.model.Ingredient;
import org.lpnu.chef_app.model.Product;
import org.lpnu.chef_app.model.enums.ProcessingState;
import org.lpnu.chef_app.model.enums.ProductType;

public class AddIngredientDialogController {
    @FXML private Label productNameLabel;
    @FXML private TextField weightField;
    @FXML private ComboBox<ProcessingState> stateComboBox;

    private Stage dialogStage;
    private Product product;
    private Ingredient resultIngredient;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
        stateComboBox.setItems(FXCollections.observableArrayList(ProcessingState.values()));
        stateComboBox.setValue(ProcessingState.RAW);
    }

    public void setDialogStage(Stage dialogStage, Product product) {
        this.dialogStage = dialogStage;
        this.product = product;
        this.productNameLabel.setText("Додавання: " + product.getName());

        ProductType type = product.getType();
        if (type == ProductType.DRESSING || type == ProductType.TOPPING) {
            stateComboBox.setValue(ProcessingState.RAW);
            stateComboBox.setDisable(true);
            stateComboBox.setPromptText("Не потребує обробки");
        }
    }

    public boolean isOkClicked() { return okClicked; }
    public Ingredient getResultIngredient() { return resultIngredient; }

    @FXML
    private void handleOk() {
        try {
            double weight = Double.parseDouble(weightField.getText());
            if (weight <= 0) throw new NumberFormatException();

            resultIngredient = new Ingredient(product, weight, stateComboBox.getValue());
            okClicked = true;
            dialogStage.close();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Введіть коректну вагу (число більше 0)");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() { dialogStage.close(); }
}