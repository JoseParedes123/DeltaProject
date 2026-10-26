package JavaApp.delta.dao;

import JavaApp.delta.model.Opcion;
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
        paquete.setChofer(resultado.getString("chofer"));
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
    public List<Paquete> buscarPorOperador(int idUsuario, String texto, String estado) throws SQLException {
        return buscarBase(texto, estado, "operador", idUsuario);
    }

    private List<Paquete> buscarBase(String texto, String estado, String filtroRol, Integer idUsuario)
            throws SQLException {

        StringBuilder consultaSQL = new StringBuilder(
            "SELECT p.*, COALESCE(e.estado,'SIN_ENVIO') estado, " +
            "COALESCE(CONCAT(u.sector,' / ',u.estante,' / ',u.posicion),'Sin ubicación') ubicacion, " +
            "COALESCE(CONCAT(uch.nombre,' ',uch.apellido),'Sin asignar') chofer " +
            "FROM paquetes p LEFT JOIN envios e ON e.id_paquete=p.id_paquete " +
            "LEFT JOIN rutas rt ON rt.id_ruta=e.id_ruta " +
            "LEFT JOIN choferes cf ON cf.id_chofer=rt.id_chofer " +
            "LEFT JOIN usuarios uch ON uch.id_usuario=cf.id_usuario " +
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
        } else if ("operador".equals(filtroRol)) {
            consultaSQL.append("WHERE e.estado IN ('PREPARADO', 'REGISTRADO' , 'DEVUELTO', 'RECIBIDO_DEPOSITO')");
        }else {
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
            "CASE WHEN h.tipo_evento='CAMBIO_ESTADO' THEN " +
            "CONCAT('Estado: ', COALESCE(h.valor_anterior,'?'), ' -> ', h.valor_nuevo, ' | ', COALESCE(h.descripcion,'')) " +
            "ELSE CONCAT('Modificación: ', COALESCE(h.descripcion, h.valor_nuevo)) END detalle " +
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

    // ------------------------------------------------------------------
    //  Asignación de paquetes PREPARADOS a un chofer (a través de una ruta)
    // ------------------------------------------------------------------

    /** Choferes que pueden salir a ruta: activos, usuario activo y licencia vigente. */
    public List<Opcion> choferesDisponibles() throws SQLException {
        String consultaSQL =
            "SELECT c.id_chofer, CONCAT(u.nombre,' ',u.apellido,' (',u.nombre_usuario,')') texto " +
            "FROM choferes c JOIN usuarios u ON u.id_usuario=c.id_usuario " +
            "WHERE u.rol='CHOFER' AND c.estado='ACTIVO' AND u.activo=1 AND c.fecha_vencimiento_licencia>=CURDATE() " +
            "ORDER BY u.apellido,u.nombre";
        return listarOpciones(consultaSQL, null);
    }

    /** Rutas PLANIFICADAS (todavía no salieron) de un chofer, con cuántos paquetes llevan. */
    public List<Opcion> rutasPlanificadas(int idChofer) throws SQLException {
        String consultaSQL =
            "SELECT r.id_ruta, CONCAT('Ruta #',r.id_ruta,' - ',DATE_FORMAT(r.fecha,'%d/%m'),' - ',r.destino,' - ',v.patente,' - ', " +
            "(SELECT COUNT(*) FROM envios e WHERE e.id_ruta=r.id_ruta),' paquete(s)') texto " +
            "FROM rutas r JOIN vehiculos v ON v.id_vehiculo=r.id_vehiculo " +
            "WHERE r.id_chofer=? AND r.estado='PLANIFICADA' ORDER BY r.fecha,r.id_ruta";
        return listarOpciones(consultaSQL, idChofer);
    }

    /** Vehículos que se pueden usar para una ruta nueva. */
    public List<Opcion> vehiculosDisponibles() throws SQLException {
        String consultaSQL =
            "SELECT id_vehiculo, CONCAT(patente,' - ',tipo,' ',COALESCE(marca,''),' (hasta ',capacidad_peso_kg,' kg)') texto " +
            "FROM vehiculos WHERE estado='DISPONIBLE' ORDER BY patente";
        return listarOpciones(consultaSQL, null);
    }

    private List<Opcion> listarOpciones(String consultaSQL, Integer parametro) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(consultaSQL)) {
            if (parametro != null) sentencia.setInt(1, parametro);
            List<Opcion> opciones = new ArrayList<>();
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    opciones.add(new Opcion(resultado.getInt(1), resultado.getString(2)));
                }
            }
            return opciones;
        }
    }

    /**
     * Asigna un paquete PREPARADO a un chofer. Si idRuta es null se crea una ruta nueva (PLANIFICADA)
     * con el vehículo indicado; si no, se agrega a esa ruta. Todo en una transacción.
     * Los errores de negocio se informan con SQLException para mostrarlos al usuario.
     * Devuelve un texto de confirmación.
     */
    public String asignarAChofer(int idPaquete, int idChofer, Integer idRuta, Integer idVehiculo, int idUsuario)
            throws SQLException {

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                // 1) El envío debe existir y estar PREPARADO (se bloquea la fila para evitar doble asignación)
                int idEnvio; String estado; Integer rutaAnterior; int idZonaDestino; double peso;
                try (PreparedStatement s = conexion.prepareStatement(
                        "SELECT e.id_envio,e.estado,e.id_ruta,e.id_zona_destino,p.peso_kg FROM envios e " +
                        "JOIN paquetes p ON p.id_paquete=e.id_paquete WHERE e.id_paquete=? FOR UPDATE")) {
                    s.setInt(1, idPaquete);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) throw new SQLException("El paquete no tiene un envío asociado.");
                        idEnvio = r.getInt(1);
                        estado = r.getString(2);
                        int ruta = r.getInt(3);
                        rutaAnterior = r.wasNull() ? null : ruta;
                        idZonaDestino = r.getInt(4);
                        peso = r.getDouble(5);
                    }
                }
                if (!"PREPARADO".equals(estado)) {
                    throw new SQLException("Solo se pueden asignar paquetes en estado PREPARADO (este está en " + estado + ").");
                }

                // 2) El chofer debe poder salir a ruta
                String nombreChofer;
                try (PreparedStatement s = conexion.prepareStatement(
                        "SELECT CONCAT(u.nombre,' ',u.apellido), c.estado, u.activo, c.fecha_vencimiento_licencia>=CURDATE() " +
                        "FROM choferes c JOIN usuarios u ON u.id_usuario=c.id_usuario WHERE c.id_chofer=?")) {
                    s.setInt(1, idChofer);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) throw new SQLException("El chofer elegido no existe.");
                        nombreChofer = r.getString(1);
                        if (!"ACTIVO".equals(r.getString(2)) || !r.getBoolean(3))
                            throw new SQLException(nombreChofer + " no está activo.");
                        if (!r.getBoolean(4))
                            throw new SQLException("La licencia de " + nombreChofer + " está vencida.");
                    }
                }

                // 3) Ruta: crear una nueva o validar la elegida
                int rutaFinal;
                int vehiculo;
                double cargaActual = 0;
                if (idRuta == null) {
                    if (idVehiculo == null) throw new SQLException("Elegí el vehículo para la ruta nueva.");
                    vehiculo = idVehiculo;
                    validarVehiculo(conexion, vehiculo, idZonaDestino, peso, cargaActual, true);
                    try (PreparedStatement s = conexion.prepareStatement(
                            "INSERT INTO rutas(fecha,origen,destino,observaciones,id_vehiculo,id_chofer,id_zona,estado) " +
                            "SELECT CURDATE(),'Depósito Central',z.nombre,'Ruta creada desde Gestión de Depósito',?,?,z.id_zona,'PLANIFICADA' " +
                            "FROM zonas z WHERE z.id_zona=?", Statement.RETURN_GENERATED_KEYS)) {
                        s.setInt(1, vehiculo);
                        s.setInt(2, idChofer);
                        s.setInt(3, idZonaDestino);
                        s.executeUpdate();
                        try (ResultSet k = s.getGeneratedKeys()) {
                            if (!k.next()) throw new SQLException("No se pudo crear la ruta.");
                            rutaFinal = k.getInt(1);
                        }
                    }
                } else {
                    rutaFinal = idRuta;
                    try (PreparedStatement s = conexion.prepareStatement(
                            "SELECT id_vehiculo,id_chofer,estado FROM rutas WHERE id_ruta=? FOR UPDATE")) {
                        s.setInt(1, rutaFinal);
                        try (ResultSet r = s.executeQuery()) {
                            if (!r.next()) throw new SQLException("La ruta elegida no existe.");
                            if (r.getInt(2) != idChofer) throw new SQLException("Esa ruta pertenece a otro chofer.");
                            if (!"PLANIFICADA".equals(r.getString(3)))
                                throw new SQLException("Esa ruta ya no está planificada (estado: " + r.getString(3) + ").");
                            vehiculo = r.getInt(1);
                        }
                    }
                    try (PreparedStatement s = conexion.prepareStatement(
                            "SELECT COALESCE(SUM(p.peso_kg),0) FROM envios e JOIN paquetes p ON p.id_paquete=e.id_paquete " +
                            "WHERE e.id_ruta=? AND e.id_envio<>?")) {
                        s.setInt(1, rutaFinal);
                        s.setInt(2, idEnvio);
                        try (ResultSet r = s.executeQuery()) { r.next(); cargaActual = r.getDouble(1); }
                    }
                    validarVehiculo(conexion, vehiculo, idZonaDestino, peso, cargaActual, false);
                }

                // 4) Asignar el envío a la ruta (parada = siguiente número)
                int parada;
                try (PreparedStatement s = conexion.prepareStatement(
                        "SELECT COALESCE(MAX(orden_parada),0)+1 FROM envios WHERE id_ruta=? AND id_envio<>?")) {
                    s.setInt(1, rutaFinal);
                    s.setInt(2, idEnvio);
                    try (ResultSet r = s.executeQuery()) { r.next(); parada = r.getInt(1); }
                }
                try (PreparedStatement s = conexion.prepareStatement(
                        "UPDATE envios SET id_ruta=?, orden_parada=? WHERE id_envio=?")) {
                    s.setInt(1, rutaFinal);
                    s.setInt(2, parada);
                    s.setInt(3, idEnvio);
                    s.executeUpdate();
                }

                // 5) Quedó registrado quién lo asignó, a quién y cuándo
                try (PreparedStatement s = conexion.prepareStatement(
                        "INSERT INTO historial(tipo_evento,tabla_afectada,registro_id,campo_modificado," +
                        "valor_anterior,valor_nuevo,id_usuario,descripcion) VALUES('MODIFICACION','envios',?,'id_ruta',?,?,?,?)")) {
                    s.setInt(1, idPaquete);
                    s.setString(2, rutaAnterior == null ? null : "Ruta #" + rutaAnterior);
                    s.setString(3, "Ruta #" + rutaFinal);
                    s.setInt(4, idUsuario);
                    s.setString(5, "Asignado al chofer " + nombreChofer + " (ruta #" + rutaFinal + ", parada " + parada + ")");
                    s.executeUpdate();
                }

                conexion.commit();
                return "Paquete asignado a " + nombreChofer + " en la ruta #" + rutaFinal + " (parada " + parada + ").";
            } catch (Exception excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        }
    }

    /** Revisa que el vehículo esté libre, pueda entrar a la zona y aguante el peso total. */
    private void validarVehiculo(Connection conexion, int idVehiculo, int idZonaDestino, double peso,
                                 double cargaActual, boolean exigirDisponible) throws SQLException {
        try (PreparedStatement s = conexion.prepareStatement(
                "SELECT patente,estado,capacidad_peso_kg,id_zona_restringida,motivo_restriccion FROM vehiculos WHERE id_vehiculo=?")) {
            s.setInt(1, idVehiculo);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) throw new SQLException("El vehículo elegido no existe.");
                String patente = r.getString(1);
                if (exigirDisponible && !"DISPONIBLE".equals(r.getString(2)))
                    throw new SQLException("El vehículo " + patente + " no está disponible (" + r.getString(2) + ").");
                int zonaRestringida = r.getInt(4);
                if (!r.wasNull() && zonaRestringida == idZonaDestino)
                    throw new SQLException("El vehículo " + patente + " no puede entrar a esa zona" +
                        (r.getString(5) == null ? "." : ": " + r.getString(5)));
                double capacidad = r.getDouble(3);
                if (cargaActual + peso > capacidad)
                    throw new SQLException("Se supera la capacidad del vehículo " + patente + ": " +
                        (cargaActual + peso) + " kg de " + capacidad + " kg.");
            }
        }
    }
}
