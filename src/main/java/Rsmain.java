import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ws.axis2.ConnectDB;
import org.apache.ws.axis2.GetInfoURL;
import org.apache.ws.axis2.GetInfoURLResponse;
import org.apache.ws.axis2.MTBSRaceServiceStub;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.wb.swt.SWTResourceManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

import jna_ext.User32ext;
import vlc.LibVlc;
import vlc.LibVlc.LibVlcCallback;
import vlc.LibVlc.LibVlcEventManager;
import vlc.LibVlc.LibVlcInstance;
import vlc.LibVlc.LibVlcMediaDescriptor;
import vlc.LibVlc.LibVlcMediaInstance;
import vlc.LibVlc.libvlc_event_t;
import vlc.LibVlc.libvlc_exception_t;

/* 
 * This file is part of the RoomSports distribution 
 * (https://github.com/jobeagle/roomsports/roomsports.git).
 * 
 * Copyright (c) 2020 Bruno Schmidt (mail@roomsports.de).
 * 
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************
 * Rsmain.java: Oberfläche und Hauptprogramm von RoomSports
 *****************************************************************************
 *
 * Diese Klasse bildet die Oberfläche von RoomSports (realisiert mittels SWT und JFace).
 * Die Klasse des Hauptfensters leitet von ApplicationWindow ab. Das
 * nimmt mir die Arbeit für die Verwaltung des Fensters und der Programmschleife ab.  
 * 
 */

public class Rsmain extends ApplicationWindow { 
	@SuppressWarnings("unused")
	private static User32ext user32ext;
    // Oberflächenelemente
	public  static Shell sShell = null;
	private Composite compHaupt;				// Haupt-Composite
	private static Composite compa1; 			// "Anzeigetafeln"	
	private static Composite compa2;
	private static Composite compa3;
	private static Composite compa4;
	private static Composite compa5;
	private static Composite compa6;
	private static Composite compa7;
	private static Composite compa8;
	private static Composite compvideo;			// Composite für Video
	private static Composite compprofil;		// Composite für Höhen- und Geschwindigkeitsprofil
	public  static Composite compInfo;			// Composite für Schaltungsanzeige, Rangfolge im Rennen und Google Earth
	public  static Composite compMap;			// Composite für Kartenanzeige
	private static Composite comptoolbar;		// Composite für Toolbar	
    public static CCombo cmbfahrer;				// Profilauswahl Combobox
    public static CCombo cmbwind;				// Windeinstellung Combobox
    public static Slider sldvideo = null;
    private Label lblLogo;						// Label für Logo-Image in Toolbar
    private Label lblGang;						// Rahmen für Ganganzeige
    private Label lblTour;						// Tournamenanzeige
    private StyledText txtGegner = null;
    
    // zusätzliche Objekte
    public static  Anzeigetafel atPower = new Anzeigetafel();
	public static  Anzeigetafel atPuls = new Anzeigetafel();
	public static  Anzeigetafel atKurbel = new Anzeigetafel();
	public static  Anzeigetafel atGeschw = new Anzeigetafel();
	public static  Anzeigetafel atStrecke = new Anzeigetafel();
	public static  Anzeigetafel atSteigung = new Anzeigetafel();
	public static  Anzeigetafel atZeit = new Anzeigetafel();
	public static  Anzeigetafel atKcal = new Anzeigetafel();
    public static Display display;  
    // Höhenprofilanzeige
    private XYPlot plot;
	private XYSeries profilData;   
	private XYSeries profilData1;  
	private XYSeriesCollection dataset = new XYSeriesCollection();
	private XYSeriesCollection dataset1 = new XYSeriesCollection();
    private ChartComposite frame;
    
    // VLC
    public static LibVlc libvlc;
    public static LibVlcInstance vlcinst;  
    public static LibVlcMediaInstance mediaplayer;  
    public static LibVlcMediaDescriptor mediaDescriptor;  
    public static libvlc_exception_t vlcex; 
    private String[] vlcparams;

    // Klassenvariablen/Konstanten
	public  static int anzmodus = 4;       		// Anzeigemodus
    public  static Trainer thisTrainer = new Trainer();  
	private String trainerpuls;
	private String trainer_v = new String("");
	private static String ergorpm;  
	private static int slidermax = 1000; 		// Auflösung des Video-Schiebereglers
	private static int sliderinc = 10;   		// Inkrement bei Benutzung der "Pfeile"
	public  static int sliderpageinc = 50;   	// Inkrement beim klicken neben dem Schieber
	private final String[] Windgeschw = {"-BF6 (48 kmh)", "-BF5 (37 kmh)", "-BF4 (27 kmh)", "-BF3 (18 kmh)", "-BF2 (10 kmh)", "-BF1 ( 4 kmh)",    
                                         " BF0 ( 0 kmh)",   
                                         " BF1 ( 4 kmh)", " BF2 (10 kmh)", " BF3 (18 kmh)", " BF4 (27 kmh)", " BF5 (37 kmh)", " BF6 (48 kmh)"};  
    private static double vwind = 0.0;
    public  static String Profildatei = Global.standardprofildatei;  
    private static Timer timer = new Timer();  
    private static Timer timer1 = new Timer();  
    private long timersek = 0;                	// Bei diesem Wert startet der Timer (benötigt für Pause)
    private long newvidtime = 0;
    private long lasttime = 0;
    public  static long trainingsmillisek = 0;	// Hier wird die Trainingszeit gespeichert
    private double dletztehoehe = 0.0;
    public  static double dzpstrecke = 0.0;		// Zielpulsstrecke
    private double dhm = 0.0;
    private double dstrecke = 0.0;
    private double dStreckenSumme = 0.0;
    public  enum Status { angehalten, laeuft, beendet, autostopped };
    public  static Status aktstatus = Status.beendet; 
    private double work = 0.0;                  // geleistete Arbeit in Ws
    private double kcalfakt = 966.0;            // Umrechnung in kCal Wirkungsgrad=0.23: Ws * s / (4.2 * 0.23 * 1000)
	public	double oldrate = 1.0;				// Rate für Dynamik-Modus speichern
    public  static Gegner aktgegner = null;  
    private long rtStartSek;					// RealTime Sekunden zum Startzeitpunkt (benötigt für CSV-Rennen) 
    private long rtSek;							// RealTime Sekunden aktuell
    private long lastrtSek = 0;
    @SuppressWarnings("unused")
	private long pausensek = 0;					// zur Zählung der Trainingspausensekunden
    private double demopuls = 120.0;			// Demowerte, änderbar mit <CTRL>3,4
    private double demorpm = 90.0;				// Demowerte, änderbar mit <CTRL>1,2
    private long gesamtsek = 0;
    private double gesamtstrecke = 0.0;
    private double gesamthm = 0.0;
    public  static double drpmmin = 10.0;		// Ab diesem Wert gilt: es wird getreten
    private double minLeistung = 20.0;			// Auf diese Leistung wird bei Pulsüberschreitung gesetzt.
    private double minLeistungOT = 100.0;		// Auf diese Leistung wird bei Pulsüberschreitung beim Onlinetraining gesetzt.
    public  static Image icon;
    private double tacxvidmax = 1.3;			// max. Videofaktor bei Tacx
    private double tacxslopecorrect = 0.0;		// Korrektorfaktor bei Steigung (wird addiert)
    public static  Rectangle startcr;			// Clientbereich der Shell nach Start (nur noch für Initialisierungszwecke!)
    public static  Rectangle aktcr;				// aktueller Clientbereich der Shell nach Start
    public Wert    Leistung = new Wert();		// speichert die Leistung (inkl. Min/Max, Summen etc.)
    public Wert    Puls = new Wert();			// speichert den Puls (inkl. Min/Max, Summen etc.)
    public Wert    Kurbel = new Wert();			// speichert die Kurbelumdrehungen (inkl. Min/Max, Summen etc.)
    public Wert    Geschwindigkeit = new Wert(); // speichert die Geschwindigkeit (inkl. Min/Max, Summen etc.)
//    public Wert    Fitness = new Wert();        // Fitnesswert (verwendet wird nur der Mittelwert)
    private int appminbreite = 1266;			// Minimalfenstergröße der RS Applikation
    private int appminhoehe = 720;
	private DecimalFormat zfk2 = new DecimalFormat("0.00");  
	private DecimalFormat zfk1 = new DecimalFormat("0.0"); 
	private DecimalFormat zfk0 = new DecimalFormat("#");  
	private DecimalFormat zfkw = new DecimalFormat("####");  
	private SimpleDateFormat tfmt;
	private	SimpleDateFormat tfmt1;
	private	SimpleDateFormat uhrzeitfmt;
	private long lastIndex = 0;
	private Vector<Marker> oldpos = new Vector<Marker>();
	public  static String strvlcaddparams = new String("");
	private static long kettlerlongwait = 240;			// vorher 260! länger Warten auf Antwort bei Kettlerfirmware AR1S!
	public  static org.eclipse.swt.graphics.Color rahmenfarbe = new org.eclipse.swt.graphics.Color(Display.getCurrent(),204,219,237);
	private double grate = 1.0;					// Gangfaktor
	private	double drate = 1.0;					// Dynamikfaktor
	private int maxgang = 9;					// höchster Gang
	private static int	tacxAntWait = 5000;		// 5 Sek. warten bis ANT+ Seriennummer abgefragt wird
    public  static String belText = new String("");
    private static long startGPSPunkt = 1;		// bei Onlinerennen kann der Startpunkt vorgegeben werden
    private static long zielGPSPunkt = 0;		// bei Onlinerennen kann der Zielpunkt vorgegeben werden
    private static int mindGPS = 5;				// erst ab diesem Start-Punkt und nur mindGPS vor dem Ende-Punkt des Trainings wird auf Wiederaufnahme geprüft
    private long timeLastKey = 0;				// wird verwendet zur Erkennung ob (Lenkertaster) prellen
    private long timeLastKeyFullScreen = 0;		// wird verwendet zur Erkennung ob Vollbildumschaltung
    private long maxTimeDiffLastKey = 250;		// ab dieser Anz. ms ist der Tastendruck ok (zuletzt: 180)
    private long maxTimeDiffLastKeyFullScreen = 1000;		// ab dieser Anz. ms ist die Umschaltung auf Vollbild ok
    public  GPXEdit gpxEdit = null;
    
    public  static String latestVersion  = "";
    private static final String versionURL = "https://www.mtbsimulator.de/rs/v.txt";
    
    public static Toolbar toolbar;
	public  Auswertung auswertung = new Auswertung();
	public  static Wettkampf wettkampf = new Wettkampf();
    public  static Fahrer biker = new Fahrer();
    public  static Konfiguration newkonfig;
    public  static Server server;
    public  static LibAnt libant = null; 
	private OnlineGegner onlgeg;
	private VerwaltungGPX ldpx;
    public  UpdateDialog showUpdate = null;		// Updatedialog
    private static Rsmain app;
    
	// OSM Variablen
	public  static OSMViewer osmv;
	private OSMViewer.PointD osm_koord;
	public  static double rs_lon = 11.33666;	// Längengrad Bürgerweiher Schnaittach
	public  static double rs_lat = 49.55666;	// Breitengrad Bürgerweiher
	public  int osmMaxZoom = 17;
	public  int osmMinZoom = 10;
	private int osmTilesrvInd = 0;				// Index des akt. Tileservers
	private int lastKeyCode;

	// Switches
    private static boolean demomodus = false;   // Demomodus
    private static boolean vlc_ein = true;      // Videoausgabe nur, wenn VLC-Player vorhanden ist - wird auch ausgeschaltet beim laden einer GPX-Datei!
    private static boolean imdialog = false;
    private static boolean gemodus = false;     // Google-Earth ist ausgeschaltet
    private static boolean csvrennen = false;   // Flag ob gegen ein CSV-Protokoll gefahren wird
    private static boolean init = true;   		// Flag für einmalige Aktionen nach dem ersten Startt
    private static boolean ausgewertet = false; // Flag für Auswertung
    public  static boolean pausetaste = false;  // Flag für Umschaltung Start / Pause
    private static boolean endlos = false;		// immer wieder von vorne starten (für Demozwecke)
    private static boolean ibl = false;			// Indoor Bike Leaque Rennen wird gefahren
    private static boolean ovddata = false;		// OVD-Datenanzeige (zur Erzeugung der MTBS-Trainingsvideos)
    private static boolean imUpdate = false;	// Updatevorgang läuft
	private static boolean tourEnde = false;	// Ende der Tour erreicht?

    private static JSAPResult config;
    
    /**
	 * @return the ibl
	 */
	public static boolean isIbl() {
		return ibl;
	}

	/**
	 * @param ibl the ibl to set
	 */
	public static void setIbl(boolean ibl) {
		Rsmain.ibl = ibl;
	}

	/**
	 * @return the demoversion
	 */
	/*
	public static boolean isDemoversion() {
		return demoversion;
	}
	*/
	/**
	 * @param demoversion the demoversion to set
	 */
	/*
	public static void setDemoversion(boolean demoversion) {
		Rsmain.demoversion = demoversion;
	}
	*/
	/**
	 * @return the vlc_ein
	 */
	public static boolean isVlc_ein() {
		return vlc_ein;
	}

	/**
	 * @param vlc_ein the vlc_ein to set
	 */
	public static void setVlc_ein(boolean vlc_ein) {
		Rsmain.vlc_ein = vlc_ein;
	}

	/**
	 * @return the imkonfig
	 */
	public static boolean isImDialog() {
		return imdialog;
	}

	/**
	 * @param imdialog the imdialog to set
	 */
	public static void setImDialog(boolean imdialog) {
		Rsmain.imdialog = imdialog;
	}

	/**
	 * @return the gemodus
	 */
	public static boolean isGemodus() {
		return gemodus;
	}

	/**
	 * @param gemodus the gemodus to set
	 */
	public static void setGemodus(boolean gemodus) {
		Rsmain.gemodus = gemodus;
	}

	/**
	 * @return the csvrennen
	 */
	public static boolean isCsvrennen() {
		return csvrennen;
	}

	/**
	 * @return the ovddata
	 */
	public static boolean isOvddata() {
		return ovddata;
	}

	/**
	 * @param ovddata the ovddata to set
	 */
	public static void setOvddata(boolean ovddata) {
		Rsmain.ovddata = ovddata;
	}

	/**
	 * @param csvrennen the csvrennen to set
	 */
	public static void setCsvrennen(boolean csvrennen) {
		Rsmain.csvrennen = csvrennen;
	}

	/**
	 * @return the ausgewertet
	 */
	public static boolean isAusgewertet() {
		return ausgewertet;
	}

	/**
	 * @param ausgewertet the ausgewertet to set
	 */
	public static void setAusgewertet(boolean ausgewertet) {
		Rsmain.ausgewertet = ausgewertet;
	}

	/**
	 * @return the rahmenfarbe
	 */
	static org.eclipse.swt.graphics.Color getRahmenfarbe() {
		return rahmenfarbe;
	}

	/**
	 * @param rahmenfarbe the rahmenfarbe to set
	 */
	public static void setRahmenfarbe(org.eclipse.swt.graphics.Color rahmenfarbe) {
		Rsmain.rahmenfarbe = rahmenfarbe;
		SetColorAnzeigetafeln();
	}

	/**
	 * @return the osmTilesrvInd
	 */
	public int getOsmTilesrvInd() {
		return osmTilesrvInd;
	}

	/**
	 * Aktuellen Tileserver-Index setzen. Wird die eingetragene Anzahl überschritten, dann wird zurückgesetzt
	 * @param osmTilesrvInd the osmTilesrvInd to set
	 */
	public void setOsmTilesrvInd(int osmTilesrvInd) {
		if (osmTilesrvInd >= OSMViewer.TILESERVERS.length)
			this.osmTilesrvInd = 0;	
		else
			this.osmTilesrvInd = osmTilesrvInd;
	}

	/**
	 * @param startGPSPunkt the startGPSPunkt to set
	 */
	public static void setStartGPSPunkt(long startGPSPunkt) {
		Rsmain.startGPSPunkt = startGPSPunkt;
	}

	/**
	 * @return the startGPSPunkt
	 */
	public static long getStartGPSPunkt() {
		return startGPSPunkt;
	}

	/**
	 * @param zielGPSPunkt the zielGPSPunkt to set
	 */
	public static void setZielGPSPunkt(long zielGPSPunkt) {
		Rsmain.zielGPSPunkt = zielGPSPunkt;
	}

	/**
	 * @return the zielGPSPunkt
	 */
	public static long getZielGPSPunkt() {
		return zielGPSPunkt;
	}

	/**
	 * Der Konstruktor.
	 */
	public Rsmain() {
		// Das Programmfenster besitzt kein übergeordnetes Fenster.
		super(null);

		// Das Programm soll den Thread in dem es geöffnet wird Blockieren.
		// Wenn es das nicht macht wird das Programm sofort wieder beendet,
		// da der Main-Thread endet.
		this.setBlockOnOpen(true);
	}

	/**
	 * Initialisierungen abhängig vom Betriebssystem.
	 * Aktuell wird nur Windows vollständig unterstützt!
	 */
	private void initMTBS() {
		if (Platform.isWindows()) {
			Global.ptz = "\\";
			Global.strProgramPfad = System.getenv("PROGRAMFILES")+"\\roomsports\\";		
		    Global.strPfad = System.getenv("APPDATA")+"\\roomsports\\";       
		    Global.strVLCPfad = getRegKey("HKLM\\SOFTWARE\\VideoLAN\\VLC", "InstallDir");
			libant = new LibAnt(); 
			Global.javaRT = System.getProperty("java.home") + "\\bin\\javaw.exe";
		} else {
			if (Platform.isMac()) {
				Global.ptz = "/";
				Global.strProgramPfad = "./";
				Global.strPfad = System.getProperty("user.home") + "/roomsports/";
				Global.strVLCPfad = "/Applications/VLC.app/Contents/MacOS/lib/";			
				Global.javaRT = System.getProperty("java.home") + "/bin/javaw.exe";
				// TODO: noch unvollständig z.B. ANT+ Kommunikation über serielles Device (suche im thisisant-Forum nach MacOs!) fehlt!				
			} else {		// Linux
				Global.ptz = "/";
				Global.strProgramPfad = "/opt/roomsports/";
				Global.strPfad = System.getProperty("user.home") + "/roomsports/";
				Global.strVLCPfad = "/usr/bin/vlc/";			
				Global.javaRT = System.getProperty("java.home") + "/bin/javaw.exe";
				// TODO: noch unvollständig z.B. ANT+ Kommunikation über serielles Device (suche im thisisant-Forum nach Linux!) fehlt!	
				// aktuell (2014) is keine LIB_ANT für Linux verfügbar!
			}
		}
	}
	
	/**
	 * Die Shell einrichten (inkl. Listener)
	 * 
	 */
	@Override
	protected void configureShell(Shell shell) {	
		sShell = shell;
		super.configureShell(sShell);

		initMTBS();

		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		zfk1.setDecimalFormatSymbols(dfs);
		zfk2.setDecimalFormatSymbols(dfs);

		// Den Title des Fensters setzen.
		sShell.setText(Global.progname);
		sShell.setSize(appminbreite, appminhoehe);
		sShell.setMinimumSize(appminbreite, appminhoehe);

		Messages.startmessaging(sShell);
		checkLock();
		testMethode();		// Hier können Tests, die beim Start durchgeführt werden, eingebaut werden
		
		newkonfig = new Konfiguration(); 

/*		
		try {
			icon = new Image(display, Global.strProgramPfad+"logo.ico"); 
		} catch (Exception e) {
			Messages.errormessage(Messages.getString("Rsmain.achtung_fehler")+ Global.strProgramPfad + Messages.getString("Rsmain.installieren"));  
			Mlog.ex(e);
		} 
		sShell.setImage(icon);
*/
		sShell.getDisplay().addFilter(SWT.KeyDown, new Listener() {
			@Override
	        public void handleEvent(org.eclipse.swt.widgets.Event e) {
	        	if (isImDialog() || tasterHatGeprellt()) // nichts machen, wenn Konfigmenü angezeigt wird!
	        		return;
	        	
            	// Testaufruf
	        	if((e.stateMask & SWT.CTRL) != 0 && e.keyCode == 'y') { 
	        		try {
	        			Mlog.debug("im Testaufruf");
	        			if (VerwaltungGPX.track != null) {
	        				if (gpxEdit == null) {
	        					gpxEdit = new GPXEdit();
	        					gpxEdit.on(480, 640);
	        				} else
	        					gpxEdit.on();
	        			}
	        		} catch (Exception e1) {
	        			Mlog.error("Fehler beim Testaufruf!");
	        			Mlog.ex(e1);
	        		}
	        	}
            
            	// Umschaltung OVD-Data-Modus
	            if((e.stateMask & SWT.CTRL) != 0 && e.keyCode == 's') { 
	            	if (isOvddata()) {
	            		//setStreaming(false);
	            		setOvddata(false);
	            	} else {
	            		//setStreaming(true);
	            		setOvddata(true);
	            	}
	            	Mlog.info("OVD: "+isOvddata());
	        		if (aktstatus == Status.laeuft)
	        			showOVD(0, 180, 1500, 100, "OVD: "+isOvddata());
	            }

	            // Fullscreen mit ESC oder "/"
		        if (e.keyCode == SWT.ESC || e.character == '/') {
		        	if (Global.fullscreen)
		        		switchFullscreen(false);
		        	else
		        		switchFullscreen(true);
		        	
		        	showClientArea();
	            }
	            
	        	// Ctrl-1/2 = demorpm ändern
	            if((e.stateMask & SWT.CTRL) != 0 && e.keyCode == '1') { 
	            	demorpm = (demorpm > 0) ? demorpm -1 : 0;
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            if((e.stateMask & SWT.CTRL) != 0 && e.keyCode == '2') { 
	            	demorpm = (demorpm < 200) ? demorpm +1 : 0;
	            	lastKeyCode = e.keyCode;
	            	return;
	            }

            	// Ctrl-3/4 = Demopulsrate ändern
	            if((e.stateMask & SWT.CTRL) != 0 && e.keyCode == '3') { 
	            	demopuls = (demopuls > 0) ? demopuls -1 : 0;
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            if((e.stateMask & SWT.CTRL) != 0 && e.keyCode == '4') { 
	            	demopuls = (demopuls < 200) ? demopuls +1 : 0;
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            
 	            // DEBUG-Logging einschalten
	            if((e.stateMask & SWT.CTRL) != 0 && e.keyCode == 'd') { 
					Mlog.setDebugstatus(true);
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
 	            // DEBUG-Logging ein und Deepdebug für die Kommunikation aktivieren --> jedes Zeichen protokollieren!
	            if((e.stateMask & SWT.CTRL) != 0 && e.keyCode == 'D') { 
					Mlog.setDebugstatus(true);
	            	thisTrainer.setDeepdebug(true);
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            
 	            // Endlosmodus ein-/ausschalten
	            if((e.stateMask & SWT.CTRL) != 0 && e.keyCode == 'e' && aktstatus == Status.laeuft) { 
	            	endlos = endlos ? false : true;
	        		if (aktstatus == Status.laeuft)
	        			showOVD(0, 180, 1500, 100, "Loop: "+endlos);
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            // OSM-Tastenbefehle
	            if (e.keyCode == SWT.PAGE_DOWN && aktstatus == Status.laeuft) {	// runter bzw. vergrößern
	            	// PAGE-UP - PAGE_DOWN nacheinander -> Vollbildanzeige ein/aus!
	            	if (lastKeyCode == SWT.PAGE_UP && tasterSchnell()) {
        				if (Global.fullscreen)
        					switchFullscreen(false);
        				else
        					switchFullscreen(true);
        	
        				showClientArea();			
					}
		            if (newkonfig.isShowmap()) {
		            	int aktzoom = osmv.getZoom();
		            	aktzoom = (aktzoom < osmMaxZoom) ? aktzoom+1 : aktzoom;
		            	osmv.setZoom(aktzoom);
     					osmv.setCenterPosition(osmv.computePosition(osm_koord));
		            	osmv.redraw();
		            	//Mlog.debug("OSM-Zoom: " + aktzoom);
		            }
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            if ((e.keyCode == SWT.PAGE_UP) && aktstatus == Status.laeuft) { // rauszoomen
	            	// PAGE-UP - PAGE_DOWN nacheinander -> Vollbildanzeige ein/aus!
	            	if (lastKeyCode == SWT.PAGE_DOWN && tasterSchnell()) {
        				if (Global.fullscreen)
        					switchFullscreen(false);
        				else
        					switchFullscreen(true);
        	
        				showClientArea();			
					}
		            if (newkonfig.isShowmap()) {
		            	int aktzoom = osmv.getZoom();
		            	aktzoom = (aktzoom > osmMinZoom) ? aktzoom-1 : aktzoom;
		            	osmv.setZoom(aktzoom);
     					osmv.setCenterPosition(osmv.computePosition(osm_koord));
		            	osmv.redraw();
		            	//Mlog.debug("OSM-Zoom: " + aktzoom);
		            }
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            // Tileserver ändern
	            if ((e.stateMask & SWT.CTRL) != 0 && e.keyCode == 't') { 
	            	if (newkonfig.isShowmap()) {
	            		setOsmTilesrvInd(getOsmTilesrvInd()+1);
	            		osmv.setTileServer(OSMViewer.TILESERVERS[getOsmTilesrvInd()]);
		            	Mlog.debug("OSM-Tileserver: " + osmv.getTileServer());
	            	}
	            	lastKeyCode = e.keyCode;
	            	return;
	            }

	            // Tastatursteuerung RoomSports
	            if (e.character == 'p' || e.character == 'P' /*|| e.character == '0'*/) { // Pause
					if (!pausetaste) {
						videostart();
						toolbar.changeImgStartbutton("pause.png");	
						pausetaste = true;
					} else {
						dletztehoehe = 0.0;
						videopause();
						atZeit.show(0, Messages.getString("Rsmain.pause"), 0); 
						toolbar.changeImgStartbutton("play.png");  
						pausetaste = false;
					}
	            	lastKeyCode = e.keyCode;
					return;
	            }
	            if (e.character == 's' || e.character == 'S' || e.character == '*' || e.keyCode == SWT.ARROW_RIGHT) { // Start
					doStart();
	            	lastKeyCode = e.keyCode;
					return;
	            }	 
	            if (e.character == 'a' || e.character == 'A' || 
	            		e.character == '.' || e.character == ',' || e.keyCode == SWT.ARROW_LEFT) { // Ansicht umschalten
	            	switchComposites();
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            
	            if (((e.character == '-') || (e.keyCode == SWT.ARROW_DOWN)) && atKurbel.getEnabled(2) && aktstatus == Status.laeuft) { // langsamer
	            	changeGangrunter();
	            	lastKeyCode = e.keyCode;
	            	return;
	            }	  	            
	            if (((e.character == '+') || (e.keyCode == SWT.ARROW_UP)) && atKurbel.getEnabled(2) && aktstatus == Status.laeuft) { // schneller
	            	changeGanghoch();
	            	lastKeyCode = e.keyCode;
	            	return;
	            }

	            if (e.character == '5' && aktstatus == Status.laeuft) {	// Gangautomatik ein/aus
	            	gangAutomatikEinAus();
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            
	            if (e.keyCode == SWT.DEL) {
		            if (newkonfig.isShowmap()) {
		            	setOsmTilesrvInd(getOsmTilesrvInd()+1);
		            	osmv.setTileServer(OSMViewer.TILESERVERS[getOsmTilesrvInd()]);
			           	Mlog.debug("OSM-Tileserver: " + osmv.getTileServer());
		            }	            		
	            }
	            
	            if (wettkampf.isAktiv()) {	
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            
	            // -----------------------------------------------------------------------------
	            // Die folgenden Tastaturkommandos werden bei Onlinerennen nicht unterstützt!
	            // -----------------------------------------------------------------------------
	            if (e.character == 't' || e.character == 'T') { // Stop
	            	doStop(true, true);
	            	lastKeyCode = e.keyCode;
	            	return;
	            }	            

	            if ((e.keyCode == SWT.INSERT) && aktstatus == Status.laeuft) { // kleinerer Blickwinkel - Schwenk runter
		            comWattsEinAus();		// Leistungsansteuerung ein/aus           		
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            if (e.character == '0') {	// Leistungsansteuerung ein/aus
	            	comWattsEinAus();
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            
	            if (e.character == 'b' || e.character == 'B' || e.character == '7') { // Bikerprofil wechseln
	            	changeBikerprofil();
	            	lastKeyCode = e.keyCode;
	        		return;
	            }
	            if (e.character == 'k' || e.character == 'K') { // Konfiguration aufrufen
			        if (Profildatei.indexOf(Global.strPfad) == -1)
			        	Profildatei = Global.strPfad+Profildatei;
			        newkonfig.show(biker, thisTrainer);
	            	lastKeyCode = e.keyCode;
			        return;
	            }	            
	            if (e.character == 'f' || e.character == 'F') {
	            	setTour(null);
	            	lastKeyCode = e.keyCode;
	            	return;
	            }
	            if ((e.character == 'g' || e.character == 'G')  && cmbwind.getEnabled()){  // Windeinstellung ändern: Gegenwind erhöhen
	            	changeGegenwind();
	            	lastKeyCode = e.keyCode;
	            	return;
	            }	            
	            if ((e.character == 'r' || e.character == 'R') && cmbwind.getEnabled()){  // Windeinstellung ändern: Rückenwind erhöhen
	            	changeRueckenwind();
	            	lastKeyCode = e.keyCode;
	            	return;
	            }	            
	            if (e.character == '4') {  // Leistungsfaktor erhöhen
	            	changeLFhoch(0.01);
	            	lastKeyCode = e.keyCode;
	            	return;
	            }	            
	            if (e.character == '6') {  // Leistungsfaktor vermindern
	            	changeLFrunter(0.01);
	            	lastKeyCode = e.keyCode;
	            	return;
	            }	            
	            if (e.character == '1') {  // Windeinstellung ändern
	            	zappWind();
	            	lastKeyCode = e.keyCode;
	            	return;
	            }	            
	            if (e.character == 'z' || e.character == 'Z' || e.character == '2') { // Slider zurück
	            	sliderjump(-sliderinc);
	            	lastKeyCode = e.keyCode;
	            	return;
	            }	            
	            if (e.character == 'v' || e.character == 'V' || e.character == '8') { // Slider vor
	            	sliderjump(sliderinc);
	            	lastKeyCode = e.keyCode;
	            	return;
	            }	            
	            if (e.character == 'u' || e.character == 'U' || e.character == '3') { // Slider Page zurück
	            	sliderjump(-sliderpageinc);
	            	lastKeyCode = e.keyCode;
	            	return;
	            }	            
	            if (e.character == 'c' || e.character == 'C' || e.character == '9') { // Slider Page vor
	            	sliderjump(sliderpageinc);
	            	lastKeyCode = e.keyCode;
	            	return;
	            }	  
	        }

		});
		
        sShell.addListener (SWT.Dispose, new Listener () {
			@Override
			public void handleEvent(Event arg0) {
			    // Settings (z.B. Windowposition) speichern
				newkonfig.createXMLFileSettings(Global.standardsettingsdatei, biker);
	        	String dateiname = Global.strPfad+biker.getName()+".xml"; 
				newkonfig.createXMLFileBiker(dateiname, biker, thisTrainer);

				videostop();
				
				// Bei Close werden die Trainingsdaten im Profil gespeichert, falls noch nicht geschehen.
				if (!auswertung.bSave && auswertung.isInitialisiert()) 
					auswertung.saveGesamtTrainingsdaten(true);
			}       	
        });

	    sShell.addListener(SWT.Resize, new Listener() {
	        public void handleEvent(Event event) {
	        	//Mlog.debug("Resize: "+startcr+"aktcr: "+aktcr);
	        	if (startcr != null) {
	        		aktcr = sShell.getClientArea();
	        		Rsmain.aktcr.x = Rsmain.sShell.getLocation().x; 
	        		Rsmain.aktcr.y = Rsmain.sShell.getLocation().y;
	        		//Mlog.debug("actcr: " + Rsmain.aktcr.width+" , "+Rsmain.aktcr.height+" , "+Rsmain.aktcr.x+" , "+Rsmain.aktcr.y);
	        		showClientArea();
	        	}	
	        }	        
	    });
	}

	/**
	 * Ermittelt, ob es sich um ein Prellen der Lenkertaster handelt
	 * oder ob es ein gültiger Tastencode ist.
	 * @return	true bei Lenkerprellen, ansonsten false
	 */
	protected boolean tasterHatGeprellt() {
		long aktTime = new Date().getTime();
		if (aktTime - timeLastKey > maxTimeDiffLastKey) {
			timeLastKey = aktTime;
			return false;
		}
		return true;
	}

	/**
	 * Bei Page-Up und anschl. Page-Down und visaversa in kurzer Zeit (1 Sekunde) 
	 * wird zur Vollbildanzeige gewechselt. Die Zeit wird hier geprüft.
	 * @return	true bei < 1 Sek, ansonsten false
	 */
	protected boolean tasterSchnell() {
		long aktTime = new Date().getTime();
		Mlog.debug("Zeitdifferenz: " + (aktTime - timeLastKeyFullScreen));
		if (aktTime - timeLastKeyFullScreen > maxTimeDiffLastKeyFullScreen) {
			timeLastKeyFullScreen = aktTime;
			return false;
		}
		return true;
	}

	/**
	 * Schaltet um auf Fullscreen-Modus (ohne Menüs, Toolbar etc.)
	 * @param schalteEin	Fullscreen ein/bzw. ausschalten
	 */
	static void switchFullscreen(boolean schalteEin) {
    	if (schalteEin == false) {
    		comptoolbar.setVisible(true);
    		sShell.setFullScreen(false);
    		sShell.setMenuBar(toolbar.menuManager.getMenu());
    		Global.fullscreen = false;
    	} else {
    		comptoolbar.setVisible(false);
    		sShell.setFullScreen(true);
    		sShell.setMenuBar(null);
    		Global.fullscreen = true;
    	}		
	}

	/**
	 * Die Inhalte des Fensters erstellen.
	 */
	protected Control createContents(Composite parent) {
		doit: try {
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			compHaupt = new Composite(parent, SWT.NONE);
			compHaupt.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			
			compa1 = new Composite(compHaupt, SWT.BORDER);
			compa1.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND));
			compa1.setBounds(1, 750, 156, 112);
			compa2 = new Composite(compHaupt, SWT.BORDER);
			compa2.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND));
			compa2.setBounds(157, 750, 156, 112);
			compa3 = new Composite(compHaupt, SWT.BORDER);
			compa3.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND));
			compa3.setBounds(313, 750, 156, 112);
			compa4 = new Composite(compHaupt, SWT.BORDER);
			compa4.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND));
			compa4.setBounds(469, 750, 156, 112);
			compa5 = new Composite(compHaupt, SWT.BORDER);
			compa5.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND));
			compa5.setBounds(625, 750, 156, 112);
			compa6 = new Composite(compHaupt, SWT.BORDER);
			compa6.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND));
			compa6.setBounds(781, 750, 156, 112);
			compa7 = new Composite(compHaupt, SWT.BORDER);
			compa7.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND));
			compa7.setBounds(937, 750, 156, 112);
			compa8 = new Composite(compHaupt, SWT.BORDER);
			compa8.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND));
			compa8.setBounds(1095, 750, 156, 112);
			
			compvideo = new Composite(compHaupt, SWT.EMBEDDED);
			if (!Platform.isWindows())
				sShell.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			compvideo.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			compvideo.redraw();
			compprofil = new Composite(compHaupt, SWT.NONE);
			compprofil.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			compprofil.setBounds(0, 580, 938, 170);
						
			compInfo = new Composite(compHaupt, SWT.NONE);
			compInfo.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			compMap = new Composite(compHaupt, SWT.NONE);
			compMap.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			
			lblGang = new Label(compInfo, SWT.NONE);
			lblGang.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			lblGang.setBounds(53, 10, 206, 150);
			lblGang.setText(Messages.getString("Rsmain.ganganzeige")); 
			lblGang.setImage(new Image(Display.getCurrent(), "g5.png")); 
			
			txtGegner = new StyledText(compInfo, SWT.MULTI);
			txtGegner.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL)); 
			txtGegner.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			txtGegner.setForeground(SWTResourceManager.getColor(255, 255, 225));
			txtGegner.setEditable(false);
			txtGegner.setBounds(0, 0, 320, 500);
			
			comptoolbar = new Composite(compHaupt, SWT.NONE);
			comptoolbar.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			comptoolbar.setBounds(0, 0, 1250, 40);
			
			InitAnzeigetafeln();
			
			cmbfahrer = new CCombo(comptoolbar, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			cmbfahrer.setItems(setcmbfahrer());
			cmbfahrer.setToolTipText(Messages.getString("Rsmain.bikerprofil")); 
			//cmbfahrer.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeCCombo(cmbfahrer);
			cmbfahrer.addSelectionListener(new SelectionAdapter() {
			    public void widgetSelected(SelectionEvent e) {
			    	biker.setName(cmbfahrer.getText());
			    	Profildatei = Global.strPfad+cmbfahrer.getText()+".xml";  
			        Mlog.info(Messages.getString("Rsmain.profildatei_dp")+Profildatei);  
					newkonfig.createXMLFileSettings(Global.standardsettingsdatei, biker);
					newkonfig.loadProfil(Profildatei, biker, thisTrainer);
					
					// Das Interface könnte sich geändert haben!
					thisTrainer.serclose();
					thisTrainer.netclose();
					thisTrainer.usbclose();
					if (libant != null) 	
						libant.stop();
					
			        initergokom();	
					cmbfahrer.clearSelection();
			    }
			});
			// nicht auf Tastatur reagieren!
			cmbfahrer.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					e.doit = false;
				}
			});

			cmbwind = new CCombo(comptoolbar, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			cmbwind.setItems(Windgeschw);
			cmbwind.setToolTipText(Messages.getString("Rsmain.wind"));  
			cmbwind.select(6);               // Default: Wind = 0
			cmbwind.clearSelection();
			//cmbwind.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeCCombo(cmbwind);
			cmbwind.addSelectionListener(new SelectionAdapter() {
			    public void widgetSelected(SelectionEvent e) {
					if (aktstatus == Status.laeuft)
						showOVD(0, 100, 1500, 100, cmbwind.getText());
			    	setWind();
			    	cmbwind.clearSelection();
			    }
			});
			// nicht auf Tastatur reagieren!
			cmbwind.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					e.doit = false;
				}
			});

			// Slider für Videoposition
			sldvideo = new Slider(comptoolbar, SWT.HORIZONTAL);
			// Tasten, die woanders verwendet werden, hier abschalten!
			sldvideo.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					switch (e.keyCode) {
						case SWT.PAGE_UP: 
						case SWT.PAGE_DOWN: 
						case SWT.ARROW_LEFT:
						case SWT.ARROW_RIGHT:
						case SWT.ARROW_DOWN:
						case SWT.ARROW_UP:
							e.doit = false;
							break;						
					}
				}
			});
			sldvideo.setMaximum(slidermax);
			sldvideo.setIncrement(sliderinc);
			sldvideo.setPageIncrement(sliderpageinc);
			sldvideo.setToolTipText(Messages.getString("Rsmain.neue_pos")); 
			sldvideo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (!wettkampf.isAktiv()) {	// nur wenn kein Onlinerennen aktiv ist!
						dletztehoehe = 0.0;
						Double dpos = new Double((double)sldvideo.getSelection()/(double)slidermax);
						if ((libvlc != null) && isVlc_ein())
							libvlc.libvlc_media_player_set_position(mediaplayer, dpos.floatValue());
						else
							timersek = (long) (gesamtsek * dpos);
					}
				}
			});
			
			lblLogo = new Label(comptoolbar, SWT.NONE);
			lblLogo.setText("Logo"); 
			lblLogo.setImage(new Image(Display.getCurrent(), "logo_h40.png"));  

			lblTour = new Label(comptoolbar, SWT.NONE);
			lblTour.setToolTipText(Messages.getString("Rsmain.videopfad"));  
			Global.setFontSizeLabel(lblTour);
			
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));  
			tfmt = new SimpleDateFormat();
			tfmt1 = new SimpleDateFormat();
			uhrzeitfmt = new SimpleDateFormat();
			
			tfmt.applyPattern("H:mm:ss");  
			tfmt1.applyPattern("H:mm"); 
			uhrzeitfmt.applyPattern("H:mm"); 
			uhrzeitfmt.setTimeZone(TimeZone.getTimeZone(Global.zeitzone));

			Mlog.info(Global.version+" Start..."); 
			Mlog.info("Umgebung: " + Global.os + " Version: " + Global.osversion);
			Mlog.debug("Font Resolution Faktor: " + Global.resolutionFactor);
			Mlog.debug("File.encoding: " + System.getProperty("file.encoding"));
			
			createCompprofil();

			profilData = new XYSeries(Messages.getString("Rsmain.hoehe_m"));   
			profilData1 = new XYSeries(Messages.getString("Rsmain.geschwindigkeit"));  

			// Fahrerprofil laden und Konfigurationsarbeiten
			startcr = sShell.getClientArea();
			newkonfig.loadProfil(Global.strPfad+Profildatei, biker, thisTrainer);
			cmbfahrer.setText(biker.getName());
			cmbfahrer.clearSelection();

			if (newkonfig.isNoOverlay())
				strvlcaddparams = "--no-overlay";

			Mlog.info("VLC-Path:" + Global.strVLCPfad);
			System.setProperty("jna.library.path", Global.strVLCPfad);
			
			if (Mlog.isDebugstatus()) {
				vlcparams = new String[] { "-v", "--plugin-path="+Global.strVLCPfad+Global.ptz+"plugins", "--no-plugins-cache", "--no-video-title-show", strvlcaddparams}; 
			} else {
				vlcparams = new String[] { "-q", "--plugin-path="+Global.strVLCPfad+Global.ptz+"plugins", "--no-plugins-cache", "--no-video-title-show", strvlcaddparams};     
			}
			// Mac: "--vout=macosx"
			Mlog.debug("vlcparams: " + Arrays.toString(vlcparams));
			
			try {
				initPlayer();  
			    vlcinst = libvlc.libvlc_new(vlcparams.length, vlcparams);
			} catch (UnsatisfiedLinkError  e1) {
				Messages.errormessage(Messages.getString("Rsmain.vlc_nicht_gefunden"));
			    Mlog.error(Messages.getString("Rsmain.vlc_nicht_gefunden"));  
			    setVlc_ein(false);
			}
			
			Mlog.info("Interface: "+thisTrainer.getErgoIP()+thisTrainer.getErgoCom());  
			Mlog.info("Dynamik: "+newkonfig.isDynamik());

			if (thisTrainer.getTrainertyp() == Trainer.typ.rolle) // Rollentrainer?
				cmbwind.setEnabled(false);

			if (!demomodus) 
				initergokom();

			if (Rsmain.libant != null)
				if (libant.isEin())
					libant.start();

			showClientArea();

			toolbar = new Toolbar(comptoolbar, this);

			latestVersion = getLatestVersion();			// neueste MTBS-Version ermitteln
			if (updatemoeglich(Global.versioncode, latestVersion)) {
			    final File f = new File(Global.strPfad + Global.lockdatei);
				Mlog.debug("Update moeglich...");
				if (showUpdate == null) {
					showUpdate = new UpdateDialog();
					showUpdate.on(480, 480);
				} else
					showUpdate.on();
				if (showUpdate.isErgebnisOK()) {
					imUpdate = true;
					showUpdate.launch();
				    if (f.exists()) 					// zuerst Lockdatei löschen...
				    	f.delete();
					app.close();						// ordentlich, ohne Exception beenden!
					break doit;
				}
			}

			setTour(config.getString("tour"));  		// beim Starten gleich die Tourenauswahl bringen

			if (newkonfig.isShowInfo()) 
				showStartInfo();						// eventuell Startinfo anzeigen

			if (newkonfig.isShowmap() && osmv == null)	// OSM initialisieren
				initMap();

			if (Global.autoStart)
				doStart();
			
			if (Global.fullscreen)
				switchFullscreen(true);
			
		} catch (Exception e) {
			Mlog.ex(e);
		}

		return parent;
	}

	/**
	 * Vergleicht die Versionsinfo. Verglichen werden nur die Ziffern
	 * als Gleitkommazahl. zusätzliche Zeichen werden entfernt.
	 * @param aktVersion      aktuelle Version
	 * @param neuesteVersion  neueste Version (auf Server)
	 * @return true: Update möglich
	 */
	private boolean updatemoeglich(String aktVersion, String neuesteVersion) {
		float aktVersNr, neuVersNr;
		Mlog.debug("<aktVersion>" + aktVersion + "<neuesteVersion>" + neuesteVersion);
		if (Platform.isWindows()) {
			try {
				Pattern pattern = Pattern.compile("(\\d*\\.\\d*)(.*)");
				Matcher matchakt = pattern.matcher(aktVersion);
				Matcher matchneu = pattern.matcher(neuesteVersion);
				if(matchakt.matches()) {
					if(matchneu.matches()) {
						aktVersNr = new Float(matchakt.group(1));
						neuVersNr = new Float(matchneu.group(1));
						if (neuVersNr > aktVersNr)
							return true;
					} else {
						Mlog.debug("kein Update moeglich...");
						return false;
					}
				} else
					return false;

			} catch (Exception e) {
				Mlog.error("Fehler beim Vergleich der Versionen!");
				Mlog.ex(e);			
			}
		}
		Mlog.debug("kein Update moeglich...");
		return false;
	}

	/**
	 * Bei Linux muss das Video verschoben werden, bei Windows wird es automatisch zentriert auf dem Composite!
	 * @param modus		Anzeigemodus (s.u.)
	 * @param x			kommt aus showClientArea()
	 * @param y			kommt aus showClientArea()
	 * @return Verschiebung (horizontal)
	 */
	int centervideo(int modus, int x, int y) {
		int vkonst = 100;
		
		if (Platform.isLinux()) {
			return (vkonst + x - y);
		} else
			return (0);
	}
	
	/**
	 * showClientArea: aktualisiert die komplette Ansicht (z. B. bei Redraw)
	 * Minimalauflösung = 1266*720 Pixel : clientarea= 1250*664; appminhoehe=720, appminbreite=1266
	 */
	public void showClientArea() {
		int weite = aktcr.width - (appminbreite - 16);
		//Mlog.debug("weite: "+weite + " - aktcr: " + aktcr);
		int weite2 = weite / 2;
		int weite4 = weite2 / 2;
		int hoehe = aktcr.height - (appminhoehe - 8);
		int hoehe2 = hoehe / 2;
		int hoehe4 = hoehe2 / 2;
		int tbhoehe = 0;
		int ah = 112;	// Höhe der Anzeigedisplays
		int aw = 156;	// Weite der Anzeigedisplays
		int vmx = 0;	// hor. Verschiebung wird unter Linux verwendet, da dort das Video linksbündig angezeigt wird

		vmx = centervideo(anzmodus,(int)(weite/1.7),hoehe2);
		comptoolbar.setBounds(0, 0, aktcr.width, 40);
		cmbfahrer.setBounds(920+weite, 6, 100, 23);
		cmbwind.setBounds(1030+weite, 6, 110, 23);
		sldvideo.setBounds(600+weite, 6, 310, 23);
		lblLogo.setBounds(1144+weite, -2, 104, 40);
		lblTour.setBounds(390+weite, 9, 200, 23);
		
		if (Global.fullscreen) // dann keinen Platz für die Toolbar lassen:
			tbhoehe = 0;
		else
			tbhoehe = 40;

		if (aktstatus == Status.laeuft)
			showOVD(0, 0, 0, 0, "");	// sonst wird der OVD-Text dauernd angezeigt!
		
		switch (anzmodus) {
		case 1:	// Anzeigefelder unten, Höhenprofilanzeige klein, Video klein
			compvideo.setBounds(145+vmx, tbhoehe, 960+weite-vmx, 480+hoehe2-tbhoehe);
			if (newkonfig.isShowmap()) {
				compprofil.setBounds(0, 480+hoehe2, 626+weite2, 120+hoehe2);
				compMap.setBounds(626+weite2, 485+hoehe2, 312+weite2, 109+hoehe2);
				compInfo.setBounds(938+weite, 480+hoehe2, 312, 110+hoehe2);								
//				lblGang.setBounds(53, 10+hoehe4, 206, 150);
				lblGang.setBounds(120, hoehe4, 140, 130);
				// Die Karte auch resizen:
				if (osmv != null)
					osmv.setBounds(0,0,compMap.getSize().x,compMap.getSize().y);
			} else {
				compprofil.setBounds(0, 480+hoehe2, 938+weite2, 120+hoehe2);
				compMap.setBounds(0, 0, 0, 0);
				compInfo.setBounds(938+weite2, 480+hoehe2, 312+weite2, 110+hoehe2);				
//				lblGang.setBounds(53+weite4, 10+hoehe4, 206, 150);
				lblGang.setBounds(120+weite4, hoehe4, 140, 130);
			}
			compa1.setBounds(1+weite2, 597 + hoehe, aw, ah);  
			compa2.setBounds(157+weite2, 597 + hoehe, aw, ah);
			compa3.setBounds(313+weite2, 597 + hoehe, aw, ah);
			compa4.setBounds(469+weite2, 597 + hoehe, aw, ah);
			compa5.setBounds(625+weite2, 597 + hoehe, aw, ah);
			compa6.setBounds(781+weite2, 597 + hoehe, aw, ah);
			compa7.setBounds(937+weite2, 597 + hoehe, aw, ah);
			compa8.setBounds(1093+weite2, 597 + hoehe, aw, ah);
			setOvddata(false);
			break;

		case 2:	// Anzeigefelder unten, ohne Höhenprofilanzeige, ohne Map, ohne Schaltung, Video groß
			compvideo.setBounds(0+vmx, tbhoehe, 1250+weite-vmx, 600+hoehe-tbhoehe);			
			compprofil.setBounds(0, 0, 0, 0);
			compInfo.setBounds(0, 0, 0, 0);
			compMap.setBounds(0, 0, 0, 0);
			compa1.setBounds(1+weite2, 597 + hoehe, aw, ah);
			compa2.setBounds(157+weite2, 597 + hoehe, aw, ah);
			compa3.setBounds(313+weite2, 597 + hoehe, aw, ah);
			compa4.setBounds(469+weite2, 597 + hoehe, aw, ah);
			compa5.setBounds(625+weite2, 597 + hoehe, aw, ah);
			compa6.setBounds(781+weite2, 597 + hoehe, aw, ah);
			compa7.setBounds(937+weite2, 597 + hoehe, aw, ah);
			compa8.setBounds(1093+weite2, 597 + hoehe, aw, ah);
			setOvddata(false);
			break;

		case 3:	// Anzeigefelder oben, ohne Höhenprofilanzeige, ohne Map, ohne Schaltung, Video groß
			compvideo.setBounds(0+vmx, 112+tbhoehe, 1250+weite, 600+hoehe-tbhoehe);			
			compprofil.setBounds(0, 0, 0, 0);
			compInfo.setBounds(0, 0, 0, 0);
			compa1.setBounds(1+weite2, tbhoehe, aw, ah);
			compa2.setBounds(157+weite2, tbhoehe, aw, ah);
			compa3.setBounds(313+weite2, tbhoehe, aw, ah);
			compa4.setBounds(469+weite2, tbhoehe, aw, ah);
			compa5.setBounds(625+weite2, tbhoehe, aw, ah);
			compa6.setBounds(781+weite2, tbhoehe, aw, ah);
			compa7.setBounds(937+weite2, tbhoehe, aw, ah);
			compa8.setBounds(1093+weite2, tbhoehe, aw, ah);
			setOvddata(false);
			break;

		case 4:		// Anzeigefelder seitlich, Höhenprofilanzeige- Map- und Schaltung: klein, Video groß
			compvideo.setBounds(0+vmx, tbhoehe, 1250+weite-vmx/2, 592+hoehe-tbhoehe);			
			if (newkonfig.isShowmap()) {
				compprofil.setBounds(0, 592+hoehe, 626+weite2, 120);
				compMap.setBounds(626+weite2, 595+hoehe, 312+weite2, 110);
				compInfo.setBounds(938+weite, 592+hoehe, 312, 130);								
				lblGang.setBounds(120, 0, 140, 130);
				// Die Karte auch resizen:
				if (osmv != null)
					osmv.setBounds(0,0,compMap.getSize().x,compMap.getSize().y);
			} else {
				compprofil.setBounds(0, 592+hoehe, 938+weite2, 120);
				compMap.setBounds(0, 0, 0, 0);
				compInfo.setBounds(938+weite2, 592+hoehe, 312+weite2, 130);
				lblGang.setBounds(120+weite4, 0, 140, 130);
			}
			compa1.setBounds(0, tbhoehe, aw, ah);
			compa2.setBounds(0, 112+tbhoehe, aw, ah);
			compa3.setBounds(0, 224+tbhoehe, aw, ah);
			compa4.setBounds(0, 336+tbhoehe, aw, ah);
			compa5.setBounds(1094+weite, tbhoehe, aw, ah); 
			compa6.setBounds(1094+weite, 112+tbhoehe, aw, ah);
			compa7.setBounds(1094+weite, 224+tbhoehe, aw, ah);
			compa8.setBounds(1094+weite, 336+tbhoehe, aw, ah);
			setOvddata(false);
			break;
	
		case 5:	// Anzeigefelder seitlich, Höhenprofilanzeige- Map- und Schaltung: groß, Video klein
			compvideo.setBounds((1250-960)/2+vmx, tbhoehe, 960+weite-vmx, 480+hoehe2-tbhoehe);			
			if (newkonfig.isShowmap()) {
				compprofil.setBounds(0, 480+hoehe2, 626+weite2, 235+hoehe2);
				compMap.setBounds(626+weite2, 485+hoehe2, 312+weite2, 220+hoehe2);
				compInfo.setBounds(938+weite, 480+hoehe2, 312, 225+hoehe2);								
				lblGang.setBounds(120, 40+hoehe4, 140, 130);
				// Die Karte auch resizen:
				if (osmv != null)
					osmv.setBounds(0,0,compMap.getSize().x,compMap.getSize().y);
			} else {
				compprofil.setBounds(0, 480+hoehe2, 938+weite2, 235+hoehe2);
				compMap.setBounds(0, 0, 0, 0);
				compInfo.setBounds(938+weite2, 480+hoehe2, 312+weite2, 225+hoehe2);
				lblGang.setBounds(120+weite4, 40+hoehe4, 140, 130);
			}
			compa1.setBounds(0, tbhoehe, aw, ah);
			compa2.setBounds(0, 112+tbhoehe, aw, ah);
			compa3.setBounds(0, 224+tbhoehe, aw, ah);
			compa4.setBounds(0, 336+tbhoehe, aw, ah);
			compa5.setBounds(1094+weite, tbhoehe, aw, ah);
			compa6.setBounds(1094+weite, 112+tbhoehe, aw, ah);
			compa7.setBounds(1094+weite, 224+tbhoehe, aw, ah);
			compa8.setBounds(1094+weite, 336+tbhoehe, aw, ah);
			setOvddata(false);
			break;

		case 6:			// Anzeigefelder seitlich, Höhenprofilanzeige- Map- und Schaltung: groß, Video ganz klein, GE/OSM groß
			if (newkonfig.isShowmap()) {
				compvideo.setBounds(622+weite2, 480+hoehe2+vmx/10, 312+weite2, 235+hoehe2);
				compprofil.setBounds(0, 480+hoehe2, 626+weite2, 235+hoehe2);
				if (isGemodus()) {
					compMap.setBounds(938+weite, 480+hoehe2, 312, 225+hoehe2);
					compInfo.setBounds(157, tbhoehe, 936+weite, 480+hoehe2-tbhoehe);													
				} else {
					compMap.setBounds(157, tbhoehe, 936+weite, 480+hoehe2-tbhoehe);
					compInfo.setBounds(938+weite, 480+hoehe2, 312, 225+hoehe2);													
				}
				lblGang.setBounds(120, 40+hoehe4, 140, 130);

				// Die Karte auch resizen:
				if (osmv != null)
					osmv.setBounds(0,0,compMap.getSize().x,compMap.getSize().y);
			} else {
				compvideo.setBounds(932+weite2, 480+hoehe2+vmx/10, 312+weite2, 235+hoehe2);
				compprofil.setBounds(0, 480+hoehe2, 936+weite2, 235+hoehe2);
				compMap.setBounds(0, 0, 0, 0);
				compInfo.setBounds(160, tbhoehe, 936+weite, 480+hoehe2-tbhoehe);			
				lblGang.setBounds(400+weite2, 95+hoehe2, 140, 130);
			}
			compa1.setBounds(0, tbhoehe, aw, ah);
			compa2.setBounds(0, 112+tbhoehe, aw, ah);
			compa3.setBounds(0, 224+tbhoehe, aw, ah);
			compa4.setBounds(0, 336+tbhoehe, aw, ah);
			compa5.setBounds(1094+weite, tbhoehe, aw, ah);
			compa6.setBounds(1094+weite, 112+tbhoehe, aw, ah);
			compa7.setBounds(1094+weite, 224+tbhoehe, aw, ah);
			compa8.setBounds(1094+weite, 336+tbhoehe, aw, ah);
			setOvddata(false);
			break;

		case 7:			// Video groß, kein GE, keine Anzeigefelder, kein Höhenprofil, OVD-Data Anzeige
			compvideo.setBounds(0, tbhoehe, 1250+weite, 712+hoehe-tbhoehe);			
			compprofil.setBounds(0, 0, 0, 0);
			compMap.setBounds(0, 0, 0, 0);
			compInfo.setBounds(0, 0, 0, 0);			
			compa1.setBounds(0, 0, 0, 0);
			compa2.setBounds(0, 0, 0, 0);
			compa3.setBounds(0, 0, 0, 0);
			compa4.setBounds(0, 0, 0, 0);
			compa5.setBounds(0, 0, 0, 0);
			compa6.setBounds(0, 0, 0, 0);
			compa7.setBounds(0, 0, 0, 0);
			compa8.setBounds(0, 0, 0, 0);
			lblGang.setBounds(0, 0, 0, 0);
			setOvddata(true);
			break;

		default:
			break;
		}
        frame.setBounds(0, 0, compprofil.getSize().x, compprofil.getSize().y-4);
        
        sldvideo.setFocus();	// sonst blinkt der Cursor in der Rennliste!
		compHaupt.redraw();
		compInfo.redraw();
		compMap.redraw();
	}

	/**
	 * "Ansicht" weiterschalten auf einen anderen Modus.
	 */
	public void switchComposites() {
		anzmodus = (anzmodus > 6) ? 1 : anzmodus+1;
		showClientArea();
	}
	
	/**
	 * Hier werden die Anzeigetafeln initialisiert.
	 * Vorsicht: Bei Änderungen müssen z. B. auch die entsprechenden .show-Aufrufe geändert werden.
	 */
	public void InitAnzeigetafeln() {
		atPower.InitAnzeigetafel(compa1, Messages.getString("Rsmain.power_w"), "Avg.", "Calc.", Messages.getString("Rsmain.power_ist"), Messages.getString("Rsmain.tour"), rahmenfarbe);  
		atPuls.InitAnzeigetafel(compa2, Messages.getString("Rsmain.puls"), "Avg.", "Max.", "", Messages.getString("Rsmain.tour"), rahmenfarbe);   
		atKurbel.InitAnzeigetafel(compa3, Messages.getString("Rsmain.kurbel_1min"), "Avg.", Messages.getString("Rsmain.gangkurz"), "Max.", Messages.getString("Rsmain.tour"), rahmenfarbe);   
		atGeschw.InitAnzeigetafel(compa4, Messages.getString("Rsmain.speed_kmh"), "Avg.", "Max.", "Wind", Messages.getString("Rsmain.tour"), rahmenfarbe);  
		atStrecke.InitAnzeigetafel(compa5, Messages.getString("Rsmain.strecke"), Messages.getString("Rsmain.tour"), Messages.getString("Rsmain.rest"), "GPS", Messages.getString("Rsmain.zielpulsstrecke"), rahmenfarbe);  
		atSteigung.InitAnzeigetafel(compa6, Messages.getString("Rsmain.steigung"), Messages.getString("Rsmain.hm"), Messages.getString("Rsmain.rest"), Messages.getString("Rsmain.hoehe"), Messages.getString("Rsmain.tour"), rahmenfarbe);   
		atZeit.InitAnzeigetafel(compa7, Messages.getString("Rsmain.trainingszeit"), Messages.getString("Rsmain.tour"), Messages.getString("Rsmain.rest"), Messages.getString("Rsmain.uhrzeit"), "", rahmenfarbe);  
		atKcal.InitAnzeigetafel(compa8, Messages.getString("Rsmain.energie_kcal"), Messages.getString("Rsmain.dynamik"), Messages.getString("Rsmain.lfakt"), "", "", rahmenfarbe);
	}

	/**
	 * Hier werden die Farben der Anzeigetafeln angepasst.
	 * Vorsicht: Bei Änderungen müssen z. B. auch die entsprechenden .show-Aufrufe geändert werden.
	 */
	public static void SetColorAnzeigetafeln() {
		atPower.SetColorAnzeigetafel(compa1, rahmenfarbe);  
		atPuls.SetColorAnzeigetafel(compa2, rahmenfarbe);   
		atKurbel.SetColorAnzeigetafel(compa3, rahmenfarbe);   
		atGeschw.SetColorAnzeigetafel(compa4, rahmenfarbe);  
		atStrecke.SetColorAnzeigetafel(compa5, rahmenfarbe);  
		atSteigung.SetColorAnzeigetafel(compa6, rahmenfarbe);   
		atZeit.SetColorAnzeigetafel(compa7, rahmenfarbe);  
		atKcal.SetColorAnzeigetafel(compa8, rahmenfarbe);
	}

	/**
     * Initialisiert den VLC-Player.
     */
	private void initPlayer() {
        libvlc = LibVlc.SYNC_INSTANCE; 
        vlcex = new libvlc_exception_t();
	}

	/**
	 * Diese Methode wird aufgerufen, wenn der timechanged-Event des VLC-Players (ca. 300 ms?)
	 * passiert.
	 */
    LibVlcCallback timechanged = new LibVlcCallback()
    {
        public void callback(libvlc_event_t libvlc_event_t, Pointer pointer)
        {
            libvlc_event_t.event_type_specific.setType(LibVlc.media_player_time_changed.class);  
            LibVlc.media_player_time_changed timeChanged = (LibVlc.media_player_time_changed) libvlc_event_t.event_type_specific
                .readField("media_player_time_changed");  
            newvidtime = timeChanged.new_time / 1000;
        	rtSek = new Date().getTime() / 1000;
			rtSek -= rtStartSek;

            if (rtSek != lastrtSek) { // alle Sekunden ...
         	   	DoSimulation(newvidtime);
         	   	lastrtSek = rtSek;
            }

        }
    };

    /**
     * Setzt den Videoplayer-Eventmanager für den Timer-Event und initialisiert Descriptor und medialplayer
     * für die entsprechende Videodatei. 
     * @param vlc         VLC
     * @param videodatei  Videodateiname
     */
	private void setPlayer(LibVlc vlc, String videodatei) {
		try {
 		    String utf8dateiname = URLEncoder.encode(videodatei, "UTF-8");	
 		    utf8dateiname = utf8dateiname.replaceAll("\\+", " "); 			

			mediaDescriptor = libvlc.libvlc_media_new_location(vlcinst, "file:///" + utf8dateiname);
			mediaplayer = libvlc.libvlc_media_player_new_from_media(mediaDescriptor);

			LibVlcEventManager mediaInstanceEventManager = libvlc.libvlc_media_player_event_manager(mediaplayer);
			libvlc.libvlc_event_attach(
	                mediaInstanceEventManager,
	                267,  //media_player_time_changed! kann sich evtl. ändern in anderen versionen!
	                timechanged,
	                null);	
		} catch (Exception e) {
	        Mlog.error(Messages.getString("Rsmain.vlc_nicht_init"));  
	        setVlc_ein(false);
		}
    }

	/**
	 * Caption entsprechend verwendeter Ergometer-Schnittstelle ausgeben.
	 */
	private void RefreshInfos() {
		if (thisTrainer.hatverbindung() == Trainer.kommunikation.seriell) {
			if (Global.ergoVersion.isEmpty()) Global.ergoVersion = thisTrainer.talk(Trainer.Commands.version, 0.0);
            // Cockpitversion ausgeben im Fenstertitel
    		sShell.setText(Global.version+Messages.getString("Rsmain.ergo_dp")+Global.ergoVersion+" - "+thisTrainer.getErgoCom());		            
		} else if (thisTrainer.hatverbindung() == Trainer.kommunikation.netzwerk) {
			if (Global.ergoVersion.isEmpty()) Global.ergoVersion = thisTrainer.talk(Trainer.Commands.version, 0.0);
            // Protokollversion ausgeben im Fenstertitel
    		sShell.setText(Global.version+Messages.getString("Rsmain.ergo_protokoll")+Global.ergoVersion+Messages.getString("Rsmain.lan"));		        		    
		} else if (thisTrainer.hatverbindung() == Trainer.kommunikation.usb) {
    		sShell.setText(Global.version+Messages.getString("Rsmain.rollentrainer")+Global.ergoVersion+Messages.getString("Rsmain.usb"));		        		    
		} else if (thisTrainer.hatverbindung() == Trainer.kommunikation.ant) {
			if (Global.ergoVersion.isEmpty()) Global.ergoVersion = thisTrainer.talk(Trainer.Commands.version, 0.0);
			sShell.setText(Global.version+Messages.getString("Rsmain.rollentrainer")+" SNR: "+Global.ergoVersion+" - ANT+");		        		    
		} else
			if (thisTrainer.getTrainerModell().startsWith("ant+"))
		   		sShell.setText(Global.version+Messages.getString("Rsmain.rollentrainer"));		        		  
			else
				sShell.setText(Global.version+Messages.getString("Rsmain.offlinemodus"));		        		  

        // Haben sich die Konfigurationsdaten geändert? 
        if (biker.isChanged()) {
            biker.setChanged(false);
    	    drate = 1.0;
        }
        
		atKurbel.show(2, thisTrainer.getGang()+"", 0);	
		
		// wurde Onlinerennen aufgerufen ? (bei CSV-Rennen die Anztafeln anpassen)
		if (wettkampf.isChanged()) {
			if (wettkampf.isCsvAktiv()) {
				String gegner = Messages.getString("Rsmain.gegn_p"); 
				atPower.Label(4, gegner);
				atPuls.Label(4, gegner);
				atKurbel.Label(4, gegner);
				atGeschw.Label(4, gegner);
				atStrecke.Label(4, gegner);
				atSteigung.Label(4, gegner);
				atKcal.Label(4, gegner);
				
				sldvideo.setEnabled(false);
			} else {
				String tour = Messages.getString("Rsmain.tour"); 
				atPower.Label(4, tour);
				atPuls.Label(4, tour);
				atKurbel.Label(4, tour);
				atGeschw.Label(4, tour);
				atStrecke.Label(4, Messages.getString("Rsmain.zielpulsstrecke")); 
				atSteigung.Label(4, tour); 
				atKcal.Label(4, ""); 
				// if (wettkampf.isAktiv() || wettkampf.isIbl())
				if (wettkampf.isAktiv())
					sldvideo.setEnabled(false);
			}
			wettkampf.setChanged(false);
		}
		
		// Wurde Mapanzeige umgeschaltet in der Konfiguration?
		if (newkonfig.isMapchg() && !isImDialog()) {
			newkonfig.setMapchg(false);
			showClientArea();
		}
	}

	/**
	 * Gibt den übergebenen String als Marquee-Ausgabe auf dem Video aus.
	 * You can enforce the marquee position on the video (0=center, 1=left,
     *     2=right, 4=top, 8=bottom, you can also use combinations of these
     *     values, eg 6 = top-right).
     * Opacity (inverse of transparency) of overlayed text. 0 = transparent,
     *     255 = totally opaque. 
	 * @param position: --marq-position={0 (Center), 1 (Left), 2 (Right), 4 (Top), 8 (Bottom), 5 (Top-Left), 6 (Top-Right), 9 (Bottom-Left), 10 (Bottom-Right)}
	 * @param opacity: Transparenz des Textes: --marq-opacity=<integer [0 .. 255]>
	 * @param size:  Größe in Pixeln
	 * @param time:  Anzeigezeit
	 * @param sout:  Text der ausgegeben wird
	 */
	private static void ovdoutput(int position, int opacity, int size, int time, String sout) {
    	if ((libvlc != null) && isVlc_ein()) {
			libvlc.libvlc_video_set_marquee_int(mediaplayer, LibVlc.libvlc_marquee_Enable, 0); // Marquee-text initialisieren
			if (time > 0)
				libvlc.libvlc_video_set_marquee_int(mediaplayer, LibVlc.libvlc_marquee_Timeout, time);

			libvlc.libvlc_video_set_marquee_int(mediaplayer, LibVlc.libvlc_marquee_Position, position);
			libvlc.libvlc_video_set_marquee_int(mediaplayer, LibVlc.libvlc_marquee_Opacity, opacity);
			libvlc.libvlc_video_set_marquee_int(mediaplayer, LibVlc.libvlc_marquee_Size, size);
			libvlc.libvlc_video_set_marquee_string(mediaplayer, LibVlc.libvlc_marquee_Text, sout);
			libvlc.libvlc_video_set_marquee_int(mediaplayer, LibVlc.libvlc_marquee_Enable, 1); 
		}
	}
		
	/**
	 * Wird vom Mediaplayer (timeChanged) regelmäsig (alle Sekunden) aufgerufen und führt die eigentliche
	 * Simulation durch.
	 * Wenn der Mediaplayer nicht vorhanden ist, wird die Simulation über einen Timer aufgerufen.
	 * @param sek  Sekunden seit Start
	 */
	private void DoSimulation(final long sek) {
		Runnable r = new Runnable(){
         		public void run(){
         			Double drpm = 0.0;
         			double dwork = 0.0;
         			double p = 0.0;
         			double p1 = 0.0;
         			double pact = 0.0;
         			double steigung = 0.0;
         			//double steigung1 = 0.0;
         			//double dstrecke = 0.0;
         			double dhoehe = 0.0;
         			Double puls = 0.0;
         			double dvtrainer = 0.0;
         			double geschw = 0.0;
         			double geschw1 = 0.0;
         			double dnormtritt = biker.getDynRPMNormal();
         			double dwiegetritt = biker.getDynRPMWiege();
         			double dhm1 = 0.0;
         			GegnerZP aktgzp = new GegnerZP();
         			String sgetpower = new String();
         			Marker marker = new ValueMarker(0.0);
         			String outstr;         			
        			TrkPt akttrkpkt = gettrackpoint(sek);
        			StyleRange styleRange = new StyleRange();
        			int pulsFarbe = 0;
        			int zeitFarbe = 0;
        			int geschwFarbe = 0;
        			int rpmFarbe = 0;
        			int powerFarbe = 0;

        			Global.aktGPSPunkt = akttrkpkt.getIndex();
        			if (isCsvrennen()) {
        				aktgzp = getGegnerZp(rtSek);
        			}
         			geschw = new Double(akttrkpkt.getV_kmh());
            		geschw1 = geschw;
            		geschw = (double) (geschw * (thisTrainer.getGang() / 10.0 + 0.5));
            		Geschwindigkeit.setWert(geschw);
            		if (newkonfig.isaveraging())
            			steigung = new Double(akttrkpkt.getSteigungAv_proz());
            		else
            			steigung = new Double(akttrkpkt.getSteigung_proz());
         			//steigung1 = steigung;
         			dstrecke = akttrkpkt.getAbstand_m() + dStreckenSumme;
         			dhm1 = akttrkpkt.getHm_m();		// Streckenhoehenmeter zum 1. Punkt
         			atStrecke.show(0, zfk1.format(dstrecke/1000.0), 0); 
         			atStrecke.show(2, zfk1.format((gesamtstrecke + dStreckenSumme - dstrecke)/1000.0), 0); 
         			atStrecke.show(3, akttrkpkt.getIndex()+"", 0);  
         			dhoehe = akttrkpkt.getHoehe();
     				atSteigung.show(3, zfk0.format(dhoehe), 0);
     				atSteigung.show(4, zfk1.format(akttrkpkt.getSteigung_proz()), 0);

        			// Onlinerennen kenntlich machen		
        			if (wettkampf.isZprace() || wettkampf.isAktiv() || wettkampf.isCsvAktiv()) {
        				//atZeit.setcolor(display.getSystemColor(SWT.COLOR_GREEN));
        				if (akttrkpkt.getIndex() >= zielGPSPunkt)
        					zeitFarbe = SWT.COLOR_RED;
        				else
        					zeitFarbe = SWT.COLOR_GREEN;
        			} 
        			         			
    				atZeit.show(0, tfmt.format(trainingsmillisek), zeitFarbe);	// Trainingszeit anzeigen
         			atZeit.show(1, tfmt1.format(sek * 1000), 0);	
         			atZeit.show(2, tfmt1.format((gesamtsek-sek > 0 ? gesamtsek-sek : 0) * 1000), 0); // Restzeit anzeigen
         			atZeit.show(3, uhrzeitfmt.format(new Date().getTime()), 0);	// Uhrzeit anzeigen
        			
         			try {       			
         				// Aktualisierung des Schiebereglers
        	        	if ((libvlc != null) && isVlc_ein()) {
         					Float fpos = new Float(libvlc.libvlc_media_player_get_position(mediaplayer)*slidermax);
         					sldvideo.setSelection(fpos.intValue());
         				} else
         					if (gesamtsek > 0)
         						sldvideo.setSelection((int) (timersek * slidermax / gesamtsek));
         			
         				// Ergometer/Rollentrainer: Puls und RPM ermitteln
         				if (thisTrainer.hatverbindung() != Trainer.kommunikation.keine || thisTrainer.getTrainerModell().startsWith("ant+")) {
         					if (!demomodus) { // Kommunikation vorhanden?  
         						trainerpuls = thisTrainer.talk(Trainer.Commands.puls, p);
         						ergorpm = thisTrainer.talk(Trainer.Commands.rpm, p);
                 				//Mlog.debug("puls = "+trainerpuls+" rpm = "+ergorpm);
         						if (trainerpuls.isEmpty())
         							puls = 0.0;
         						else
         							puls = new Double(trainerpuls);
         						
         						if (ergorpm.isEmpty())
         							drpm = 0.0;
         						else
         							drpm = new Double(ergorpm);

         					 }
         					//Mlog.debug("Debug! Puls: "+puls+" - RPM: "+drpm);
         				} else {
         					puls = demopuls;
         					drpm = demorpm;
         				}
         				// ANT+ Sensoren für Puls und Cadence abfragen
 						if (libant != null) {
 							if (libant.isEin() && (libant.getaktTHr() == LibAnt.THr.antppuls) && (libant.getPuls() > 0))
 								puls = (double) libant.getPuls();
 							
 							if (libant.isEin() && (libant.getaktTCadence() != LibAnt.TCadence.notset) && libant.getRpm() > 0)
 								drpm = (double) libant.getRpm();
 						}
 						// Rückgabewerte bei RPM und Puls im Fall von Übertragungsfehlern begrenzen:
 						puls = checkWertMaxMin(puls, Global.maxPuls, 0.0);       					  		  
 						drpm = checkWertMaxMin(drpm, Global.maxRPM, 0.0);

 						Puls.setWert(puls);
         				Kurbel.setWert(drpm);      				
         				
         				atPuls.show(1, zfk0.format(Puls.getAverage()), 0);	// Mittelwert ausgeben
         				atPuls.show(2, zfk0.format(Puls.getMaxwert()), 0);	// Maximalwert ausgeben
         				
         				// auf Autostopp überprüfen
         				if (!Global.noAutoStop) {
         					if ((aktstatus != Status.autostopped) && !demomodus && !wirdgetreten(drpm)) {
         						videopause();
         						atZeit.show(0, Messages.getString("Rsmain.pause1"), 0);
         						aktstatus = Status.autostopped;
         						timer1.cancel();  
         						timer1 = new Timer();
         						// Starte Timer für regelmäsige Aufgaben (z. B. Autostart) nach 1 Sek. und Aufruf alle Sekunden (vorher: 2s)
         						timer1.schedule(new PeriodTask(), 1000, 1000);
         					} else {
         						if (timer1 != null && wirdgetreten(drpm))		// neu Prüfung, ob wieder getreten wird!
         							timer1.cancel();  
         						else
         							atZeit.show(0, Messages.getString("Rsmain.pause1"), 0);
         					}
         				}

     					// Höhenmeteranzeige (nur wenn getreten wird und nicht beim ersten Punkt
             			if (wirdgetreten(drpm) && dletztehoehe != 0.0 && akttrkpkt.getIndex() > 1) {
             				if (dhoehe > dletztehoehe)
             					dhm += dhoehe - dletztehoehe;
            			}
         				dletztehoehe = dhoehe;
         				atSteigung.show(1, zfk0.format(dhm), 0);
         				atSteigung.show(2, zfk0.format(gesamthm - dhm1), 0);

     					// Dynamik-Modus: Videogeschwindigkeit ändern
         				if (newkonfig.isDynamik() && !demomodus && (aktstatus != Status.autostopped)) {
         					//Mlog.debug("Dynamik: Ein...");
         					if (thisTrainer.hatverbindung() == Trainer.kommunikation.usb || 
         							thisTrainer.hatverbindung() == Trainer.kommunikation.ant ||
                 					(thisTrainer.hatverbindung() == Trainer.kommunikation.keine && thisTrainer.getTrainerModell().equals("ant+gsc10")) ||		// mech. Rolle mit ANT+ Cadence/Speed-Sensor?
         							thisTrainer.getTrainerModell().equals("cyclus2")) { // USB/ANT (Rollentrainer) oder Cyclus2 native vorhanden?  
         						// Rollentrainer? -> abh. von Rollentrainergeschwindigkeit
         						atKurbel.setEnabled(2, false);	// Gangschaltung abschalten beim Rollentrainer
         						trainer_v = thisTrainer.talk(Trainer.Commands.geschwindigkeit, 0);
         						if (trainer_v.isEmpty())
         							dvtrainer = 0.0;
         						else
         							dvtrainer = new Double(trainer_v);

         						if (geschw > 0) {
             						drate = dvtrainer / geschw;
             						//drate = drate > tacxvidmax ? tacxvidmax : drate;
             						drate = checkWertMaxMin(drate, tacxvidmax, 0.0);
         							if (drate < 0.1)  // untere Grenze
         								drate = 0.1;
             						geschw = dvtrainer;  // es wird die aktuell gefahrene Geschw. angezeigt!
          							if (Math.abs(drate-oldrate) > 0.05) { // Änderung größer 5% ?
         								if ((libvlc != null) && isVlc_ein())
         									libvlc.libvlc_media_player_set_rate(mediaplayer, (float) (drate * grate));
         								else { // GPX-Tour ohne Video?
         									Rescheduletimer(drate * grate);
         								}
         								oldrate = drate;
         							}
             					}
         						if (thisTrainer.getTrainerModell().equals("cyclus2")) {
         							// eingelegten Gang zeigen (aktuell nur hinten!)
         							String c2Gang = thisTrainer.talk(Trainer.Commands.gangHinten, 0);
         							if (!c2Gang.isEmpty()) {
         								int aktgang = new Integer(c2Gang);
         								aktgang = thisTrainer.mapCyclus2Gang(aktgang);
         								if (thisTrainer.getGang() != aktgang) {
         									thisTrainer.setGang(aktgang);
         									atKurbel.show(2, aktgang+"", 0);
         									if (aktgang >= 1 && aktgang <= 9)
         										lblGang.setImage(new Image(Display.getCurrent(), "g"+aktgang+".png"));  
         								}
         							}
         						}
         					} else { // abhängig von Anzahl Kurbelumdrehungen
         						if (dnormtritt > dwiegetritt) {
     								drate = drpm / dnormtritt;
         							if ((drpm > dnormtritt) || (drpm > (dwiegetritt * 1.3))) {  // im "Normaltrittbereich"
         								//drate = drpm / dnormtritt;
     			        				atSteigung.setcolor(rahmenfarbe);
         							}
         							if ((drpm > 0) && (drpm <= (dwiegetritt * 1.3))) {  // im "Wiegetrittbereich"
         								drate = drpm / dwiegetritt;
         			        			// Wiegetrittautomatik anzeigen		
         								atSteigung.setcolor(display.getSystemColor(SWT.COLOR_GREEN));
         							} 
         							if (drate < 0.1) { // untere Grenze
         								drate = 0.1;
         								atSteigung.setcolor(rahmenfarbe);
         							}
         							geschw *= drate;
         							// Mlog.debug("sek= "+sek+" rpm= "+drpm+" drate= "+drate+" grate= "+grate+" geschw= "+geschw+" dstrecke= "+dstrecke);
         							if (Math.abs(drate-oldrate) > 0.05) { // Änderung größer 5% ?
         								if ((libvlc != null) && isVlc_ein())
         									libvlc.libvlc_media_player_set_rate(mediaplayer, (float) (drate * grate));
         								else { // GPX-Tour ohne Video?
         									Rescheduletimer(drate * grate);
         								}
         								oldrate = drate;
         							}
         						}
         					}
         				}
         				
         				// TODO Geschwindigkeitsgrenzen wählbar machen über Konfiguration
         				geschw = checkWertMaxMin(geschw, Global.maxGeschw, 0.0);
         				if (geschw == geschw1)
         					geschwFarbe = 0;
         				else 
         					if (geschw1 > 0.0) {
         						if (Math.abs(1 - geschw / geschw1) < 0.15)
         							geschwFarbe = SWT.COLOR_GREEN;
         						else
         							if (Math.abs(1 - geschw / geschw1) < 0.35)
         								geschwFarbe = SWT.COLOR_YELLOW;
         							else
         								geschwFarbe = SWT.COLOR_RED;
         					}
         				atGeschw.show(0, zfk0.format(geschw), geschwFarbe);	
         	         	atGeschw.show(1, zfk1.format(Geschwindigkeit.getAverage()), 0);	// Mittelwert anzeigen
         	         	atGeschw.show(2, zfk0.format(Geschwindigkeit.getMaxwert()), 0);	// Maximalwert anzeigen
         	         	atGeschw.show(3, zfk0.format(vwind*3.6), 0);	// Windgeschwindigkeit anzeigen
         	         	
         	         	// Leistung berechnen oder aus GPX/TCX übernehmen
     	         		p1 = calcbikeleistung(geschw, steigung, vwind);
         	         	if (ldpx.isGpsleistung() && newkonfig.isTcxpower()) {
         	         		p = akttrkpkt.getLeistung();
         	         		p = p * biker.getLfakt();
         	         	} else
         	         		p = p1;
          				         					
         				p = checkWertMaxMin(p, biker.getMaxleistung(), biker.getMinleistung());
         				
         				// Pulsüberschreitung?
         				if (puls > biker.getMaxpuls()) {
         					if (wettkampf.isAktiv()) {
         						p = minLeistungOT;
         					} else {
             					p = minLeistung;
             					changeGang(1);         						
         					}
         					Mlog.debug("Pulsüberschreitung: Die Leistung wird auf " + p +" begrenzt!");
         				}
         				
         				// bei Tourende auf Minleistung gehen zum austrainieren        				
         				if (tourEnde) 
         					p = biker.getMinleistung();
         				
         				// Trainingsgerät: Leistung übermitteln
         				if (Global.comWatts) {
         					if (!(thisTrainer.hatverbindung() == Trainer.kommunikation.keine) && !demomodus) { // Kommunikation vorhanden?      
     							// Ist-Leistung ermitteln
     	             			sgetpower = thisTrainer.talk(Trainer.Commands.getpower, p + 0.5);	// für die Daums wird die akt. leistung nochmal übergeben
     	         				if (!sgetpower.isEmpty())
     	         					pact = new Double(sgetpower);         		

     	         				if (sek % 2 == 0) {
         							thisTrainer.talk(Trainer.Commands.leistung, p + 0.5);	// aufrunden, da bei Kommunikation cast auf int oder byte
         						}	
         					};
         				}
         				
         				// nur Rollentrainer: Steigung übermitteln
         				if ((thisTrainer.hatverbindung() == Trainer.kommunikation.usb || 
         						thisTrainer.hatverbindung() == Trainer.kommunikation.ant ||
         						thisTrainer.getTrainerModell().equals("cyclus2")) && !demomodus 
         						&& !thisTrainer.getTrainerModell().equals("wahookickr")
         						&& !thisTrainer.getTrainerModell().equals("antfec")) { 
         					// USB/ANT-Kommunikation, bzw Cyclus2 native vorhanden? - nicht beim KICKR und ANT+FEC!      
         					if (thisTrainer.hatverbindung() == Trainer.kommunikation.usb)	// dann wird Korrekturfaktor addiert (bei imagic und fortius)
         						tacxslopecorrect = 3.0;
         					
         					if (puls > biker.getMaxpuls()) {
             					steigung = 0.0;
             					changeGang(1);
             					Mlog.info("Pulsüberschreitung: Die Steigung wird auf 0% gesetzt!");
         					}	
             				steigung = checkWertMaxMin(steigung, biker.getMaxSteigung(), biker.getMinSteigung());
             				thisTrainer.talk(Trainer.Commands.steigung, steigung*biker.getLfakt() + tacxslopecorrect);  // Korrekturfaktor
             				if (!sgetpower.isEmpty())
             					p = new Double(sgetpower);
         				};

         				Leistung.setWert(p);
             			atSteigung.show(0, zfk1.format(steigung), 0);	
             			
             			// kCal berechnen
         				work += calcworkinws(drpm, p);
    	            	dwork = work/kcalfakt;
    	        		if (thisTrainer.getTrainertyp() == Trainer.typ.crosstrainer) // der Wirkungsgrad beim Crosstrainer ist ca. 1/3 höher
        	            	dwork *= 1.33;

     					atKcal.show(0, zfkw.format(dwork), 0);	
         				// RPM-einfärben
         				if (drpm > biker.getRpmrot() && drpm < biker.getRpmgelb()) {
         					rpmFarbe = SWT.COLOR_RED;
         				} else if (drpm >= biker.getRpmgelb() && drpm < biker.getRpmgruen()) {
         					rpmFarbe = SWT.COLOR_YELLOW;         					
         				} else if (drpm >= biker.getRpmgruen()) {
         					rpmFarbe = SWT.COLOR_GREEN;
         				} 
         				atKurbel.show(0, zfk0.format(drpm), rpmFarbe);
         				atKurbel.show(1, zfk0.format(Kurbel.getAverage()), 0);	// Mittelwert
         				atKurbel.show(3, zfk0.format(Kurbel.getMaxwert()), 0); // Maximalwert
         				
         				// Fitnesswert anzeigen 
/*
         				if (puls > 0) { 
         					Fitness.setWert(p / puls);
         					atKcal.show(1, zfk2.format(Fitness.getAverage()), 0);	// textfit.setText(zfk1.format(p / puls));
         				} 
*/
         				// Felder einfärben entsprechend der aktuellen Werte
         				if (Global.comWatts) {	// nur, wenn die Leistung übertragen wird!
         					if (p > biker.getPgruen() && p < biker.getPgelb()) {
         						powerFarbe = SWT.COLOR_GREEN;
         					} else if (p >= biker.getPgelb() && p < biker.getProt()) {
         						powerFarbe = SWT.COLOR_YELLOW;
         					} else if (p >= biker.getProt()) {
         						powerFarbe = SWT.COLOR_RED;
         					} 
         				} else
         					atPower.setcolor(rahmenfarbe); 
         				
         				// Puls: Farbanzeige und bei ZPT Zielpulsstrecke berechnen
         				if (wettkampf.isZprace()) {	// Zielpulstraining?
         					// Achtung: getPulsgelb() liefert maxpuls+1 !!!
         					if (puls < biker.getPulsgruen()) {
         						pulsFarbe = SWT.COLOR_RED;
         					} else if (puls >= biker.getPulsgruen() && puls <= biker.getPulsgruen()+1) {
         						pulsFarbe = SWT.COLOR_YELLOW;
     							// Zielpulsstreckenbehandlung:
     							if (lastIndex != akttrkpkt.getIndex() && akttrkpkt.getIndex() > 3) {	// Zielpulsstrecke erst ab dem 3. Punkt zählen
     								dzpstrecke += akttrkpkt.getAbstvorg_m();
     							}
         					} else if (puls > biker.getPulsgruen()+1 && puls < biker.getPulsgelb()-2) {
         						pulsFarbe = SWT.COLOR_GREEN;
     							// Zielpulsstreckenbehandlung:
     							if (lastIndex != akttrkpkt.getIndex() && akttrkpkt.getIndex() > 3) {
     								dzpstrecke += akttrkpkt.getAbstvorg_m();
     							}
         					} else if (puls >= biker.getPulsgelb()-2 && puls <= biker.getPulsgelb()-1) {
         						pulsFarbe = SWT.COLOR_YELLOW;
         						// Zielpulsstreckenbehandlung:
     							if (lastIndex != akttrkpkt.getIndex() && akttrkpkt.getIndex() > 3) {
     								dzpstrecke += akttrkpkt.getAbstvorg_m();
     							}
         					} else
         						pulsFarbe = SWT.COLOR_RED;
         				} else {		// Normaltraining
         					if (puls >= biker.getPulsgruen() && (puls < biker.getPulsgelb())) {
         						pulsFarbe = SWT.COLOR_GREEN;
         					} else if (puls >= biker.getPulsgelb() && (puls < biker.getPulsrot())) {
         						pulsFarbe = SWT.COLOR_YELLOW;
         					} else if (puls >= biker.getPulsrot()) {
          						pulsFarbe = SWT.COLOR_RED;
         					} else
         						pulsFarbe = 0;

         				}
     					atPuls.show(0, zfk0.format(puls), pulsFarbe);

         				// Einige Werte ausgeben
         				atPower.show(0, zfk0.format(new Double(p))+"", powerFarbe);	  
         				atPower.show(2, (p1 > -999.0) ? zfk0.format(new Double(p1)) : "-999", 0);  // berechnete Leistung ausgeben 
         				atPower.show(1, zfk0.format(new Double(Leistung.getAverage()))+"", 0);		// Mittelwert ausgeben 
         				atPower.show(3, zfk0.format(new Double(pact))+"", 0);						// Istwert ausgeben 
         				atKcal.show(2, zfk0.format(new Double(biker.getLfakt()*100.0))+"", 0);	  
             			atKcal.show(1, Messages.einaus(newkonfig.isDynamik()), 0);					// Dynamik

         				if (lastIndex != akttrkpkt.getIndex()) {	// neuer Punkt?
          					// OSM: Position anzeigen
         					if (newkonfig.isShowmap()) {
         						if (osmv == null)
         							initMap();
         						osm_koord.x = akttrkpkt.getLongitude();
         						osm_koord.y = akttrkpkt.getLatitude();
         						osmv.setCenterPosition(osmv.computePosition(osm_koord));
         						osmv.redraw();
         					}
         					if (gpxEdit != null)
         						gpxEdit.tblPosListe.setSelection((int) akttrkpkt.getIndex());
         				}

         				auswertung.schreibedaten(true, akttrkpkt.getIndex(), cmbwind.getText(), thisTrainer.getGang(), dwork, puls.doubleValue(), 
         						drpm.doubleValue(), p, geschw, dstrecke, steigung, dhoehe, akttrkpkt.getLatitude(), akttrkpkt.getLongitude());				   
         				
         				// Onlinetraining verwalten, Rangfolge und Positionsanzeige im Höhenprofil
         				if (wettkampf.isAktiv() || wettkampf.isVirtGeg() || wettkampf.isVirtZP()) {
         					if (lastIndex != akttrkpkt.getIndex() && akttrkpkt.getIndex() <= zielGPSPunkt) {
         						if (wettkampf.isAktiv()) {	// Onlinetraining und Netzwerktraining
         							long gpsPunkt = (akttrkpkt.getIndex() > startGPSPunkt) ? akttrkpkt.getIndex() : startGPSPunkt;
         							wettkampf.sendData(akttrkpkt.getLatitude(), akttrkpkt.getLongitude(), gpsPunkt, (int) vwind, 
         								thisTrainer.getGang(), (int) dwork, (int) puls.doubleValue(), drpm.doubleValue(), p, geschw, dstrecke, 
         								steigung, dhoehe, dzpstrecke);
         							wettkampf.receiveData();
         						} else {		// Rennen gegen virtuellen Gegner
         							wettkampf.calcVirtGegPos(trainingsmillisek / 1000, biker.getName(), puls.doubleValue(), drpm.doubleValue(), p, dstrecke, dzpstrecke);
         						}
         						txtGegner.setText(""); 
         						lblGang.setVisible(false);
         						plot.clearDomainMarkers(0);	// die alten Marker löschen
         						int i = 1;
         						int j = 0;
          						for (Iterator<?> iterator = wettkampf.gegliste.iterator(); iterator.hasNext();) {
         							onlgeg = (OnlineGegner) iterator.next();
         							int gegpuls = (int) onlgeg.getPuls();
         							String name = onlgeg.getNachname();
         							if (name.length() > 11)	// wenn zu lang, dann begrenzen.
         								name = onlgeg.getNachname().substring(0,11);
         							if (wettkampf.isZprace())
         								outstr = String.format("%2d.%11s%4dP%4dU%6.2fkm%4dW  \n",i, name, gegpuls, (int) onlgeg.getRpm(), 
							                      onlgeg.getZpstrecke()/1000, (int) onlgeg.getLeistung());  
         							else	
         								outstr = String.format("%2d.%11s%4dP%4dU%6.2fkm%4dW  \n",i, name, gegpuls, (int) onlgeg.getRpm(), 
         									                      onlgeg.getStrecke()/1000, (int) onlgeg.getLeistung()); 
         							txtGegner.append(outstr);
         							// eigenen Rang blau markieren und auf Wiederaufnahme prüfen
         							if (onlgeg.getUserid() == Global.rennenUserID && onlgeg.getUserid() != 0) {
         								txtGegner.setLineBackground(i-1, 1, SWTResourceManager.getColor(SWT.COLOR_BLUE));
         								int gpsPunkt = (int) onlgeg.getPunkt();
         								// Mlog.debug("akt. Punkt: " + akttrkpkt.getIndex() + " - letzter Punkt: " + gpsPunkt);
         								if (akttrkpkt.getIndex() < gpsPunkt && akttrkpkt.getIndex() < mindGPS) {	// Auf Wiederaufnahme prüfen (nur auf den ersten Punkten)
         									dzpstrecke = onlgeg.getZpstrecke();
         									rennenWiederaufnahme(gpsPunkt);
         								}
         							} else {	// eigenen Rang blau markieren beim fahrem mit virtuellen Mitfahrern
         								if (wettkampf.isVirtGeg() || wettkampf.isVirtZP()) {
         									if (onlgeg.getNachname().equals(biker.getName()))
                 								txtGegner.setLineBackground(i-1, 1, SWTResourceManager.getColor(SWT.COLOR_BLUE));         										
         								}
         							}
         							// Bei Zielpulstraining den Pulsbereich des Gegners darstellen:
         		         			if (wettkampf.isZprace()) {
         		         				styleRange.start = 38+(i-1)*40;		// am Ende der Zeile 1 Zeichen breit
         		         				styleRange.length = 1;
         		         				if (gegpuls < biker.getPulsgruen())
         		         					styleRange.background = SWTResourceManager.getColor(SWT.COLOR_RED);
         		         				else if (gegpuls >= biker.getPulsgruen() && gegpuls <= biker.getPulsgruen()+1) {
         		         					styleRange.background = SWTResourceManager .getColor(SWT.COLOR_YELLOW);
         		         				} else if (gegpuls > biker.getPulsgruen()+1 && gegpuls < biker.getPulsgelb()-2) {
         		         					styleRange.background = SWTResourceManager.getColor(SWT.COLOR_GREEN);
         		         				} else if (gegpuls >= biker.getPulsgelb()-2 && gegpuls <= biker.getPulsgelb()-1) {
         		         					styleRange.background = SWTResourceManager.getColor(SWT.COLOR_YELLOW);
         		         				} else
         		         					styleRange.background = SWTResourceManager.getColor(SWT.COLOR_RED);
         		         				txtGegner.setStyleRange(styleRange);
         							}
									// Position in der OSM-Map darstellen
									if (newkonfig.isShowmap() && (onlgeg.getUserid() != Global.rennenUserID || onlgeg.getUserid() == 0))
										osmv.setPosition(j++, onlgeg.getLon(), onlgeg.getLat(),onlgeg.getNachname());
         							// Position des Teilnehmers im Höhenprofil anzeigen
         							double gegstrecke = onlgeg.getStrecke()/1000.0;
         							Marker markgeg = new ValueMarker(gegstrecke);
         							if (oldpos.size() < i+1)	// Vector vergrößern, wenn zu klein
         								oldpos.addElement(markgeg);
         							markgeg.setPaint(Color.lightGray);
         							markgeg.setLabelPaint(Color.lightGray);
         							markgeg.setLabel(onlgeg.getNachname());
         							markgeg.setLabelAnchor(RectangleAnchor.TOP);
         							markgeg.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
         							markgeg.setLabelFont(new Font("Arial", 0, 10)); 
         							markgeg.setLabelOffset(new RectangleInsets(10D*i, 0D, 0D, 5D));
         							plot.addDomainMarker(markgeg);
         							try {
             							oldpos.setElementAt(markgeg, i);										
									} catch (Exception e) {
										Mlog.debug("Ex bei marker oldpos!");
									}
									i++;
         						}
         						// Aktualisierung der eigenen Position im Höhenprofil
                 				double start = akttrkpkt.getAbstand_m()/1000 - gesamtstrecke/1000/compprofil.getSize().x;
                 				double ende = akttrkpkt.getAbstand_m()/1000 + gesamtstrecke/1000/compprofil.getSize().x;
             					marker = new IntervalMarker(start, ende);
         						marker.setPaint(Color.cyan);
         						plot.addDomainMarker(marker);
         					}
         				} else {	// kein Onlinerennen, nur auf Wiederaufnahme prüfen und Position im Höhenprofil anzeigen
         		            if (Global.gPXfile.equals(Global.lastGPXFile))
         		            	if (Global.lastTourGPSPkt > mindGPS && akttrkpkt.getIndex() < Global.lastTourGPSPkt && akttrkpkt.getIndex() < mindGPS) {	// Auf Wiederaufnahme prüfen (nur auf den ersten Punkten)
         		            		Mlog.debug("Wiederaufnahme! Index: " + akttrkpkt.getIndex() + "; lastTourGPSPkt: "+Global.lastTourGPSPkt + "; GPXDatei: " + Global.gPXfile + "; letzte GPXDatei: " + Global.lastGPXFile); 	
         		            		rennenWiederaufnahme((int) Global.lastTourGPSPkt);
         		            	}
     						plot.clearDomainMarkers(0);	// die alten Marker löschen
             				if (isCsvrennen()) {
                 				Marker markergeg = new ValueMarker(aktgzp.getStrecke()/1000);                 				
        						markergeg.setPaint(Color.lightGray);
               					plot.addDomainMarker(markergeg, Layer.BACKGROUND);
             				}
         					
         					// Aktualisierung der eigenen Position im Höhenprofil
             				double start = akttrkpkt.getAbstand_m()/1000 - gesamtstrecke/1000/compprofil.getSize().x;
             				double ende = akttrkpkt.getAbstand_m()/1000 + gesamtstrecke/1000/compprofil.getSize().x;
             				//Mlog.debug("start: " + start + " ende: " + ende);
         					marker = new IntervalMarker(start, ende);
         					marker.setPaint(Color.cyan);
         					plot.addDomainMarker(marker);
         				}
         				
         				if (sek % 2 == 0) {		// Aktionen bei jedem zweiten Aufruf (alle 2 Sekunden)
         					RefreshInfos();
         					if (biker.isAutomatik() && atKurbel.getEnabled(2))
         						gangAutomatik(biker.getAutomatik1(), biker.getAutomatik2(), p, thisTrainer.getGang());
         				}
         				
            			// Einblendung der Belohnung beim vorletzten Punkt
             			if (akttrkpkt.getIndex() == zielGPSPunkt-1) {	
             				belText = Messages.getString("Rsmain.belohnung1") + "\n" + 
             				               zfk1.format(work / kcalfakt / newkonfig.BelohnungkCal[biker.getBelohnungindex()]) + 
             				               Messages.getString("Rsmain.belohnung2") + newkonfig.Belohnung[biker.getBelohnungindex()];
        	        		if (aktstatus == Status.laeuft) {
        	        			//Mlog.debug(belText);
        	        			showOVD(0, 40, 5000, 180, "\n\n\n\n" + belText);
        	        		}
             			}
             			
             			// letzten Punkt erreicht?
             			if (akttrkpkt.getIndex() >= zielGPSPunkt) { 
             				zeitFarbe = SWT.COLOR_RED;
             				tourEnde = true;
		    				atZeit.show(0, tfmt.format(trainingsmillisek), zeitFarbe);	// Trainingszeit rot anzeigen
                 			if (wettkampf.isZprace() || wettkampf.isAktiv()) {
             					auswertung.setIblEnde(true);
             					if (isVlc_ein())             						
             						doStop(false, true);		// Video weiterlaufen lassen
             					else
             						doStop(true, true);
             				} else {							// prüfen, ob Endlosloop eingeschaltet
             					if (endlos) {
             						if (isVlc_ein() && (libvlc != null)) {	
             							libvlc.libvlc_media_player_set_position(mediaplayer, 0);
             							trainingsmillisek = 0;
             							work = 0;
             						} 			
             					} else							// kein Endlosbetrieb
            						if (!isVlc_ein()) {			// ohne Video? stoppen!           	        				
            							doStop(true, false);	
            						}
              				}
             			}

             			if (isOvddata()){	// On Video Display Datenanzeige
         					showOVD(8, 50, 2500, 70, zfk1.format(steigung)+" %% .. "+zfk0.format(new Double(p))+" W .. "+zfk0.format(geschw)+" km/h .. "+zfk1.format((gesamtstrecke - dstrecke)/1000.0)+" km");
         				}
         				
         				if (isCsvrennen()) {
         					// gegnerische Daten ausgeben an Pos. 4
             				atPower.show(4, zfk0.format(aktgzp.getLeistung()), 0);	
         					atPuls.show(4, zfk0.format(aktgzp.getPuls()), 0); 
             				atKurbel.show(4, zfk0.format(aktgzp.getRpm()), 0);  
             	         	atGeschw.show(4, zfk0.format(aktgzp.getGeschwindigkeit()), 0);	
             	         	atStrecke.show(4, zfk1.format(aktgzp.getStrecke()/1000.0), 0);
             	         	atSteigung.show(4, zfk1.format(aktgzp.getSteigung()), 0);
             	         	atKcal.show(4, zfk0.format(aktgzp.getEnergie()), 0);
             	         	// Strecke rot oder grün je nachdem ob Gegner vorne oder hinten:
             	         	if (dstrecke > aktgzp.getStrecke())
             					atStrecke.setcolor(display.getSystemColor(SWT.COLOR_GREEN)); 
             	         	else
             					atStrecke.setcolor(display.getSystemColor(SWT.COLOR_RED)); 
         				} else {
             				atPower.show(4, zfk0.format(akttrkpkt.getLeistung()), 0);	            // Watts aus TCX-Datei ausgeben
         					atPuls.show(4, zfk0.format(akttrkpkt.getPuls()), 0); // Puls aus TCX-Datei ausgeben
             				atKurbel.show(4, zfk0.format(akttrkpkt.getRpm()), 0);  // akt. Wert Cadence aus TCX-Datei
             	         	atGeschw.show(4, zfk0.format(geschw1), 0);	// org. Tourgeschwindigkeit
   							atStrecke.show(4, zfk1.format(dzpstrecke/1000.0), 0);  
         				}
         				lastIndex = akttrkpkt.getIndex();
         			} catch(Exception e) {
         				Mlog.ex(e);
         			}	
        	};   
		};
		display.asyncExec(r);
	}

	/**
	 * Wiederaufnahme des Netzwerk- oder Onlinerennens.
	 * Es wurde festgestellt, daß der vom Server zurückgegebene GPS-Punkt größer als der aktuelle Punkt ist.
	 * Daher wird nun vorgegesprungen und die Trainingswerte (Zeit, Strecke etc.) werden aktuell nicht neu gesetzt.
	 * 13.7.2014: Wiederaufnahme ist nun auch bei abgebrochenen (Einzel-)Trainings möglich!
	 * @param gpsPunkt  GPS-Punkt
	 */
	private void rennenWiederaufnahme(int gpsPunkt) {
		if (gpsPunkt > 2) {					// wenns in der Nähe des Starts passiert, dann zurück zum Start!
			Mlog.debug("starte Wiederaufnahme Punkt: " + gpsPunkt);
			long sek = getTrkSek(gpsPunkt-1);		// Tourzeit des GPS-Punktes berechnen 
			newAktPos(sek);
		}
	}
	
	/**
	 * Wenn die Gangautomatik eingeschaltet ist, dann wird hier nachgesehen, ob die Leistung größer/kleiner als die konfigurierten
	 * Schaltschwellen ist und dann entsprechend geschaltet.
	 * @param low	unterer Grenzwert
	 * @param high  oberer Grenzwert
	 * @param p     Leistung
	 * @param gang  akt. eingelegter Gang (1..9)
	 */
	protected void gangAutomatik(double low, double high, double p, int gang) {
		if (!atKurbel.getEnabled(2))
			return;
		if (p > high && gang > 1)
			changeGangrunter();
		else if (p < low && gang < maxgang)
			changeGanghoch();
	}

	/**
	 * Gangautomatik ein- bzw. ausschalten mit Einblendung im Video
	 */
	protected void gangAutomatikEinAus() {
		if (biker.isAutomatik()) {
			biker.setAutomatik(false);
    		if (aktstatus == Status.laeuft)
    			showOVD(0, 90, 1500, 100, Messages.getString("Rsmain.AutomatikAus"));
		} else {
			biker.setAutomatik(true);
    		if (aktstatus == Status.laeuft)
    			showOVD(0, 90, 1500, 100, Messages.getString("Rsmain.AutomatikEin"));
		}
	}

	/**
	 * Übertragung zum Trainingsgerät ein- bzw. ausschalten mit Einblendung im Video
	 */
	protected void comWattsEinAus() {
		if (Global.comWatts) {
			Global.comWatts = false;
    		if (aktstatus == Status.laeuft)
    			showOVD(0, 90, 1500, 100, Messages.getString("Rsmain.comWattsAus"));
		} else {
			Global.comWatts = true;
    		if (aktstatus == Status.laeuft)
    			showOVD(0, 90, 1500, 100, Messages.getString("Rsmain.comWattsEin"));
		}
	}
	
	/**
	 * Timertask für Simulation (ohne Video)
	 *
	 */
	class SimTask extends TimerTask 
	{ 
	    @Override public void run() 
	    { 
	    	timersek += 1;
        	rtSek = new Date().getTime() / 1000;
			rtSek -= rtStartSek;
			//Mlog.debug("timer: nanosek: "+System.nanoTime());
            if (rtSek != lastrtSek) { // alle echten Sekunden aufrufen
            	//Mlog.debug("timer: timersek: "+timersek);
         	   	DoSimulation(timersek);
         	   	lastrtSek = rtSek;
            }

	    } 
	}

	/**
	 * Timertask für regelmäsige Aufgaben (z. B. Autostart)
	 *
	 */
	class PeriodTask extends TimerTask 
	{ 
		@Override public void run() 
	    { 
   	   		DoPeriods();
	    } 
	}
	
	/**
	 * Diese Methode initialisiert das Composite compprofil	für das Höhenprofil und erzeugt
	 * das Höhen-/Geschwindigkeitsprofil
	 *
	 */
	private void createCompprofil() {
		JFreeChart chart = ChartFactory.createXYLineChart(Global.strTourvideo, "", "", dataset, PlotOrientation.VERTICAL, true, false, false);  
		frame = new ChartComposite(compprofil, SWT.NONE, chart, true);

        chart.setBackgroundPaint(Color.black);        

        plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.black);

        // customise the range axis...
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(0);			
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(false);

        NumberAxis rangeAxis2 = new NumberAxis("");		 
        rangeAxis.setTickLabelPaint(Color.gray);
        rangeAxis2.setTickLabelPaint(Color.gray);
        plot.setRangeAxis(1, rangeAxis2);
        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setDataset(1, dataset1);
        plot.mapDatasetToRangeAxis(1, 1);
        ValueAxis valueAxis = plot.getDomainAxis();
        valueAxis.setTickLabelPaint(Color.gray);
        XYItemRenderer renderer2 = new XYLineAndShapeRenderer(true, false);
        plot.setRenderer(1, renderer2);
        renderer2.setSeriesPaint(0, Color.darkGray);

        valueAxis.setUpperMargin(0);	// sonst rechts 5 % Lücke!
        LegendTitle legend = chart.getLegend();
        legend.setBackgroundPaint(Color.black);
        legend.setItemPaint(Color.gray);

        frame.pack();
        frame.setBounds(0, 0, compprofil.getSize().x, compprofil.getSize().y);
	}

	/**
	 * GPS-Daten einlesen und in Profildaten für Diagramm übernehmen
	 * @return Anzahl der Trackpunkte
	 */
	private long loadProfil(){
		long i = 1;

		// Profildaten löschen
		profilData.clear();
		profilData1.clear();
		dataset.removeAllSeries();
		dataset1.removeAllSeries();
		
		// GPS-Daten in List einlesen
		ldpx = new VerwaltungGPX();

		if (Global.gPXfile.endsWith(".gpx") || Global.gPXfile.endsWith(".tcx")) {   
            setVlc_ein(false);
		} else {
			Global.gPXfile = Global.gPXfile+".tcx";  
			setVlc_ein(true);
		}

		Global.gPXfile = ldpx.loadGPS(Global.gPXfile, newkonfig.isaveraging());
		toolbar.actionStart.setEnabled(true);	// Startbutton enablen
		toolbar.actionStop.setEnabled(true);	// Stopbutton enablen
		Iterator<TrkPt> it = VerwaltungGPX.track.iterator();
		gesamtsek = ldpx.getGesamtsek();
		gesamtstrecke = ldpx.getGesamtstrecke();
		gesamthm = ldpx.getGesamthm();
		//Mlog.debug("GesamtHm = "+gesamthm);
		
		if (profilData.getItemCount() > 0) {
			profilData.clear();
		}
		while(it.hasNext()) {
			TrkPt aktpoint = (TrkPt) it.next(); 
			profilData.add(aktpoint.getAbstand_m()/1000, aktpoint.getHoehe());  
			profilData1.add(aktpoint.getAbstand_m()/1000, aktpoint.getV_kmh());  
			i++;
		}
			
		dataset.addSeries(profilData);
		dataset1.addSeries(profilData1);
        return (i-1);
	}

	/**
	 * Trackpunkt zu der übergebenen Zeit ermitteln und zurückgeben
	 * @param sek Sekunden seit Start des Videos
	 * @return Trackpunkt
	 */
	public TrkPt gettrackpoint(long sek){  
		Iterator<TrkPt> it = VerwaltungGPX.track.iterator();
		TrkPt aktpoint = (TrkPt) it.next(); 
		while(it.hasNext() && aktpoint.getAnzsek() < sek) {
			aktpoint = (TrkPt) it.next(); 
		}
		return aktpoint;
	}

	/**
	 * Ermittelt die Trackzeit des übergebenen Punktes
	 * @param punkt		Nummer (Index) des GPS-Punktes
	 * @return			Anzahl der Sekunden am GPS-Punkt seit Start
	 */
	public long getTrkSek (int punkt){  
		TrkPt trkpt = VerwaltungGPX.track.get(punkt);
		return trkpt.getAnzsek();
	}

	/**
	 * Initialisiert die Kommunikation mit dem Ergometer und gibt die Version in der Captionzeile aus.
	 *
	 */
	public static void initergokom() {
		if (thisTrainer.hatverbindung() == Trainer.kommunikation.seriell) {
    		sShell.setText(Global.version+Messages.getString("Rsmain.suche_ergo_ser")+thisTrainer.getErgoCom());	  
  	  		if (thisTrainer.trainerModell.equals("daum2001") || thisTrainer.trainerModell.equals("ergoline") || thisTrainer.trainerModell.contains("cyclus2")) {
            	thisTrainer.seropen(thisTrainer.getErgoCom(), 4800);	
  	  		}
            else  // die neueren Daum und Kettler kommunizieren mit 9600 Baud
            	thisTrainer.seropen(thisTrainer.getErgoCom(), 9600);	
	        String ret = new String();
	        ret = thisTrainer.talk(Trainer.Commands.getadress, 0.0);
	        Mlog.info("get_adress: "+ret);  
	        ret = thisTrainer.talk(Trainer.Commands.check_cockpit, 0.0);
	        Mlog.info("check_cockpit: "+ret); 
	        Global.ergoVersion = thisTrainer.talk(Trainer.Commands.version, 0.0);
	        Mlog.info("Baud: 9600/4800 "+Messages.getString("Rsmain.herst_p_dp")+thisTrainer.getTrainerModell()+Messages.getString("Rsmain.version_dp")+Global.version+Messages.getString("Rsmain.ergo_dp")+Global.ergoVersion+" - "+thisTrainer.getErgoCom()); 
            // Cockpitversion ausgeben im Fenstertitel
    		sShell.setText(Global.version+Messages.getString("Rsmain.ergo_dp")+Global.ergoVersion+" - "+thisTrainer.getErgoCom()); 
    		if (!Global.ergoVersion.isEmpty()) {
    			if (Global.ergoVersion.charAt(0) > 127) {
    				Mlog.debug(Global.ergoVersion.charAt(0)+": vermutlich falsche Baudrate - wechsle auf 57600...");
    				Global.ergoVersion = "0";
    			}
    		}
    		if ((thisTrainer.trainerModell.equals("kettler") || thisTrainer.trainerModell.equals("kettlercr")) && Global.ergoVersion.compareTo("0") == 0) {  
    			thisTrainer.serclose();
    			// bei Kettler ab Cockpit SI/SJ/SK mit 57600 kommunizieren!
        		thisTrainer.seropen(thisTrainer.getErgoCom(), 57600);
        		Global.ergoVersion = thisTrainer.talk(Trainer.Commands.version, 0.0);
    	        Mlog.info("Baud: 57600 "+Messages.getString("Rsmain.herst_p_dp")+thisTrainer.getTrainerModell()+Messages.getString("Rsmain.version_dp")+Global.version+Messages.getString("Rsmain.ergo_dp")+Global.ergoVersion+" - "+thisTrainer.getErgoCom()); 
        		sShell.setText(Global.version+Messages.getString("Rsmain.ergo_dp")+Global.ergoVersion+" - "+thisTrainer.getErgoCom());           		
    			
        		if (Global.ergoVersion.compareTo("0") == 0) {  
        			Messages.errormessage(Messages.getString("Rsmain.ergo_meldet_nicht"));    			  
        			thisTrainer.serclose();
        		}
    		}
    		else if ((Global.ergoVersion.compareTo("AR1S") == 0) || (Global.ergoVersion.compareTo("KX") == 0)) {
    			Mlog.info("Timing für Kettler AR1S/KX wurde angepasst!");   			  
    			thisTrainer.setKettlerwait(kettlerlongwait);
    		}
    		// bei "fliessendem Wechsel Reset, sonst wird keine Leistung übertragen
    		if (Global.ergoVersion.compareTo("0") != 0) {
    			ret = thisTrainer.talk(Trainer.Commands.reset, 0.0);  // Reset für Kettler
    			ret = thisTrainer.talk(Trainer.Commands.init, 0.0);  
    		}
		} else if (thisTrainer.hatverbindung() == Trainer.kommunikation.netzwerk) {
    		sShell.setText(Global.version+Messages.getString("Rsmain.suche_ergo_lan")+thisTrainer.getErgoIP());
  	  		if (thisTrainer.trainerModell.equals("cyclus2_el") || thisTrainer.trainerModell.equals("cyclus2"))
  	    		thisTrainer.netopen(thisTrainer.getNetTimout(), 25000);
  	  		else		// Daum Trainingsgerät mit LAN-Anschluß
  	  			thisTrainer.netopen(thisTrainer.getNetTimout(), 51955);
  	  		Global.ergoVersion = thisTrainer.talk(Trainer.Commands.version, 0.0);
	        Mlog.info(Messages.getString("Rsmain.herst_p_dp")+thisTrainer.getTrainerModell()+Messages.getString("Rsmain._version_dp_")+
	        		Global.version+Messages.getString("Rsmain.ergo_dp")+Global.ergoVersion+" - "+thisTrainer.getErgoCom()+" - "+thisTrainer.getErgoIP()); 
            // Protokollversion ausgeben im Fenstertitel
    		sShell.setText(Global.version+Messages.getString("Rsmain._ergo_protokoll_dp_")+Global.ergoVersion+" - LAN");		        		    
    		if (Global.ergoVersion.compareTo("0") == 0) {  
    			Messages.errormessage(Messages.getString("Rsmain.ergo_meldet_nicht_lan"));  
    			thisTrainer.netclose();
    		}
		} else if (thisTrainer.hatverbindung() == Trainer.kommunikation.usb) {
    		sShell.setText(Global.version+Messages.getString("Rsmain.suche_rolle"));  
    		thisTrainer.usbopen();
    		Global.ergoVersion = thisTrainer.talk(Trainer.Commands.version, biker.getFahrergewicht());
    		sShell.setText(Global.version+Messages.getString("Rsmain.rollentrainer")+Global.ergoVersion+" - USB");		        		    
    		if (Global.ergoVersion.compareTo("0") == 0) {  
    			Messages.errormessage(Messages.getString("Rsmain.rolle_meldet_nicht"));  
    			thisTrainer.usbclose();
    		}
		} else if (thisTrainer.hatverbindung() == Trainer.kommunikation.ant) {
			sShell.setText(Global.version+Messages.getString("Rsmain.oeffne_ant"));  
			
			if (libant != null) {
				thisTrainer.antopen();
			}
			Global.sleep(tacxAntWait);
			Global.ergoVersion = thisTrainer.talk(Trainer.Commands.version, 0.0);
			sShell.setText(Global.version+Messages.getString("Rsmain.rollentrainer")+" SNR: "+Global.ergoVersion+" - ANT+");		        		    
			if (Global.ergoVersion.compareTo("0") == 0) {  
				Messages.errormessage(Messages.getString("Rsmain.rolle_meldet_nicht_ant"));  
				//thisTrainer.antclose();
				thisTrainer.antopen();
			}
		}
		// ANT+ Kommunikation starten, wenn nur Pulser oder RPM/Speed vorhanden
		if (libant != null) {	
			if (!libant.isEin())
				if (libant.getaktTHr() == LibAnt.THr.antppuls || libant.getaktTCadence() != LibAnt.TCadence.notset) 
					Rsmain.thisTrainer.antopen();
		}
	}

	/**
	 * calcbikeleistung berechnet die benötigte Leistung abh. von der
	 * Geschwindigkeit und Steigung.
	 * Verwendete Formel: 
	 * P = 0,5*p*cwA*v^3 + k2*Mg*v + Mg*v*g*st
	 * Addition von: 
	 *                Leistung um Windwiderstand zu überwinden +
	 *                Leistung um Rollwiderstand zu überwinden +
	 *                Leistung um Höhenunterschied zu überwinden 
     * siehe http://www.radpanther.de/indexreload.html?training/leistung.html
	 * aber Bergsteigung von: http://www.wolfgang-menn.de/powerhill_d.htm	 
	 * @param v		   Geschwindigkeit
	 * @param steigung aktuelle Steigung
	 * @param vwind    Windgeschwindigkeit
	 * @return benötigte Leistung in Watt
	 */
	private double calcbikeleistung(double v, double steigung, double vwind) {
		double p = 1.225;   // Luftdichte
		double g = 9.81;    // Erdbeschleunigung 9,81 m/s2
		
		double vm = v / 3.6; // umrechnen in m/s
		double gewicht = biker.getFahrergewicht()+biker.getBikegewicht();
		double P = 0.5 * p * biker.getCwa() * (vm-vwind)*(vm-vwind)*vm + (biker.getK2() * gewicht * vm) + (vm * gewicht * g * steigung/100);
		
		P = P * biker.getLfakt();
		
		return P;
	}
	
	/**
	 * Setzt den Filenamenfilter auf .xml
	 *
	 */
	private static class TxtFilenameFilter implements FilenameFilter { 
		  @Override public boolean accept( File f, String s ) { 
		    return new File(f, s).isFile() && 
		           s.toLowerCase().endsWith( ".xml" ) &&  
		           !s.contains("settings");   
		  } 
	}
	
	/**
	 * Füllen der Combobox zur Auswahl des Bikerprofils.
	 * @return Stringarray mit Fahrernamen (0 = Standard)
	 */
	public static String[] setcmbfahrer() {
        int i = 1;
        //Mlog.debug("strPfad: " + Global.strPfad);
	    File userdir = new File(Global.strPfad); 
	    
	    for (File file : userdir.listFiles(new TxtFilenameFilter())) { 
	    	if (file.getName().compareTo(Global.standardprofildatei) != 0) {
	    		i++;
	    	}
	    }
        
		String[] Profile = new String[i];
		Profile[0] = "Standard";  
		i = 1;
		
	    for (File file : userdir.listFiles(new TxtFilenameFilter())) { 
	    	if (file.getName().compareTo(Global.standardprofildatei) != 0)
	    		Profile[i++] = ""+file.getName().replaceFirst(".xml", "");  
	    }
	    
		return Profile;
	}

	/**
	 * startet das Video
	 *
	 */
	public void videostart() {
		if (isVlc_ein() && (libvlc != null)) {	
	        Mlog.info("Starte VLC, Tourzeit: " + tfmt.format(newvidtime * 1000));
	        //long hId = 0;
	        int hId = 0;
	        String handleName;
	        
            if (Platform.isWindows()) {
            	handleName = "handle";
            } else if (Platform.isLinux() && aktstatus==Status.beendet) {
            	Mlog.debug("compvideo wird neu erzeugt");
            	compvideo.dispose();
    			compvideo = new Composite(compHaupt, SWT.EMBEDDED);
    			compvideo.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    			compvideo.redraw();
    			showClientArea();
            	handleName = "embeddedHandle";           
	        } else {						// isMac
            	handleName = "view";		// TODO: das funktioniert noch nicht!
	        }
            Class<? extends org.eclipse.swt.widgets.Composite> compClass = compvideo.getClass();
            Field handleField;

            try {
            		handleField = compClass.getField(handleName);
            		hId = (int) handleField.getLong(compvideo);            		
            } catch (SecurityException e) {
            		Mlog.debug("SecurityException beim ermitteln von "+handleName+" in videostart");
            } catch (NoSuchFieldException e) {
            		Mlog.debug("NoSuchFieldException beim ermitteln von "+handleName+" in videostart");
            } catch (IllegalArgumentException e) {
            		Mlog.debug("IllegalArgumentException beim ermitteln von "+handleName+" in videostart");
            } catch (IllegalAccessException e) {
            		Mlog.debug("IllegalAccessException beim ermitteln von "+handleName+" in videostart");
            }

            Mlog.debug("hId: " + hId + " Hex: " + Long.toHexString(hId));
            if (Platform.isWindows()) {
                libvlc.libvlc_media_player_set_hwnd(mediaplayer, (int) hId);
            } else if (Platform.isMac()) {
            	//SWT_AWT.embeddedFrameClass = "sun.lwawt.macosx.CViewEmbeddedFrame";
            	/* das klappt leider nicht:
            	Frame awtFrame = SWT_AWT.new_Frame(compvideo);
            	awtFrame.setVisible(true);
            	hId = Native.getComponentID(awtFrame);
            	libvlc.libvlc_media_player_set_nsobject(mediaplayer, hId);
            	*/
            } else {	// Linux
            	libvlc.libvlc_media_player_set_xwindow(mediaplayer, hId);
            }
	        
	        libvlc.libvlc_media_player_play(mediaplayer);
         
			//VlcAudio.play();		
		} else {
	        Mlog.info("Starte Simulationstimer...");  
	        Rescheduletimer(grate * drate);
		}
		auswertung.init(newkonfig, biker, Global.strTourvideo.substring(0, Global.strTourvideo.indexOf('.')));	// textvideo.getText()			
		aktstatus = Status.laeuft;
		setAusgewertet(false);
	    
	    if (timer1 != null)
	    	timer1.cancel();
	    
	    drate = 1.0;
	    Global.ergoVersion = "";		// bei Start Versionsabfrage beim Ergometer    
	}

	/**
	 * Video: pause
	 *
	 */
	public void videopause() {
		Mlog.debug("do videopause...");
		if (isVlc_ein() && (libvlc != null)) {
			libvlc.libvlc_media_player_pause(mediaplayer);
			//VlcAudio.pause();
		} else {  // GPX-Tour...
			if (aktstatus == Status.angehalten || aktstatus == Status.autostopped) {
				timer = new Timer();
				// Starte Simulationstimer sofort und Aufruf alle Sekunden
				Rescheduletimer(grate * drate);
			} else {
				if (timer != null)
					timer.cancel();
			}
		}

		if (aktstatus == Status.autostopped)  {
			if (timer1 != null)
				timer1.cancel();
		}

		if (aktstatus == Status.laeuft)
			aktstatus = Status.angehalten;
		else
			aktstatus = Status.laeuft;
	}

	/**
	 * Video: Ende
	 *
	 */
	private void videostop() {
		if (isVlc_ein() && (libvlc != null)) {
			if (aktstatus == Status.laeuft || aktstatus == Status.angehalten || aktstatus == Status.autostopped) {
				Mlog.info("Stop VLC...");
				libvlc.libvlc_media_player_stop(mediaplayer);
			}
			//VlcAudio.stop();

		} else {
			if (timer != null)
				timer.cancel();
			timersek = 0;
		}
		aktstatus = Status.beendet;
		if (timer1 != null)
			timer1.cancel();
	}
		
    /**
     * Hier wird mittels Dateidialog die Tour ausgewählt.
     * Wird eine Tour übergeben, dann kommt kein Dateiauswahldialog, sondern es wird alles ohne
     * Auswahl gesetzt.
     * @param tour		Name der Tourdatei
     */
    public void setTour(String tour) {
    	String tourdatei;
		if (aktstatus != Status.beendet)
			doStop(true, false);
		
		if (trainingsmillisek > 60000) {	// wurde schon trainiert?
			if (Global.autoStart)
				resetTrainingsdata();
			else	
				if (Messages.entscheidungmessage(sShell, Messages.getString("Rsmain.sollreset")))
					resetTrainingsdata();
				else
					dStreckenSumme = dstrecke;
		}
		FileDialog dialog = new FileDialog(sShell, SWT.OPEN);
		dialog.setFilterNames (new String [] {Messages.getString("Rsmain.videodateien"), Messages.getString("Rsmain.gpx_dateien"), Messages.getString("Rsmain.alle_dateien")}); 
		dialog.setFilterExtensions (new String [] {"*.m?v;*.avi;*.mpeg;*.mpg;*.mp4", "*.gpx;*.tcx", "*.*"}); //Windows wild cards  
		dialog.setFilterPath (newkonfig.getStrTourenpfad()); 
		if (tour == null)
			tourdatei = dialog.open();
		else 
			tourdatei = newkonfig.getStrTourenpfad() + Global.ptz + tour;
		if (tourdatei != null) {
			File vidfile = new File(tourdatei);
			Global.gPXfile = vidfile.getPath();
			String str = vidfile.getPath();
			newkonfig.setStrTourenpfad(str.substring(0, str.lastIndexOf(Global.ptz)));  
			Mlog.info("Tourenpfad: "+newkonfig.getStrTourenpfad()); 
			Global.strTourvideo = tourdatei.substring(tourdatei.lastIndexOf(Global.ptz)+1);
            if (isVlc_ein()) {
				setPlayer(libvlc, tourdatei);  
			}
			zielGPSPunkt = loadProfil();
            Mlog.info("GPS-Datei: "+Global.gPXfile+" - Anz. Punkte:"+zielGPSPunkt); 
            //Mlog.debug("letzte GPS-Datei: "+Global.lastGPXFile+" - Anz. Punkte:"+Global.lastTourGPSPkt);
            if (Global.gPXfile.equals(Global.lastGPXFile)) {
            	if (Global.lastTourGPSPkt > mindGPS && (zielGPSPunkt - Global.lastTourGPSPkt > mindGPS)) {
            		if (!Messages.entscheidungmessage(sShell, Messages.getString("Rsmain.tourwiederaufnahme")))
            			Global.lastTourGPSPkt = 0;
            	} else
        			Global.lastTourGPSPkt = 0;
            } else
    			Global.lastTourGPSPkt = 0;
            	
			toolbar.changeImgStartbutton("play.png");
	        atStrecke.show(1, zfk1.format(gesamtstrecke/1000), 0);	// Restwerte anzeigen
	        atStrecke.show(2, zfk1.format(gesamtstrecke/1000), 0);
			atSteigung.show(2, zfk0.format(gesamthm), 0);
 			atZeit.show(2, tfmt1.format(gesamtsek * 1000), 0); 
	        	        
			lblTour.setText(tourdatei.substring(tourdatei.lastIndexOf(Global.ptz)+1));  
			// Kartenanzeige: neuen Track anzeigen
			if (newkonfig.isShowmap()) {
				if (osmv == null)
					initMap();
				else {
					TrkPt aktpoint = (TrkPt) VerwaltungGPX.track.get(0); 
					OSMViewer.PointD koord = new OSMViewer.PointD(aktpoint.getLongitude(), aktpoint.getLatitude());
					Point pos = osmv.computePosition(koord);
			    	osmv.setCenterPosition(pos);
				}
				osmv.setAktTrack(VerwaltungGPX.track);
				osmv.redraw();
			}
		}  
    }
    
    /**
     * Setzt die Anzeigen und die Liste für die Auswertung zurück.
     * Wird aufgerufen bei neuer Tourauswahl.
     */
    private void resetTrainingsdata() {
    	dhm = 0.0;
    	trainingsmillisek = 0;
    	work = 0.0;
//    	Fitness.reset();
    	Puls.reset();
    	Geschwindigkeit.reset();
    	Leistung.reset();
    	Kurbel.reset();
    	
    	auswertung.pliste.clear();
    }
    
    /**
     * Windgeschwindigkeit aus der Combobox cmbwind ermitteln.
     */
    private static void setWind() {
    	if (cmbwind.getText().substring(0,1).equals("-")) {  
			vwind    = (-1.0)*Double.parseDouble(cmbwind.getText().substring(6,8));
    	} else {
			vwind    = Double.parseDouble(cmbwind.getText().substring(6,8));
    	}
    	vwind /= 3.6;  // umrechnen in m/s    	
    }

	/**
     * Slider vor-/zurücksetzen: entspricht Sprung um Inkrement vor oder zurück
     * @param increment		Inkrement bei Benutzung der "Pfeile"
     */
    public void sliderjump(int increment) {
    	Double dpos = new Double((double)(sldvideo.getSelection() + increment)/(double)slidermax);
		if (isVlc_ein() && (libvlc != null))
			libvlc.libvlc_media_player_set_position(mediaplayer, dpos.floatValue());
    	else
    		timersek = (long) (gesamtsek * dpos);
    }
    
    /**
     * Springt zur neuen absoluten Position.
     * @param sek	Sekunden seit Start
     */
    public void newAktPos(long sek) {
    	Double dpos = new Double((double)(sek)/(double)gesamtsek);
    	//Mlog.debug("sek: " + sek + " - gesamtsek: " + gesamtsek);
		if (isVlc_ein() && (libvlc != null))
			libvlc.libvlc_media_player_set_position(mediaplayer, dpos.floatValue());
    	else
    		timersek = (long) (gesamtsek * dpos);
    }

    /**
     * Berechnet die erledigte Arbeit seit dem letzten Aufruf.
     * Wenn nicht getreten wird, (rpm <= 10) dann wird nur der Zeitpunkt gesetzt und
     * 0 zurückgegeben. Wenn getreten wird, dann wird die Zeitdifferenz ermittelt und
     * die erledigte Arbeit in Ws ermittelt und zurückgegeben.
     * @param rpm	aktuelle Pedalumdrehungen
     * @param p		aktuelle Leistung
     * @return      Arbeit seit letztem Aufruf in Ws
     */
    private double calcworkinws(double rpm, double p) {
    	double work = 0.0;
    	long timediff = 0;
		Date aktzeit = new Date();

		if (wirdgetreten(rpm)) {	// wird getreten ?
			if (lasttime != 0) {
				timediff = aktzeit.getTime() - lasttime;  // Zeitdifferenz zum letzten Aufruf
				work = p * (double) timediff / 1000.0;
				trainingsmillisek += timediff;
			}
			lasttime = aktzeit.getTime();
    	} else {
    		lasttime = 0;
    	}
    	return work;
    }
    
	/**
	 * gegnerischen Satz (gegnerzp) zu der übergebenen Zeit ermitteln und zurückgeben
	 * @param sek Sekunden seit Start des Videos
	 * @return gegnerzp-objekt, das die benötigten Daten zu dem Zeitpunkt enthält 
	 */
	public GegnerZP getGegnerZp(long sek){  
		Iterator<?> it = aktgegner.gegnerdaten.iterator();
		GegnerZP aktzp = (GegnerZP) it.next(); 
		while(it.hasNext() && aktzp.getSekseitStart() < sek) {
			aktzp = (GegnerZP) it.next(); 
		}
		return aktzp;
	}

	/**
	 * Hier wird nachgeprüft, ob überhaupt getreten wird.
	 * Die Frequenz muss über dem Minimalwert sein, falls ein Wahoo KICKR verwendet wird,
	 * dann wird true zurückgegeben (keine Drehzahlermittlung)
	 * @param rpm  RPM
	 * @return true oder false
	 */
	private boolean wirdgetreten(double rpm){
		if  (thisTrainer.trainerModell.equals("wahookickr"))
			return true;

		if (rpm > drpmmin) 	// wird getreten ?
			return true;
		else
			return false;
	}
	
	/**
	 * Unterprogramm für Prüfung auf Autostart. Wird aktuell 1 mal pro 2 Sekunden aufgerufen von Timer1
	 */
	private void DoPeriods() {
		double drpm;
		// Prüfung auf Autostart
		pausensek += 2;
		if ((aktstatus == Status.autostopped) && (thisTrainer.hatverbindung() != Trainer.kommunikation.keine || thisTrainer.getTrainerModell().startsWith("ant+")) && !demomodus) { // Kommunikation vorhanden?      
			ergorpm = thisTrainer.talk(Trainer.Commands.rpm, 0);
			//Mlog.debug("Debug(DoPeriods)! ergorpm: "+ergorpm);
			if (ergorpm.isEmpty())
				drpm = 0.0;
			else
				drpm = new Double(ergorpm);

			if (wirdgetreten(drpm)) {
				videopause();  // wieder starten ...
				aktstatus = Status.laeuft;
			}
		}		
		else {
			if (demomodus && wirdgetreten(demorpm)) {
				videopause();  // wieder starten im Demomodus ...
				aktstatus = Status.laeuft;
			} 
		}
	}
	
	/**
	 * Der Timer wird mit neuer Rate aufgesetzt.
	 * 
	 * @param rate neue Rate
	 */
	private void Rescheduletimer(double rate) {
		    if (timer != null) {
			    timer.cancel();
				timer = new Timer();
			    //timer.schedule(new SimTask(), (long) (1000/rate), (long) (1000/rate)); 
			    timer.scheduleAtFixedRate(new SimTask(), (long) (1000/rate), (long) (1000/rate));
		    }
	}
	
	/**
	 * Was beim betätigen der Starttaste (oder "*") passieren soll, steht hier.
	 */
	public void doStart() {
		if (!pausetaste) {
			if (init) {
				rtStartSek = new Date().getTime() / 1000;
				pausensek = 0;
				work = 0.0;
				dletztehoehe = 0.0;
				Leistung.reset();
				Puls.reset();
				Kurbel.reset();
				Geschwindigkeit.reset();
				changeGang(config.getInt("gang"));			
				init = false;
			}
			lasttime = 0;		// für kCal-Berechnung

			Mlog.debug("doStart: Verbindung: "+thisTrainer.hatverbindung()+" - Status: "+aktstatus);
			if ((thisTrainer.hatverbindung() == Trainer.kommunikation.netzwerk || thisTrainer.hatverbindung() == Trainer.kommunikation.seriell) && 
					aktstatus != Status.angehalten && aktstatus != Status.autostopped) {
  	  			if (thisTrainer.trainerModell.equals("cyclus2_el") || thisTrainer.trainerModell.equals("cyclus2")) {
  	  				thisTrainer.talk(Trainer.Commands.setpc, 0.0);   // Cyclus2 Ergoline: ergo=1
  	  			}
				if (!wettkampf.isAktiv()) {
					if (thisTrainer.trainerModell.equals("kettler") || thisTrainer.trainerModell.equals("kettlercr")) {
						thisTrainer.talk(Trainer.Commands.reset, 0.0);  // Reset für Kettler
						thisTrainer.talk(Trainer.Commands.init, 0.0);  
					}
				}
			}
			
	  		lblGang.setVisible(true);
			txtGegner.setText("");
			if (wettkampf.isLanRaceAuto()) {				// nicht direkt starten sondern Rennen eintragen und Countdown
				String rennenname = wettkampf.vwRace.doAutostart(wettkampf.lanRaceServer);	
				if (rennenname == null && wettkampf.lanRaceServer == false) {
					wettkampf.aktualisiereAusDB(0);
					rennenname = wettkampf.cmbOnlineRennen.getText();
					Mlog.debug("Client hat ermittelt Rennennamen: " + rennenname);
					if (rennenname == null) {
						Messages.errormessage(Messages.getString("Onlinerennen.serverantwortetnicht"));
						return;
					}
				}
				wettkampf.aktivesRennen = rennenname;
				if (wettkampf.konfigonline() == false) {
					Messages.infomessage(Messages.getString("OnlineRennen.fehlerconfig")); 
					return;
				}
				wettkampf.rennanmeldung(rennenname);
				toolbar.disableItems4Race();
				toolbar.actionStart.setEnabled(false);
				toolbar.actionStop.setEnabled(false);
				wettkampf.warteStartfreigabe();
				toolbar.actionStart.setEnabled(true);	// Startbutton enablen
				toolbar.actionStop.setEnabled(true);	// Stopbutton enablen
			} 
			videostart();
			toolbar.changeImgStartbutton("pause.png");
			pausetaste = true;
			sShell.setMinimized(false);
			sShell.forceActive();
			sShell.forceFocus();
		} else {	// Pause
			dletztehoehe = 0.0;
			videopause();
			atZeit.show(0, Messages.getString("Rsmain.pause"), 0);
			toolbar.changeImgStartbutton("play.png");
			pausetaste = false;
		}
	}
	
	/**
	 * Was beim betätigen der Stoptaste (oder "/") passieren soll, steht hier.
	 * @param vidstop			VLC stoppen (Achtung, manchmal  wird Deadlock verursacht)
	 * @param showAuswertung	Auswertung anzeigen oder nicht
	 */
	public void doStop(boolean vidstop, boolean showAuswertung) {
		if (vidstop)
			videostop();
		if (showAuswertung) {
			auswertung.doAuswertung();
			setAusgewertet(true);
		}
		wettkampf.setAktiv(false);
		wettkampf.setCsvAktiv(false);
		wettkampf.setVirtGeg(false);
		wettkampf.setVirtZP(false);
		toolbar.changeImgStartbutton("play.png");
		pausetaste = false;
		aktstatus = Status.beendet;
		if (thisTrainer.hatverbindung() == Trainer.kommunikation.netzwerk || thisTrainer.hatverbindung() == Trainer.kommunikation.seriell) {
	  		if (thisTrainer.trainerModell.equals("cyclus2_el") || 
	  			thisTrainer.trainerModell.equals("cyclus2"))  {
	  				thisTrainer.talk(Trainer.Commands.setpc_aus, 0.0);   // Cyclus2 Ergoline: ergo=0
	  		}
		}
	}

	/**
	 * Bikerprofil wechseln zum nächsten Profil
	 */
	public void changeBikerprofil() {
    	int selprofil = cmbfahrer.getSelectionIndex();
    	selprofil += 1;
    	if (selprofil >= cmbfahrer.getItemCount()) {
    		selprofil = 0;
    	}
    	cmbfahrer.select(selprofil);
    	biker.setName(cmbfahrer.getText());
		if (aktstatus == Status.laeuft)
			showOVD(0, 90, 1500, 100, cmbfahrer.getText());
    	Profildatei = Global.strPfad+cmbfahrer.getText()+".xml";  
        Mlog.info(Messages.getString("Rsmain.profildatei_dp")+Profildatei);  
		newkonfig.createXMLFileSettings(Global.standardsettingsdatei, biker);
		newkonfig.loadProfil(Profildatei, biker, thisTrainer);
	}

	/**
	 * Rückenwind erhöhen
	 */
	public void changeRueckenwind() {
    	int selwind = cmbwind.getSelectionIndex();
    	selwind += 1;
    	if (selwind >= cmbwind.getItemCount()) {
    		selwind = 0;
    	}
    	setWindAndShow(selwind);
	}
	
	/**
	 * Gegenwind erhöhen
	 */
	public void changeGegenwind() {
    	int selwind = cmbwind.getSelectionIndex();
    	selwind -= 1;
    	if (selwind <= 0) {
    		selwind = 0;
    	}
    	setWindAndShow(selwind);
	}

	/**
	 * Windeinstellung durchzappen (erhöhen und bei Überschreitung auf max. Rückenwind)
	 */
	public void zappWind() {
    	int selwind = cmbwind.getSelectionIndex();
    	selwind -= 1;
    	if (selwind < 0) {
    		selwind = cmbwind.getItemCount()-1;
    	}
    	setWindAndShow(selwind);
	}

	/**
	 * Setzt die Windeinstellung und zeigts kurz an.
	 * @param index		Windstärke in Beaufort (Rückenwind:+, Gegenwind:-) setzen
	 */
	public static void setWindAndShow(int index) {
    	cmbwind.select(index);
		if (aktstatus == Status.laeuft)
			showOVD(0, 100, 3000, 100, cmbwind.getText());
    	setWind();		
	}
	
	/**
	 * Gangschaltung: auf den entspechenden Gang schalten.
	 * @param gang	eingelegter Gang (1..9)
	 */
	public void changeGang(int gang) {
		if (!atKurbel.getEnabled(2))
			return;
   		lblGang.setImage(new Image(Display.getCurrent(), "g"+gang+".png")); 
    	thisTrainer.setGang(gang);
		atKurbel.show(2, gang+"", 0);
    	grate = (double) gang / 10.0 + 0.5;
    	if (isVlc_ein() && (libvlc != null)) {
   				libvlc.libvlc_media_player_set_rate(mediaplayer, (float) (grate * drate));
        		if (aktstatus == Status.laeuft)
        			showOVD(0, 180, 1500, 100, gang+"");
    	}
		else { // GPX-Tour ohne Video?
			Rescheduletimer(grate*drate);
		}
	}
	
	/**
	 * Gangschaltung: auf den nächstniedrigeren Gang schalten.
	 */
	public void changeGangrunter() {
		if (!atKurbel.getEnabled(2))
			return;
    	int aktgang = thisTrainer.getGang();
    	if (aktgang > 1) {
    		aktgang--;
    		lblGang.setImage(new Image(Display.getCurrent(), "g"+aktgang+".png")); 
    		thisTrainer.setGang(aktgang);
			atKurbel.show(2, aktgang+"", 0);
    		grate = (double) aktgang / 10.0 + 0.5;
    		if (isVlc_ein() && (libvlc != null)) {
   				libvlc.libvlc_media_player_set_rate(mediaplayer, (float) (grate * drate));
        		if (aktstatus == Status.laeuft)
        			showOVD(0, 180, 1500, 100, aktgang+"");
    		}
			else { // GPX-Tour ohne Video?
				Rescheduletimer(grate*drate);
			}
    	}
	}
	
	/**
	 * Gangschaltung: auf den nächsthöheren Gang schalten.
	 */
	public void changeGanghoch() {
		if (!atKurbel.getEnabled(2))
			return;
    	int aktgang = thisTrainer.getGang();
    	if (aktgang < maxgang) {
    		aktgang++;
    		lblGang.setImage(new Image(Display.getCurrent(), "g"+aktgang+".png"));  
    		thisTrainer.setGang(aktgang);
			atKurbel.show(2, aktgang+"", 0);
    		grate = aktgang / 10.0 + 0.5;
    		if (isVlc_ein() && (libvlc != null)){
    			libvlc.libvlc_media_player_set_rate(mediaplayer, (float) (grate * drate));
        		if (aktstatus == Status.laeuft)
        			showOVD(0, 180, 1500, 100, aktgang+"");
    		}
			else { // GPX-Tour ohne Video?
				Rescheduletimer(grate*drate);
			}
    	}
	}

	/** 
	 * Leistungsfaktor erhöhen
	 * @param pfakt		neuer Leistungsfaktor
	 */
	public void changeLFhoch(double pfakt) {
    	double lf = biker.getLfakt();
    	lf += pfakt;
    	if (lf >= 2.0) {
    		lf = 2.0;
    	}
    	biker.setLfakt(lf);
    	setWind();
	}
	
	/**
	 * Leistungsfaktor vermindern
	 * @param pfakt		neuer Leistungsfaktor
	 */
	public void changeLFrunter(double pfakt) {
    	double lf = biker.getLfakt();
    	lf -= pfakt;
    	if (lf <= 0.05) {
    		lf = 0.05;
    	}
    	biker.setLfakt(lf);
	}

	/**
	 * Ausgabe eines Textes im Video. Der Text wird eine vorgegebene Dauer angezeigt.
	 * @param pos		Position im Bild
	 * @param size		Fontgröße des Ausgabetextes
	 * @param dauer		Dauer der Anzeige
	 * @param opacity	Durchsichtigkeitsfaktor
	 * @param text		anzuzeigender Text
	 */
	public static void showOVD(int pos, int size, int dauer, int opacity, String text) {
		if (isVlc_ein() && (libvlc != null)) {
			int videoweite = libvlc.libvlc_video_get_width(mediaplayer);
			size = size * videoweite / 960;
			ovdoutput(pos, opacity, size, dauer, text);
		}		
	}

	/**
	 * Der VLC-Path wird nun unter Windows aus der Registry gelesen.
	 * (Bei Linux wird er unter /usr/bin erwartet.)
	 * @param  regPath Registrierungspfad
	 * @param  regKey  Registrierungsschlüssel
	 * @return Pfad zum VLC-Player (ohne vlc)
	 */
	public static String getRegKey(String regPath, String regKey){
		//System.out.println("Betriebssystem: " + Global.osName);
		//if (Global.osName.startsWith("Windows")) {
		String command = "reg query " + regPath + " /v " + regKey;

		Runtime runtime = Runtime.getRuntime();
		StringBuffer result = new StringBuffer();

		try{
			Process process = runtime.exec(command);
			byte[] data = new byte[1024];
			int cnt = process.getInputStream().read(data);

			while(cnt > 0){
				result.append(new String(data, 0, cnt));
				cnt = process.getInputStream().read(data);
			}
		}catch(Exception e){
			Mlog.ex(e);
		}

		String[] splitResult = result.toString().split("REG_SZ");

		return splitResult[splitResult.length-1].trim();
	}

	/**
	 * Auswertung der Kommandozeilenparameter mittels JSAP
	 * @param args
	 */
	static private void parseCommandLine(String[] args) {
		JSAP jsap = new JSAP();

		// erweiterte Debugmeldungen im Logfile
        Switch swDebug = new Switch("debug")
        	.setShortFlag('d')
        	.setLongFlag("debug");
        
        // erweiterte Kommunikationsmeldungen (alle Kanäle)
        Switch swDeepDebug = new Switch("deepdebug")
    		.setShortFlag('D')
    		.setLongFlag("deepdebug");
        
        // Demomodus ohne Kommunikation zum Trainingsgerät
        Switch swDemo = new Switch("demo")
    		.setShortFlag('q')
    		.setLongFlag("demo");
    
        // Umstellung auf Englisch
        Switch swEnglisch = new Switch("englisch")
			.setShortFlag('e')
			.setLongFlag("englisch");

        // VLC-Overlaymodus abschalten (gut z.B. für Screenshots)
        Switch swNoOverlay = new Switch("nooverlay")
			.setShortFlag('o')
			.setLongFlag("nooverlay");
        
        // Gang vorgeben
        FlaggedOption optGang = new FlaggedOption("gang")
        	.setStringParser(JSAP.INTEGER_PARSER)
        	.setRequired(true) 
        	.setShortFlag('g') 
        	.setLongFlag("gang")
        	.setDefault("5");

        // Endlosbetrieb 
        Switch swLoop = new Switch("loop")
			.setShortFlag('l')
			.setLongFlag("loop");

        // Tourdatei vorgeben
        FlaggedOption optTour = new FlaggedOption("tour")
        	.setStringParser(JSAP.STRING_PARSER)
        	.setRequired(false) 
        	.setShortFlag('f') 
        	.setLongFlag("tour");

        // Autostopp abschalten
        Switch swNoStop = new Switch("nostop")
			.setShortFlag('a')
			.setLongFlag("nostop");
        
        // Sofort die Tour ohne Rückfrage starten (sinnvoll nur in Verbindung mit "tour")
        Switch swAutostart = new Switch("start")
			.setShortFlag('s')
			.setLongFlag("start");
        
        // ohne Menü und Toolbar (zurück mit ESC)
        Switch swNoMenu = new Switch("nomenu")
			.setShortFlag('m')
			.setLongFlag("nomenu");
        
//        Switch swGPXEdit = new Switch("gpxedit")
  //      	.setShortFlag('x')
  //      	.setLongFlag("gpxedit");
        
        try {
			jsap.registerParameter(swDebug);
			jsap.registerParameter(swDeepDebug);
			jsap.registerParameter(swDemo);
			jsap.registerParameter(swEnglisch);
			jsap.registerParameter(swNoOverlay);
			jsap.registerParameter(optGang);
			jsap.registerParameter(swLoop);
			jsap.registerParameter(optTour);
			jsap.registerParameter(swNoStop);
			jsap.registerParameter(swAutostart);
			jsap.registerParameter(swNoMenu);
//			jsap.registerParameter(swGPXEdit);
		} catch (JSAPException e) {
			e.printStackTrace();
		}
        
        config = jsap.parse(args);  
        if (config.getBoolean("debug")) {
        	Mlog.setDebugstatus(true);
        }
        if (config.getBoolean("deepdebug")) {
        	Mlog.setDebugstatus(true);
        	thisTrainer.setDeepdebug(true);
        }
		if (config.getBoolean("demo")) {  
			demomodus = true;
		}				
		if (config.getBoolean("englisch")) {  
			Messages.setbundle("messages_en"); 
		}				
		if (config.getBoolean("nooverlay")) {
			strvlcaddparams = "--no-overlay";
		}				
		if (config.getBoolean("loop")) {  
			endlos = true;
		}				
		if (config.getBoolean("nostop")) {
			Global.noAutoStop = true;
		}				
		if (config.getBoolean("start")) {
			Global.autoStart = true;
		}
		if (config.getBoolean("nomenu")) {
			Global.fullscreen = true;
		}
//		if (config.getBoolean("gpxedit")) {
//			if (gpxEdit == null)
//				gpxEdit = new GPXEdit1();
//		}
		
	}
	

	/**
	 * Initialisierung des MTBS als Netzwerktraining.
	 * Wird beim Aufruf -n <serverurl> oder -server <serverurl> übergeben, dann wird diese Methode beim Start aufgerufen.
	 * serverurl: 
	 *   localhost (initialisierung als Server)
	 *   IP-Adresse (initialisierung als Client)
	 * @param server = serverurl
	 */
	static public void initNetzwerktraining(String server) {
		InetAddress inet;
		if (Rsmain.server == null)
			Rsmain.server = new Server();
		
		try {
			inet = InetAddress.getByName(server);
			
			if (!inet.isReachable(2000)) {
				Messages.errormessage("Der Server: "+server+" ist nicht erreichbar!");
				return;
			}
			
			Global.serverAdr = server;
			wettkampf.setLanRaceAuto(true);
			wettkampf.createOnlineRennen(server);
		} catch (UnknownHostException e) {
			Messages.errormessage("Der Server: "+server+" ist unbekannt!");
		} catch (IOException e) {
			Messages.errormessage("Der Server: "+server+" ist nicht erreichbar!");
		}
	}
	
	/**
	 * Hier wird eine lfd. Instanz erkannt und der Benutzer darauf aufmerksam gemacht.
	 * Verwendet wird dazu die Lockdatei im Userverzeichnis.
	 */
	private void checkLock() {
	    final File f = new File(Global.strPfad + Global.lockdatei);
	    if (f.exists()) {
	    	f.delete();
	    	Messages.errormessage(Messages.getString("Rsmain.checkLock"));
	    }
	    try {
			f.createNewFile();
		} catch (IOException e) {
			Mlog.ex(e);
		}
		
	    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	    	public void run() {
	    		f.delete();
	    	}
	    }));
	}
	
	/**
	 * Initialisierung der OSM-Karte
	 * 
	 */
	private void initMap() {
        osmv = new OSMViewer(compMap, SWT.NONE, VerwaltungGPX.track);
    	osm_koord = new OSMViewer.PointD(rs_lon, rs_lat);
        Point size = compMap.getSize();
        Point pos =  osmv.computePosition(osm_koord);
    	Point mittelpunkt = new Point(pos.x, pos.y);
        osmv.setBounds(0, 0, size.x, size.y);
        // Karte auf Startpunkt setzen:
        if (VerwaltungGPX.track != null) {
			TrkPt aktpoint = (TrkPt) VerwaltungGPX.track.get(0); 
			OSMViewer.PointD koord = new OSMViewer.PointD(aktpoint.getLongitude(), aktpoint.getLatitude());
			pos = osmv.computePosition(koord);
	    	mittelpunkt = new Point(pos.x, pos.y);
        }
        osmv.setCenterPosition(mittelpunkt);
        osmv.redraw();
	}

	/**
	 * Anzeige der Infomeldung (über Webservice getInfoURL)
	 */
	private void showStartInfo() {
		MTBSRaceServiceStub stub;
		try {
			stub = new MTBSRaceServiceStub();
			ConnectDB connect = new ConnectDB();
			connect.setDb(Global.db);
			stub.connectDB(connect);

			GetInfoURL url = new GetInfoURL();
			url.setDb(Global.db);
			url.setUrl(Global.infoURL);
			GetInfoURLResponse urlres = stub.getInfoURL(url);
			String urlRet = urlres.get_return();
			Mlog.debug("getInfoURL: " + urlRet); 
			if (!urlRet.isEmpty()) {
				Global.infoURL = urlRet;
				Messages.showHTMLImBrowser(urlRet);				
			}
		} catch (Exception e1) {
			Mlog.error("Fehler beim Webservice GetInfoURL()");
		}
	}

	/**
	 * liefert die neueste Versionsnr. des RS aus der URL versionURL (vom Server)
	 * @return	neueste Version
	 * @throws Exception
	 */
    public static String getLatestVersion() throws Exception
    {
        String data = getData(versionURL);
        int strLaenge = data.length();
        if (strLaenge > 0)
        	return data.substring(0, strLaenge-1);
        else 
        	return data;
    }
       
    /**
     * Interne Funktion um die Versionsnummer zu ermitteln.
     * Die Funktion kann aber generell für den Zugriff auf Dateien im Internet verwendet werden.
     * @param address
     * @return
     * @throws Exception
     */
    private static String getData(String address)throws Exception
    {			
    	URL url = new URL(address);       
    	InputStream html = null;
    	int c = 0;
    	StringBuffer buffer = new StringBuffer("");
    	try {
    		html = url.openStream();

    		while(c != -1) {
    			c = html.read();
    			buffer.append((char) c);
    		}
    	} catch (Exception e) {
//    		Mlog.ex(e);
			Mlog.error("Fehler beim Zugriff auf " + address + " Fehler: " + e.getMessage());
		}
    	return buffer.toString();
    }

    /**
	 * Es werden die Werte, die über Schnittstelle ermittelt werden auf Grenzwerte 
	 * überprüft und evtl. begrenzt.
	 * @param wert		Wert der geprüft wird
	 * @param maxWert	maximaler Wert, Begrenzung wenn größer
	 * @param minWert	minimaler Wert, Begrenzung wenn kleiner
	 * @return			Rückgabewert
	 */
	public double checkWertMaxMin(double wert, double maxWert, double minWert) {
		if (wert > maxWert) {
			return (maxWert);
		} 
		if (wert < minWert) {
			return (minWert);
		} 
		return (wert);
	}
	
	/**
	 * ********** Hauptprogramm ****************
	 * Aufruf z. B. mit: java -jar rs.jar <-d> <-q>
	 * -d : Debugmodus
	 * -q : Quiet (Demomodus)
	 * -e : english-mode
	 * -t : Testmode (Demoversion)
	 * u.v.m.
	 * @param args  Argumente
	 */
	public static void main(String[] args) {
		Mlog.init();

		Rsmain.setExceptionHandler(new Rsmain.IExceptionHandler() {			
			@Override
			public void handleException(Throwable error) {
				Mlog.ex(error);
			}
		});

		app = new Rsmain();
		
		parseCommandLine(args);
		
		display = Display.getDefault();
		
    	user32ext = (User32ext) Native.loadLibrary("user32",User32ext.class);
		
		try {
			app.open();			
		} catch (Exception e) {
			if (!imUpdate) {			// Beim Updatevorgang Exception unterdrücken
				Mlog.ex(e);
				try {
					if (Messages.isInitialized)
						Messages.errormessage(Messages.getString("Rsmain.ex"));
				} catch (Exception e1) {
					Mlog.info("Programmabbruch!");  
				}
			}
		}
		

		// Ende? --> noch etwas aufräumen:
		if (libvlc != null && isVlc_ein()) {
	        libvlc.libvlc_media_release(mediaDescriptor);
			libvlc.libvlc_release(vlcinst);
		}
	    // ANT+ beenden
	    if (Rsmain.libant != null) {
	    	if (libant.isEin())
	    		libant.stop();
	    }
	    // alle möglichen Kommunikationskanäle schliessen (Schnittstelle wurde evtl. eben umgestellt)
	    try {
	    	thisTrainer.serclose();
			thisTrainer.netclose();
			thisTrainer.usbclose();			
		} catch (Exception e) {
			;
		}
	    
	    if (timer != null)
	    	timer.cancel();
	    if (timer1 != null)
	    	timer1.cancel();
	    
	    Mlog.info(Messages.getString("Rsmain.tschuess"));  
	    System.exit(0);
	}
	
	/**
	 * nur für Testzwecke, wird beim Start des RS aufgerufen.
	 */
	private void testMethode() {
		// hier ist Platz für Tests...
	}
}

