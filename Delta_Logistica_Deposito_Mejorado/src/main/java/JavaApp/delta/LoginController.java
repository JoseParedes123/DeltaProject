package JavaApp.delta;

import JavaApp.delta.dao.UsuarioDAO;
import JavaApp.delta.model.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField usuario;
    @FXML private PasswordField password;
    @FXML private Label mensaje;

    @FXML private void ingresar() {
        mensaje.setText("");
        if(usuario.getText().trim().isEmpty() || password.getText().isEmpty()){
            mensaje.setText("Completá usuario y contraseña.");
            return;
        }
        try{
            Usuario u=new UsuarioDAO().autenticar(usuario.getText().trim(),password.getText());
            if(u==null){mensaje.setText("Usuario o contraseña incorrectos.");return;}
            DashboardController.setUsuarioActual(u);
            App.setRoot("Dashboard");
        }catch(Exception e){
            mensaje.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void salir(){System.exit(0);}
}
