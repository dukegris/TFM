package es.rcs.tfm.srv.model;

import java.util.ArrayList;
import java.util.HashMap;

public class MarkedText {
	
	public static final HashMap<String, String> BLOCKS;
	static {
		BLOCKS = new HashMap<String, String>();
		BLOCKS.put("t", "title");
		BLOCKS.put("a", "abstract");
		BLOCKS.put("title", "title");
		BLOCKS.put("abstract", "abstract");
	}

	public static final HashMap<String, String> MUTATIONS;
	static {
		MUTATIONS = new HashMap<String, String>();
		MUTATIONS.put("DNAMutation", "MUT_DNA");
		MUTATIONS.put("ProteinMutation", "MUT_PRO");
		MUTATIONS.put("SNP", "MUT_SNP");
	}
	
	public class Position {
		public int offset = 0;
		public int length = 0;
		public Position(int offset, int length) {
			super();
			this.offset = offset;
			this.length = length;
		}
		@Override
		public String toString() {
			return "Position [offset=" + offset + ", length=" + length + "]";
		}
	}
	public class Note {
		public String id = "";
		public String type = "";
		public String value = "";
		public String text = "";
		public ArrayList<Position> pos = new ArrayList<Position>();
		@Override
		public String toString() {
			return "Note [" + (id != null ? "id=" + id + ", " : "") 
					+ (type != null ? "type=" + type + ", " : "")
					+ (value != null ? "value=" + value + ", " : "") 
					+ (text != null ? "text=" + text + ", " : "")
					+ (pos != null ? "pos=" + pos : "") 
					+ "]";
		}
	}
	public class Block {
		public String offset = "";
		public String type = "";
		public String text = "";
		public HashMap<String, Note> notes = new HashMap<String, Note>();
		@Override
		public String toString() {
			return "Block [" 
					+ (offset != null ? "offset=" + offset + ", " : "")
					+ (type != null ? "type=" + type + ", " : "") 
					+ (text != null ? "text=" + text + ", " : "")
					+ (notes != null ? "notes=" + notes : "") 
					+ "]";
		}
		
	}
	public String pmid = "";
	public HashMap<String, Block> blocks = new HashMap<String, Block>();
	@Override
	public String toString() {
		return "MarkedText [" 
				+ (pmid != null ? "pmid=" + pmid + ", " : "") 
				+ (blocks != null ? "blocks=" + blocks : "")
				+ "]";
	}
	
}
