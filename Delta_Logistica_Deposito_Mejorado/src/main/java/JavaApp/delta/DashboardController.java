package JavaApp.delta;

import JavaApp.delta.dao.PaqueteDAO;
import JavaApp.delta.model.Paquete;
import JavaApp.delta.model.Ubicacion;
import JavaApp.delta.model.Usuario;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class DashboardController {
    private static Usuario usuarioActual;

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    @FXML private Label bienvenida;
    @FXML private Label etiquetaRol;
    @FXML private Label detalle;
    @FXML private TextField busqueda;
    @FXML private ComboBox<String> filtroEstado;
    @FXML private TableView<Paquete> tabla;
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

    @FXML
    public void initialize() {
        columnaCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        columnaDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        columnaUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        columnaMercaderia.setCellValueFactory(new PropertyValueFactory<>("tipoMercaderia"));
        columnaPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));

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

        configurarPorRol();
        buscar();
    }

    private void configurarPorRol() {
        if (usuarioActual == null) return;

        String rol = usuarioActual.getRol();
        bienvenida.setText("Usuario: " + usuarioActual.getNombre() + " " + usuarioActual.getApellido());
        etiquetaRol.setText("Rol: " + rol.replace('_', ' '));

        boolean puedeOperarDeposito =
            "OPERADOR_DEPOSITO".equals(rol) || "ADMINISTRADOR".equals(rol);

        boolean puedeOperarReparto =
            "CHOFER".equals(rol) || "ADMINISTRADOR".equals(rol);

        boolean puedeAdministrar =
            "ADMINISTRADOR".equals(rol) || "ADMINISTRACION".equals(rol);

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

    private boolean puedeOperarDeposito() {
        return usuarioActual != null &&
            ("OPERADOR_DEPOSITO".equals(usuarioActual.getRol()) ||
             "ADMINISTRADOR".equals(usuarioActual.getRol()));
    }

    @FXML
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
            " | Ubicación: " + paquete.getUbicacion()
        );

        if (puedeOperarDeposito()) {
            nuevoEstado.setValue(
                paquete.getEstado().equals("SIN_ENVIO") ? null : paquete.getEstado()
            );
            cargarUbicaciones();
        }
    }

    @FXML
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

    @FXML
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
