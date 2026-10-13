package AplicacionJava.delta.modulos;

public class paquete {
	//datos del paquete
	private int id;
	private String codigo, descripcion, tipo_mercaderia, estado, ubicacion;
	private double peso;
	private boolean es_fragil;
	//buscar datos
	public int getId() {return id;}
	public String getCodigo() {return codigo;}
	public String getDesc() {return descripcion;}
	public double getPeso() {return peso;}
	public String getTipo() {return tipo_mercaderia;}
	public boolean esFragil() {return es_fragil;}
	public String getEstado() {return estado;}
	public String getUbicacion() {return ubicacion;}
	//reescribir datos
	public void setId(int _id) {id = _id;}
	public void setCodigo(String _codigo) {codigo = _codigo;}
	public void setDesc(String _desc) {descripcion = _desc;}
	public void setPeso(double _peso) {peso = _peso;}
	public void setTipo(String _tipo) {tipo_mercaderia = _tipo;}
	public void setFragil(boolean _fragil) {es_fragil = _fragil;}
	public void setEstado(String _estado) {estado = _estado;}
	public void setUbicacion(String _ubicacion) {ubicacion = _ubicacion;}
	
}
