package es.rcs.tfm.srv.setup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import es.rcs.tfm.srv.model.Articulo;

public class MutationFinderProcessor extends ArticleProcessor {

	private BufferedReader input = null;
	private long totalSize = -1;
	private long readedSize = -1;
	private boolean allOk = false;
	private StringBuffer nextItem = new StringBuffer();
	private Map<String, Pattern> notes = new HashMap<String, Pattern>();
	
	public MutationFinderProcessor(
			Path pathTexts,
			Path pathResults) {

		if (	(pathTexts == null) ||
				!pathTexts.toFile().exists() ||
				!pathTexts.toFile().isFile()) throw new IllegalArgumentException(); 

		if (	(pathResults == null) ||
				!pathResults.toFile().exists() ||
				!pathResults.toFile().isFile()) throw new IllegalArgumentException(); 


		try {
			totalSize = pathTexts.toFile().length();
			readedSize = 0;
			this.input = new BufferedReader(new FileReader(pathTexts.toFile().getAbsolutePath()));
			processNotes(pathResults);
			
			allOk = totalSize>0;
		} catch (IOException e) {
		}
		
	}

	@Override
	public boolean hasNext() {

		boolean more = 
				(this.allOk) && 
				(this.input != null) && (
				(this.readedSize < this.totalSize) ||
				(this.nextItem.length()>0));
		
		/*
		if (!more) {
			System.out.println("MUTACIONES");
			MarkedText.MUTATIONS.forEach((e,v) -> System.out.println ("TYPE: " + e + ": " + v));
		}
		 */
		return more;
		
	}

	private static final Pattern LINE_NOTE_PTRN = Pattern.compile("(\\d+)(\t(.+))*");
	private void processNotes(Path pathResults) {
		
		
	}

	@Override
	public Articulo next() {
		// TODO Auto-generated method stub
		return null;
	}

}
