package JavaApp.delta;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private static Scene escena;

    @Override
    public void start(Stage ventana) throws Exception {
        escena = new Scene(cargarFXML("Login"), 900, 600);
        ventana.setTitle("Delta Logística - Gestión de Depósito");
        ventana.setMinWidth(900);
        ventana.setMinHeight(600);
        ventana.setScene(escena);
        ventana.show();
    }

    static void establecerRaiz(String archivoFXML) throws Exception {
        escena.setRoot(cargarFXML(archivoFXML));
    }

    private static Parent cargarFXML(String archivoFXML) throws Exception {
        return FXMLLoader.load(App.class.getResource(archivoFXML + ".fxml"));
    }

    public static void main(String[] argumentos) {
        launch(argumentos);
    }
}
