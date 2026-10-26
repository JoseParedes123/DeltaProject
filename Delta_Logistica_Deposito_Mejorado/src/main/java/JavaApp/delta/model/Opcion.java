package JavaApp.delta.model;

/**
 * Opción sencilla para listas desplegables: conserva un identificador de base de datos
 * y un texto legible para la persona usuaria.
 */

/** Par id / texto para llenar ComboBox (choferes, rutas, vehículos). */
public class Opcion {
	//atributos
    private final int id;
    private final String texto;
    //constructor
    public Opcion(int id, String texto) {
        this.id = id;
        this.texto = texto;
    }
    //obtener el id
    public int getId() { return id; }

    @Override //convertir a texto
    public String toString() { return texto; }
}
