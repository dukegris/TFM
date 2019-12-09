package es.rcs.tfm.srv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import es.rcs.tfm.srv.setup.PubmedXmlProcessor;

@RunWith(
		SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		PubmedXmlProcessorTest.class })
public class PubmedXmlProcessorTest {

	public static final String PUBMED_FILE = "../es.rcs.tfm.corpus/data/corpus/pubmed_xml/pubmed19n0973.xml";
	
	@Test
	public void unmarshallFile() {
		
		String[] ficheros = {
				PUBMED_FILE
		};

		Stream<String> ficherosStream = Arrays.asList(ficheros).stream();
		
		Stream<Articulo> articulosStream = ficherosStream.
			flatMap(f -> 
					StreamSupport.stream(
							Spliterators.spliteratorUnknownSize(
									new PubmedXmlProcessor(Paths.get(f)), 
									Spliterator.DISTINCT), 
									false));
								
		List<Articulo> a = articulosStream.collect(Collectors.toList());
		assertNotNull(a);
		assertEquals(29999, a.size());
		
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
