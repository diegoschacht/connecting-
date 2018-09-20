/**
 * comentario de prueba 2
 */
package py.edu.upa.connecting.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Grupo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	Long codGrupo;
	String nombre;
	String objetivo;
	Double latitud;
	Double longitud;
	Integer codUsuarioCreacion;
	Date fechaUsuarioCreacion;
	
	public Long getCodGrupo() {
		return codGrupo;
	}
	public void setCodGrupo(Long codGrupo) {
		this.codGrupo = codGrupo;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getObjetivo() {
		return objetivo;
	}
	public void setObjetivo(String objetivo) {
		this.objetivo = objetivo;
	}
	public Double getLatitud() {
		return latitud;
	}
	public void setLatitud(Double latitud) {
		this.latitud = latitud;
	}
	public Double getLongitud() {
		return longitud;
	}
	public void setLongitud(Double longitud) {
		this.longitud = longitud;
	}
	
	public Integer getCodUsuarioCreacion() {
		return codUsuarioCreacion;
	}
	public void setCodUsuarioCreacion(Integer codUsuarioCreacion) {
		this.codUsuarioCreacion = codUsuarioCreacion;
	}
	public Date getFechaUsuarioCreacion() {
		
		return fechaUsuarioCreacion;
	}
	public void setFechaUsuarioCreacion(Date fechaUsuarioCreacion) {
		this.fechaUsuarioCreacion = fechaUsuarioCreacion;
	}
	
	
}
