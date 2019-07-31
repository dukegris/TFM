package es.rcs.tfm.srv.setup;

/**
 * Writes the CoNLL 2003 format.
 *
 * @see <a href="http://www.cnts.ua.ac.be/conll2003/ner/">CoNLL 2003 shared task</a>
 */
public class Conll2003Writer {

	// BILUO Scheme
	public static final String IOB_BEGIN =			"B-";			// The first token of a multi-token entity.
	public static final String IOB_IN =				"I-";			// An inner token of a multi-token entity.
	public static final String IOB_LAST =			"L-";			// The final token of a multi-token entity.
	public static final String IOB_UNIT =			"U-";			// A single-token entity.
	public static final String IOB_OUT =			"O";			// A non-entity token.
	
	public static final String POS_CC =				"CC";			// coordinating conjunction
	public static final String POS_CD =				"CD";			// cardinal digit
	public static final String POS_DT =				"DT";			// determiner
	public static final String POS_EX =				"EX";			// existential there ("like": there is ... think of it like "there exists")
	public static final String POS_FW =				"FW";			// foreign word
	public static final String POS_IN =				"IN";			// preposition/subordinating conjunction
	public static final String POS_JJ =				"JJ";			// adjective big
	public static final String POS_JJR =			"JJR";			// adjective, comparative bigger
	public static final String POS_JJS =			"JJS";			// adjective, superlative biggest
	public static final String POS_LS =				"LS";			// list marker 1)
	public static final String POS_MD =				"MD";			// modal could, will
	public static final String POS_NN =				"NN";			// noun, singular desk
	public static final String POS_NNS =			"NNS";			// noun plural desks
	public static final String POS_NNP =			"NNP";			// proper noun, singular Harrison
	public static final String POS_NNPS =			"NNPS";			// proper noun, plural Americans
	public static final String POS_PDT =			"PDT";			// predeterminer all the kids
	public static final String POS_POS =			"POS";			// possessive ending parents
	public static final String POS_PRP =			"PRP";			// personal pronoun I, he, she
	public static final String POS_PRP_ =			"PRP$";			// possessive pronoun my, his, hers
	public static final String POS_RB =				"RB";			// adverb very, silently,
	public static final String POS_RBR =			"RBR";			// adverb, comparative better
	public static final String POS_RBS =			"RBS";			// adverb, superlative best
	public static final String POS_RP =				"RP";			// particle give up
	public static final String POS_TO =				"TO";			// to go to the store.
	public static final String POS_UH =				"UH";			// interjection
	public static final String POS_VB =				"VB";			// verb, base form take
	public static final String POS_VBD =			"VBD";			// verb, past tense took
	public static final String POS_VBG =			"VBG";			// verb, gerund/present participle taking
	public static final String POS_VBN =			"VBN";			// verb, past participle taken
	public static final String POS_VBP =			"VBP";			// verb, sing. present, non-3d take
	public static final String POS_VBZ =			"VBZ";			// verb, 3rd person sing. present takes
	public static final String POS_WDT =			"WDT";			// wh-determiner which
	public static final String POS_WP =				"WP";			// wh-pronoun who, what
	public static final String POS_WP_ =			"WP$";			// possessive wh-pronoun whose
	public static final String POS_WRB =			"WRB";			// wh-abverb where, when

	// OntoNotes 5 
	public static final String NER_PERSON =			"PERSON";		// People, including fictional.
	public static final String NER_NORP =			"NORP";			// Nationalities or religious or political groups.
	public static final String NER_FAC =			"FAC";			// Buildings, airports, highways, bridges, etc.
	public static final String NER_ORG_ =			"ORG";			// Companies, agencies, institutions, etc.
	public static final String NER_GPE =			"GPE";			// Countries, cities, states.
	public static final String NER_LOC_ =			"LOC";			// Non-GPE locations, mountain ranges, bodies of water.
	public static final String NER_PRODUCT =		"PRODUCT";		// Objects, vehicles, foods, etc. (Not services.)
	public static final String NER_EVENT =			"EVENT";		// Named hurricanes, battles, wars, sports events, etc.
	public static final String NER_WORK_OF_ART =	"WORK_OF_ART";	// Titles of books, songs, etc.
	public static final String NER_LAW =			"LAW";			// Named documents made into laws.
	public static final String NER_LANGUAGE =		"LANGUAGE";		// Any named language.
	public static final String NER_DATE =			"DATE";			// Absolute or relative dates or periods.
	public static final String NER_TIME =			"TIME";			// Times smaller than a day.
	public static final String NER_PERCENT =		"PERCENT";		// Percentage, including %.
	public static final String NER_MONEY =			"MONEY";		// Monetary values, including unit.
	public static final String NER_QUANTITY =		"QUANTITY";		// Measurements, as of weight or distance.
	public static final String NER_ORDINAL =		"ORDINAL";		// first, second, etc.
	public static final String NER_CARDINAL =		"CARDINAL";		// Numerals that do not fall under another type.

	// Wikipedia corpus (Nothman et al., 2013)
	public static final String NER_PER =			"PER";			// Named person or family.
	public static final String NER_LOC =			"LOC";			// Name of politically or geographically defined location (cities, provinces, countries, international regions, bodies of water, mountains).
	public static final String NER_ORG =			"ORG";			// Named corporate, governmental, or other organizational entity.
	public static final String NER_MISC =			"MISC";			// Miscellaneous entities, e.g. events, nationalities, products or works of art.

	public static final String UNUSED =				"_";
	public static final String IOB_EXT =			".iob";
	public static final String CONLL_EXT =			".conll";
	
	public class Conll2003Entry {
		
		public String token = UNUSED;
		public String pos = UNUSED;
		public String chunk = UNUSED;
		public String ner = UNUSED;
		
		public Conll2003Entry(String token, String pos, String chunk, String ner) {
			super();
			this.token = token;
			this.pos = pos;
			this.chunk = chunk;
			this.ner = ner;
		}
		
	}

	/*

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix),
                    targetEncoding));
            convert(aJCas, out);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            closeQuietly(out);
        }
    }

    private void convert(JCas aJCas, PrintWriter aOut)
    {
        Type chunkType = JCasUtil.getType(aJCas, Chunk.class);
        Feature chunkValue = chunkType.getFeatureByBaseName("chunkValue");

        Type neType = JCasUtil.getType(aJCas, NamedEntity.class);
        Feature neValue = neType.getFeatureByBaseName("value");
        
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            HashMap<Token, Row> ctokens = new LinkedHashMap<Token, Row>();

            // Tokens
            List<Token> tokens = selectCovered(Token.class, sentence);

            // Chunks
            IobEncoder chunkEncoder = new IobEncoder(aJCas.getCas(), chunkType, chunkValue, true);

            // Named Entities
            IobEncoder neEncoder = new IobEncoder(aJCas.getCas(), neType, neValue, true);
            
            for (Token token : tokens) {
                Row row = new Row();
                row.token = token;
                row.chunk = chunkEncoder.encode(token);
                row.ne = neEncoder.encode(token);
                ctokens.put(row.token, row);
            }

            // Write sentence in CONLL 2006 format
            for (Row row : ctokens.values()) {
                String form = row.token.getCoveredText();
                if (!writeCovered) {
                    form = row.token.getText();
                }
                
                String pos = UNUSED;
                if (writePos && (row.token.getPos() != null)) {
                    POS posAnno = row.token.getPos();
                    pos = posAnno.getPosValue();
                }

                String chunk = UNUSED;
                if (writeChunk && (row.chunk != null)) {
                    chunk = row.chunk;
                }

                String namedEntity = UNUSED;
                if (writeNamedEntity && (row.ne != null)) {
                    namedEntity = row.ne;
                }

                aOut.printf("%s %s %s %s\n", form, pos, chunk, namedEntity);
            }

            aOut.println();
        }
    }

	 */
	
}
