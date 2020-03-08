import static org.junit.Assert.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bruno Schmidt (mail@mtbsimulator.de)
 *
 */
public class AuswertungTest {
	private Auswertung ausw;
	@SuppressWarnings("unused")
	private Rsmain app;
    private String tcxdatei;
    private String csvdatei;
//    private int iret = 0;
    @SuppressWarnings("unused")
	private long lret = 0;
//    private String stravaToken = "0937dfeb4257a0367b77010bed413e5a29ef59f7";
   
    @Before
    public void setUp() throws Exception {
		app = new Rsmain();
		ausw = new Auswertung();
		
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));  
        ausw.tfmt = new SimpleDateFormat();
        ausw.tfmt.applyPattern("HH:mm:ss");  
        ausw.tfmttcx = new SimpleDateFormat();
        ausw.tfmttcx.applyPattern("yyyy-MM-dd");    

		Mlog.init();
		Mlog.setDebugstatus(true);
		
		tcxdatei = System.getProperty("user.home") + "/mtbsimulator/" + "test/DemotourTeamtrainingStandard20161014-145307.tcx";
		csvdatei = System.getProperty("user.home") + "/mtbsimulator/" + "test/DemotourTeamtrainingStandard20161014-145307.csv";
    }

    @After
    public void tearDown() throws Exception {
 //   	JStravaV3 strava= new JStravaV3(stravaToken);
    	
//		if (iret > 0)
//			strava.deleteActivity(iret);		
//    	Mlog.logger.removeAllAppenders();
    }

    /**
	 * Test method for {@link Auswertung#writeTCXFile(java.lang.String, boolean)}.
	 */
	@Test 
	public void testWriteTCXFile() {

        try {
        	ausw.readCSV(csvdatei);			
		} catch (NullPointerException e) {
			Mlog.debug("NPE wegen Zuweisung von SWT-Komponenten, die hier nicht gebraucht werden!");
		}
    	assertTrue(ausw.pliste.size() > 50);		// ca. 88 Zeilen in CSV!
    	
    	ausw.writeTCXFile(tcxdatei, true);
    	// prüfe, ob Datei erzeugt wurde:
		File testdatei = new File(tcxdatei);    		
		assertTrue(testdatei.exists());
//		testdatei.delete();
//		assertFalse(testdatei.exists());		
	}
/*
	@Test
	public void testsendeTraining2Strava() {

        try {
        	ausw.readCSV(csvdatei);			
		} catch (NullPointerException e) {
			Mlog.debug("NPE wegen Zuweisung von SWT-Komponenten, die hier nicht gebraucht werden!");
		}
    	assertTrue(ausw.pliste.size() == 85);		// 85 Zeilen in CSV!
    	// tcx ist nicht vorhanden...
		lret = ausw.sendeTraining2Strava(csvdatei, "");	// 1. Test ohne Key - nur Zugriffstoken bzw. Anmeldung und danach Token
		lret = ausw.sendeTraining2Strava(csvdatei, stravaToken);	// 2. Test mit Key - Upload...	
	}
	
	@Test
	public void testsetStravaTitel() {
		assertTrue(ausw.setStravaTitel("RothenbergBikeMarathon20091120.m4v").equals("MTBS - RothenbergBikeMarathon"));
		assertTrue(ausw.setStravaTitel("das ist eine lange datei ohne jahreszahl.mov").equals("MTBS - das ist eine lange datei ..."));
		assertTrue(ausw.setStravaTitel("das ist kürzer.mov").equals("MTBS - das ist kürzer"));		
	}
*/
}
