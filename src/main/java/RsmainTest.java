import static org.junit.Assert.*;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Bruno Schmidt (mail@roomsports.de)
 * 
 * Bei Fehler bei getLatestVersion: muss das Zertifikat ins Java übernommen werden:
 * keytool -import -noprompt -trustcacerts -alias <AliasName> -file   <certificate> -keystore <KeystoreFile> -storepass <Password>
 * 
 * bei mir:
 * keytool -import -noprompt -trustcacerts -alias <AliasName> -file   <certificate> -keystore /Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/security/cacerts -storepass changeit
 * 
 *
 */
public class RsmainTest {
	private Rsmain app = new Rsmain();
	
	@BeforeClass
	public static void setUp() throws Exception {
		Global.strPfad = System.getProperty("user.home") + "/roomsports/" + "test/";
		Global.gPXfile = Global.strPfad + "Frankenbikemarathon20130623.mov.gpx";
    	Mlog.init();
		Mlog.setDebugstatus(true);	
	}

	@AfterClass
	public static void tearDown() throws Exception {
	}


	@Test
	public void testcheckWertMaxMin() {		
		assertTrue(app.checkWertMaxMin(1.5, 1.0, 0.0) == 1.0);	// Testwert über Maximum
		assertTrue(app.checkWertMaxMin(1.5, 1.6, 0.0) == 1.5);	// Testwert unter Maximum
		assertTrue(app.checkWertMaxMin(1.5, 1.6, 1.55) == 1.55);// Testwert unter Minimum
		assertTrue(app.checkWertMaxMin(1.5, 1.6, 1.4) == 1.5);	// Testwert über Minimum
	}
	
	@Test
	public void testgetLatestVersion() {
		String version = "";
		try {
			version = Rsmain.getLatestVersion();
			Mlog.debug("Version: "+version);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		int len = version.length();
		assertTrue(len > 0 && len < 10);		// Rückgabe z.B. 4.25: Länge >0 und < 10
	}
	
	@Test
	public void testgettrackpoint() {
	TrkPt trkpt = null;	
	long millisecsStart = 0;
	VerwaltungGPX ldpx = new VerwaltungGPX();
	ldpx.loadGPS(Global.gPXfile, false);
	millisecsStart = new Date().getTime();
	trkpt = app.gettrackpoint(600);				// Trackpunkt nach 10 Min. ermitteln
	Mlog.debug("Trackpunkt ("+trkpt.getIndex()+") - Dauer: "+(new Date().getTime()-millisecsStart)+" ms");
	millisecsStart = new Date().getTime();
	trkpt = app.gettrackpoint(145*60);			// kurz vor Ende der Tour nach 145 Minuten: Trackpunkt ermitteln
	Mlog.debug("Trackpunkt ("+trkpt.getIndex()+") - Dauer: "+(new Date().getTime()-millisecsStart)+" ms");
	assertTrue(trkpt.getIndex() >= 1723);
	}
}
