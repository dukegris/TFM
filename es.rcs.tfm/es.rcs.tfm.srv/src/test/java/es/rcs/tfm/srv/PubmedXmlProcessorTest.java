package es.rcs.tfm.srv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Calendar;
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
import es.rcs.tfm.srv.model.Fichero;
import es.rcs.tfm.srv.setup.PubmedXmlProcessor;

@RunWith(
		SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		PubmedXmlProcessorTest.class })
public class PubmedXmlProcessorTest {

	public static final String PUBMED_FILE = "pubmed19n0973.xml";
	
	@Test
	public void unmarshallFile() {
		
		Fichero[] ficheros = {
				Fichero.getInstance(PUBMED_FILE, Calendar.getInstance(), 0) 
		};
		
		System.out.println ("-" + "A novel missense mutation Asp506Gly in Exon 13 of the F11 gene in an asymptomatic Korean woman with mild factor XI deficiency.".substring(26,35) + "-");

		Stream<Fichero> ficherosStream = Arrays.asList(ficheros).stream();
		
		Stream<Articulo> articulosStream = ficherosStream.
			flatMap(f -> 
					StreamSupport.stream(
							Spliterators.spliteratorUnknownSize(
									new PubmedXmlProcessor(
											f,
											"/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/data/corpus/pubmed_xml"), 
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
