module org.lpnu.chef_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;
    requires java.sql;


    opens org.lpnu.chef_app to javafx.fxml;
    exports org.lpnu.chef_app;
}