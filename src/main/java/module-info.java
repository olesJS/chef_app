module org.lpnu.chef_app {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.lpnu.chef_app to javafx.fxml;
    exports org.lpnu.chef_app;
}