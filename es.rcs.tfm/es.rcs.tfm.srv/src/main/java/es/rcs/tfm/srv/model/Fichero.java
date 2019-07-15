package es.rcs.tfm.srv.model;

import java.time.ZonedDateTime;
import java.util.List;

import es.rcs.tfm.db.model.PubFileEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Fichero {
	
	private String nombre;
	private int tipo;

	private String gzFichero;
	private ZonedDateTime gzInstante;
	private long gzTamanio;

	private String xmlFichero;
	private ZonedDateTime xmlInstante;
	private long xmlTamanio;

	private String md5Fichero;
	List<Articulo> articulos;

	PubFileEntity entidad = null;
	
	private boolean tieneMd5 = false;
	private boolean tieneXml = false;

	private boolean descargaRequerida = false;
	private boolean hayCambiosEnBD = false;
	private boolean hayCambiosEnDisco = false;
	private boolean procesarRequerido = false;
	private boolean borrarXml = false;
	
	public Fichero(
			String filename, 
			int type, 
			String gzfilename,
			ZonedDateTime timestamp, 
			long size, 
			String xmlfilename, 
			boolean hasMd5, 
			String md5filename) {
		super();
		this.nombre = filename;
		this.tipo = type;

		this.gzFichero = gzfilename;
		this.gzInstante = timestamp;
		this.gzTamanio = size;

		this.xmlFichero = xmlfilename;

		this.tieneMd5 = hasMd5;
		this.md5Fichero = md5filename;
	}

	
}
