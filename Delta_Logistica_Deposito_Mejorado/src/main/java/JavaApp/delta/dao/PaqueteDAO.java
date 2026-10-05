package JavaApp.delta.dao;

import JavaApp.delta.model.Paquete;
import JavaApp.delta.model.Ubicacion;
import java.sql.*;
import java.util.*;

public class PaqueteDAO {

    private Paquete convertirResultado(ResultSet resultado) throws SQLException {
        Paquete paquete = new Paquete();
        paquete.setId(resultado.getInt("id_paquete"));
        paquete.setCodigo(resultado.getString("codigo"));
        paquete.setDescripcion(resultado.getString("descripcion"));
        paquete.setPeso(resultado.getDouble("peso_kg"));
        paquete.setTipoMercaderia(resultado.getString("tipo_mercaderia"));
        paquete.setFragil(resultado.getBoolean("es_fragil"));
        paquete.setEstado(resultado.getString("estado"));
        paquete.setUbicacion(resultado.getString("ubicacion"));
        return paquete;
    }

    public List<Paquete> buscar(String texto, String estado) throws SQLException {
        return buscarBase(texto, estado, null, null);
    }

    public List<Paquete> buscarPorCliente(int idUsuario, String texto, String estado) throws SQLException {
        return buscarBase(texto, estado, "cliente", idUsuario);
    }

    public List<Paquete> buscarPorChofer(int idUsuario, String texto, String estado) throws SQLException {
        return buscarBase(texto, estado, "chofer", idUsuario);
    }

    private List<Paquete> buscarBase(String texto, String estado, String filtroRol, Integer idUsuario)
            throws SQLException {

        StringBuilder consultaSQL = new StringBuilder(
            "SELECT p.*, COALESCE(e.estado,'SIN_ENVIO') estado, " +
            "COALESCE(CONCAT(u.sector,' / ',u.estante,' / ',u.posicion),'Sin ubicación') ubicacion " +
            "FROM paquetes p LEFT JOIN envios e ON e.id_paquete=p.id_paquete " +
            "LEFT JOIN movimientos_deposito md ON md.id_paquete=p.id_paquete AND md.es_actual=1 " +
            "LEFT JOIN ubicaciones u ON u.id_ubicacion=md.id_ubicacion_nueva "
        );

        List<Object> parametros = new ArrayList<>();

        if ("cliente".equals(filtroRol)) {
            consultaSQL.append("JOIN clientes cl ON cl.id_cliente=e.id_cliente ");
            consultaSQL.append("WHERE cl.id_usuario=? ");
            parametros.add(idUsuario);
        } else if ("chofer".equals(filtroRol)) {
            consultaSQL.append("JOIN rutas r ON r.id_ruta=e.id_ruta ");
            consultaSQL.append("JOIN choferes ch ON ch.id_chofer=r.id_chofer ");
            consultaSQL.append("WHERE ch.id_usuario=? ");
            parametros.add(idUsuario);
        } else {
            consultaSQL.append("WHERE 1=1 ");
        }

        if (texto != null && !texto.trim().isEmpty()) {
            consultaSQL.append(
                "AND (p.codigo LIKE ? OR p.descripcion LIKE ? OR p.tipo_mercaderia LIKE ?) "
            );
            String textoBusqueda = "%" + texto.trim() + "%";
            parametros.add(textoBusqueda);
            parametros.add(textoBusqueda);
            parametros.add(textoBusqueda);
        }

        if (estado != null && !estado.equals("TODOS")) {
            consultaSQL.append("AND e.estado=? ");
            parametros.add(estado);
        }

        consultaSQL.append("ORDER BY p.fecha_registro DESC");

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(consultaSQL.toString())) {

            for (int indice = 0; indice < parametros.size(); indice++) {
                sentencia.setObject(indice + 1, parametros.get(indice));
            }

            List<Paquete> paquetes = new ArrayList<>();

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    paquetes.add(convertirResultado(resultado));
                }
            }

            return paquetes;
        }
    }

    public List<Ubicacion> ubicacionesLibres() throws SQLException {
        String consultaSQL =
            "SELECT u.id_ubicacion,d.nombre deposito,u.sector,u.estante,u.posicion,u.ocupada " +
            "FROM ubicaciones u JOIN depositos d ON d.id_deposito=u.id_deposito " +
            "WHERE u.ocupada=0 AND d.activo=1 ORDER BY d.nombre,u.sector,u.estante,u.posicion";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(consultaSQL);
             ResultSet resultado = sentencia.executeQuery()) {

            List<Ubicacion> ubicaciones = new ArrayList<>();

            while (resultado.next()) {
                Ubicacion ubicacion = new Ubicacion();
                ubicacion.setId(resultado.getInt("id_ubicacion"));
                ubicacion.setDeposito(resultado.getString("deposito"));
                ubicacion.setSector(resultado.getString("sector"));
                ubicacion.setEstante(resultado.getString("estante"));
                ubicacion.setPosicion(resultado.getString("posicion"));
                ubicacion.setOcupada(resultado.getBoolean("ocupada"));
                ubicaciones.add(ubicacion);
            }

            return ubicaciones;
        }
    }

    public void mover(int idPaquete, int idNuevaUbicacion, int idUsuario, String motivo)
            throws SQLException {

        String consultaUbicacionActual =
            "SELECT md.id_movimiento,md.id_ubicacion_nueva FROM movimientos_deposito md " +
            "WHERE md.id_paquete=? AND md.es_actual=1 FOR UPDATE";

        String consultaInsertarMovimiento =
            "INSERT INTO movimientos_deposito(" +
            "id_paquete,id_ubicacion_anterior,id_ubicacion_nueva,es_actual,id_usuario,motivo" +
            ") VALUES(?,?,?,?,?,?)";

        String consultaDesactivarMovimiento =
            "UPDATE movimientos_deposito SET es_actual=0 WHERE id_paquete=? AND es_actual=1";

        String consultaLiberarUbicacion =
            "UPDATE ubicaciones SET ocupada=0 WHERE id_ubicacion=?";

        String consultaOcuparUbicacion =
            "UPDATE ubicaciones SET ocupada=1 WHERE id_ubicacion=?";

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                Integer idUbicacionAnterior = null;

                try (PreparedStatement sentencia = conexion.prepareStatement(consultaUbicacionActual)) {
                    sentencia.setInt(1, idPaquete);

                    try (ResultSet resultado = sentencia.executeQuery()) {
                        if (resultado.next()) {
                            idUbicacionAnterior = resultado.getInt(2);
                        }
                    }
                }

                if (idUbicacionAnterior != null) {
                    try (PreparedStatement sentencia =
                             conexion.prepareStatement(consultaDesactivarMovimiento)) {
                        sentencia.setInt(1, idPaquete);
                        sentencia.executeUpdate();
                    }

                    try (PreparedStatement sentencia =
                             conexion.prepareStatement(consultaLiberarUbicacion)) {
                        sentencia.setInt(1, idUbicacionAnterior);
                        sentencia.executeUpdate();
                    }
                }

                try (PreparedStatement sentencia =
                         conexion.prepareStatement(consultaOcuparUbicacion)) {
                    sentencia.setInt(1, idNuevaUbicacion);
                    sentencia.executeUpdate();
                }

                try (PreparedStatement sentencia =
                         conexion.prepareStatement(consultaInsertarMovimiento)) {
                    sentencia.setInt(1, idPaquete);
                    sentencia.setObject(2, idUbicacionAnterior);
                    sentencia.setInt(3, idNuevaUbicacion);
                    sentencia.setInt(4, 1);
                    sentencia.setInt(5, idUsuario);
                    sentencia.setString(6, motivo);
                    sentencia.executeUpdate();
                }

                conexion.commit();
            } catch (Exception excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        }
    }

    public void cambiarEstado(int idPaquete, String estado, int idUsuario) throws SQLException {
        String consultaSQL = "UPDATE envios SET estado=? WHERE id_paquete=?";
        String consultaHistorial =
            "INSERT INTO historial(" +
            "tipo_evento,tabla_afectada,registro_id,campo_modificado," +
            "valor_nuevo,id_usuario,descripcion" +
            ") VALUES('CAMBIO_ESTADO','envios',?,'estado',?,?,?)";

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try (PreparedStatement sentencia = conexion.prepareStatement(consultaSQL)) {
                sentencia.setString(1, estado);
                sentencia.setInt(2, idPaquete);

                if (sentencia.executeUpdate() == 0) {
                    throw new SQLException("El paquete no tiene un envío asociado.");
                }
            }

            try (PreparedStatement sentencia = conexion.prepareStatement(consultaHistorial)) {
                sentencia.setInt(1, idPaquete);
                sentencia.setString(2, estado);
                sentencia.setInt(3, idUsuario);
                sentencia.setString(4, "Cambio realizado desde Gestión de Depósito");
                sentencia.executeUpdate();
            }

            conexion.commit();
        }
    }

    public List<String> historial(int idPaquete) throws SQLException {
        String consultaSQL =
            "SELECT DATE_FORMAT(fecha_hora,'%d/%m/%Y %H:%i') fecha, " +
            "CONCAT('Movimiento: ', COALESCE(" +
            "CONCAT('ubicación ', id_ubicacion_anterior, ' -> ', id_ubicacion_nueva), " +
            "'sin ubicación anterior'), ' | ', motivo) detalle " +
            "FROM movimientos_deposito WHERE id_paquete=? " +
            "UNION ALL " +
            "SELECT DATE_FORMAT(h.fecha_hora,'%d/%m/%Y %H:%i') fecha, " +
            "CONCAT('Estado: ', COALESCE(h.valor_anterior,'?'), ' -> ', " +
            "h.valor_nuevo, ' | ', COALESCE(h.descripcion,'')) detalle " +
            "FROM historial h WHERE h.tabla_afectada='envios' AND h.registro_id=? " +
            "ORDER BY fecha DESC";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(consultaSQL)) {

            sentencia.setInt(1, idPaquete);
            sentencia.setInt(2, idPaquete);

            try (ResultSet resultado = sentencia.executeQuery()) {
                List<String> historial = new ArrayList<>();

                while (resultado.next()) {
                    historial.add(
                        resultado.getString("fecha") + "  -  " +
                        resultado.getString("detalle")
                    );
                }

                return historial;
            }
        }
    }

    public int contarPaquetes() throws SQLException {
        return contar("SELECT COUNT(*) FROM paquetes");
    }

    public int contarEnvios(String estado) throws SQLException {
        String consultaSQL =
            "SELECT COUNT(*) FROM envios" +
            (estado == null ? "" : " WHERE estado=?");

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(consultaSQL)) {

            if (estado != null) {
                sentencia.setString(1, estado);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    public int contar(String consultaSQL) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(consultaSQL);
             ResultSet resultado = sentencia.executeQuery()) {

            resultado.next();
            return resultado.getInt(1);
        }
    }

    public void registrarIncidencia(
            int idPaquete, int idUsuario, String tipo, String descripcion)
            throws SQLException {

        String consultaSQL =
            "INSERT INTO incidencias(" +
            "id_envio,fecha,hora,lugar,id_usuario_registro,tipo,descripcion" +
            ") " +
            "SELECT e.id_envio,CURDATE(),CURTIME()," +
            "'Gestión de Logística Delta',?,?,? " +
            "FROM envios e WHERE e.id_paquete=?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(consultaSQL)) {

            sentencia.setInt(1, idUsuario);
            sentencia.setString(2, tipo);
            sentencia.setString(3, descripcion);
            sentencia.setInt(4, idPaquete);

            if (sentencia.executeUpdate() == 0) {
                throw new SQLException("El paquete no tiene un envío asociado.");
            }
        }
    }
}
