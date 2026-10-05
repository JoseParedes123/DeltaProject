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
        StringBuilder sql=new StringBuilder(
          "SELECT p.*, COALESCE(e.estado,'SIN_ENVIO') estado, " +
          "COALESCE(CONCAT(u.sector,' / ',u.estante,' / ',u.posicion),'Sin ubicación') ubicacion " +
          "FROM paquetes p LEFT JOIN envios e ON e.id_paquete=p.id_paquete " +
          "LEFT JOIN movimientos_deposito md ON md.id_paquete=p.id_paquete AND md.es_actual=1 " +
          "LEFT JOIN ubicaciones u ON u.id_ubicacion=md.id_ubicacion_nueva " +
          "WHERE 1=1");
        List<Object> params=new ArrayList<>();
        if(texto!=null && !texto.trim().isEmpty()){
            sql.append(" AND (p.codigo LIKE ? OR p.descripcion LIKE ? OR p.tipo_mercaderia LIKE ?)");
            String x="%"+texto.trim()+"%"; params.add(x);params.add(x);params.add(x);
        }
        if(estado!=null && !estado.equals("TODOS")){
            sql.append(" AND e.estado=?"); params.add(estado);
        }
        sql.append(" ORDER BY p.fecha_registro DESC");
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
        String sql="SELECT DATE_FORMAT(fecha_hora,'%d/%m/%Y %H:%i') fecha, motivo " +
                   "FROM movimientos_deposito WHERE id_paquete=? ORDER BY fecha_hora DESC";
        try(Connection c=ConexionBD.obtenerConexion();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,paqueteId);
            try(ResultSet rs=ps.executeQuery()){List<String> l=new ArrayList<>();while(rs.next())l.add(rs.getString("fecha")+"  -  "+rs.getString("motivo"));return l;}}
    }
}