package JavaApp.delta.model;

public class Paquete {
    private int id;
    private String codigo;
    private String descripcion;
    private double peso;
    private String tipoMercaderia;
    private boolean fragil;
    private String estado;
    private String ubicacion;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getPeso() { return peso; }
    public void setPeso(double peso) { this.peso = peso; }
    public String getTipoMercaderia() { return tipoMercaderia; }
    public void setTipoMercaderia(String tipoMercaderia) { this.tipoMercaderia = tipoMercaderia; }
    public boolean isFragil() { return fragil; }
    public void setFragil(boolean fragil) { this.fragil = fragil; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
}