package es.rcs.tfm.srv.setup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import es.rcs.tfm.srv.model.Anotacion;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.BloqueAnotado;
import es.rcs.tfm.srv.model.Posicion;

public class TmVarTxtProcessor extends ArticleProcessor {

	private static final Pattern LINE_PUBTATOR_PTRN = Pattern.compile("(\\d+)[\\|\\s].*");
	private static final Pattern TEXT_PUBTATOR_PTRN = Pattern.compile("^(\\d+)\\|([ta])\\|(.*)$");
	private static final Pattern DATA_PUBTATOR_PTRN = Pattern.compile("^(\\d+)\\t(\\d+)\\t(\\d+)\\t(.+)[\\s\\t]+(.+)\\s+(.+)$");

	private BufferedReader input = null;
	private long totalSize = -1;
	private long readedSize = -1;
	private boolean allOk = false;
	private StringBuffer nextItem = new StringBuffer();
	
	public TmVarTxtProcessor(
			Path path) {

		if (	(path == null) ||
				!path.toFile().exists() ||
				!path.toFile().isFile()) throw new IllegalArgumentException(); 

		try {
			totalSize = path.toFile().length();
			readedSize = 0;
			this.input = new BufferedReader(new FileReader(path.toFile().getAbsolutePath()));
			
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

	@Override
	public Articulo next() {
		
		if (!this.hasNext()) {
			this.allOk = false;
			throw new NoSuchElementException();
		}
		
		String pmc = "";
		boolean exit = false;
		Articulo result = null;
		while (!exit) {

			String text = "";
			try {
				text = this.input.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				exit = true;
				text = "";
				this.allOk = false;
			}
			
			if (!exit && this.allOk) {
				if (StringUtils.isNotBlank(text)) {
				
					readedSize += text.length() + 2;
					Matcher m = LINE_PUBTATOR_PTRN.matcher(text);
					if (m.find()) {
						String str = m.group(1);
						if (StringUtils.isNotBlank(str)) {
							if (StringUtils.isBlank(pmc)) {
								pmc = str;
							}
							if (str.equals(pmc)) {
								this.nextItem.append(text);
								this.nextItem.append("\r\n");
							} else {
								exit = true;						
							} 
						}
					}
					
				} else { 
					readedSize += 2;
					exit = true;
				}
			}
			
			if (exit) {
				if (this.nextItem.length()>0) {
					result = process(this.nextItem.toString());
				}
				this.nextItem = new StringBuffer();
				if (StringUtils.isNotBlank(text)) {
					this.nextItem.append(text);	
					this.nextItem.append("\r\n");
				}
			}
					
		}

		if(!this.hasNext()) {
			try {
				this.input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
		
	}
	
	private Articulo process(String str) {

		Articulo result = new Articulo();
		
		BloqueAnotado title = null;
		BloqueAnotado summary = null;
		int noteId = 0;
		for (String line: str.split("\r\n")) {

			Matcher m = TEXT_PUBTATOR_PTRN.matcher(line);
			if (m.find()) {
				
				// ID ARTICLE
				String pmid = m.group(1);
				if (	(StringUtils.isBlank(result.getPmid())) &&
						(StringUtils.isNotBlank(pmid))) {
					result.setPmid(pmid);
				}

				BloqueAnotado block = null;

				// TITLE/ABSTRACT
				String type = m.group(2);
				if (	(StringUtils.isNotBlank(type))) {
					boolean found = false;
					if (ArticleProcessor.BLOCKS_NORMALIZE.containsKey(type)) {
						if (result.containsBlockOfType(ArticleProcessor.BLOCKS_NORMALIZE.get(type))) {
							block = result.getBlocksOfType(ArticleProcessor.BLOCKS_NORMALIZE.get(type));
							found = true;
						} else {
							block = new BloqueAnotado();
							block.setType(ArticleProcessor.BLOCKS_NORMALIZE.get(type));
						};
					} else {
						// TIPO DE BLOQUE DESCONOCIDO. NO SABRIAMOS COMO CONTAR
						block = new BloqueAnotado();
						block.setType(type);
					}
					
					String text = m.group(3);
					block.setText(text);
					if (!found) result.addBlock(block);
				}
				
				if ((block != null) && BloqueAnotado.PASSAGE_TYPE_TITLE.equals(block.getType())) title = block;
				if ((block != null) && BloqueAnotado.PASSAGE_TYPE_ABSTRACT.equals(block.getType())) summary = block;
				
			}

			m = DATA_PUBTATOR_PTRN.matcher(line);
			if (m.find()) {
				
				// ID ARTICLE
				String pmid = m.group(1);
				if (	(StringUtils.isBlank(result.getPmid())) &&
						(StringUtils.isNotBlank(pmid))) {
					result.setPmid(pmid);
				}

				Anotacion note = new Anotacion();
				note.setId(Integer.toString(noteId));

				// TOKEN
				String text = m.group(4);
				if (StringUtils.isNotBlank(text)) note.setText(text);
				
				// TYPE
				String type = m.group(5);
				if (ArticleProcessor.MUTATIONS_NORMALIZE.containsKey(type)) {
					note.setType(ArticleProcessor.MUTATIONS_NORMALIZE.get(type));
				} else {
					ArticleProcessor.MUTATIONS_NORMALIZE.put(type, type);
				}
				
				// STD IDENTIFIER
				String value = m.group(6);
				if (StringUtils.isNotBlank(text)) note.setValue(value);
				
				if (	(title != null) &&
						(StringUtils.isNotBlank(title.getText())) &&
						(summary != null)) {
					
					// POSITION
					Long start = Long.parseLong(m.group(2));
					Long end = Long.parseLong(m.group(3));
					
					int titleSize = (title.getText() != null) ? title.getText().length() : 0;
					Long size = end - start; //OJO Marca el caracter siguiente aunque sea en blanco
					if (start < titleSize) {
						note.addPosition(new Posicion(start.intValue(), size.intValue()));
						title.setOffset(0);
						title.addNote(String.valueOf(noteId), note);
					} else {
						// offset: Title has an offset of zero, while the other passages 
						// (e.g., abstract) are assumed to begin after the previous passages and one space
						
						// En tmVar solo hay un pasaje titulo y un pasaje abstract
						summary.setOffset((title.getText() != null) ? title.getText().length() : 0);
						
						// El offset del abstract incluye los parrafos anteriores + 1 caracter por parrafo
						// en tmVar solo hay un titulo, por lo que hay que restar al offset el tamaño del título y uno
						/*
						String fortest = title.text + " " + summary.text;
						System.out.println(
								"Compare: " + 
								fortest.substring(start.intValue(), start.intValue() + size.intValue()) +
								" con " +
								note.getText() +
								" y con " +
								summary.text.substring(start.intValue()-title.text.length()-1, start.intValue()-title.text.length()-1 + size.intValue()));
						 */
						note.addPosition(new Posicion(start.intValue()-titleSize-1, size.intValue()));
						summary.addNote(String.valueOf(noteId), note);
					}

				}
				
				noteId ++;
				
			}
			
		}

		return result;
		
	}

}
