package JavaApp.delta.model;

public class Ubicacion {
    private int id;
    private String deposito;
    private String sector;
    private String estante;
    private String posicion;
    private boolean ocupada;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDeposito() { return deposito; }
    public void setDeposito(String deposito) { this.deposito = deposito; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getEstante() { return estante; }
    public void setEstante(String estante) { this.estante = estante; }
    public String getPosicion() { return posicion; }
    public void setPosicion(String posicion) { this.posicion = posicion; }
    public boolean isOcupada() { return ocupada; }
    public void setOcupada(boolean ocupada) { this.ocupada = ocupada; }

    @Override public String toString() {
        return sector + " / Estante " + estante + " / Posición " + posicion +
               (ocupada ? " (ocupada)" : " (libre)");
    }
}