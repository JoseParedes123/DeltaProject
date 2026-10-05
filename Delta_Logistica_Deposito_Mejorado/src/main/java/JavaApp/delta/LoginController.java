package JavaApp.delta;

import JavaApp.delta.dao.UsuarioDAO;
import JavaApp.delta.model.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField usuario;
    @FXML private PasswordField contrasena;
    @FXML private Label mensaje;

    @FXML private void ingresar() {
        mensaje.setText("");
        if(usuario.getText().trim().isEmpty() || contrasena.getText().isEmpty()){
            mensaje.setText("Completá usuario y contraseña.");
            return;
        }
        try{
            Usuario usuarioAutenticado=new UsuarioDAO().autenticar(usuario.getText().trim(),contrasena.getText());
            if(usuarioAutenticado==null){mensaje.setText("Usuario o contraseña incorrectos.");return;}
            DashboardController.setUsuarioActual(usuarioAutenticado);
            App.establecerRaiz("Dashboard");
        }catch(Exception excepcion){
            mensaje.setText("Error: " + excepcion.getMessage());
            excepcion.printStackTrace();
        }
    }

    @FXML private void salir(){System.exit(0);}
}
