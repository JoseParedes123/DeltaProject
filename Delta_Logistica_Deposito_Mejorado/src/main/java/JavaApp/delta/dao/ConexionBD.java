package JavaApp.delta.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Único punto central para abrir conexiones JDBC a la base compartida. */
public final class ConexionBD {
    private static final String URL_CONEXION = valor("DELTA_DB_URL", "jdbc:mysql://localhost:3306/logistica_delta?serverTimezone=America/Argentina/Buenos_Aires");
    private static final String USUARIO = valor("DELTA_DB_USER", "root");
    private static final String CONTRASENA = valor("DELTA_DB_PASSWORD", "");

    private ConexionBD() {}

    private static String valor(String nombre, String valorPorDefecto) {
        String propiedad = System.getProperty(nombre);
        if (propiedad != null && !propiedad.isBlank()) return propiedad;
        String entorno = System.getenv(nombre);
        return entorno != null && !entorno.isBlank() ? entorno : valorPorDefecto;
    }

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL_CONEXION, USUARIO, CONTRASENA);
    }
}
