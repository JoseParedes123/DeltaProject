package AplicacionJava.delta.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class conexion {
	private static final String url =//la url de la DB local
	        "jdbc:mysql://localhost:3306/logistica_delta?useSSL=false&serverTimezone=America/Argentina/Buenos_Aires&allowPublicKeyRetrieval=true";
	private static final String usuario = "root";//el usuario local
	private static final String contrasena = "";//la contraseña del usuario local
	
	private conexion() {}

        public static Connection obtenerConexion() throws SQLException {//hacer conexión
            return DriverManager.getConnection(url, usuario, contrasena);}
        
}
