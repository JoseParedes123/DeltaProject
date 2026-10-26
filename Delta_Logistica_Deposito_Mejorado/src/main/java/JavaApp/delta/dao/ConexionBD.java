package JavaApp.delta.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Único punto central para abrir conexiones JDBC con MySQL.
 * Los DAO solicitan una conexión a esta clase en vez de repetir la configuración.
 */
public final class ConexionBD { 
	//datos necesarios para conectarse a la base
	private static final String URL_CONEXION =
	        "jdbc:mysql://pma.torga.com.ar:3306/s11_logistica_delta?serverTimezone=America/Argentina/Buenos_Aires";
	    private static final String USUARIO = "u11_ri1RgHICT0";
	    private static final String CONTRASENA = "QdhO09kHGLJSAVWjv.l^W@@@";

    private ConexionBD() {}

    public static Connection obtenerConexion() throws SQLException {
    	// Abre y devuelve una conexión usando los parámetros definidos.
        return DriverManager.getConnection(URL_CONEXION, USUARIO, CONTRASENA);
    }
}