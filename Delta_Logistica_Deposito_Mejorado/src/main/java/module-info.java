module JavaApp.delta {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    opens JavaApp.delta to javafx.fxml;
    exports JavaApp.delta;
    exports JavaApp.delta.model;
    exports JavaApp.delta.dao;
}