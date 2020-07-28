package es.rcs.tfm.srv.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import es.rcs.tfm.db.model.PubFileEntity;
import es.rcs.tfm.srv.model.Articulo.IdType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Fichero {

	private String filename;
	private String type;

	private String gzDirectory;
	private String gzFilename;
	private LocalDateTime gzInstant;
	private long gzSize;

	private boolean md5 = false;
	private String md5Filename;

	private String uncompressFilename;

	private int numArticlesProcessed = 0;
	private int numArticlesTotal = 0;

	private boolean changesInDb = false;
	private boolean changesInDisk = false;
	
	private boolean processCompleted = false;

	private PubFileEntity entity = null;

	private List<Articulo> articles;

	public Fichero() {
		super();
	}

	private static Pattern PMC_PTR=Pattern.compile("PMC(\\d+)");
	/**
	 * Se encarga de instanciar un objeto fichero a partir de una linea
	 * de un fichero CSV. Necesario para la librer�a SFM-CSV
	 * @param fullFilename Ruta al fichero
	 * @param title T�tulo del art�culo
	 * @param pmcId Identificador en Pubmed Central
	 * @param timestamp Fecha del art�culo
	 * @param pubmed Identificador en Pubmed
	 * @param license Tipo de licencia
	 * @return El Fichero
	 */
	public static Fichero of(
			final String fullFilename,
			final String title,
			final String pmcId,
			final LocalDateTime timestamp,
			final String pubmed,
			final String license) {

		String pmc = pmcId;
		final Matcher m = PMC_PTR.matcher(pmcId);
		if (m.find()) {
			pmc = m.group(1);
		}

		final String directory = FilenameUtils.getFullPath(fullFilename);
		final String gzFilename = FilenameUtils.getName(fullFilename);
		final String uncompressFilename = FilenameUtils.removeExtension(gzFilename);
		final String filename = FilenameUtils.removeExtension(uncompressFilename);

		/*
		 * ZonedDateTime gztimestamp = ZonedDateTime.ofInstant( timestamp.toInstant(),
		 * ZoneId.of("UTC"));
		 */

		final Map<IdType, String> ids = new HashMap<>();
		ids.put(IdType.PUBMED, pubmed);
		ids.put(IdType.PMC, pmc);

		final Fichero fichero = new Fichero();

		final Articulo articulo = new Articulo();
		articulo.setPmcFile(fichero);
		articulo.setTitle(new Titulo(title));
		articulo.setPmid(pubmed);
		articulo.setIds(ids);

		fichero.setType(PubFileEntity.FTP_PMC);
		fichero.setFilename(filename);
		fichero.setGzDirectory(directory);
		fichero.setGzFilename(gzFilename);
		fichero.setGzInstant(timestamp);
		fichero.setMd5(false);
		fichero.setUncompressFilename(uncompressFilename);
		fichero.articles = new ArrayList<Articulo>();
		fichero.articles.add(articulo);

		return fichero;

	}

	/**
	 * Se encarga de instanciar un objeto fichero a partir de un fichero del FTP de
	 * PubMed
	 *
	 * @param fullFilename  Ruta al fichero
	 * @param timestamp Fecha del art�culo
	 * @param size      Tamanio del articulo
	 * @return El Fichero
	 */
	public static Fichero getInstance(
			final String fullFilename,
			final Calendar timestamp,
			final long size) {

		final String directory = FilenameUtils.getFullPath(fullFilename);
		final String gzFilename = FilenameUtils.getName(fullFilename);
		final String uncompressFilename = FilenameUtils.removeExtension(gzFilename);
		final String filename = FilenameUtils.removeExtension(uncompressFilename);
		final String md5Filename = gzFilename + ".md5";

		/*
		 * ZonedDateTime gztimestamp = ZonedDateTime.ofInstant( timestamp.toInstant(),
		 * ZoneId.of("UTC"));
		 */

		final Fichero fichero = new Fichero();
		fichero.setType(PubFileEntity.FTP_PUBMED);
		fichero.setFilename(filename);
		fichero.setGzDirectory(directory);
		fichero.setGzFilename(gzFilename);
		if (timestamp != null) {
			fichero.setGzInstant(LocalDateTime.ofInstant(
					timestamp.toInstant(), 
					ZoneId.of("UTC")));
		}
		fichero.setGzSize(size);
		fichero.setUncompressFilename(uncompressFilename);
		fichero.setMd5(true);
		fichero.setMd5Filename(md5Filename);

		return fichero;

	}

}
