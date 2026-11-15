package JavaApp.delta.dao;

import JavaApp.delta.model.Usuario;
import java.sql.SQLException;
import java.util.List;

/** Contrato de autenticación y administración de usuarios. */
public interface UsuarioRepositorio {
    Usuario autenticar(String nombreUsuario, String contrasena) throws SQLException;
    boolean registrar(String nombre, String apellido, String dni, String email, String nombreUsuario, String rol, String contrasena, String licencia, String categoria, java.sql.Date vencimiento) throws SQLException;
    boolean modificarRol(int idUsuario, String rol, String licencia, String categoria, java.sql.Date vencimiento) throws SQLException;
    List<Usuario> buscarPorNombre();
    List<Usuario> buscarBase(String filtro);
}
