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
	        "jdbc:mysql://localhost:3306/logistica_delta?useSSL=false&serverTimezone=America/Argentina/Buenos_Aires&allowPublicKeyRetrieval=true";
	    private static final String USUARIO = "root";
	    private static final String CONTRASENA = "";

    private ConexionBD() {}

    public static Connection obtenerConexion() throws SQLException {
    	// Abre y devuelve una conexión usando los parámetros definidos.
        return DriverManager.getConnection(URL_CONEXION, USUARIO, CONTRASENA);
    }
}