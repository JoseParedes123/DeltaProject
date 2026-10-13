package AplicacionJava.delta.DAO;

import AplicacionJava.delta.modulos.paquete;
import AplicacionJava.delta.modulos.ubicacion;
import java.sql.*;
import java.util.*;


public class paqueteDAO {
 private paquete convertirResultado(ResultSet resultado) throws SQLException {
        paquete paquete = new paquete();
        paquete.setId(resultado.getInt("id_paquete"));
        paquete.setCodigo(resultado.getString("codigo"));
        paquete.setDesc(resultado.getString("descripcion"));
        paquete.setPeso(resultado.getDouble("peso_kg"));
        paquete.setTipo(resultado.getString("tipo_mercaderia"));
        paquete.setFragil(resultado.getBoolean("es_fragil"));
        paquete.setEstado(resultado.getString("estado"));
        paquete.setUbicacion(resultado.getString("ubicacion"));
        return paquete;
    }

    public List<paquete> buscar(String texto, String estado) throws SQLException {
        return buscarBase(texto, estado, null, null);
    }

    public List<paquete> buscarPorCliente(int idUsuario, String texto, String estado) throws SQLException {
        return buscarBase(texto, estado, "cliente", idUsuario);
    }

    public List<paquete> buscarPorChofer(int idUsuario, String texto, String estado) throws SQLException {
        return buscarBase(texto, estado, "chofer", idUsuario);
    }

    private List<paquete> buscarBase(String texto, String estado, String filtroRol, Integer idUsuario)
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

        try (Connection conexionDB = conexion.obtenerConexion();
             PreparedStatement sentencia = conexionDB.prepareStatement(consultaSQL.toString())) {

            for (int indice = 0; indice < parametros.size(); indice++) {
                sentencia.setObject(indice + 1, parametros.get(indice));
            }

            List<paquete> paquetes = new ArrayList<>();

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    paquetes.add(convertirResultado(resultado));
                }
            }

            return paquetes;
        }
    }
}
