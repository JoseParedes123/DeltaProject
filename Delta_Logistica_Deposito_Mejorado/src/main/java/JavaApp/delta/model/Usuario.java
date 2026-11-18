package JavaApp.delta.model;

/**
 * Modelo de datos de una persona con acceso al sistema.
 * El rol se utiliza para decidir qué pantallas y operaciones puede usar.
 */
public class Usuario {

    // Atributos
    private int id;
    private String nombre;
    private String apellido;
    private String nombreUsuario;
    private String rol;
    private String fechaCreacion;

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * Define el texto que se muestra cuando un Usuario aparece en una lista desplegable.
     */
    @Override
    public String toString() {
        if (nombreUsuario == null) {
            return nombre == null ? "" : nombre;
        }

        return nombreUsuario + " (" + nombre + " " + apellido + ")";
    }
}
