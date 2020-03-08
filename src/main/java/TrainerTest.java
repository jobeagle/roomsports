import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bruno Schmidt (mail@roomsports.de)
 * TODO aktuell sind nur ein paar Tests zu KETTLER enthalten!
 */
public class TrainerTest {

	@Before
	public void setUp() throws Exception {
    	Mlog.init();
		Mlog.setDebugstatus(true);	
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReadKettlerSingleValue() {
		String antwort = "094\t023\t000\t003\t155\t0045\t01:01\t155\t";
		String rueckgabe = "";
		
		// Test auf Puls
		rueckgabe = Trainer.readKettlerSingleValue(antwort, 1);
		assertEquals("094", rueckgabe);
		
		// Test auf RPM
		rueckgabe = Trainer.readKettlerSingleValue(antwort, 2);
		assertEquals("023", rueckgabe);
		
		// Test auf aktuelle Leistung
		rueckgabe = Trainer.readKettlerSingleValue(antwort, 8);
		assertEquals("155", rueckgabe);

		// Tests mit falschem Index
		rueckgabe = Trainer.readKettlerSingleValue(antwort, 0);
		assertEquals("0", rueckgabe);
		rueckgabe = Trainer.readKettlerSingleValue(antwort, 9);
		assertEquals("0", rueckgabe);

		// Test mit beliebigen String
		antwort = "ich bin ein teststring\n";
		rueckgabe = Trainer.readKettlerSingleValue(antwort, 8);
		assertEquals("0", rueckgabe);
		
		// Test mit abgebrochenem String
		antwort = "094\t023\t000\t003\t155\t";
		rueckgabe = Trainer.readKettlerSingleValue(antwort, 8);
		assertEquals("0", rueckgabe);
		// die RPM sollten trotzdem klappen:
		rueckgabe = Trainer.readKettlerSingleValue(antwort, 2);
		assertEquals("023", rueckgabe);
				
		// Test mit Leerstring
		antwort = "";
		rueckgabe = Trainer.readKettlerSingleValue(antwort, 8);
		assertEquals("0", rueckgabe);
	}
}
