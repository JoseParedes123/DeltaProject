package JavaApp.delta.dao;

import JavaApp.delta.model.Opcion;
import JavaApp.delta.model.Paquete;
import JavaApp.delta.model.Ubicacion;
import java.sql.SQLException;
import java.util.List;

/** Contrato de operaciones de paquetes que necesita la interfaz. */
public interface PaqueteRepositorio {
    List<Paquete> buscar(String texto, String estado) throws SQLException;
    List<Paquete> buscarPorCliente(int idUsuario, String texto, String estado) throws SQLException;
    List<Paquete> buscarPorChofer(int idUsuario, String texto, String estado) throws SQLException;
    List<Paquete> buscarPorOperador(int idUsuario, String texto, String estado) throws SQLException;
    List<Ubicacion> ubicacionesLibres() throws SQLException;
    void mover(int idPaquete, int idNuevaUbicacion, int idUsuario, String motivo) throws SQLException;
    void cambiarEstado(int idPaquete, String estado, int idUsuario) throws SQLException;
    List<String> historial(int idPaquete) throws SQLException;
    int contarPaquetes() throws SQLException;
    int contarEnvios(String estado) throws SQLException;
    int contar(String consultaSQL) throws SQLException;
    void registrarIncidencia(int idPaquete, int idUsuario, String tipo, String descripcion) throws SQLException;
    List<Opcion> choferesDisponibles() throws SQLException;
    List<Opcion> rutasPlanificadas(int idChofer) throws SQLException;
    List<Opcion> vehiculosDisponibles() throws SQLException;
    String asignarAChofer(int idPaquete, int idChofer, Integer idRuta, Integer idVehiculo, int idUsuario) throws SQLException;
}
