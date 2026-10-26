package JavaApp.delta.dao;

import JavaApp.delta.model.Usuario;
import java.sql.*;
import java.util.*;

public class UsuarioDAO {
    public Usuario autenticar(String nombreUsuario, String contrasena) throws SQLException {
        String sql = "SELECT id_usuario,nombre,apellido,nombre_usuario,rol FROM usuarios " +
                     "WHERE nombre_usuario=? AND password_hash=SHA2(?,256) AND activo=1";
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Usuario u = new Usuario();
                u.setId(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre"));
                u.setApellido(rs.getString("apellido"));
                u.setNombreUsuario(rs.getString("nombre_usuario"));
                u.setRol(rs.getString("rol"));
                return u;
            }
        }
    }

    public boolean registrar(String nombre, String apellido, String dni, String email,
                             String nombreUsuario, String rol, String contrasena,
                             String licencia, String categoria, java.sql.Date vencimiento) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                String sql = "INSERT INTO usuarios (nombre,apellido,dni,email,nombre_usuario,rol,password_hash,activo) " +
                             "VALUES (?,?,?,?,?,?,SHA2(?,256),1)";
                int id;
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, nombre);
                    ps.setString(2, apellido);
                    ps.setString(3, dni);
                    ps.setString(4, email);
                    ps.setString(5, nombreUsuario);
                    ps.setString(6, rol);
                    ps.setString(7, contrasena);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No se pudo obtener el ID del usuario creado.");
                        id = keys.getInt(1);
                    }
                }
                if ("CHOFER".equals(rol)) {
                    insertarOActualizarChofer(c, id, licencia, categoria, vencimiento);
                }
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public boolean modificarRol(int idUsuario, String rol, String licencia,
                                String categoria, java.sql.Date vencimiento) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("UPDATE usuarios SET rol=? WHERE id_usuario=?")) {
                    ps.setString(1, rol);
                    ps.setInt(2, idUsuario);
                    if (ps.executeUpdate() == 0) throw new SQLException("No se encontró el usuario seleccionado.");
                }
                if ("CHOFER".equals(rol)) {
                    insertarOActualizarChofer(c, idUsuario, licencia, categoria, vencimiento);
                }
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private void insertarOActualizarChofer(Connection c, int idUsuario, String licencia,
                                            String categoria, java.sql.Date vencimiento) throws SQLException {
        if (licencia == null || licencia.trim().isEmpty() ||
            categoria == null || categoria.trim().isEmpty() || vencimiento == null) {
            throw new SQLException("Para asignar el rol CHOFER, completá licencia, categoría y fecha de vencimiento.");
        }
        if (vencimiento.before(new java.sql.Date(System.currentTimeMillis()))) {
            throw new SQLException("La licencia del chofer debe tener una fecha de vencimiento vigente.");
        }

        String update = "UPDATE choferes SET licencia=?, categoria_licencia=?, fecha_vencimiento_licencia=?, estado='ACTIVO' WHERE id_usuario=?";
        try (PreparedStatement ps = c.prepareStatement(update)) {
            ps.setString(1, licencia.trim());
            ps.setString(2, categoria.trim().toUpperCase(Locale.ROOT));
            ps.setDate(3, vencimiento);
            ps.setInt(4, idUsuario);
            if (ps.executeUpdate() > 0) return;
        }
        String insert = "INSERT INTO choferes (id_usuario,licencia,categoria_licencia,fecha_vencimiento_licencia,estado) VALUES (?,?,?,?, 'ACTIVO')";
        try (PreparedStatement ps = c.prepareStatement(insert)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, licencia.trim());
            ps.setString(3, categoria.trim().toUpperCase(Locale.ROOT));
            ps.setDate(4, vencimiento);
            ps.executeUpdate();
        }
    }

    private Usuario convertirResultado(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        u.setNombreUsuario(rs.getString("nombre_usuario"));
        u.setRol(rs.getString("rol"));
        return u;
    }

    public List<Usuario> buscarPorNombre() {
        return buscarBase("");
    }

    public List<Usuario> buscarBase(String filtro) {
        String sql = "SELECT id_usuario,nombre,apellido,nombre_usuario,rol FROM usuarios ORDER BY nombre,apellido";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) usuarios.add(convertirResultado(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarios;
    }
}
