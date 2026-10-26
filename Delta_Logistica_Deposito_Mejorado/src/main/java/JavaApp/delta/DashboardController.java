package JavaApp.delta;

import JavaApp.delta.dao.PaqueteDAO;
import JavaApp.delta.model.Opcion;
import JavaApp.delta.model.Paquete;
import JavaApp.delta.model.Ubicacion;
import JavaApp.delta.model.Usuario;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

/**
 * Controlador principal de gestión logística.
 * Conecta los controles de Dashboard.fxml con los DAO y adapta la pantalla
 * a la sesión activa y al rol del usuario.
 */
public class DashboardController {
    // Usuario autenticado compartido entre las pantallas mientras dura la sesión.
    private static Usuario usuarioActual;

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    @FXML private Button botonAdmin;
    @FXML private Label bienvenida;
    @FXML private Label etiquetaRol;
    @FXML private Label detalle;
    @FXML private TextField busqueda;
    @FXML private ComboBox<String> filtroEstado;
    @FXML private TableView<Paquete> tabla;
    @FXML private TableColumn<Paquete,String> columnaChofer;
    @FXML private TitledPane panelAsignacion;
    @FXML private ComboBox<Opcion> choferAsignar, rutaAsignar, vehiculoAsignar;
    @FXML private Label etiquetaVehiculo;
    private static final Opcion RUTA_NUEVA = new Opcion(0, "➕ Crear ruta nueva (hoy)");
    @FXML private TableColumn<Paquete,String> columnaCodigo, columnaDescripcion, columnaEstado, columnaUbicacion, columnaMercaderia;
    @FXML private TableColumn<Paquete,Double> columnaPeso;
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

    private final PaqueteDAO gestorPaquetes = new PaqueteDAO();

    private static final List<String> ESTADOS = List.of(
        "REGISTRADO", "RECIBIDO_DEPOSITO", "PREPARADO", "EN_TRANSITO",
        "EN_REPARTO", "ENTREGADO", "NO_ENTREGADO", "DEVUELTO",
        "EXTRAVIADO", "CANCELADO"
    );

    /**
     * Inicialización de la vista: enlaza columnas con propiedades del modelo,
     * carga las opciones de los filtros y registra los eventos de selección.
     * Al final configura los paneles según el rol y consulta los paquetes visibles.
     */
    @FXML
    public void initialize() {
        columnaCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        columnaDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        columnaUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        columnaMercaderia.setCellValueFactory(new PropertyValueFactory<>("tipoMercaderia"));
        columnaPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        columnaChofer.setCellValueFactory(new PropertyValueFactory<>("chofer"));

        filtroEstado.setItems(FXCollections.observableArrayList("TODOS"));
        filtroEstado.getItems().addAll(ESTADOS);
        filtroEstado.setValue("TODOS");

        nuevoEstado.setItems(FXCollections.observableArrayList(ESTADOS));
        estadoReparto.setItems(FXCollections.observableArrayList(
            "EN_REPARTO", "ENTREGADO", "NO_ENTREGADO"
        ));

        tipoIncidencia.setItems(FXCollections.observableArrayList(
            "EXTRAVIO", "DANIO", "DIRECCION_INCORRECTA",
            "PROBLEMA_ENTREGA", "PROBLEMA_PAQUETE", "OTRO"
        ));
        tipoIncidencia.setValue("OTRO");

        tabla.getSelectionModel().selectedItemProperty()
            .addListener((observable, anterior, seleccionado) -> seleccionar(seleccionado));

        choferAsignar.valueProperty().addListener((o, anterior, nuevo) -> cargarRutasDelChofer());
        rutaAsignar.valueProperty().addListener((o, anterior, nueva) -> mostrarVehiculoSiHaceFalta());
        mostrarVehiculoSiHaceFalta();

        configurarPorRol();
        buscar();
    }

    /**
     * Controla la presentación de la interfaz según el rol autenticado.
     * Visible decide si un panel se ve; Managed además evita que reserve espacio
     * cuando está oculto. Esta configuración mejora la experiencia de uso,
     * pero las operaciones sensibles también deben validarse en la lógica de negocio.
     */
    private void configurarPorRol() {
        if (usuarioActual == null) return;

        String rol = usuarioActual.getRol();
        bienvenida.setText("Usuario: " + usuarioActual.getNombre() + " " + usuarioActual.getApellido());
        etiquetaRol.setText("Rol: " + rol.replace('_', ' '));

        // Permisos de interfaz: depósito, reparto y administración son capacidades distintas.
        boolean puedeOperarDeposito =
            "OPERADOR_DEPOSITO".equals(rol) || "ADMINISTRADOR".equals(rol);

        boolean puedeOperarReparto =
            "CHOFER".equals(rol) || "ADMINISTRADOR".equals(rol);

        boolean puedeAdministrar =
            "ADMINISTRADOR".equals(rol) || "ADMINISTRACION".equals(rol);

        // El acceso al panel de gestión de usuarios queda reservado al administrador.
        boolean esAdministrador = "ADMINISTRADOR".equals(rol);
        botonAdmin.setVisible(esAdministrador);
        botonAdmin.setManaged(esAdministrador);

        panelAsignacion.setVisible(puedeOperarDeposito);
        panelAsignacion.setManaged(puedeOperarDeposito);
        if (puedeOperarDeposito) cargarChoferes();

        panelOperaciones.setVisible(puedeOperarDeposito);
        panelOperaciones.setManaged(puedeOperarDeposito);

        panelReparto.setVisible(puedeOperarReparto);
        panelReparto.setManaged(puedeOperarReparto);

        panelAdministracion.setVisible(puedeAdministrar);
        panelAdministracion.setManaged(puedeAdministrar);

        if ("CLIENTE".equals(rol)) {
            detalle.setText("Mostrando solamente tus envíos.");
        } else if ("CHOFER".equals(rol)) {
            detalle.setText("Mostrando los envíos asignados a tus rutas.");
        }
    }

    /** Reutiliza la regla de permisos de depósito en las acciones que la necesitan. */
    private boolean puedeOperarDeposito() {
        return usuarioActual != null &&
            ("OPERADOR_DEPOSITO".equals(usuarioActual.getRol()) ||
             "ADMINISTRADOR".equals(usuarioActual.getRol()));
    }

    @FXML
    /**
     * Actualiza la tabla con los paquetes permitidos para el usuario actual.
     * La consulta cambia según el rol: por ejemplo, el cliente ve sus envíos
     * y el chofer los que tiene asignados.
     */
    private void buscar() {
        if (usuarioActual == null) return;

        try {
            String rol = usuarioActual.getRol();
            List<Paquete> paquetes;

            if ("CLIENTE".equals(rol)) {
                paquetes = gestorPaquetes.buscarPorCliente(
                    usuarioActual.getId(), busqueda.getText(), filtroEstado.getValue()
                );
            } else if ("CHOFER".equals(rol)) {
                paquetes = gestorPaquetes.buscarPorChofer(
                    usuarioActual.getId(), busqueda.getText(), filtroEstado.getValue()
                );
            } else if ("OPERADOR_DEPOSITO".equals(rol)) {
                paquetes = gestorPaquetes.buscarPorOperador(
                        usuarioActual.getId(), busqueda.getText(), filtroEstado.getValue()
                    );
                } else {
                paquetes = gestorPaquetes.buscar(
                    busqueda.getText(), filtroEstado.getValue()
                );
            }

            tabla.setItems(FXCollections.observableArrayList(paquetes));

            String textoResultado =
                "CLIENTE".equals(rol) ? "Tus envíos encontrados: " :
                "CHOFER".equals(rol) ? "Envíos asignados encontrados: " :
                "Paquetes encontrados: ";

            detalle.setText(textoResultado + paquetes.size());
        } catch (Exception excepcion) {
            mostrarAlerta("Error", "No se pudo consultar la base de datos. " + excepcion.getMessage());
        }
    }

    @FXML
    private void cargarUbicaciones() {
        if (!puedeOperarDeposito()) return;

        try {
            ubicacion.setItems(
                FXCollections.observableArrayList(gestorPaquetes.ubicacionesLibres())
            );
        } catch (Exception excepcion) {
            mostrarAlerta("Error", "No se pudieron cargar las ubicaciones: " + excepcion.getMessage());
        }
    }

    private void seleccionar(Paquete paquete) {
        if (paquete == null) return;

        detalle.setText(
            "Código: " + paquete.getCodigo() +
            " | Estado: " + paquete.getEstado() +
            " | Ubicación: " + paquete.getUbicacion() +
            " | Chofer: " + paquete.getChofer()
        );

        if (puedeOperarDeposito()) {
            nuevoEstado.setValue(
                paquete.getEstado().equals("SIN_ENVIO") ? null : paquete.getEstado()
            );
            cargarUbicaciones();
        }
    }

    @FXML
    /**
     * Guarda el nuevo estado del paquete seleccionado.
     * La persistencia y el registro del cambio en el historial corresponden al DAO.
     */
    private void guardarEstado() {
        if (!puedeOperarDeposito()) {
            mostrarAlerta("Permiso denegado", "Tu rol no puede modificar estados desde el depósito.");
            return;
        }

        Paquete paquete = tabla.getSelectionModel().getSelectedItem();

        if (paquete == null || nuevoEstado.getValue() == null) {
            mostrarAlerta("Atención", "Seleccioná un paquete y un estado.");
            return;
        }

        try {
            gestorPaquetes.cambiarEstado(
                paquete.getId(), nuevoEstado.getValue(), usuarioActual.getId()
            );
            buscar();
            mostrarAlerta("Listo", "Estado actualizado correctamente.");
        } catch (Exception excepcion) {
            mostrarAlerta("Error", excepcion.getMessage());
        }
    }

    @FXML
    /**
     * Solicita mover el paquete a otra ubicación e incluye el motivo indicado.
     * El DAO registra el movimiento y actualiza la ocupación de las ubicaciones.
     */
    private void moverPaquete() {
        if (!puedeOperarDeposito()) {
            mostrarAlerta("Permiso denegado", "Tu rol no puede mover paquetes.");
            return;
        }

        Paquete paquete = tabla.getSelectionModel().getSelectedItem();

        if (paquete == null || ubicacion.getValue() == null) {
            mostrarAlerta("Atención", "Seleccioná un paquete y una ubicación libre.");
            return;
        }

        if (motivo.getText().trim().isEmpty()) {
            mostrarAlerta("Atención", "Indicá el motivo del movimiento.");
            return;
        }

        try {
            gestorPaquetes.mover(
                paquete.getId(),
                ubicacion.getValue().getId(),
                usuarioActual.getId(),
                motivo.getText().trim()
            );
            motivo.clear();
            buscar();
            mostrarAlerta("Listo", "Paquete ubicado correctamente.");
        } catch (Exception excepcion) {
            mostrarAlerta("Error", "No se pudo mover el paquete: " + excepcion.getMessage());
        }
    }


    // ---------------- Asignación de paquetes preparados a un chofer ----------------

    private void cargarChoferes() {
        try {
            choferAsignar.setItems(FXCollections.observableArrayList(gestorPaquetes.choferesDisponibles()));
        } catch (Exception excepcion) {
            mostrarAlerta("Error", "No se pudieron cargar los choferes: " + excepcion.getMessage());
        }
    }

    private void cargarRutasDelChofer() {
        Opcion chofer = choferAsignar.getValue();
        if (chofer == null) {
            rutaAsignar.getItems().clear();
            return;
        }
        try {
            List<Opcion> rutas = new java.util.ArrayList<>(gestorPaquetes.rutasPlanificadas(chofer.getId()));
            rutas.add(RUTA_NUEVA);
            rutaAsignar.setItems(FXCollections.observableArrayList(rutas));
            rutaAsignar.setValue(rutas.get(0));
        } catch (Exception excepcion) {
            mostrarAlerta("Error", "No se pudieron cargar las rutas: " + excepcion.getMessage());
        }
    }

    /** El vehículo solo se pide cuando se crea una ruta nueva. */
    private void mostrarVehiculoSiHaceFalta() {
        boolean nueva = rutaAsignar.getValue() == RUTA_NUEVA;
        vehiculoAsignar.setVisible(nueva);
        vehiculoAsignar.setManaged(nueva);
        etiquetaVehiculo.setVisible(nueva);
        etiquetaVehiculo.setManaged(nueva);
        if (nueva) {
            try {
                vehiculoAsignar.setItems(FXCollections.observableArrayList(gestorPaquetes.vehiculosDisponibles()));
            } catch (Exception excepcion) {
                mostrarAlerta("Error", "No se pudieron cargar los vehículos: " + excepcion.getMessage());
            }
        }
    }

    @FXML
    /**
     * Asigna un paquete preparado a un chofer y a una ruta/vehículo.
     * Las reglas de licencia, estado de ruta, capacidad y zona se verifican en el DAO.
     */
    private void asignarChofer() {
        if (!puedeOperarDeposito()) {
            mostrarAlerta("Permiso denegado", "Tu rol no puede asignar paquetes a choferes.");
            return;
        }

        Paquete paquete = tabla.getSelectionModel().getSelectedItem();
        Opcion chofer = choferAsignar.getValue();
        Opcion ruta = rutaAsignar.getValue();

        if (paquete == null || chofer == null || ruta == null) {
            mostrarAlerta("Atención", "Seleccioná un paquete, un chofer y una ruta.");
            return;
        }
        if (!"PREPARADO".equals(paquete.getEstado())) {
            mostrarAlerta("Atención", "Solo se pueden asignar paquetes en estado PREPARADO (este está en " +
                paquete.getEstado() + ").");
            return;
        }

        boolean nueva = ruta == RUTA_NUEVA;
        if (nueva && vehiculoAsignar.getValue() == null) {
            mostrarAlerta("Atención", "Elegí el vehículo para la ruta nueva.");
            return;
        }

        try {
            String resultado = gestorPaquetes.asignarAChofer(
                paquete.getId(), chofer.getId(),
                nueva ? null : ruta.getId(),
                nueva ? vehiculoAsignar.getValue().getId() : null,
                usuarioActual.getId()
            );
            buscar();
            cargarRutasDelChofer();
            mostrarAlerta("Listo", resultado);
        } catch (Exception excepcion) {
            mostrarAlerta("No se pudo asignar", excepcion.getMessage());
        }
    }

    @FXML
    /** Permite al chofer actualizar el resultado de la entrega de un envío. */
    private void actualizarEntrega() {
        if (usuarioActual == null ||
            !("CHOFER".equals(usuarioActual.getRol()) ||
              "ADMINISTRADOR".equals(usuarioActual.getRol()))) return;

        Paquete paquete = tabla.getSelectionModel().getSelectedItem();

        if (paquete == null || estadoReparto.getValue() == null) {
            mostrarAlerta("Atención", "Seleccioná un envío y un estado de reparto.");
            return;
        }

        try {
            gestorPaquetes.cambiarEstado(
                paquete.getId(), estadoReparto.getValue(), usuarioActual.getId()
            );
            buscar();
            mostrarAlerta("Listo", "Estado de reparto actualizado.");
        } catch (Exception excepcion) {
            mostrarAlerta("Error", excepcion.getMessage());
        }
    }

    @FXML
    /** Registra una incidencia vinculada al paquete seleccionado. */
    private void registrarIncidencia() {
        if (usuarioActual == null ||
            !("CHOFER".equals(usuarioActual.getRol()) ||
              "ADMINISTRADOR".equals(usuarioActual.getRol()))) return;

        Paquete paquete = tabla.getSelectionModel().getSelectedItem();

        if (paquete == null) {
            mostrarAlerta("Atención", "Seleccioná un envío.");
            return;
        }

        if (incidenciaTexto.getText().trim().isEmpty()) {
            mostrarAlerta("Atención", "Escribí una descripción del incidente.");
            return;
        }

        try {
            gestorPaquetes.registrarIncidencia(
                paquete.getId(),
                usuarioActual.getId(),
                tipoIncidencia.getValue(),
                incidenciaTexto.getText().trim()
            );
            incidenciaTexto.clear();
            mostrarAlerta("Listo", "Incidencia registrada correctamente.");
        } catch (Exception excepcion) {
            mostrarAlerta("Error", excepcion.getMessage());
        }
    }

    @FXML
    /** Consulta y presenta los movimientos y cambios de estado asociados al paquete. */
    private void verHistorial() {
        Paquete paquete = tabla.getSelectionModel().getSelectedItem();

        if (paquete == null) {
            mostrarAlerta("Atención", "Seleccioná un paquete.");
            return;
        }

        try {
            List<String> historial = gestorPaquetes.historial(paquete.getId());

            Alert ventana = new Alert(Alert.AlertType.INFORMATION);
            ventana.setTitle("Historial - " + paquete.getCodigo());
            ventana.setHeaderText("Trazabilidad del paquete");

            TextArea areaHistorial = new TextArea(
                historial.isEmpty()
                    ? "No hay movimientos ni cambios registrados."
                    : String.join("\n", historial)
            );
            areaHistorial.setEditable(false);
            areaHistorial.setWrapText(true);
            areaHistorial.setPrefRowCount(14);
            areaHistorial.setPrefColumnCount(70);

            ventana.getDialogPane().setContent(areaHistorial);
            ventana.showAndWait();
        } catch (Exception excepcion) {
            mostrarAlerta("Error", "No se pudo consultar el historial: " + excepcion.getMessage());
        }
    }

    @FXML
    /** Obtiene los conteos resumidos que se muestran en el panel informativo. */
    private void cargarResumen() {
        if (usuarioActual == null ||
            !("ADMINISTRADOR".equals(usuarioActual.getRol()) ||
              "ADMINISTRACION".equals(usuarioActual.getRol()))) return;

        try {
            int cantidadPaquetes = gestorPaquetes.contarPaquetes();
            int cantidadEntregados = gestorPaquetes.contarEnvios("ENTREGADO");
            int cantidadEnReparto = gestorPaquetes.contarEnvios("EN_REPARTO");
            int cantidadIncidencias = gestorPaquetes.contar(
                "SELECT COUNT(*) FROM incidencias WHERE estado IN ('ABIERTA','EN_INVESTIGACION')"
            );

            resumen.setText(
                "Paquetes: " + cantidadPaquetes +
                "   |   Entregados: " + cantidadEntregados +
                "   |   En reparto: " + cantidadEnReparto +
                "   |   Incidencias abiertas: " + cantidadIncidencias
            );
        } catch (Exception excepcion) {
            resumen.setText("No se pudo cargar el resumen: " + excepcion.getMessage());
        }
    }

    @FXML
    /** Navega al panel administrativo desde la interfaz principal. */
    private void irAdmin() throws Exception {
        if (usuarioActual == null || !"ADMINISTRADOR".equals(usuarioActual.getRol())) {
            mostrarAlerta("Permiso denegado", "Solo un administrador puede entrar al panel de usuarios.");
            return;
        }
        App.establecerRaiz("Admin");
    }

    @FXML
    /** Limpia el usuario de la sesión antes de volver al inicio de sesión. */
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
