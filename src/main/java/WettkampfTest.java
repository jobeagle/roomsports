import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.ws.axis2.ConnectDB;
import org.apache.ws.axis2.ConnectDBResponse;
import org.apache.ws.axis2.MTBSRaceServiceStub;
import org.apache.ws.axis2.RennenListe;
import org.apache.ws.axis2.RennenListeResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bruno
 *
 */
public class WettkampfTest {
	private String db = Global.db;  
	private String[] rennliste = null;
	public  List<OnlineGegner> ergliste;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
    	Mlog.init();
		Mlog.setDebugstatus(true);	
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link Wettkampf#aktualisiereAusDB(int)}.
	 */
	@Test
	public void testAktualisiereAusDB() {
		MTBSRaceServiceStub stub;
		int i;
		String[] ergzeile = null;
		String	 rennen = null;

		try {
			stub = new MTBSRaceServiceStub();
			ConnectDB connect = new ConnectDB();
			connect.setDb(db);
			ConnectDBResponse res = stub.connectDB(connect);
			Mlog.debug("Rückgabe Connect: "+res.get_return());		
			RennenListe rl = new RennenListe();
			rl.setDb(db);
			//rl.setSNR("1234567890");
			RennenListeResponse rlr;
			rlr = stub.rennenListe(rl);
			rennliste = rlr.get_return();
			if (!rennliste[0].equals("0")) { 
				for (i=0; i<rennliste.length; i++) {
					ergzeile = rennliste[i].split(";"); 
					rennen = ergzeile[0] + " am " + ergzeile[1] + " Teiln.: " + ergzeile[2];  
					Mlog.debug("Rennen: "+rennen);
				}
			} else {
				assertTrue(true);
			}
			assertFalse(rennen.isEmpty());
		 } catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
