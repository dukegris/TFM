package es.rcs.tfm.srv.setup;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.simpleflatmapper.csv.CsvMapperFactory;
import org.simpleflatmapper.csv.CsvParser;
import org.simpleflatmapper.csv.CsvParser.MapWithDSL;

import es.rcs.tfm.srv.model.Fichero;

public class PmcProcessor {

	private String directory = null;
	private String filename = null;
	private MapWithDSL<Fichero> csvParser = null;

	public Stream<Fichero> stream() {
		Stream<Fichero> stream = null;
		if (csvParser != null) {
			try {
				Path path = Paths.get(FilenameUtils.concat(directory, filename));
	            Reader reader = Files.newBufferedReader(path);
	            stream = csvParser.stream(reader);
			} catch (InvalidPathException | IOException ex) {
				stream = null;
			}
		}
		return stream;
	}

	public PmcProcessor(String directory, String filename) {
		super();
		this.directory = directory;
		this.filename = filename;
		this.csvParser = CsvParser.
				mapWith(CsvMapperFactory.
			        newInstance().
			        addAlias("File", "filename").
			        addAlias("Article Citation", "title").
			        addAlias("Accession ID", "pmcId").
			        addAlias("Last Updated (YYYY-MM-DD HH:MM:SS)", "timestamp").
			        addAlias("PMID", "pmid").
			        addAlias("License", "license").
			        defaultDateFormat("yyyy-MM-dd HH:mm:ss").
			        newMapper(Fichero.class));
	}

}
