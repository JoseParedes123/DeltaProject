package JavaApp.delta.model;

/**
 * Modelo de una opción para una lista desplegable.
 * Conserva el identificador de la base y el texto que se muestra a la persona usuaria.
 */
public class Opcion {

    // Atributos
    private final int id;
    private final String texto;

    // Constructor
    public Opcion(int id, String texto) {
        this.id = id;
        this.texto = texto;
    }

    // Getter del identificador
    public int getId() {
        return id;
    }

    /**
     * Devuelve el texto que JavaFX muestra en el ComboBox.
     */
    @Override
    public String toString() {
        return texto;
    }
}
