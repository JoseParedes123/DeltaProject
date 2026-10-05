package JavaApp.delta.model;

public class Usuario {
    private int id;
    private String nombre;
    private String apellido;
    private String nombreUsuario;
    private String rol;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}