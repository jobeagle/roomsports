import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Bruno Schmidt (mail@roomsports.de)
 *
 */
public class GegnerTest {
    private String csvdatei;
    private Gegner gegner;

    @Before
    public void setUp() throws Exception {
    	gegner = new Gegner();
    	
    	Mlog.init();
		Mlog.setDebugstatus(true);
		
		csvdatei = System.getProperty("user.home") + "/roomsports/" + "test/DemotourTeamtrainingStandard20161014-145307.csv";
    }

    @Test
	public void testLoadCSVFile() {
		gegner.loadCSVFile(csvdatei);
		assertTrue(gegner.gegnerdaten.size() == 85);	// 85 gegnerische Koordinaten wurden eingelesen
	}
}
