open module org.lpnu.chef_app {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires java.sql;
    requires io.github.cdimascio.dotenv.java;

    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires jakarta.mail;

    uses jakarta.mail.util.StreamProvider;

    exports org.lpnu.chef_app;
    exports org.lpnu.chef_app.controller;
    exports org.lpnu.chef_app.model;
    exports org.lpnu.chef_app.model.enums;
    exports org.lpnu.chef_app.repository;
}