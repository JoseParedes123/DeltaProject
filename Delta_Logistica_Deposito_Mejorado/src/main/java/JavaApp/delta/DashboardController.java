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
    public static void setUsuarioActual(Usuario u){usuarioActual=u;}

    @FXML private Label bienvenida;
    @FXML private Label rolLabel;
    @FXML private Label detalle;
    @FXML private TextField busqueda;
    @FXML private ComboBox<String> filtroEstado;
    @FXML private TableView<Paquete> tabla;
    @FXML private TableColumn<Paquete,String> colCodigo,colDescripcion,colEstado,colUbicacion,colTipo;
    @FXML private TableColumn<Paquete,Double> colPeso;
    @FXML private ComboBox<String> nuevoEstado;
    @FXML private ComboBox<Ubicacion> ubicacion;
    @FXML private TextField motivo;
    @FXML private TitledPane operacionesPane;
    @FXML private TitledPane repartoPane;
    @FXML private TitledPane adminPane;
    @FXML private ComboBox<String> estadoReparto;
    @FXML private TextArea incidenciaTexto;
    @FXML private ComboBox<String> tipoIncidencia;
    @FXML private Label resumen;

    private final PaqueteDAO dao=new PaqueteDAO();

    private static final List<String> ESTADOS = List.of("REGISTRADO","RECIBIDO_DEPOSITO","PREPARADO","EN_TRANSITO","EN_REPARTO","ENTREGADO","NO_ENTREGADO","DEVUELTO","EXTRAVIADO","CANCELADO");

    @FXML public void initialize(){
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoMercaderia"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        filtroEstado.setItems(FXCollections.observableArrayList("TODOS"));
        filtroEstado.getItems().addAll(ESTADOS);
        filtroEstado.setValue("TODOS");
        nuevoEstado.setItems(FXCollections.observableArrayList(ESTADOS));
        estadoReparto.setItems(FXCollections.observableArrayList("EN_REPARTO","ENTREGADO","NO_ENTREGADO"));
        tipoIncidencia.setItems(FXCollections.observableArrayList("EXTRAVIO","DANIO","DIRECCION_INCORRECTA","PROBLEMA_ENTREGA","PROBLEMA_PAQUETE","OTRO"));
        tipoIncidencia.setValue("OTRO");
        tabla.getSelectionModel().selectedItemProperty().addListener((o,a,b)->seleccionar(b));
        configurarPorRol();
        buscar();
    }

    private void configurarPorRol(){
        if(usuarioActual==null) return;
        String rol=usuarioActual.getRol();
        bienvenida.setText("Usuario: "+usuarioActual.getNombre()+" "+usuarioActual.getApellido());
        rolLabel.setText("Rol: "+rol.replace('_',' '));
        boolean deposito="OPERADOR_DEPOSITO".equals(rol) || "ADMINISTRADOR".equals(rol);
        boolean chofer="CHOFER".equals(rol) || "ADMINISTRADOR".equals(rol);
        boolean admin="ADMINISTRADOR".equals(rol) || "ADMINISTRACION".equals(rol);
        operacionesPane.setVisible(deposito); operacionesPane.setManaged(deposito);
        repartoPane.setVisible(chofer); repartoPane.setManaged(chofer);
        adminPane.setVisible(admin); adminPane.setManaged(admin);
        if("CLIENTE".equals(rol)) detalle.setText("Mostrando solamente tus envíos.");
        else if("CHOFER".equals(rol)) detalle.setText("Mostrando los envíos asignados a tus rutas.");
    }

    private boolean esDeposito(){return usuarioActual!=null && ("OPERADOR_DEPOSITO".equals(usuarioActual.getRol()) || "ADMINISTRADOR".equals(usuarioActual.getRol()));}

    @FXML private void buscar(){
        if(usuarioActual==null)return;
        try{
            String rol=usuarioActual.getRol();
            List<Paquete> datos;
            if("CLIENTE".equals(rol)) datos=dao.buscarPorCliente(usuarioActual.getId(),busqueda.getText(),filtroEstado.getValue());
            else if("CHOFER".equals(rol)) datos=dao.buscarPorChofer(usuarioActual.getId(),busqueda.getText(),filtroEstado.getValue());
            else datos=dao.buscar(busqueda.getText(),filtroEstado.getValue());
            tabla.setItems(FXCollections.observableArrayList(datos));
            detalle.setText(("CLIENTE".equals(rol)?"Tus envíos encontrados: ":"CHOFER".equals(rol)?"Envíos asignados encontrados: ":"Paquetes encontrados: ")+datos.size());
        }catch(Exception e){alert("Error","No se pudo consultar la base de datos. "+e.getMessage());}
    }

    @FXML private void cargarUbicaciones(){
        if(!esDeposito()) return;
        try{ubicacion.setItems(FXCollections.observableArrayList(dao.ubicacionesLibres()));}
        catch(Exception e){alert("Error","No se pudieron cargar las ubicaciones: "+e.getMessage());}
    }

    private void seleccionar(Paquete p){
        if(p==null)return;
        detalle.setText("Código: "+p.getCodigo()+" | Estado: "+p.getEstado()+" | Ubicación: "+p.getUbicacion());
        if(esDeposito()){
            nuevoEstado.setValue(p.getEstado().equals("SIN_ENVIO")?null:p.getEstado());
            cargarUbicaciones();
        }
    }

    @FXML private void guardarEstado(){
        if(!esDeposito()){alert("Permiso denegado","Tu rol no puede modificar estados desde el depósito.");return;}
        Paquete p=tabla.getSelectionModel().getSelectedItem();
        if(p==null||nuevoEstado.getValue()==null){alert("Atención","Seleccioná un paquete y un estado.");return;}
        try{dao.cambiarEstado(p.getId(),nuevoEstado.getValue(),usuarioActual.getId());buscar();alert("Listo","Estado actualizado correctamente.");}
        catch(Exception e){alert("Error",e.getMessage());}
    }

    @FXML private void moverPaquete(){
        if(!esDeposito()){alert("Permiso denegado","Tu rol no puede mover paquetes.");return;}
        Paquete p=tabla.getSelectionModel().getSelectedItem();
        if(p==null||ubicacion.getValue()==null){alert("Atención","Seleccioná un paquete y una ubicación libre.");return;}
        if(motivo.getText().trim().isEmpty()){alert("Atención","Indicá el motivo del movimiento.");return;}
        try{dao.mover(p.getId(),ubicacion.getValue().getId(),usuarioActual.getId(),motivo.getText().trim());motivo.clear();buscar();alert("Listo","Paquete ubicado correctamente.");}
        catch(Exception e){alert("Error","No se pudo mover el paquete: "+e.getMessage());}
    }

    @FXML private void actualizarEntrega(){
        if(usuarioActual==null || !("CHOFER".equals(usuarioActual.getRol()) || "ADMINISTRADOR".equals(usuarioActual.getRol()))) return;
        Paquete p=tabla.getSelectionModel().getSelectedItem();
        if(p==null || estadoReparto.getValue()==null){alert("Atención","Seleccioná un envío y un estado de reparto.");return;}
        try{dao.cambiarEstado(p.getId(),estadoReparto.getValue(),usuarioActual.getId());buscar();alert("Listo","Estado de reparto actualizado.");}
        catch(Exception e){alert("Error",e.getMessage());}
    }

    @FXML private void registrarIncidencia(){
        if(usuarioActual==null || !("CHOFER".equals(usuarioActual.getRol()) || "ADMINISTRADOR".equals(usuarioActual.getRol()))) return;
        Paquete p=tabla.getSelectionModel().getSelectedItem();
        if(p==null){alert("Atención","Seleccioná un envío.");return;}
        if(incidenciaTexto.getText().trim().isEmpty()){alert("Atención","Escribí una descripción del incidente.");return;}
        try{dao.registrarIncidencia(p.getId(),usuarioActual.getId(),tipoIncidencia.getValue(),incidenciaTexto.getText().trim());incidenciaTexto.clear();alert("Listo","Incidencia registrada correctamente.");}
        catch(Exception e){alert("Error",e.getMessage());}
    }

    @FXML private void verHistorial(){
        Paquete p=tabla.getSelectionModel().getSelectedItem();
        if(p==null){alert("Atención","Seleccioná un paquete.");return;}
        try{
            List<String> historial=dao.historial(p.getId());
            Alert a=new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Historial - "+p.getCodigo());
            a.setHeaderText("Trazabilidad del paquete");
            TextArea area=new TextArea(historial.isEmpty()?"No hay movimientos ni cambios registrados.":String.join("\n",historial));
            area.setEditable(false); area.setWrapText(true); area.setPrefRowCount(14); area.setPrefColumnCount(70);
            a.getDialogPane().setContent(area); a.showAndWait();
        }catch(Exception e){alert("Error","No se pudo consultar el historial: "+e.getMessage());}
    }

    @FXML private void cargarResumen(){
        if(usuarioActual==null || !("ADMINISTRADOR".equals(usuarioActual.getRol()) || "ADMINISTRACION".equals(usuarioActual.getRol()))) return;
        try{
            int paquetes=dao.contarPaquetes();
            int entregados=dao.contarEnvios("ENTREGADO");
            int reparto=dao.contarEnvios("EN_REPARTO");
            int incidencias=dao.contar("SELECT COUNT(*) FROM incidencias WHERE estado IN ('ABIERTA','EN_INVESTIGACION')");
            resumen.setText("Paquetes: "+paquetes+"   |   Entregados: "+entregados+"   |   En reparto: "+reparto+"   |   Incidencias abiertas: "+incidencias);
        }catch(Exception e){resumen.setText("No se pudo cargar el resumen: "+e.getMessage());}
    }

    @FXML private void cerrarSesion() throws Exception {usuarioActual=null;App.setRoot("Login");}

    private void alert(String t, String m) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(t); alert.setHeaderText(null); alert.setContentText(m); alert.showAndWait();
    }
}
