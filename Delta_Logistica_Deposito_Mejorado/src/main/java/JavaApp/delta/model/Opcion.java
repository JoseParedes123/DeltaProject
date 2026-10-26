package JavaApp.delta.model;

/** Par id / texto para llenar ComboBox (choferes, rutas, vehículos). */
public class Opcion {
    private final int id;
    private final String texto;

    public Opcion(int id, String texto) {
        this.id = id;
        this.texto = texto;
    }

    public int getId() { return id; }

    @Override
    public String toString() { return texto; }
}
