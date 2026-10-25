package JavaApp.delta;

import JavaApp.delta.dao.UsuarioDAO;
import JavaApp.delta.dao.PaqueteDAO;
import JavaApp.delta.model.Ubicacion;
import JavaApp.delta.model.Usuario;
import JavaApp.delta.model.Paquete;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class AdminController {
    private static Usuario usuarioActual;

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    
    @FXML private TextField UsuarioNombre;
    @FXML private PasswordField UsuarioContrasena;
    @FXML ComboBox<String> UsuarioRol;
    @FXML private Label mensaje;
    @FXML private Label rolMensaje;
    
    @FXML private ComboBox<Usuario> CambiarUsuario;
    @FXML private ComboBox<String> CambiarRol;
    
    @FXML private Label bienvenida;
    @FXML private Label etiquetaRol;
    @FXML private Label detalle;
    @FXML private TextField busqueda;
    @FXML private ComboBox<String> filtroEstado;
    @FXML private TableView<Usuario> tabla;
    @FXML private TableColumn<Usuario,String> ColumnaUsuario, ColumnaRol, ColumnaNombre, ColumnaApellido;
    @FXML private TableColumn<Usuario,Integer> ColumnaId;
    @FXML private ComboBox<String> nuevoEstado;
    @FXML private ComboBox<Ubicacion> ubicacion;
    @FXML private TextField motivo;
    @FXML private TitledPane panelOperaciones;
    @FXML private TitledPane panelReparto;
    @FXML private TitledPane panelAdministracion;
    @FXML private ComboBox<String> estadoReparto;
    @FXML private TextArea incidenciaTexto;
    @FXML private ComboBox<String> tipoIncidencia;
    @FXML private Label resumen;

    private final UsuarioDAO GestorUsuarios = new UsuarioDAO();

    private static final List<String> ROLES = List.of(
    		"cliente", "chofer", "operador", "administrador"
        );
    
    @FXML
    public void initialize() {
    	UsuarioRol.setItems(FXCollections.observableArrayList(ROLES));
    	ColumnaUsuario.setCellValueFactory(new PropertyValueFactory<>("NombreUsuario"));
    	ColumnaRol.setCellValueFactory(new PropertyValueFactory<>("Rol"));
    	ColumnaNombre.setCellValueFactory(new PropertyValueFactory<>("Nombre"));
    	ColumnaApellido.setCellValueFactory(new PropertyValueFactory<>("Apellido"));
    	ColumnaId.setCellValueFactory(new PropertyValueFactory<>("Id"));

    	
    	buscar();
    }
    
    private void buscar() {
    	tabla.setItems(FXCollections.observableArrayList(GestorUsuarios.buscarBase("")));
    	CambiarUsuario.setItems(FXCollections.observableArrayList(GestorUsuarios.buscarPorNombre()));//este es un combo box
    	CambiarRol.setItems(FXCollections.observableArrayList(ROLES));
    };

    @FXML public void modificarRol() {
    	String nombre = CambiarUsuario.getValue().toString().trim();
    	String rol = CambiarRol.getValue();
        if(nombre==null ||nombre.isBlank() || rol==null ||rol.isBlank()){
            rolMensaje.setText("Completá todos los campos.");
            return;
        }
        try{
        	System.out.println("Enviando a DAO -> Nombre: '" + nombre + "', Rol: '" + rol + "'");
        	Boolean RegistrarUsuario = new UsuarioDAO().modificarRol(rol, nombre);
        	if(RegistrarUsuario) {
        		rolMensaje.setText("registro exitoso");
        	}else {
        		rolMensaje.setText("Error");
        	}
        }catch(Exception excepcion){
        	rolMensaje.setText("Error: " + excepcion.getMessage());
            mostrarAlerta("error", excepcion.getMessage());
            excepcion.printStackTrace();
        }
    }
    
    
    @FXML
    private void registrar() {
    	String nombre = UsuarioNombre.getText().trim();
        String contrasena = UsuarioContrasena.getText();
        String rol = UsuarioRol.getValue();
    	mensaje.setText("");
        if(nombre.isEmpty() || contrasena.isEmpty() || rol==null ||rol.isBlank()){
            mensaje.setText("Completá todos los campos.");
            return;
        }
        try{
        	Boolean RegistrarUsuario = new UsuarioDAO().registrar(nombre, rol, contrasena);
        	if(RegistrarUsuario) {
        		mensaje.setText("registro exitoso");
        	}else {
        		mensaje.setText("Error");
        	}
        }catch(Exception excepcion){
            mensaje.setText("Error: " + excepcion.getMessage());
            mostrarAlerta("error", excepcion.getMessage());
            excepcion.printStackTrace();
        }
    }
  
    @FXML
    private void cerrarSesion() throws Exception {
        usuarioActual = null;
        App.establecerRaiz("Login");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert ventanaAlerta = new Alert(Alert.AlertType.INFORMATION);
        ventanaAlerta.setTitle(titulo);
        ventanaAlerta.setHeaderText(null);
        ventanaAlerta.setContentText(mensaje);
        ventanaAlerta.showAndWait();
    }
}
