import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Bruno Schmidt (mail@roomsports.de)
 *
 */
public class KonfigurationTest {
	private Konfiguration konfig;
	private Fahrer biker;
	private Trainer ergo;

    @Before
    public void setUp() throws Exception {
    	konfig = new Konfiguration();
    	
    	Mlog.init();
		Mlog.setDebugstatus(true);	
		
		Global.strPfad = System.getProperty("user.home") + "/roomsports/" + "test/";
		biker = new Fahrer();
		ergo = new Trainer();
	}

	@Test
	public void testEnabledisableControls() {
		// NPE wird in Prozedur abgefangen ?
		konfig.enabledisableControls();
	}

	@Test
	public void testSaveProfil() {
		
		// NPE wird abgefangen!
		konfig.saveProfil(biker, ergo);
		// Test ob Datei vorhanden ist:
		File testdatei = new File(Global.strPfad+"Standard.xml");    		
		assertTrue(testdatei.exists());
	}

	@Test
	public void testLoadProfil() {
		// NPE wird abgefangen!
		konfig.loadProfil("Standardtest.xml", biker, ergo);
	}

	@Test
	public void testCreateXMLFileSettings() {
		// NPE wird abgefangen!
		konfig.createXMLFileSettings("test.xml", biker);
	}

	@Test
	public void testCreateXMLFileBiker() {
		String dateiname = Global.strPfad+"testbiker.xml";
		konfig.createXMLFileBiker(dateiname, biker, ergo);
		// Test ob Datei vorhanden ist:
		File testdatei = new File(dateiname);    		
		assertTrue(testdatei.exists());
	}
}
