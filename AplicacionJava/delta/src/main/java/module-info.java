module AplicacionJava.delta {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    opens AplicacionJava.delta to javafx.fxml;
    exports AplicacionJava.delta;
    requires java.sql;
}
