package es.rcs.tfm.srv;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.setup.TmVarBiocProcessor;
import es.rcs.tfm.srv.setup.TmVarPubtatorProcessor;

@RunWith(
		SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		TmVarProcessorTest.class })
public class TmVarProcessorTest {

	public static final String[] XMLFILES = {
			"J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/tmVar/tmVarCorpus/train.BioC.xml",
			"J:/BioC_Java_1.0.1/xml/everything.xml",
			"J:/BioC_Java_1.0.1/xml/everything-sentence.xml",
			"J:/BioC_Java_1.0.1/xml/PMID-8557975-simplified-sentences.xml",
			"J:/BioC_Java_1.0.1/xml/PMID-8557975-simplified-sentences-tokens.xml",
			"J:/BioC_Java_1.0.1/xml/sentence.xml"};

	public static final String[] FILES = {
			"J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/tmVar/tmVarCorpus/train.PubTator.txt"};

	@Test
	public void checkMethod() {
		
		for (String file: FILES) {

			List<Articulo> tmvar = StreamSupport.
					stream(
							Spliterators.spliteratorUnknownSize(
									new TmVarPubtatorProcessor(Paths.get(file)), 
									Spliterator.DISTINCT), 
									false).
					collect(Collectors.toList());
			assertNotNull(tmvar);
			
			Set<Articulo> resultTMC = tmvar.
					stream().
					distinct().
					filter(tmvar::contains).
					collect(Collectors.toSet());
			assertNotNull(resultTMC);
			
			int size = (tmvar.size() >= 5) ? 5 : tmvar.size();
			for (int i=0; i<size; i++) {
				tmvar.get(i).getBlocks().forEach(b -> {
					b.getNotes().forEach((kn, n) -> {
						if (n.getPos().size()>1) {
							System.out.print("DEBUG");
						}
						n.getPos().forEach(p -> {
							try {
								String str = b.getText().substring(p.getOffset(), p.getEnd());
								if (	(StringUtils.isNotBlank(str)) &&
										(!str.equals(n.getText()))) {
									System.out.println("DEBUG-DIF: " + str + "\t" + n.getText());
								}
							} catch(Exception ex) {
								System.out.println("DEBUG-EX: " + b.getText() + "\t" + n.getText());
							}
						});
					});
				});
			}

		}
		
		for (String file: XMLFILES) {
			
			List<Articulo> bioc = StreamSupport.
					stream(
							Spliterators.spliteratorUnknownSize(
									new TmVarBiocProcessor(Paths.get(file)), 
									Spliterator.DISTINCT), 
									false).
					collect(Collectors.toList());
			assertNotNull(bioc);
			
			Set<Articulo> resultBIOC = bioc.
					stream().
					distinct().
					filter(bioc::contains).
					collect(Collectors.toSet());
			assertNotNull(resultBIOC);
			
			int size = (bioc.size() >= 5) ? 5 : bioc.size();
			for (int i=0; i<size; i++) {
				bioc.get(i).getBlocks().forEach(b -> {
					b.getNotes().forEach((kn, n) -> {
						if (n.getPos().size()>1) {
							System.out.print("DEBUG");
						}
						n.getPos().forEach(p -> {
							try {
								String str = b.getText().substring(p.getOffset(), p.getEnd());
								if (	(StringUtils.isNotBlank(str)) &&
										(!str.equals(n.getText()))) {
									System.out.println("DEBUG-DIF: " + str + "\t" + n.getText());
								}
							} catch(Exception ex) {
								System.out.println("DEBUG-EX: " + b.getText() + "\t" + n.getText());
							}
						});
					});
				});
			}

		}
		
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
