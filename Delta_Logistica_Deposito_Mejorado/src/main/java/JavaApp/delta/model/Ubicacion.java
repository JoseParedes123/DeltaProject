package JavaApp.delta.model;

/**
 * Modelo de una ubicación física dentro del depósito.
 * Sus datos identifican el lugar y permiten saber si está disponible.
 */
public class Ubicacion {

    // Atributos
    private int id;
    private String deposito;
    private String direccionDeposito;
    private String sector;
    private String estante;
    private String posicion;
    private boolean ocupada;

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeposito() {
        return deposito;
    }

    public void setDeposito(String deposito) {
        this.deposito = deposito;
    }

    public String getDireccionDeposito() {
        return direccionDeposito;
    }

    public void setDireccionDeposito(String direccionDeposito) {
        this.direccionDeposito = direccionDeposito;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getEstante() {
        return estante;
    }

    public void setEstante(String estante) {
        this.estante = estante;
    }

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }

    public boolean isOcupada() {
        return ocupada;
    }

    public void setOcupada(boolean ocupada) {
        this.ocupada = ocupada;
    }

    /**
     * Devuelve una descripción legible para mostrar la ubicación en una lista.
     */
    @Override
    public String toString() {
        return deposito + " · " + direccionDeposito + " · " + sector
                + " / Estante " + estante + " / Posición " + posicion
                + (ocupada ? " (ocupada)" : " (libre)");
    }
}
