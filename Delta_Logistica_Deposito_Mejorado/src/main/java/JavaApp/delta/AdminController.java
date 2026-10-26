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

/**
 * Controlador de administración de usuarios.
 * Permite consultar usuarios, crear cuentas y modificar roles/licencias.
 */
public class AdminController {
    /** La sesión es una sola (la guarda DashboardController al iniciar sesión). */
    private static Usuario usuarioActual() {
        return DashboardController.getUsuarioActual();
    }

    /** Solo el ADMINISTRADOR puede gestionar usuarios y roles. */
    private static boolean esAdministrador() {
        Usuario u = usuarioActual();
        return u != null && "ADMINISTRADOR".equals(u.getRol());
    }

    
    @FXML private TextField UsuarioNombre;
    @FXML private PasswordField UsuarioContrasena;
    @FXML ComboBox<String> UsuarioRol;
    @FXML private Label mensaje;
    @FXML private TextField UsuarioApellido;
    @FXML private TextField UsuarioDni;
    @FXML private TextField UsuarioEmail;
    @FXML private TextField UsuarioLicencia;
    @FXML private TextField UsuarioCategoriaLicencia;
    @FXML private TextField UsuarioVencimientoLicencia;
    @FXML private TextField RolLicencia;
    @FXML private TextField RolCategoriaLicencia;
    @FXML private TextField RolVencimientoLicencia;
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
    		"CLIENTE", "CHOFER", "OPERADOR_DEPOSITO", "ADMINISTRACION", "ADMINISTRADOR"
        );
    
    /**
     * Comprueba primero que la sesión sea de administrador.
     * Si no lo es, redirige al inicio de sesión y no inicializa el panel sensible.
     */
    @FXML
    public void initialize() {
    	if (!esAdministrador()) {
    		// Defensa extra: si alguien que no es administrador llega acá, se lo manda al inicio de sesión.
    		javafx.application.Platform.runLater(() -> {
    			try { App.establecerRaiz("Login"); } catch (Exception e) { e.printStackTrace(); }
    		});
    		return;
    	}
    	Usuario actual = usuarioActual();
    	bienvenida.setText("Usuario: " + actual.getNombre() + " " + actual.getApellido());
    	etiquetaRol.setText("Rol: " + actual.getRol().replace('_', ' '));
    	UsuarioRol.setItems(FXCollections.observableArrayList(ROLES));
    	ColumnaUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
    	ColumnaRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
    	ColumnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
    	ColumnaApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
    	ColumnaId.setCellValueFactory(new PropertyValueFactory<>("id"));

    	
    	buscar();
    }
    
    private void buscar() {
    	tabla.setItems(FXCollections.observableArrayList(GestorUsuarios.buscarBase("")));
    	CambiarUsuario.setItems(FXCollections.observableArrayList(GestorUsuarios.buscarBase("")));//este es un combo box
    	CambiarRol.setItems(FXCollections.observableArrayList(ROLES));
    };

    /**
     * Cambia el rol de la cuenta seleccionada después de validar permisos y selección.
     * Para un chofer se exige además una fecha de vencimiento de licencia.
     */
    @FXML public void modificarRol() {
        if (!esAdministrador()) { mostrarAlerta("Permiso denegado", "Solo un administrador puede cambiar roles."); return; }
        Usuario seleccionado = CambiarUsuario.getValue();
        String rol = CambiarRol.getValue();
        if (seleccionado == null || rol == null || rol.isBlank()) {
            rolMensaje.setText("Seleccioná un usuario y el nuevo rol.");
            return;
        }
        try {
            java.sql.Date vencimiento = null;
            if ("CHOFER".equals(rol)) {
                vencimiento = parsearFecha(RolVencimientoLicencia.getText());
            }
            boolean ok = GestorUsuarios.modificarRol(seleccionado.getId(), rol,
                RolLicencia.getText(), RolCategoriaLicencia.getText(), vencimiento);
            if (ok) {
                rolMensaje.setText("Rol actualizado correctamente.");
                buscar();
                CambiarUsuario.setValue(null);
                CambiarRol.setValue(null);
                RolLicencia.clear();
                RolCategoriaLicencia.clear();
                RolVencimientoLicencia.clear();
            }
        } catch (Exception e) {
            rolMensaje.setText("Error: " + e.getMessage());
            mostrarAlerta("No se pudo cambiar el rol", e.getMessage());
        }
    }

    /**
     * Valida los datos del formulario antes de solicitar el alta al UsuarioDAO.
     * Las validaciones evitan guardar campos obligatorios vacíos, DNI/correo inválidos
     * o contraseñas demasiado cortas.
     */
    @FXML
    private void registrar() {
        if (!esAdministrador()) { mostrarAlerta("Permiso denegado", "Solo un administrador puede crear usuarios."); return; }
        String nombre = UsuarioNombre.getText() == null ? "" : UsuarioNombre.getText().trim();
        String apellido = UsuarioApellido.getText() == null ? "" : UsuarioApellido.getText().trim();
        String dni = UsuarioDni.getText() == null ? "" : UsuarioDni.getText().trim();
        String email = UsuarioEmail.getText() == null ? "" : UsuarioEmail.getText().trim();
        String nombreUsuario = nombreUsuarioDesdeFormulario();
        String contrasena = UsuarioContrasena.getText();
        String rol = UsuarioRol.getValue();
        mensaje.setText("");
        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || email.isEmpty() ||
            nombreUsuario.isEmpty() || contrasena.isEmpty() || rol == null || rol.isBlank()) {
            mensaje.setText("Completá todos los campos.");
            return;
        }
        if (!dni.matches("\\d{7,8}")) {
            mensaje.setText("El DNI debe tener 7 u 8 números.");
            return;
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            mensaje.setText("Ingresá un correo electrónico válido.");
            return;
        }
        if (contrasena.length() < 6) {
            mensaje.setText("La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        try {
            java.sql.Date vencimiento = null;
            if ("CHOFER".equals(rol)) vencimiento = parsearFecha(UsuarioVencimientoLicencia.getText());
            boolean ok = GestorUsuarios.registrar(nombre, apellido, dni, email, nombreUsuario,
                rol, contrasena, UsuarioLicencia.getText(), UsuarioCategoriaLicencia.getText(), vencimiento);
            if (ok) {
                mensaje.setText("Usuario creado correctamente.");
                UsuarioNombre.clear(); UsuarioApellido.clear(); UsuarioDni.clear(); UsuarioEmail.clear();
                UsuarioNombreUsuarioField.clear(); UsuarioContrasena.clear(); UsuarioRol.setValue(null);
                UsuarioLicencia.clear(); UsuarioCategoriaLicencia.clear(); UsuarioVencimientoLicencia.clear();
                buscar();
            }
        } catch (Exception e) {
            mensaje.setText("Error: " + e.getMessage());
            mostrarAlerta("No se pudo crear el usuario", e.getMessage());
        }
    }

    @FXML private TextField UsuarioNombreUsuarioField;

    private String nombreUsuarioDesdeFormulario() {
        return UsuarioNombreUsuarioField.getText() == null ? "" : UsuarioNombreUsuarioField.getText().trim();
    }

    /** Convierte la fecha escrita en el formulario al tipo SQL esperado por la base. */
    private java.sql.Date parsearFecha(String valor) throws java.sql.SQLException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new java.sql.SQLException("Para un chofer, ingresá el vencimiento de la licencia en formato AAAA-MM-DD.");
        }
        try {
            return java.sql.Date.valueOf(valor.trim());
        } catch (IllegalArgumentException e) {
            throw new java.sql.SQLException("La fecha de vencimiento debe tener formato AAAA-MM-DD (por ejemplo, 2028-12-31).");
        }
    }

    @FXML
    private void irPaquetes() throws Exception {
        App.establecerRaiz("Dashboard");
    }

    /** Elimina el contexto de sesión y regresa a la pantalla de acceso. */
    @FXML
    private void cerrarSesion() throws Exception {
        DashboardController.setUsuarioActual(null);
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
