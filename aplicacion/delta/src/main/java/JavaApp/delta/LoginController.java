package JavaApp.delta;

import java.io.IOException;
import javafx.fxml.FXML;

public class LoginController {

    @FXML
    private void Salir() throws IOException {
        App.setRoot("home");
    }
}