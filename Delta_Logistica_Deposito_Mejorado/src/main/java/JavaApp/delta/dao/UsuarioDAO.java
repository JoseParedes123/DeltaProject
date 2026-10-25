package JavaApp.delta.dao;

import JavaApp.delta.model.Usuario;
import java.sql.*;
import java.util.*;

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
    public Boolean registrar(String nombre_usuario, String rol, String password_hash ) throws SQLException{
    	String consultaSQL = "INSERT INTO usuarios (nombre, apellido, nombre_usuario, rol, password_hash, activo, email) VALUES(00, 00, ?, ?, SHA2(?,256), 1, 00)";
    	try (Connection conexion=ConexionBD.obtenerConexion();
            PreparedStatement sentencia=conexion.prepareStatement(consultaSQL)) {
    		sentencia.setString(1, nombre_usuario);
    		sentencia.setString(2, rol);
    		sentencia.setString(3, password_hash);
    		int cambios = sentencia.executeUpdate();
			return cambios > 0;
    	}
    }
    
    private Usuario convertirResultado(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(resultado.getInt("id_usuario"));
        usuario.setNombre(resultado.getString("nombre"));
        usuario.setApellido(resultado.getString("apellido"));
        usuario.setNombreUsuario(resultado.getString("nombre_usuario"));
        usuario.setRol(resultado.getString("rol"));
        return usuario;
    }
    
    public List<Usuario> buscarPorNombre(){
		String consultaSQL = "SELECT nombre FROM usuarios";
    	List<Usuario> ListaUsuarios = new ArrayList<>();
    	try (Connection conexion=ConexionBD.obtenerConexion();
            PreparedStatement sentencia=conexion.prepareStatement(consultaSQL);
    		ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                	Usuario usuario = new Usuario();
                	usuario.setNombre(resultado.getString("nombre"));
                    ListaUsuarios.add(usuario);
            }
    	}catch(Exception e) {e.printStackTrace();}
    	return ListaUsuarios;
    }
    
    public Boolean modificarRol(String rol, String nombre) throws SQLException{
		String consultaSQL = "UPDATE usuarios SET rol = ? WHERE nombre = ?";
		int cambios = 0;
    	try (Connection conexion=ConexionBD.obtenerConexion();
                PreparedStatement sentencia=conexion.prepareStatement(consultaSQL)) {
        		sentencia.setString(1, rol);
        		sentencia.setString(2, nombre);
        		cambios = sentencia.executeUpdate();
    			return cambios > 0;
    	}
    }
    
    
    public List<Usuario> buscarBase(String rol){
    	String consultaSQL = "SELECT Id_usuario, nombre, apellido, email, nombre_usuario, rol FROM usuarios";
    	List<Usuario> ListaUsuarios = new ArrayList<>();
    	try (Connection conexion=ConexionBD.obtenerConexion();
            PreparedStatement sentencia=conexion.prepareStatement(consultaSQL);
    		ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    ListaUsuarios.add(convertirResultado(resultado));
            }
    	}catch(Exception e) {e.printStackTrace();}
    	return ListaUsuarios;
    }

}
