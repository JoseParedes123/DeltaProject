package JavaApp.delta;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación JavaFX.
 * Crea la ventana principal y centraliza el cambio entre las pantallas FXML.
 */
public class App extends Application {
    private static Scene escena;

    @Override
    public void start(Stage ventana) throws Exception {
        escena = new Scene(cargarFXML("Login"), 1280, 720);
        ventana.setTitle("Delta Logística - Gestión de Depósito");
        ventana.setMinWidth(900);
        ventana.setMinHeight(600);
        ventana.setScene(escena);
        ventana.show();
    }

    /**
     * Cambia la vista que se muestra dentro de la misma ventana.
     * Se utiliza, por ejemplo, al iniciar sesión, ir a administración o cerrar sesión.
     */
    static void establecerRaiz(String archivoFXML) throws Exception {
        escena.setRoot(cargarFXML(archivoFXML));
    }

    /** Carga un archivo FXML de recursos y devuelve el nodo raíz de esa pantalla. */
    private static Parent cargarFXML(String archivoFXML) throws Exception {
        return FXMLLoader.load(App.class.getResource(archivoFXML + ".fxml"));
    }

    /** Inicia el ciclo de vida de JavaFX. */
    public static void main(String[] argumentos) {
        launch(argumentos);
    }
}
