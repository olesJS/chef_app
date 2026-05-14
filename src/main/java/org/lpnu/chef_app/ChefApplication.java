package org.lpnu.chef_app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChefApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/lpnu/chef_app/views/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1050, 650);
        stage.setTitle("Шеф-кухар");
        stage.setScene(scene);
        stage.show();
    }
}
