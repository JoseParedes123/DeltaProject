package JavaApp.delta.dao;

import JavaApp.delta.model.Usuario;
import java.sql.*;

public class UsuarioDAO {
    public Usuario autenticar(String usuario, String password) throws SQLException {
        String sql = "SELECT id_usuario,nombre,apellido,nombre_usuario,rol FROM usuarios " +
                     "WHERE nombre_usuario=? AND password_hash=SHA2(?,256) AND activo=1";
        try (Connection c=ConexionBD.obtenerConexion();
             PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            try (ResultSet rs=ps.executeQuery()) {
                if (!rs.next()) return null;
                Usuario u=new Usuario();
                u.setId(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre"));
                u.setApellido(rs.getString("apellido"));
                u.setNombreUsuario(rs.getString("nombre_usuario"));
                u.setRol(rs.getString("rol"));
                return u;
            }
        }
    }
}