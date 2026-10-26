// Declara los módulos requeridos y qué paquetes quedan accesibles a JavaFX y al resto del proyecto.
module JavaApp.delta {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    opens JavaApp.delta to javafx.fxml;
    exports JavaApp.delta;
    exports JavaApp.delta.model;
    exports JavaApp.delta.dao;
}