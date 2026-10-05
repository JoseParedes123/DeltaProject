package JavaApp.delta.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionBD {
    private static final String URL_CONEXION =
        "jdbc:mysql://localhost:3306/logistica_delta?useSSL=false&serverTimezone=America/Argentina/Buenos_Aires&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "";

    private ConexionBD() {}

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL_CONEXION, USUARIO, CONTRASENA);
    }
}
