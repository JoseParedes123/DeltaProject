package JavaApp.delta;

import java.io.IOException;
import javafx.fxml.FXML;

public class HomeController {

    @FXML 
    private void VentanaLogin() throws IOException {
        App.setRoot("Login");
    }
    @FXML 
    private void salir() throws IOException {
        System.out.println("papapap");
    }
}
