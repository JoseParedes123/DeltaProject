package JavaApp.delta;

import JavaApp.delta.dao.PaqueteDAO;
import JavaApp.delta.model.Paquete;
import JavaApp.delta.model.Ubicacion;
import JavaApp.delta.model.Usuario;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class DashboardController {
    private static Usuario usuarioActual;
    public static void setUsuarioActual(Usuario u){usuarioActual=u;}
    @FXML private Label bienvenida;
    @FXML private TextField busqueda;
    @FXML private ComboBox<String> filtroEstado;
    @FXML private TableView<Paquete> tabla;
    @FXML private TableColumn<Paquete,String> colCodigo,colDescripcion,colEstado,colUbicacion,colTipo;
    @FXML private TableColumn<Paquete,Double> colPeso;
    @FXML private ComboBox<String> nuevoEstado;
    @FXML private ComboBox<Ubicacion> ubicacion;
    @FXML private TextField motivo;
    @FXML private Label detalle;
    private final PaqueteDAO dao=new PaqueteDAO();

    @FXML public void initialize(){
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoMercaderia"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        filtroEstado.setItems(FXCollections.observableArrayList("TODOS","REGISTRADO","RECIBIDO_DEPOSITO","PREPARADO","EN_TRANSITO","EN_REPARTO","ENTREGADO","NO_ENTREGADO","DEVUELTO","EXTRAVIADO","CANCELADO"));
        filtroEstado.setValue("TODOS");
        nuevoEstado.setItems(FXCollections.observableArrayList("REGISTRADO","RECIBIDO_DEPOSITO","PREPARADO","EN_TRANSITO","EN_REPARTO","ENTREGADO","NO_ENTREGADO","DEVUELTO","EXTRAVIADO","CANCELADO"));
        tabla.getSelectionModel().selectedItemProperty().addListener((o,a,b)->seleccionar(b));
        if(usuarioActual!=null) bienvenida.setText("Operador: "+usuarioActual.getNombre()+" "+usuarioActual.getApellido());
        buscar();
    }

    @FXML private void buscar(){
        try{tabla.setItems(FXCollections.observableArrayList(dao.buscar(busqueda.getText(),filtroEstado.getValue())));detalle.setText("Paquetes encontrados: "+tabla.getItems().size());}
        catch(Exception e){alert("Error","No se pudo consultar la base de datos. Verificá MySQL y la configuración de ConexionBD.");}
    }
    @FXML private void cargarUbicaciones(){
        try{ubicacion.setItems(FXCollections.observableArrayList(dao.ubicacionesLibres()));}
        catch(Exception e){alert("Error","No se pudieron cargar las ubicaciones.");}
    }
    private void seleccionar(Paquete p){
        if(p==null)return;
        detalle.setText("Código: "+p.getCodigo()+" | Estado: "+p.getEstado()+" | Ubicación: "+p.getUbicacion());
        nuevoEstado.setValue(p.getEstado().equals("SIN_ENVIO")?null:p.getEstado());
        cargarUbicaciones();
    }
    @FXML private void guardarEstado(){
        Paquete p=tabla.getSelectionModel().getSelectedItem();
        if(p==null||nuevoEstado.getValue()==null){alert("Atención","Seleccioná un paquete y un estado.");return;}
        try{dao.cambiarEstado(p.getId(),nuevoEstado.getValue(),usuarioActual.getId());buscar();alert("Listo","Estado actualizado correctamente.");}
        catch(Exception e){alert("Error",e.getMessage());}
    }
    @FXML private void moverPaquete(){
        Paquete p=tabla.getSelectionModel().getSelectedItem();
        if(p==null||ubicacion.getValue()==null){alert("Atención","Seleccioná un paquete y una ubicación libre.");return;}
        if(motivo.getText().trim().isEmpty()){alert("Atención","Indicá el motivo del movimiento.");return;}
        try{dao.mover(p.getId(),ubicacion.getValue().getId(),usuarioActual.getId(),motivo.getText().trim());motivo.clear();buscar();alert("Listo","Paquete ubicado correctamente.");}
        catch(Exception e){alert("Error","No se pudo mover el paquete: "+e.getMessage());}
    }
    @FXML private void cerrarSesion() throws Exception {usuarioActual=null;App.setRoot("Login");}
    private void alert(String t, String m) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(t);
        alert.setHeaderText(null);
        alert.setContentText(m);
        alert.showAndWait();
    }
}