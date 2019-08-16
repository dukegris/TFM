package es.rcs.tfm.srv.setup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import es.rcs.tfm.srv.model.MarkedText;
import es.rcs.tfm.srv.model.MarkedText.Block;
import es.rcs.tfm.srv.model.MarkedText.Note;

public class PubtatorTxtProcessor extends MarkedTextProcessor {

	private static final Pattern LINE_PUBTATOR_PTRN = Pattern.compile("(\\d+)[\\|\\s].*");
	private static final Pattern TEXT_PUBTATOR_PTRN = Pattern.compile("^(\\d+)\\|([ta])\\|(.*)$");
	private static final Pattern DATA_PUBTATOR_PTRN = Pattern.compile("^(\\d+)\\t(\\d+)\\t(\\d+)\\t(.+)[\\s\\t]+(.+)\\s+(.+)$");

	private BufferedReader input = null;
	private long totalSize = -1;
	private long readedSize = -1;
	private boolean allOk = false;
	private StringBuffer nextItem = new StringBuffer();
	
	public PubtatorTxtProcessor(
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
	public MarkedText next() {
		
		if (!this.hasNext()) {
			this.allOk = false;
			throw new NoSuchElementException();
		}
		
		String pmc = "";
		boolean exit = false;
		MarkedText result = null;
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
	
	private MarkedText process(String str) {

		MarkedText result = new MarkedText();
		
		Block title = null;
		Block summary = null;
		int noteId = 0;
		for (String line: str.split("\r\n")) {

			Matcher m = TEXT_PUBTATOR_PTRN.matcher(line);
			if (m.find()) {
				String pmc = m.group(1);
				if (	(StringUtils.isBlank(result.pmid)) &&
						(StringUtils.isNotBlank(pmc))) {
					result.pmid = pmc;
				}

				Block block = null;

				String type = m.group(2);
				if (	(StringUtils.isNotBlank(type))) {
					boolean found = false;
					if (MarkedText.BLOCKS.containsKey(type)) {
						if (result.blocks.containsKey(MarkedText.BLOCKS.get(type))) {
							block = result.blocks.get(MarkedText.BLOCKS.get(type));
							found = true;
						} else {
							block = result.new Block();
							block.type = MarkedText.BLOCKS.get(type);
						};
					} else {
						// TIPO DE BLOQUE DESCONOCIDO. NO SABRIAMOS COMO CONTAR
						block = result.new Block();
						block.type = type;
					}
					
					String text = m.group(3);
					block.text = text;
					if (!found) result.blocks.put(block.type, block);
				}
				
				if ((block != null) && "title".equals(block.type)) title = block;
				if ((block != null) && "abstract".equals(block.type)) summary = block;
				
			}

			m = DATA_PUBTATOR_PTRN.matcher(line);
			if (m.find()) {
				
				noteId ++;
				
				String pmc = m.group(1);
				if (	(StringUtils.isBlank(result.pmid)) &&
						(StringUtils.isNotBlank(pmc))) {
					result.pmid = pmc;
				}

				Note note = result.new Note();
				String text = m.group(4);
				if (StringUtils.isNotBlank(text)) note.text = text;
				String value = m.group(6);
				if (StringUtils.isNotBlank(text)) note.value = value;
				String type = m.group(5);
				if (MarkedText.MUTATIONS.containsKey(type)) {
					note.type = MarkedText.MUTATIONS.get(type);
				} else {
					MarkedText.MUTATIONS.put(type, type);
				}

				if (	(title != null) &&
						(StringUtils.isNotBlank(title.text)) &&
						(summary != null)) {
					
					int titleSize = title.text.length();
					Long start = Long.parseLong(m.group(2));
					Long end = Long.parseLong(m.group(3));
					Long size = end - start; //OJO Marca el caracter siguiente aunque sea en blanco
					if (start < titleSize) {
						note.pos.add(result.new Position(start.intValue(), size.intValue()));
						title.notes.put(String.valueOf(noteId), note);
					} else {
						note.pos.add(result.new Position(start.intValue()-titleSize-1, size.intValue()));
						summary.notes.put(String.valueOf(noteId), note);
					}

				}
				
			}
			
		}

		return result;
		
	}

}
