module org.lpnu.chef_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;
    requires java.sql;


    opens org.lpnu.chef_app to javafx.fxml;
    exports org.lpnu.chef_app;

    opens org.lpnu.chef_app.controller to javafx.fxml;
    exports org.lpnu.chef_app.controller;

    opens org.lpnu.chef_app.model to javafx.base;
}