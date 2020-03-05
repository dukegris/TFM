package es.rcs.tfm.srv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.setup.TmBiocXmlProcessor;
import es.rcs.tfm.srv.setup.TmVarTxtProcessor;

@RunWith(
		SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		TmVarProcessorTest.class })
public class TmVarProcessorTest {

	public static final String BIOC_FILE = "../es.rcs.tfm.corpus/datasets/tmVar/tmVarCorpus/train.BioC.xml";
	public static final String TMVAR_FILE = "../es.rcs.tfm.corpus/datasets/tmVar/tmVarCorpus/train.PubTator.txt";

//	public static final String BIOC_FILE = "../es.rcs.tfm.corpus/datasets/tmVar/tmVarCorpus/test.BioC.xml";
//	public static final String TMVAR_FILE = "../es.rcs.tfm.corpus/datasets/tmVar/tmVarCorpus/test.PubTator.txt";
	
	@Test
	public void checkMethod() {
		
		
		List<Articulo> tmvar = StreamSupport.
				stream(
						Spliterators.spliteratorUnknownSize(
								new TmVarTxtProcessor(Paths.get(TMVAR_FILE)), 
								Spliterator.DISTINCT), 
								false).
				collect(Collectors.toList());

		List<Articulo> bioc = StreamSupport.
				stream(
						Spliterators.spliteratorUnknownSize(
								new TmBiocXmlProcessor(Paths.get(BIOC_FILE)), 
								Spliterator.DISTINCT), 
								false).
				collect(Collectors.toList());
		
		Set<Articulo> resultBIOC = bioc.
				stream().
				distinct().
				filter(tmvar::contains).
				collect(Collectors.toSet());
		
		Set<Articulo> resultTMC = tmvar.
				stream().
				distinct().
				filter(bioc::contains).
				collect(Collectors.toSet());

		for (int i=0; i<5; i++) {
			tmvar.get(i).getBlocks().forEach(b -> {
				b.getNotes().forEach((kn, n) -> {
					if (n.getPos().size()>1) {
						System.out.print("DEBUG");
					}
					n.getPos().forEach(p -> {
						assertEquals(n.getText(), b.getText().substring(p.getOffset(), p.getEnd()));
					});
				});
			});
		}

		for (int i=0; i<5; i++) {
			bioc.get(i).getBlocks().forEach(b -> {
				b.getNotes().forEach((kn, n) -> {
					if (n.getPos().size()>1) {
						System.out.print("DEBUG");
					}
					n.getPos().forEach(p -> {
						assertEquals(n.getText(), b.getText().substring(p.getOffset(), p.getEnd()));
					});
				});
			});
		}
		assertNotNull(tmvar);
		assertNotNull(bioc);

		assertNotNull(resultBIOC);
		assertNotNull(resultTMC);

		//assertEquals(resultBIOC.size(), 0);

		// Para entrenamiento
		//assertEquals(tmvar.size()-1, bioc.size());
		//assertEquals(resultTMC.size(), 1);

		// Para test
		//assertEquals(tmvar.size(), bioc.size());
		//assertEquals(resultTMC.size(), 0);
		
	}

	@BeforeClass
	public static void startUp() throws InterruptedException {
	}
	
	@Before
	public void setUp() throws Exception {
	}
		
    @After
    public void tearDown() {
    }
	
	@AfterClass
	public static void shutDown() {
	}

}
