package JavaApp.delta.dao;

import JavaApp.delta.model.Usuario;
import java.sql.*;

public class UsuarioDAO {
    public Usuario autenticar(String nombreUsuario, String contrasena) throws SQLException {
        String consultaSQL = "SELECT id_usuario,nombre,apellido,nombre_usuario,rol FROM usuarios " +
                     "WHERE nombre_usuario=? AND password_hash=SHA2(?,256) AND activo=1";
        try (Connection conexion=ConexionBD.obtenerConexion();
             PreparedStatement sentencia=conexion.prepareStatement(consultaSQL)) {
            sentencia.setString(1, nombreUsuario);
            sentencia.setString(2, contrasena);
            try (ResultSet resultado=sentencia.executeQuery()) {
                if (!resultado.next()) return null;
                Usuario usuario=new Usuario();
                usuario.setId(resultado.getInt("id_usuario"));
                usuario.setNombre(resultado.getString("nombre"));
                usuario.setApellido(resultado.getString("apellido"));
                usuario.setNombreUsuario(resultado.getString("nombre_usuario"));
                usuario.setRol(resultado.getString("rol"));
                return usuario;
            }
        }
    }
}
