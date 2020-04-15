import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class VerwaltungGPXTest {
	@SuppressWarnings("unused")
	private Rsmain app;
    private String gpxdatei;
    private VerwaltungGPX loadGPX;
    private SimpleDateFormat sdf;
    private int anzPunkte1;
    
    @BeforeClass
    public static void initAllTests() {
		Mlog.init();
		Mlog.setDebugstatus(true);
		Mlog.debug("starte Tests");
    }
    
	@Before
	public void setUp() throws Exception {
		app = new Rsmain();
		loadGPX = new VerwaltungGPX();
		gpxdatei = System.getProperty("user.home") + "/roomsports/" + "test/Frankenbikemarathon20130623.mov.gpx";
		sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testloadGPS() {
		long zeitPunkt1;
		Mlog.debug("************ starte testloadGPS");
		loadGPX.loadGPS(gpxdatei, true);
		anzPunkte1 = VerwaltungGPX.track.size();
		Mlog.debug("Anz. der GPS-Punkte: " + anzPunkte1);
		assertTrue(anzPunkte1 == 1790);
		// neu: Zeitpunkt muss nun auch befüllt werden (1. Punkt hat: 2013-06-23T08:30:22Z)
		zeitPunkt1 = VerwaltungGPX.track.get(0).getZeitpunkt();
		Date dateZeitpunkt1 = new Date(zeitPunkt1);
		Mlog.debug("Zeitpunkt Punkt 1 " + dateZeitpunkt1 + " - " + sdf.format(dateZeitpunkt1));
		assertTrue(sdf.format(dateZeitpunkt1).equalsIgnoreCase("23.06.2013 08:30:22"));
	}

	@Test
	public void testreCalcTrack() {
		long aktindex  = 0;
		long anzsekAkt = 0;  		// Index 17: 111
		long aktsekAkt = 0;  		//   9
		double aktAbstVorg = 0.0;	// 59.7 m
		double aktAbstSumme = 0.0;	// 880.6 m
		double aktSteigung = 0.0;
		long anzsekAktAlt = 0;  
		long aktsekAktAlt = 0;  
		double aktAbstVorgAlt = 0.0;
		double aktAbstSummeAlt = 0.0;
		double aktHm = 0.0;
		double aktHmAlt = 0.0;
		double abstLetztPunkt = 0.0;

		Mlog.debug("************ starte testreCalcTrack");
		loadGPX.loadGPS(gpxdatei, true);
		anzPunkte1 = VerwaltungGPX.track.size();
		aktHmAlt = VerwaltungGPX.track.get(1789).getHm_m();		// Hm am letzten Punkt?
		// Punkt Index = 17 sollte 9 Sek als aktsek und 110 Sek als anzsec haben:
		aktsekAkt = VerwaltungGPX.track.get(17).getAktsek();
		anzsekAkt = VerwaltungGPX.track.get(17).getAnzsek();
		aktAbstVorg = VerwaltungGPX.track.get(17).getAbstvorg_m();
		aktAbstSumme = VerwaltungGPX.track.get(17).getAbstand_m();
		aktSteigung = VerwaltungGPX.track.get(17).getSteigung_proz();
		aktHm = VerwaltungGPX.track.get(17).getHm_m();
		aktsekAktAlt = aktsekAkt;
		anzsekAktAlt = anzsekAkt;
		aktAbstVorgAlt = aktAbstVorg;
		aktAbstSummeAlt = aktAbstSumme;
		Mlog.debug("<aktsekAkt>"+aktsekAkt+ " <anzsekAkt>"+anzsekAkt+" <aktabstVorg>"+aktAbstVorg+
				" <aktAbstSumme>"+aktAbstSumme+ " <aktsteigung>"+aktSteigung+ " <aktHm>"+aktHm);
		assertTrue(aktsekAkt == 9);
		assertTrue(anzsekAkt == 111);
		// nun zwei Punkte löschen
		VerwaltungGPX.track.remove(17);
		VerwaltungGPX.track.remove(27);
		VerwaltungGPX.reCalcTrackNachDel();		
		assertTrue(VerwaltungGPX.track.size() == 1788);		// 1790 - 2
		// Test auf fortlaufenden Index:
		for (TrkPt aktTrkPt : VerwaltungGPX.track) {
			aktindex++;
			if (aktindex != aktTrkPt.getIndex()) {
			  Mlog.error("<aktindex>"+aktindex+ " <getIndex>"+aktTrkPt.getIndex());
			  assertTrue(false);
			}
		}
		// passen aktsek und anzsek, abstvorg_m, abstand
		aktsekAkt = VerwaltungGPX.track.get(17).getAktsek();
		anzsekAkt = VerwaltungGPX.track.get(17).getAnzsek();	
		aktAbstVorg = VerwaltungGPX.track.get(17).getAbstvorg_m();
		aktAbstSumme = VerwaltungGPX.track.get(17).getAbstand_m();
		aktSteigung = VerwaltungGPX.track.get(17).getSteigung_proz();
		Mlog.debug("nach loeschen Punkt 17 <aktsekAkt>"+aktsekAkt+ " <anzsekAkt>"+anzsekAkt+" <aktabstVorg>"+aktAbstVorg+
				" <aktAbstSumme>"+aktAbstSumme+ " <aktsteigung>"+aktSteigung+ " <aktHm>"+aktHm);
		assertTrue(VerwaltungGPX.track.get(17).getAnzsek() > anzsekAktAlt);
		assertTrue(VerwaltungGPX.track.get(17).getAktsek() > aktsekAktAlt);
		assertTrue(VerwaltungGPX.track.get(17).getAbstvorg_m() > aktAbstVorgAlt+1.0);
		assertTrue(VerwaltungGPX.track.get(17).getAbstand_m() > aktAbstSummeAlt+1.0);
		assertEquals(VerwaltungGPX.track.get(17).getAbstvorg_m()/VerwaltungGPX.track.get(17).getAktsek()*3.6, VerwaltungGPX.track.get(17).getV_kmh(), 0.01);
		// was passiert beim ersten Punkt? Wir löschen den mal...
		VerwaltungGPX.track.remove(0);
		VerwaltungGPX.reCalcTrackNachDel();	
		assertTrue(VerwaltungGPX.track.size() == 1787);		// 1790 - 3		
		aktsekAkt = VerwaltungGPX.track.get(0).getAktsek();
		anzsekAkt = VerwaltungGPX.track.get(0).getAnzsek();		
		Mlog.debug("nach loeschen Punkt 0 <aktsekAkt>"+aktsekAkt+ " <anzsekAkt>"+anzsekAkt);
		assertTrue(VerwaltungGPX.track.get(0).getAnzsek() == 0);
		assertTrue(VerwaltungGPX.track.get(0).getAktsek() == 0);
		// ok, dann löschen wir den letzten  Punkt
		VerwaltungGPX.track.remove(1786);
		VerwaltungGPX.reCalcTrackNachDel();	
		assertTrue(VerwaltungGPX.track.size() == 1786);		// 1790 - 4
		// TODO Gesamtstrecke ca. 43,8 km ?
		abstLetztPunkt = VerwaltungGPX.track.get(1785).getAbstand_m() / 1000;
		Mlog.debug("Abstand Gesamt letzter Punkt: "+abstLetztPunkt);
		assertTrue(abstLetztPunkt > 43.7);		// vor löschen der Punkte: 43.82 km	
		// HM prüfen, wir löschen Punkt 42+43, dadurch müssten die Hm etwas kleiner zum Schluss sein
		Mlog.debug("<aktHmAlt>"+aktHmAlt+"<aktHoehe38>"+VerwaltungGPX.track.get(38).getHoehe()+
				"<aktHoehe39>"+VerwaltungGPX.track.get(39).getHoehe()+"<aktHoehe40>"+
				VerwaltungGPX.track.get(40).getHoehe()+"<aktHoehe41>"+VerwaltungGPX.track.get(41).getHoehe());
		VerwaltungGPX.track.remove(38);			// es wurden ja zwischenzeitlich drei Punkte gelöscht davor
		VerwaltungGPX.track.remove(39);			
		VerwaltungGPX.track.remove(40);
		VerwaltungGPX.track.remove(41);
		VerwaltungGPX.reCalcTrackNachDel();	
		aktHm = VerwaltungGPX.track.get(1781).getHm_m();
		Mlog.debug("<aktHm>"+aktHm);
		assertTrue(aktHm < (aktHmAlt - 0.1));
	}
	
	@Test
	public void testreCalcZeitenAbIndex() {
		long zeitpunkt1, zeitpunkt2, zeitpunkt1neu, zeitpunkt2neu, zeitpunktVorg;
		long  anzsek1, anzsek1neu;
		long  aktsek1, aktsek1neu;
		double v1, v1neu;
		Mlog.debug("************ starte testreCalcZeitenAbIndex");
		loadGPX.loadGPS(gpxdatei, true);
		// Zeitpunkt um 10 Sekunden nach vorne schieben
		zeitpunkt1 = VerwaltungGPX.track.get(17).getZeitpunkt();
		zeitpunkt2 = VerwaltungGPX.track.get(18).getZeitpunkt();
		anzsek1 = VerwaltungGPX.track.get(17).getAnzsek();
		aktsek1 = VerwaltungGPX.track.get(17).getAktsek();
		v1 = VerwaltungGPX.track.get(17).getV_kmh();
		Mlog.debug("v1: "+v1+" Zeit an Index 17: "+sdf.format(zeitpunkt1)+" Zeit an Index 18: "+sdf.format(zeitpunkt2));
		VerwaltungGPX.reCalcZeitenAbIndex(17, 10);
		zeitpunkt1neu = VerwaltungGPX.track.get(17).getZeitpunkt();
		zeitpunkt2neu = VerwaltungGPX.track.get(18).getZeitpunkt();		
		anzsek1neu = VerwaltungGPX.track.get(17).getAnzsek();
		aktsek1neu = VerwaltungGPX.track.get(17).getAktsek();
		aktsek1neu = VerwaltungGPX.track.get(17).getAktsek();
		v1neu = VerwaltungGPX.track.get(17).getV_kmh();
		Mlog.debug("v1neu: "+v1neu+" neue Zeit an Index 17: "+sdf.format(zeitpunkt1neu)+" neue Zeit an Index 18: "+sdf.format(zeitpunkt2neu));
		assertTrue(zeitpunkt1neu == zeitpunkt1 + 10000);
		assertTrue(zeitpunkt2neu == zeitpunkt2 + 10000);
		assertTrue(anzsek1neu == anzsek1 + 10);
		assertTrue(aktsek1neu == aktsek1 + 10);
		assertTrue(v1neu < v1);
		// Zeitpunkt um 3 Sek. nach hinten schieben
		zeitpunktVorg = VerwaltungGPX.track.get(16).getZeitpunkt();
		zeitpunkt1 = VerwaltungGPX.track.get(17).getZeitpunkt();
		zeitpunkt2 = VerwaltungGPX.track.get(18).getZeitpunkt();
		Mlog.debug("2. Test: Zeit an Index 16: "+sdf.format(zeitpunktVorg)+ " Zeit an Index 17: "+sdf.format(zeitpunkt1)+" Zeit an Index 18: "+sdf.format(zeitpunkt2));
		VerwaltungGPX.reCalcZeitenAbIndex(17, -3);
		zeitpunkt1neu = VerwaltungGPX.track.get(17).getZeitpunkt();
		zeitpunkt2neu = VerwaltungGPX.track.get(18).getZeitpunkt();		
		Mlog.debug("2. Test: neue Zeit an Index 17: "+sdf.format(zeitpunkt1neu)+" neue Zeit an Index 18: "+sdf.format(zeitpunkt2neu));
		assertTrue(zeitpunkt1neu == zeitpunkt1 - 3000);
		assertTrue(zeitpunkt2neu == zeitpunkt2 - 3000);
		// Test ungültiger Index
		assertFalse(VerwaltungGPX.reCalcZeitenAbIndex(-5, 1));
		assertFalse(VerwaltungGPX.reCalcZeitenAbIndex(0, 1));
		assertFalse(VerwaltungGPX.reCalcZeitenAbIndex(2000, 1));
		// ungültige ZeitDiff
		assertFalse(VerwaltungGPX.reCalcZeitenAbIndex(17, -150));		
	}
}
