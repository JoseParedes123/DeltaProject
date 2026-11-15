package JavaApp.delta.dao;

import java.sql.Connection;
import java.sql.SQLException;

/** Clase base para los DAO: centraliza el acceso a la conexión JDBC. */
public abstract class BaseDAO {
    /** Las clases hijas reutilizan este método sin conocer los datos de conexión. */
    protected Connection obtenerConexion() throws SQLException {
        return ConexionBD.obtenerConexion();
    }
}
