module JavaApp.delta {
    requires javafx.controls;
    requires javafx.fxml;

    opens JavaApp.delta to javafx.fxml;
    exports JavaApp.delta;
}
