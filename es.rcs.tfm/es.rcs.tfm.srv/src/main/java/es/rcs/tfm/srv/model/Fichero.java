package es.rcs.tfm.srv.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

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
	private String tipo;

	private String gzDirectorio;
	private String gzFichero;
	private ZonedDateTime gzInstante;
	private long gzTamanio;

	private boolean md5 = false;
	private String md5Fichero;

	private String uncompressFichero;

	private boolean hayCambiosEnBD = false;
	private boolean hayCambiosEnDisco = false;

	private PubFileEntity entidad = null;

	private List<Articulo> articulos;

	public Fichero() {
		super();
	}
/*	
	public Fichero(
			String filename, 
			String type, 
			String gzdirectorio,
			String gzfilename,
			ZonedDateTime timestamp, 
			long size, 
			String xmlfilename, 
			boolean hasMd5, 
			String md5filename) {
		super();
		this.nombre = filename;
		this.tipo = type;

		this.gzDirectorio = gzdirectorio;
		this.gzFichero = gzfilename;
		this.gzInstante = timestamp;
		this.gzTamanio = size;

		this.xmlFichero = xmlfilename;

		this.tieneMd5 = hasMd5;
		this.md5Fichero = md5filename;
	}
*/
	private static Pattern PMC_PTR=Pattern.compile("PMC(\\d+)");
	/**
	 * Se encarga de instanciar un objeto fichero a partir de una linea 
	 * de un fichero CSV. Necesario para la librería SFM-CSV
	 * @param filename Ruta al fichero
	 * @param title Título del artículo
	 * @param pmcId Identificador en Pubmed Central
	 * @param timestamp Fecha del artículo
	 * @param pmid Identificador en Pubmed
	 * @param license Tipo de licencia
	 * @return El Fichero
	 */
	public static Fichero of(
			String filename,
			String title,
			String pmcId,
			Date timestamp,
			String pmid,
			String license) {
		
		Matcher m = PMC_PTR.matcher(pmcId);
		if (m.find()) {
			pmcId = m.group(1);
		}
		
		String gzDirectorio = FilenameUtils.getFullPath(filename);
		String gzFilename = FilenameUtils.getName(filename);
		String uncompressFilename = FilenameUtils.removeExtension(gzFilename);
		String nombre = FilenameUtils.removeExtension(uncompressFilename);

		ZonedDateTime gztimestamp = ZonedDateTime.ofInstant(
				timestamp.toInstant(), 
				ZoneId.systemDefault());
		
		Fichero fichero = new Fichero();
		fichero.setTipo(PubFileEntity.FTP_PMC);
		fichero.setNombre(nombre);
		fichero.setGzDirectorio(gzDirectorio);
		fichero.setGzFichero(gzFilename);
		fichero.setGzInstante(gztimestamp);
		
		fichero.setUncompressFichero(uncompressFilename);
		
		HashMap<String, String> ids = new HashMap<String, String>();
		ids.put(Articulo.PUBMED_ID_NAME, pmid);
		ids.put(Articulo.PMC_ID_NAME, pmcId);
		
		Articulo articulo = new Articulo();
		articulo.setFicheroPmc(fichero);
		articulo.setTitulo(title);
		articulo.setFicheroPmc(fichero);
		articulo.setPmid(pmid);
		articulo.setIds(ids);
		
		fichero.articulos = new ArrayList<Articulo>();
		fichero.articulos.add(articulo);
		
		return fichero;
		
	}
	
	/**
	 * Se encarga de instanciar un objeto fichero a partir de un fichero del FTP de PubMed
	 * @param filename Ruta al fichero
	 * @param timestamp Fecha del artículo
	 * @param size Tamanio del articulo
	 * @return El Fichero
	 */
	public static Fichero getInstance(
			String filename, 
			Calendar timestamp, 
			long size) {

		String gzDirectorio = FilenameUtils.getFullPath(filename);
		String gzFilename = FilenameUtils.getName(filename);
		String uncompressFilename = FilenameUtils.removeExtension(gzFilename);
		String nombre = FilenameUtils.removeExtension(uncompressFilename);

		ZonedDateTime gztimestamp = ZonedDateTime.ofInstant(
				timestamp.toInstant(), 
				ZoneId.systemDefault());
		
		Fichero fichero = new Fichero();
		fichero.setTipo(PubFileEntity.FTP_PUBMED);
		fichero.setNombre(nombre);
		fichero.setGzDirectorio(gzDirectorio);
		fichero.setGzFichero(nombre);
		fichero.setGzInstante(gztimestamp);
		fichero.setGzTamanio(size);
		fichero.setUncompressFichero(uncompressFilename);
		
		return fichero;
		
	}

}
