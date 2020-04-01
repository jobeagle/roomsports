import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

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
 * Global.java: 
 * Diese Klasse beinhaltet globale Funktionen und Werte die mehrfach benötigt
 * werden.
 *****************************************************************************
 */
public class Global {
	// globale Variablen
	public  static String ptz = null;	// Path-Trennzeichen sind unterschiedlich unter den Betriebssystemen
	public  static String progname = new String("RoomSports"); 
	public  static String versioncode = "4.35b";
    public  static String version = new String(progname+" Version " + versioncode);  
    public  static String ergoVersion = new String(Messages.getString("Rsmain.kein_ergo"));  
	public  static String db = "c2mtbsrace";  
	public  static String serverAdr = "";
	public  static String zeitzone = new String("Europe/Berlin");  
    public  static String standardprofildatei = "Standard.xml";		// initiale Profildatei (wenn keine andere ausgewählt) 
    public  static String standardsettingsdatei = "settings.xml";	// Dateiname der Settingsdaten    
    public  static String lockdatei = new String("rs.lock");		// Datei um lfd. Instanz zu erkennen   
    public  static String strPfad = null;       
    public  static String strVLCPfad = null;
    public  static String strbrowserOpen = null;
    
    public  static String lastGPXFile = null;
    public  static String javaRT = null;				// Java Runtime Path
    public  static String os = System.getProperty("os.name");
    public  static String osversion = System.getProperty("os.version");

    public  static String strProgramPfad = null;
    public  static String strTourvideo = new String("");
    public  static String gPXfile = new String("");
    public  static String infoURL = new String("");
    public  static String stravaKey = new String("");

    public  static boolean regValid = true;				// bisher false! freigeschaltet am 4.11.19
    public  static boolean autoStart = false;			// Verwendung für automatischen Tourstart
    public  static boolean noAutoStop = false;			// wenn aktiviert, dann keine Autostopp-Erkennung!
    public  static boolean comWatts = true;				// Leistung wird zum Trainingsgerät übertragen
    public  static boolean fullscreen = false;			// umschalten in Fullscreen ein/aus mittels F10
    public  static int     rennenUserID = 0;
    public  static long    aktGPSPunkt = 0;
    public  static long    lastTourGPSPkt = 0;

    public static Image posimage = new Image(Display.getCurrent(), "osm_pos.png");
    public static Image gegimage = new Image(Display.getCurrent(), "osm_gegpos.png");
    public static Image startimage = new Image(Display.getCurrent(), "osm_start.png");
    public static Image zielimage = new Image(Display.getCurrent(), "osm_ziel.png");

	public static DecimalFormat zfk2 = new DecimalFormat("0.00");  
	public static DecimalFormat zfk1 = new DecimalFormat("0.0"); 
	public static DecimalFormat zfk0 = new DecimalFormat("#");  
	public static int maxPuls = 220;				// Pulswert begrenzen
	public static int maxRPM = 250;					// RPM begrenzen
	public static int maxGeschw = 100;				// Geschwindigkeit begrenzen
	private static Toolkit tk = Toolkit.getDefaultToolkit();
	private static int resolution = tk.getScreenResolution();
	public static double resolutionFactor = ((double)resolution) / 96;	// ggf. für MACOS anpassen!
    private static int fontSize = (int) (8.0 / resolutionFactor);
	
    public static SimpleDateFormat tfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * setzt einheitliche Fonthöhe für das Widget
     * @param label label
     */
	public static void setFontSizeLabel(Label label) {
		FontData[] fD = label.getFont().getFontData();
		fD[0].setHeight(fontSize);
		label.setFont( new Font(Display.getCurrent(),fD[0]));
	}

    /**
     * setzt einheitliche Fonthöhe für das Widget
     * @param text text
     */
	public static void setFontSizeText(Text text) {
		FontData[] fD = text.getFont().getFontData();
		fD[0].setHeight(fontSize);
		text.setFont( new Font(Display.getCurrent(),fD[0]));
	}

    /**
     * setzt einheitliche Fonthöhe für das Widget
     * @param text text
     */
	public static void setFontSizeStyledText(StyledText text) {
		FontData[] fD = text.getFont().getFontData();
		fD[0].setHeight(fontSize);
		text.setFont( new Font(Display.getCurrent(),fD[0]));
	}

    /**
     * setzt einheitliche Fonthöhe für das Widget
     * @param button button
     */
	public static void setFontSizeButton(Button button) {
		FontData[] fD = button.getFont().getFontData();
		fD[0].setHeight(fontSize);
		button.setFont( new Font(Display.getCurrent(),fD[0]));
	}

    /**
     * setzt einheitliche Fonthöhe für das Widget
     * @param combo combo
     */
	public static void setFontSizeCCombo(CCombo combo) {
		FontData[] fD = combo.getFont().getFontData();
		fD[0].setHeight(fontSize);
		combo.setFont( new Font(Display.getCurrent(),fD[0]));
	}

    /**
     * setzt einheitliche Fonthöhe für das Widget
     * @param table table
     */
	public static void setFontSizeTable(Table table) {
		FontData[] fD = table.getFont().getFontData();
		fD[0].setHeight(fontSize);
		table.setFont( new Font(Display.getCurrent(),fD[0]));
	}

	public static class RegDat {
//		private static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy"); 

    	public static String sNr = "freie Version";			// 4.11.2019 vorher: ""
    	public static String userName = "RS";				// 4.11.2019 vorher: ""
    	public static String level = "R";					// D -> Demo  4.11.2019 vorher: "D"
    	public static String ablaufDatum = "31.12.2999";	// 4.11.2019 vorher: "31.12.2000"
    	
    	/**
    	 * Concateniert die Registrierungsdaten mit ~
    	 * @return Registrierungsdaten: sNr~userName~level~ablaufDatum
    	 */
    	public static String mergeRegDat() {
    		return sNr + "~" + userName + "~" + level + "~" + ablaufDatum; 
    	}
    	
    	/**
    	 * dekodiert den Eingangsstring, splitted die Einzeldaten heraus und speichert die
    	 * neuen Registrierungsdaten.
    	 * Bei RoomSports gibt es keine Registrierung mehr, wird nur wegem MTBS-Onlinetraining noch verwendet!
    	 * 
    	 * @param keyString
    	 * @return
    	 */
    	public static void setRegdatfromCode (String keyString) {
    		String[] regCodes = null;
    		if (keyString.isEmpty()) {
//				Mlog.error("Registrierungscode ist leer!");
//				regValid =  false;
				return;
    		}
    		
			byte[] decode;
			try {
				decode = DatatypeConverter.parseBase64Binary(keyString);
				InputStream is = new ByteArrayInputStream(decode);
				String decoded = new String(Mtbscipher.decode(is));
				//Mlog.debug("decodiert: " + decoded); 
				
				regCodes = decoded.split("~");
				if (regCodes.length != 4) {
					Mlog.error("falsche Anzahl der codierten Elemente im Registrierungscode!");
					regValid =  false;
					return;
				}
				sNr = regCodes[0];
				userName = regCodes[1];
				level = regCodes[2];
				ablaufDatum = regCodes[3];
				
//				Date dAblauf = format.parse(ablaufDatum);
//				if (dAblauf.before(new Date())) {
//					Mlog.error("der Registrierungscode war nur gültig bis: " + dAblauf);
//					regValid =  false;
//					return;
//				}
				
			} catch (Exception e) {
				Mlog.ex(e);
//				regValid =  false;
				return;
			} 

			regValid =  true;
			return;
    	}
    }
    

    
	// Klassen zur Überprüfung der Eingaben
	// nur Zahlen und Grossbuchstaben:
	static VerifyListener VLZahlenUndGrossbuchstaben = new VerifyListener() {
	      public void verifyText(VerifyEvent event) {
	          // nur Zeichen != 0 beachten:
	          char myChar = event.character;
	          if (myChar == 0)
		          event.doit = true; 
	          else
		          event.doit = false; 
	        	  
	          // erlaube Zahlen:
	          if (Character.isDigit(myChar))
	            event.doit = true;
	          // erlaube Zahlen:
	          if (Character.isUpperCase(myChar))
	            event.doit = true;
	          // und backspace und delete:
	          if (myChar == '\u0008' || myChar == '\u007F')
	            event.doit = true;
	      }
	};
	// nur Zahlen, Backspace und Delete:
	static VerifyListener VLZahlen = new VerifyListener() {
	      public void verifyText(VerifyEvent event) {
	          // nur Zeichen != 0 beachten:
	          char myChar = event.character;
	          if (myChar == 0)
		          event.doit = true; 
	          else
		          event.doit = false; 
	        	  
	          // erlaube nur Zahlen:
	          if (Character.isDigit(myChar))
	            event.doit = true;
	          // und backspace und delete:
	          if (myChar == '\u0008' || myChar == '\u007F')
	            event.doit = true;
	      }
	};
	// VLZahlen + Komma:
	static VerifyListener VLZahlenKomma = new VerifyListener() {
	      public void verifyText(VerifyEvent event) {
	          char myChar = event.character;
	          
	          // nur Zeichen != 0 beachten:
	          if (myChar == 0)
		          event.doit = true; 
	          else
		          event.doit = false; 
	        	  
	          // erlaube nur Zahlen:
	          if (Character.isDigit(myChar))
	        	  event.doit = true;
	          
	          // und backspace und delete:
	          if (myChar == '\u0008' || myChar == '\u007F')
	        	  event.doit = true;
	          // und Komma
	          if (myChar == ',')
	        	  event.doit = true;
	      }
	};
	// VLZahlen + Punkt:
	static VerifyListener VLZahlenPunkt = new VerifyListener() {
	      public void verifyText(VerifyEvent event) {
	          char myChar = event.character;
	          
	          // nur Zeichen != 0 beachten:
	          if (myChar == 0)
		          event.doit = true; 
	          else
		          event.doit = false; 
	        	  
	          // erlaube nur Zahlen:
	          if (Character.isDigit(myChar))
	        	  event.doit = true;
	          
	          // und backspace und delete:
	          if (myChar == '\u0008' || myChar == '\u007F')
	        	  event.doit = true;
	          // und Punkt
	          if (myChar == '.')
	        	  event.doit = true;
	      }
	};
	// VLZahlenKomma + Minus
	static VerifyListener VLZahlenKommaMinus = new VerifyListener() {
	      public void verifyText(VerifyEvent event) {
	          char myChar = event.character;
	          
	          // nur Zeichen != 0 beachten:
	          if (myChar == 0)
		          event.doit = true; 
	          else
		          event.doit = false; 
	        	  
	          // erlaube nur Zahlen:
	          if (Character.isDigit(myChar))
	        	  event.doit = true;
	          
	          // und backspace und delete:
	          if (myChar == '\u0008' || myChar == '\u007F')
	        	  event.doit = true;
	          // und Komma
	          if (myChar == ',')
	        	  event.doit = true;
	          // und Minus
	          if (myChar == '-')
	        	  event.doit = true;
	      }
	};
	
	/**
	 * Ermittelt die eigene MAC-Adresse (verwendet als Key fürs Netzwerktraining 
	 * @return	MAC-Adresse als String
	 */
	public static String getMacAddress() {
		String result = "";
		try {
			for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				byte[] hardwareAddress = ni.getHardwareAddress();

				if (hardwareAddress != null) {
					for (int i = 0; i < hardwareAddress.length; i++) {
						result += String.format((i == 0 ? "" : "") + "%02X", hardwareAddress[i]);
					}
					if (result.length() > 0 && !ni.isLoopback()) 
						return result;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Liefert den Nummerncode (nur die Ziffern aus versioncode, z.B. 4.06a liefert 406).
	 * Wird verwendet bei der Anmeldung zum Onlinerennen.
	 * @return Ziffern des Versioncodes
	 */
	public static int getVersionCodeNr() {
		String svnr = versioncode.replaceAll("\\D", "");
		Mlog.debug("svnr= " + svnr);
		return new Integer(svnr);
	}

	/**
	 * lass den Thread schlafen...
	 * @param millisecs Millisekunden
	 */
	public static void sleep(long millisecs) {
		try {
			Thread.sleep(millisecs);
		} catch (InterruptedException e) {
			Mlog.ex(e);
		}
	}
}
