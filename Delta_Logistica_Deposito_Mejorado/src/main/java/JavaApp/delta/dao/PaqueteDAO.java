package JavaApp.delta.dao;

import JavaApp.delta.model.Paquete;
import JavaApp.delta.model.Ubicacion;
import java.sql.*;
import java.util.*;

public class PaqueteDAO {

    private Paquete map(ResultSet rs) throws SQLException {
        Paquete p=new Paquete();
        p.setId(rs.getInt("id_paquete"));
        p.setCodigo(rs.getString("codigo"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPeso(rs.getDouble("peso_kg"));
        p.setTipoMercaderia(rs.getString("tipo_mercaderia"));
        p.setFragil(rs.getBoolean("es_fragil"));
        p.setEstado(rs.getString("estado"));
        p.setUbicacion(rs.getString("ubicacion"));
        return p;
    }

    public List<Paquete> buscar(String texto, String estado) throws SQLException {
        return buscarBase(texto, estado, null, null);
    }

    public List<Paquete> buscarPorCliente(int usuarioId, String texto, String estado) throws SQLException {
        return buscarBase(texto, estado, "cliente", usuarioId);
    }

    public List<Paquete> buscarPorChofer(int usuarioId, String texto, String estado) throws SQLException {
        return buscarBase(texto, estado, "chofer", usuarioId);
    }

    private List<Paquete> buscarBase(String texto, String estado, String rolFiltro, Integer usuarioId) throws SQLException {
        StringBuilder sql=new StringBuilder(
          "SELECT p.*, COALESCE(e.estado,'SIN_ENVIO') estado, " +
          "COALESCE(CONCAT(u.sector,' / ',u.estante,' / ',u.posicion),'Sin ubicación') ubicacion " +
          "FROM paquetes p LEFT JOIN envios e ON e.id_paquete=p.id_paquete " +
          "LEFT JOIN movimientos_deposito md ON md.id_paquete=p.id_paquete AND md.es_actual=1 " +
          "LEFT JOIN ubicaciones u ON u.id_ubicacion=md.id_ubicacion_nueva ");
        List<Object> params=new ArrayList<>();
        if ("cliente".equals(rolFiltro)) {
            sql.append("JOIN clientes cl ON cl.id_cliente=e.id_cliente ");
            sql.append("WHERE cl.id_usuario=? ");
            params.add(usuarioId);
        } else if ("chofer".equals(rolFiltro)) {
            sql.append("JOIN rutas r ON r.id_ruta=e.id_ruta ");
            sql.append("JOIN choferes ch ON ch.id_chofer=r.id_chofer ");
            sql.append("WHERE ch.id_usuario=? ");
            params.add(usuarioId);
        } else {
            sql.append("WHERE 1=1 ");
        }
        if(texto!=null && !texto.trim().isEmpty()){
            sql.append("AND (p.codigo LIKE ? OR p.descripcion LIKE ? OR p.tipo_mercaderia LIKE ?) ");
            String x="%"+texto.trim()+"%"; params.add(x);params.add(x);params.add(x);
        }
        if(estado!=null && !estado.equals("TODOS")){
            sql.append("AND e.estado=? "); params.add(estado);
        }
        sql.append("ORDER BY p.fecha_registro DESC");
        try(Connection c=ConexionBD.obtenerConexion(); PreparedStatement ps=c.prepareStatement(sql.toString())){
            for(int i=0;i<params.size();i++) ps.setObject(i+1,params.get(i));
            List<Paquete> out=new ArrayList<>();
            try(ResultSet rs=ps.executeQuery()){while(rs.next()) out.add(map(rs));}
            return out;
        }
    }

    public List<Ubicacion> ubicacionesLibres() throws SQLException {
        String sql="SELECT u.id_ubicacion,d.nombre deposito,u.sector,u.estante,u.posicion,u.ocupada " +
                   "FROM ubicaciones u JOIN depositos d ON d.id_deposito=u.id_deposito " +
                   "WHERE u.ocupada=0 AND d.activo=1 ORDER BY d.nombre,u.sector,u.estante,u.posicion";
        try(Connection c=ConexionBD.obtenerConexion(); PreparedStatement ps=c.prepareStatement(sql);
            ResultSet rs=ps.executeQuery()){
            List<Ubicacion> out=new ArrayList<>();
            while(rs.next()){Ubicacion u=new Ubicacion();u.setId(rs.getInt("id_ubicacion"));
                u.setDeposito(rs.getString("deposito"));u.setSector(rs.getString("sector"));
                u.setEstante(rs.getString("estante"));u.setPosicion(rs.getString("posicion"));
                u.setOcupada(rs.getBoolean("ocupada"));out.add(u);}
            return out;
        }
    }

    public void mover(int paqueteId,int nuevaUbicacion,int usuarioId,String motivo) throws SQLException {
        String actualSql="SELECT md.id_movimiento,md.id_ubicacion_nueva FROM movimientos_deposito md " +
                         "WHERE md.id_paquete=? AND md.es_actual=1 FOR UPDATE";
        String insert="INSERT INTO movimientos_deposito(id_paquete,id_ubicacion_anterior,id_ubicacion_nueva,es_actual,id_usuario,motivo) VALUES(?,?,?,?,?,?)";
        String oldOff="UPDATE movimientos_deposito SET es_actual=0 WHERE id_paquete=? AND es_actual=1";
        String free="UPDATE ubicaciones SET ocupada=0 WHERE id_ubicacion=?";
        String occupy="UPDATE ubicaciones SET ocupada=1 WHERE id_ubicacion=?";
        try(Connection c=ConexionBD.obtenerConexion()){
            c.setAutoCommit(false);
            try{
                Integer anterior=null;
                try(PreparedStatement ps=c.prepareStatement(actualSql)){ps.setInt(1,paqueteId);try(ResultSet rs=ps.executeQuery()){if(rs.next()) anterior=rs.getInt(2);}}
                if(anterior!=null){try(PreparedStatement ps=c.prepareStatement(oldOff)){ps.setInt(1,paqueteId);ps.executeUpdate();}
                    try(PreparedStatement ps=c.prepareStatement(free)){ps.setInt(1,anterior);ps.executeUpdate();}}
                try(PreparedStatement ps=c.prepareStatement(occupy)){ps.setInt(1,nuevaUbicacion);ps.executeUpdate();}
                try(PreparedStatement ps=c.prepareStatement(insert)){ps.setInt(1,paqueteId);ps.setObject(2,anterior);ps.setInt(3,nuevaUbicacion);ps.setInt(4,1);ps.setInt(5,usuarioId);ps.setString(6,motivo);ps.executeUpdate();}
                c.commit();
            }catch(Exception ex){c.rollback();throw ex;}
        }
    }

    public void cambiarEstado(int paqueteId,String estado,int usuarioId) throws SQLException {
        String sql="UPDATE envios SET estado=? WHERE id_paquete=?";
        String hist="INSERT INTO historial(tipo_evento,tabla_afectada,registro_id,campo_modificado,valor_nuevo,id_usuario,descripcion) VALUES('CAMBIO_ESTADO','envios',?,'estado',?,?,?)";
        try(Connection c=ConexionBD.obtenerConexion()){
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement(sql)){ps.setString(1,estado);ps.setInt(2,paqueteId);if(ps.executeUpdate()==0)throw new SQLException("El paquete no tiene un envío asociado.");}
            try(PreparedStatement ps=c.prepareStatement(hist)){ps.setInt(1,paqueteId);ps.setString(2,estado);ps.setInt(3,usuarioId);ps.setString(4,"Cambio realizado desde Gestión de Depósito");ps.executeUpdate();}
            c.commit();
        }
    }

    public List<String> historial(int paqueteId) throws SQLException {
        String sql="SELECT DATE_FORMAT(fecha_hora,'%d/%m/%Y %H:%i') fecha, " +
                   "CONCAT('Movimiento: ', COALESCE(CONCAT('ubicación ', id_ubicacion_anterior, ' -> ', id_ubicacion_nueva), 'sin ubicación anterior'), ' | ', motivo) detalle " +
                   "FROM movimientos_deposito WHERE id_paquete=? " +
                   "UNION ALL " +
                   "SELECT DATE_FORMAT(h.fecha_hora,'%d/%m/%Y %H:%i') fecha, " +
                   "CONCAT('Estado: ', COALESCE(h.valor_anterior,'?'), ' -> ', h.valor_nuevo, ' | ', COALESCE(h.descripcion,'')) detalle " +
                   "FROM historial h WHERE h.tabla_afectada='envios' AND h.registro_id=? " +
                   "ORDER BY fecha DESC";
        try(Connection c=ConexionBD.obtenerConexion();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,paqueteId); ps.setInt(2,paqueteId);
            try(ResultSet rs=ps.executeQuery()){
                List<String> l=new ArrayList<>();
                while(rs.next()) l.add(rs.getString("fecha")+"  -  "+rs.getString("detalle"));
                return l;
            }
        }
    }

    public int contarPaquetes() throws SQLException {
        return contar("SELECT COUNT(*) FROM paquetes");
    }

    public int contarEnvios(String estado) throws SQLException {
        String sql="SELECT COUNT(*) FROM envios"+(estado==null?"":" WHERE estado=?");
        try(Connection c=ConexionBD.obtenerConexion(); PreparedStatement ps=c.prepareStatement(sql)){
            if(estado!=null) ps.setString(1,estado);
            try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}
        }
    }

    public int contar(String sql) throws SQLException {
        try(Connection c=ConexionBD.obtenerConexion();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
            rs.next(); return rs.getInt(1);
        }
    }

    public void registrarIncidencia(int paqueteId,int usuarioId,String tipo,String descripcion) throws SQLException {
        String sql="INSERT INTO incidencias(id_envio,fecha,hora,lugar,id_usuario_registro,tipo,descripcion) " +
                   "SELECT e.id_envio,CURDATE(),CURTIME(),'Gestión de Logística Delta',?,?,? FROM envios e WHERE e.id_paquete=?";
        try(Connection c=ConexionBD.obtenerConexion();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,usuarioId); ps.setString(2,tipo); ps.setString(3,descripcion); ps.setInt(4,paqueteId);
            if(ps.executeUpdate()==0) throw new SQLException("El paquete no tiene un envío asociado.");
        }
    }
}
