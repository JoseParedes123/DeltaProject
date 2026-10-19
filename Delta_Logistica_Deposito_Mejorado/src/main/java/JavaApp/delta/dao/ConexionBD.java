package JavaApp.delta.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionBD {
    private static final String URL_CONEXION =
            "jdbc:mysql://pma.torga.com.ar:3306/s11_logistica_delta?serverTimezone=America/Argentina/Buenos_Aires";
    private static final String USUARIO = "u11_ri1RgHICT0";
    private static final String CONTRASENA = "QdhO09kHGLJSAVWjv.l^W@@@";

    private ConexionBD() {}

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL_CONEXION, USUARIO, CONTRASENA);
    }
}