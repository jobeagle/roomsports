import gnu.io.CommPortIdentifier;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jna.Platform;

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
 * Konfiguration.java: Die Konfigurationsdaten und der Konfigurationsdialog.
 *****************************************************************************
 *
 * Konfigurationsdialog fürs Profil inkl. speichern und laden mittels XML-Datei.
 *
 */
public class Konfiguration {
	private Shell sShell = null; 
	private Composite compkonfig = null;  
	private Text txtfgewicht = null;
	private Text txtbgewicht = null;
	private Text txtalter = null;
	private Text txtmaxleistung = null;
	private Text txtminleistung = null;
	private Text txtcwa = null;
	private Text txtk2 = null;
	private Text txtname = null;
	private Text txtmaxpuls = null;
	private Text txtergoip = null;
	private Text txtDynRPMNormal = null;
	private Text txtDynRPMWiege = null;
	private Text txtSteigungMin = null;
	private Text txtSteigungMax = null;
	private Text txtlfakt = null;
	private Text txtStravaKey = null;
	private Text txtpulsrot = null;
	private Text txtpulsgelb = null;
	private Text txtpulsgruen = null;
	private Text txtrpmrot = null;
	private Text txtrpmgelb = null;
	private Text txtrpmgruen = null;
	private Text txtprot = null;
	private Text txtpgelb = null;
	private Text txtpgruen = null;
	private Text txtAutomatik1 = null;
	private Text txtAutomatik2 = null;
	private Text txtTileserver = null;
	
	private Label labelgewicht = null;
	private Label labeldynnormal = null;
	private Label labeldynwiege = null;
	private Label labelSteigungMax = null;
	private Label labelSteigungMin = null;
	private Label labelmaxleistung = null;
	private Label labelminleistung = null;
	private Label labelcwa = null;
	private Label labelbgewicht = null;
	private Label labelk2 = null;
	private Label labelpulsampel = null;
	private Label imgpulsampel = null;
	private Label labelrpmampel = null;
	private Label imgrpmampel = null;
	private Label labelpampel = null;
	private Label imgpampel = null;
	private Label labelAutomatik2 = null;
	private Label labelBelohnung = null;
	private Label labelRahmenfarbe = null;
	private Label labelmaxpuls = null;
	private Label labelinterface = null;
	private Label labelergoip = null;
	
	private boolean liveprofil = true;
    private boolean averaging = true;
    private boolean dynamik = false; 
    private boolean tcxpower = true;
    private boolean nooverlay = false;  
    private boolean showmap = false;
    private boolean mapchg = false;
    private boolean showInfo = true;
    private boolean autoOK = false;
    
	private Button butok = null;
	private Button butreset = null;
	private Button butsuche = null;
	private Button butcolor = null;
    private Document dom;  
	private Button chkAveraging = null;
	private Button chkTCXPower = null;
	private Button chkDynamik = null;
	private Button chkAutomatik = null;
	private Button chkOverlay = null;
	private Button chkAntpPuls = null;
	private Button chkAntpCadenceSpeed = null;
	private Button chkAntpCadence = null;
	private Button chkMap = null;
	private Button chkInfo = null;
	private Button chkAutoOK = null;
	private Button chkSwitchXY = null;
	private Button chkSwitchJpg = null;
	
	//private String Profildatei = null;  
	private CCombo cmbinterface = null;
	private CCombo cmbtrainer = null;
	private CCombo cmbspart = null;
	private CCombo cmbLF = null;
	private CCombo cmbBelohnung = null;
	private String[] Interfaces;
	private int AnzTrainer = 18;
	private String[] trainer = new String[AnzTrainer];
	private String[] Trainertypen = { "daum", "daum2001", "kettler", "tacx1", "tacx2", "daumcr", "kettlercr", 
			"tacxvortex", "tacxbushido", "ant+gsc10", "cyclus2_el", "cyclus2", "ergofit", "ergofitcr", "antfec",
			"ergoline", "wahookickr", "ant+rpm" };  // bei Erweiterung AnzTrainer anpassen
	private int AnzSpArt = 3;
	private String[] SPArt = new String[AnzSpArt];  // Sportart (aktuell MTB, Rennrad, Langlauf-Ski)
	private int AnzLFStufen = 5;
	private String[] LFStufen = new String[AnzLFStufen];   
	private int AnzBelohnung = 11;
	public  String[] Belohnung = new String[AnzBelohnung];
	public  int[] BelohnungkCal = new int[AnzBelohnung];
    private final int maxintf = 20;
    private final int maxNameLength = 15;
    private String strTourenpfad = Global.strProgramPfad+"touren";  
    private String regCode = null;
    private String txtErgoIPOld = null;
    private int indcmbInterfaceOld = 0;
    private int indcmbTrainerOld = 0;
    
    /**
	 * @return the tcxpower
	 */
	public boolean isTcxpower() {
		return tcxpower;
	}

	/**
	 * @param tcxpower the tcxpower to set
	 */
	public void setTcxpower(boolean tcxpower) {
		this.tcxpower = tcxpower;
	}

	/**
     * Getterfunktion für liveprofil-Flag
     * @return liveprofil
     */
	public boolean isliveprofil() {
		return liveprofil;
	}

	/**
	 * Setterfunktion für liveprofil-Flag
	 * @param liveprofil Liveprofil anzeigen?
	 */
	public void setliveprofil(boolean liveprofil) {
		this.liveprofil = liveprofil;
	}

	/**
	 * Getterfunktion für Averaging-Flag (Glättung des Höhenprofils)
	 * @return averaging
	 */
	public boolean isaveraging() {
		return averaging;
	}

	/**
	 * Setterfunktion für Averaging-Flag (Glättung des Höhenprofils)
	 * @param averaging Glättung ein/aus
	 */
	public void setaveraging(boolean averaging) {
		this.averaging = averaging;
	}

	/**
	 * Getterfunktion für den Dynamikmodus (Videogeschw. abh. von Pedalumdrehungen
	 * @return the dynamik
	 */
	public boolean isDynamik() {
		return dynamik;
	}

	/**
	 * Setterfunktion für den Dynamikmodus
	 * @param dynamik the dynamik to set
	 */
	public void setDynamik(boolean dynamik) {
		this.dynamik = dynamik;
	}

	/**
	 * @return the strTourenpfad
	 */
	public String getStrTourenpfad() {
		return strTourenpfad;
	}

	/**
	 * @param strTourenpfad the strTourenpfad to set
	 */
	public void setStrTourenpfad(String strTourenpfad) {
		this.strTourenpfad = strTourenpfad;
	}

	/**
	 * @return the regCode
	 */
	public String getRegCode() {
		return regCode;
	}
	
	/**
	 * @param regCode the regCode to set
	 */
	public void setRegCode(String regCode) {
		this.regCode = regCode;
	}
	
	/**
	 * @return the overlay
	 */
	public boolean isNoOverlay() {
		return nooverlay;
	}

	/**
	 * @param nooverlay setze Videoanzeige auf nooverlay (kann bei modernen Rechnern eingeschaltet bleiben)
	 */
	public void setNoOverlay(boolean nooverlay) {
		this.nooverlay = nooverlay;
	}

	/**
	 * @return the showmap
	 */
	public boolean isShowmap() {
		return showmap;
	}

	/**
	 * @param showmap the showmap to set
	 */
	public void setShowmap(boolean showmap) {
		this.showmap = showmap;
	}

	/**
	 * @return the mapchg
	 */
	public boolean isMapchg() {
		return mapchg;
	}

	/**
	 * @param mapchg the mapchg to set
	 */
	public void setMapchg(boolean mapchg) {
		this.mapchg = mapchg;
	}

	/**
	 * @return showInfo
	 */
	public boolean isShowInfo() {
		return showInfo;
	}

	/**
	 * @param showInfo setzt showInfo
	 */
	public void setShowInfo(boolean showInfo) {
		this.showInfo = showInfo;
	}

	/**
	 * @return autoOK
	 */
	public boolean isAutoOK() {
		return autoOK;
	}

	/**
	 * Konstruktor initialisiert Strings und Werte.
	 */
	public Konfiguration() {
		init();
	}
	
	/**
	 * Darstellung des Konfigurationsdialogs
	 * @param biker  Fahrer
	 * @param ergo   Trainingsgerät
	 */
	public void show(Fahrer biker, Trainer ergo) {
		getInterfaces();
		createSShell(biker, ergo);
		sShell.open();		 
	}
	
	/**
	 * aktiviert/deaktiviert verschiedene Controls
	 *
	 */
	public void enabledisableControls() {
		try {
			if (cmbinterface.getText().compareTo("LAN") == 0) {  // dann Netzwerksachen disablen und nullsetzen 
				txtergoip.setEnabled(true);
				butsuche.setEnabled(true);					
			} else {
				txtergoip.setText(""); 
				txtergoip.setEnabled(false);
				butsuche.setEnabled(false);
			}
			int ind = cmbtrainer.getSelectionIndex();
			switch (ind) {
			case 11:	// Cyclus 2
				txtSteigungMax.setEnabled(true);
				txtSteigungMin.setEnabled(true);
				labelSteigungMax.setEnabled(true);
				labelSteigungMin.setEnabled(true);
				labelgewicht.setEnabled(false);
				txtfgewicht.setEnabled(false);
				labelbgewicht.setEnabled(false);
				txtbgewicht.setEnabled(false);
				txtDynRPMNormal.setEnabled(true);
				txtDynRPMWiege.setEnabled(true);
				labeldynwiege.setEnabled(true);
				labeldynnormal.setEnabled(true);
				txtmaxleistung.setEnabled(false);
				txtminleistung.setEnabled(false);
				labelmaxleistung.setEnabled(false);
				labelminleistung.setEnabled(false);
				txtcwa.setEnabled(false);
				txtk2.setEnabled(false);
				labelcwa.setEnabled(false);
				labelk2.setEnabled(false);
				cmbspart.setEnabled(true);
				chkTCXPower.setEnabled(false);
				chkAutomatik.setEnabled(false);
				txtAutomatik1.setEnabled(false);
				txtAutomatik2.setEnabled(false);				
				labelAutomatik2.setEnabled(false);
				//setDynamik(true);
				//chkDynamik.setSelection(true);
				//chkDynamik.setEnabled(false);
				chkDynamik.setEnabled(true);
				cmbinterface.setEnabled(true);
				labelinterface.setEnabled(true);
				//cmbinterface.setText("LAN");
				break;
			case 16:	// Wahoo KICKR
				txtSteigungMax.setEnabled(false);
				txtSteigungMin.setEnabled(false);
				labelSteigungMax.setEnabled(false);
				labelSteigungMin.setEnabled(false);
				labelgewicht.setEnabled(false);
				txtfgewicht.setEnabled(false);
				labelbgewicht.setEnabled(false);
				txtbgewicht.setEnabled(false);
				txtDynRPMNormal.setEnabled(true);
				txtDynRPMWiege.setEnabled(true);
				labeldynwiege.setEnabled(true);
				labeldynnormal.setEnabled(true);
				txtmaxleistung.setEnabled(true);
				txtminleistung.setEnabled(true);
				labelmaxleistung.setEnabled(true);
				labelminleistung.setEnabled(true);
				txtcwa.setEnabled(true);
				txtk2.setEnabled(true);
				labelcwa.setEnabled(true);
				labelk2.setEnabled(true);
				cmbspart.setEnabled(true);
				chkTCXPower.setEnabled(true);
				chkAutomatik.setEnabled(false);
				txtAutomatik1.setEnabled(false);
				txtAutomatik2.setEnabled(false);				
				labelAutomatik2.setEnabled(false);
				setDynamik(true);
				chkDynamik.setSelection(true);
				chkDynamik.setEnabled(false);
				cmbinterface.setText("ANT+");
				cmbinterface.setEnabled(false);
				labelinterface.setEnabled(false);
				chkAntpCadenceSpeed.setEnabled(true);
				chkAntpCadence.setEnabled(true);
				break;
			case 3:
			case 4:
			case 7:
			case 8:
				 // Rollentrainer TACX
				txtSteigungMax.setEnabled(true);
				txtSteigungMin.setEnabled(true);
				labelSteigungMax.setEnabled(true);
				labelSteigungMin.setEnabled(true);
				txtDynRPMNormal.setEnabled(true);
				txtDynRPMWiege.setEnabled(true);
				labeldynwiege.setEnabled(true);
				labeldynnormal.setEnabled(true);
				txtmaxleistung.setEnabled(false);
				txtminleistung.setEnabled(false);
				labelmaxleistung.setEnabled(false);
				labelminleistung.setEnabled(false);
				txtcwa.setEnabled(false);
				txtk2.setEnabled(false);
				labelcwa.setEnabled(false);
				labelk2.setEnabled(false);
				cmbspart.setEnabled(false);
				chkTCXPower.setEnabled(false);
				chkAutomatik.setEnabled(false);
				txtAutomatik1.setEnabled(false);
				txtAutomatik2.setEnabled(false);				
				labelAutomatik2.setEnabled(false);
				labelgewicht.setEnabled(true);
				txtfgewicht.setEnabled(true);
				labelbgewicht.setEnabled(true);
				txtbgewicht.setEnabled(true);
				setDynamik(true);
				chkDynamik.setSelection(true);
				chkDynamik.setEnabled(true);
				cmbinterface.setEnabled(true);
				labelinterface.setEnabled(true);
				if (ind == 3 || ind == 4)			// TACX iMagic oder Fortius?
					cmbinterface.setText("USB");
				else {
					cmbinterface.setText("ANT+");
					chkAntpCadenceSpeed.setSelection(false);
					chkAntpCadenceSpeed.setEnabled(false);
					chkAntpCadence.setSelection(false);
					chkAntpCadence.setEnabled(false);
					chkAntpPuls.setSelection(false);
					chkAntpPuls.setEnabled(false);
				}
				break;
			case 9:
				 // Rollentrainer manuell mit Trittfrequenz/Speed GSC10
				txtSteigungMax.setEnabled(false);
				txtSteigungMin.setEnabled(false);
				labelSteigungMax.setEnabled(false);
				labelSteigungMin.setEnabled(false);
				txtDynRPMNormal.setEnabled(true);
				txtDynRPMWiege.setEnabled(true);
				labeldynwiege.setEnabled(true);
				labeldynnormal.setEnabled(true);
				txtmaxleistung.setEnabled(true);
				txtminleistung.setEnabled(true);
				labelmaxleistung.setEnabled(true);
				labelminleistung.setEnabled(true);
				txtcwa.setEnabled(true);
				txtk2.setEnabled(true);
				labelcwa.setEnabled(true);
				labelk2.setEnabled(true);
				cmbspart.setEnabled(true);
				chkTCXPower.setEnabled(true);
				chkAutomatik.setEnabled(false);
				txtAutomatik1.setEnabled(false);
				txtAutomatik2.setEnabled(false);				
				labelAutomatik2.setEnabled(false);
				labelgewicht.setEnabled(true);
				txtfgewicht.setEnabled(true);
				labelbgewicht.setEnabled(true);
				txtbgewicht.setEnabled(true);
				setDynamik(true);
				chkDynamik.setSelection(true);
				chkDynamik.setEnabled(false);
				chkAntpCadenceSpeed.setSelection(true);
				chkAntpCadenceSpeed.setEnabled(false);
				chkAntpCadence.setSelection(false);
				chkAntpCadence.setEnabled(false);
				cmbinterface.setText("");
				cmbinterface.setEnabled(false);
				txtmaxpuls.setEnabled(false);
				labelmaxpuls.setEnabled(false);
				labelinterface.setEnabled(false);
				labelergoip.setEnabled(false);
				break;
			case 14:
				 // Rollentrainer ANT+ FE-C
				txtSteigungMax.setEnabled(false);
				txtSteigungMin.setEnabled(false);
				labelSteigungMax.setEnabled(false);
				labelSteigungMin.setEnabled(false);
				txtDynRPMNormal.setEnabled(true);
				txtDynRPMWiege.setEnabled(true);
				labeldynwiege.setEnabled(true);
				labeldynnormal.setEnabled(true);
				txtmaxleistung.setEnabled(true);
				txtminleistung.setEnabled(true);
				labelmaxleistung.setEnabled(true);
				labelminleistung.setEnabled(true);
				txtcwa.setEnabled(true);
				txtk2.setEnabled(true);
				labelcwa.setEnabled(true);
				labelk2.setEnabled(true);
				cmbspart.setEnabled(true);
				chkTCXPower.setEnabled(false);
				chkAutomatik.setEnabled(false);
				txtAutomatik1.setEnabled(false);
				txtAutomatik2.setEnabled(false);				
				labelAutomatik2.setEnabled(false);
				labelgewicht.setEnabled(true);
				txtfgewicht.setEnabled(true);
				labelbgewicht.setEnabled(true);
				txtbgewicht.setEnabled(true);
				//setDynamik(true);
				//chkDynamik.setSelection(true);
				//chkDynamik.setEnabled(false);
				chkDynamik.setEnabled(true);
				chkAntpCadenceSpeed.setEnabled(true);
				chkAntpCadence.setEnabled(true);
				cmbinterface.setText("ANT+");
				cmbinterface.setEnabled(false);
				txtmaxpuls.setEnabled(false);
				labelmaxpuls.setEnabled(false);
				labelinterface.setEnabled(false);
				labelergoip.setEnabled(false);
				break;
			case 17:
				 // Rollentrainer manuell mit ANT+ nur Trittfrequenz
				txtSteigungMax.setEnabled(false);
				txtSteigungMin.setEnabled(false);
				labelSteigungMax.setEnabled(false);
				labelSteigungMin.setEnabled(false);
				txtDynRPMNormal.setEnabled(true);
				txtDynRPMWiege.setEnabled(true);
				labeldynwiege.setEnabled(true);
				labeldynnormal.setEnabled(true);
				txtmaxleistung.setEnabled(true);
				txtminleistung.setEnabled(true);
				labelmaxleistung.setEnabled(true);
				labelminleistung.setEnabled(true);
				txtcwa.setEnabled(true);
				txtk2.setEnabled(true);
				labelcwa.setEnabled(true);
				labelk2.setEnabled(true);
				cmbspart.setEnabled(true);
				chkTCXPower.setEnabled(false);
				chkAutomatik.setEnabled(false);
				txtAutomatik1.setEnabled(false);
				txtAutomatik2.setEnabled(false);				
				labelAutomatik2.setEnabled(false);
				labelgewicht.setEnabled(true);
				txtfgewicht.setEnabled(true);
				labelbgewicht.setEnabled(true);
				txtbgewicht.setEnabled(true);
				//setDynamik(true);
				//chkDynamik.setSelection(true);
				//chkDynamik.setEnabled(true);
				chkAntpCadenceSpeed.setSelection(false);
				chkAntpCadenceSpeed.setEnabled(false);
				chkAntpCadence.setSelection(true);
				chkAntpCadence.setEnabled(false);
				cmbinterface.setText("");
				cmbinterface.setEnabled(false);
				txtmaxpuls.setEnabled(false);
				labelmaxpuls.setEnabled(false);
				labelinterface.setEnabled(false);
				labelergoip.setEnabled(false);
				break;
			default:
				// versch. Ergometer / Crosstrainer, Ergoline
				txtSteigungMax.setEnabled(false);
				txtSteigungMin.setEnabled(false);
				labelSteigungMax.setEnabled(false);
				labelSteigungMin.setEnabled(false);
				txtmaxleistung.setEnabled(true);
				txtminleistung.setEnabled(true);
				labelmaxleistung.setEnabled(true);
				labelminleistung.setEnabled(true);
				txtcwa.setEnabled(true);
				txtk2.setEnabled(true);
				labelcwa.setEnabled(true);
				labelk2.setEnabled(true);
				cmbspart.setEnabled(true);
				chkTCXPower.setEnabled(true);
				labelgewicht.setEnabled(true);
				txtfgewicht.setEnabled(true);
				labelbgewicht.setEnabled(true);
				txtbgewicht.setEnabled(true);
				chkDynamik.setEnabled(true);
				if (chkDynamik.getSelection()) {
					setDynamik(true);
					txtDynRPMNormal.setEnabled(true);
					txtDynRPMWiege.setEnabled(true);
					labeldynwiege.setEnabled(true);
					labeldynnormal.setEnabled(true);
				}
				else {
					setDynamik(false);
					txtDynRPMNormal.setEnabled(false);
					txtDynRPMWiege.setEnabled(false);
					labeldynwiege.setEnabled(false);
					labeldynnormal.setEnabled(false);
				}
				if (chkAutomatik.getSelection()) {
					txtAutomatik1.setEnabled(true);
					txtAutomatik2.setEnabled(true);
					labelAutomatik2.setEnabled(true);
				}
				else {
					txtAutomatik1.setEnabled(false);
					txtAutomatik2.setEnabled(false);				
					labelAutomatik2.setEnabled(false);
				}				
				chkAntpCadenceSpeed.setSelection(false);
				chkAntpCadenceSpeed.setEnabled(false);
				chkAntpCadence.setSelection(false);
				chkAntpCadence.setEnabled(false);
				cmbinterface.setEnabled(true);
				labelinterface.setEnabled(true);
			}
		} catch (Exception e) {
			Mlog.ex(e);
		}		
	}
	
	/**
	 * Löschen der Konfigurationsdateien und Userprofile = RESET
	 */
	public void resetAll2FactoryDefaults() {
		if (Messages.entscheidungmessage(sShell, Messages.getString("konfig.resetallewerte"))) {
			File stdsettings = new File(Global.strPfad+Global.standardprofildatei);    		
			stdsettings.delete();
			stdsettings = new File(Global.strPfad+Global.standardsettingsdatei);    		
			stdsettings.delete();
			if (Messages.entscheidungmessage(sShell, Messages.getString("konfig.resetalleprofile"))) {
				File folder = new File(Global.strPfad);
						File[] files = folder.listFiles( new FilenameFilter() {
						    @Override
						    public boolean accept(File dir, String name) {
						        return name.matches( ".*\\.xml" );
						    }
						} );
						for ( final File file : files ) {
						    if ( !file.delete() ) {
						        Mlog.error("folgende Datei kann nicht gelöscht werden: " + file.getAbsolutePath());
						    }
						}
			}
			sShell.close();
			Messages.infomessage(Messages.getString("konfig.resetdone"));
			System.exit(0);
		}		
	}
	
	/**
	 * Ermittelt alle brauchbaren Interfaces des PCs.
	 * Es werden die seriellen Interfaces gesucht und USB", "LAN" und ANT+ angehängt
	 */
	public void getInterfaces() {
	    try {
			CommPortIdentifier serialPortId;
			Enumeration<?> enumComm;
			int i = 0;
			String[] tmpInterfaces = new String[maxintf];
			
			enumComm = CommPortIdentifier.getPortIdentifiers();
			while (enumComm.hasMoreElements() && i < maxintf) {
			 	serialPortId = (CommPortIdentifier) enumComm.nextElement();
			 	if(serialPortId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					Mlog.info(Messages.getString("konfig.interface_dp") + serialPortId.getName()); 
					tmpInterfaces[i++] = serialPortId.getName();
				}
			}
			
			Interfaces = new String[i+4];
			for (int j=0; j<i; j++) {
			    Interfaces[j] = tmpInterfaces[j];
			}
			
			Interfaces[i]   = "USB";      // USB wird immer vorausgesetzt... 
			Interfaces[i+1] = "LAN";      // LAN wird immer vorausgesetzt... 
			Interfaces[i+2] = "ANT+";     // ANT+
			Interfaces[i+3] = "";         // Demomodus 
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}
	
	/**
	 * Initialisierung des Fensters (Shell)
	 * @param biker der Fahrer
	 * @param ergo  das Trainingsgerät
	 */
	private void createSShell(Fahrer biker, Trainer ergo) {
		GridLayout gridLayout = new GridLayout();
		try {
			if (sShell == null) {
				sShell = new Shell(Rsmain.sShell.getDisplay(), SWT.TITLE);
				//sShell = new Shell(Rsmain.sShell.getDisplay(), SWT.TITLE | SWT.ON_TOP);
				sShell.setText(Messages.getString("konfig.konfiguration")); 
				sShell.setLayout(gridLayout);
				sShell.setBounds(Rsmain.aktcr.width/2+Rsmain.aktcr.x-230, Rsmain.aktcr.height/2+Rsmain.aktcr.y-250, 460, 500);
				createCompkonfig(biker, ergo);
			} else {
				if (!sShell.isVisible()) {
					initFelder(biker, ergo);
			        sShell.setVisible(true);
				}
			}
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * Initialisierung vom compkonfig (Composite)
	 * @param biker  Fahrer
	 * @param ergo	 Trainingsgerät
	 */
	private void createCompkonfig(final Fahrer biker, final Trainer ergo) {
		try {
			compkonfig = new Composite(sShell, SWT.NONE);
			
			// Textfelder
			txtname = new Text(compkonfig, SWT.BORDER);
			txtname.setToolTipText(Messages.getString("konfig.name_des_fahrers")); 
			txtname.setBounds(120, 0, 60, 20);
			Global.setFontSizeText(txtname);
			
			txtfgewicht = new Text(compkonfig, SWT.BORDER);
			txtfgewicht.setToolTipText(Messages.getString("konfig.bikergewicht")); 
			txtfgewicht.setBounds(120, 25, 60, 20);
			Global.setFontSizeText(txtfgewicht);

			txtalter = new Text(compkonfig, SWT.BORDER);
			txtalter.setToolTipText(Messages.getString("konfig.pulsbereich")); 
			txtalter.setBounds(120, 50, 60, 20);
			Global.setFontSizeText(txtalter);

			txtmaxleistung = new Text(compkonfig, SWT.BORDER);
			txtmaxleistung.setToolTipText(Messages.getString("konfig.leistung_ueber")); 
			txtmaxleistung.setBounds(120, 75, 60, 20);
			Global.setFontSizeText(txtmaxleistung);
			
			txtminleistung = new Text(compkonfig, SWT.BORDER);
			txtminleistung.setToolTipText(Messages.getString("konfig.leistung_unter")); 
			txtminleistung.setBounds(120, 100, 60, 20);
			Global.setFontSizeText(txtminleistung);
			
			txtmaxpuls = new Text(compkonfig, SWT.BORDER);
			txtmaxpuls.setToolTipText(Messages.getString("konfig.maxpulsrate")); 
			txtmaxpuls.setBounds(120, 125, 60, 20);
			Global.setFontSizeText(txtmaxpuls);
			
			txtbgewicht = new Text(compkonfig, SWT.BORDER);
			txtbgewicht.setToolTipText(Messages.getString("konfig.bikegewicht")); 
			txtbgewicht.setBounds(120, 150, 60, 20);
			Global.setFontSizeText(txtbgewicht);

			txtlfakt = new Text(compkonfig, SWT.BORDER);
			txtlfakt.setToolTipText(Messages.getString("konfig.leistungsfaktor_in_proz")); 
			txtlfakt.setBounds(120, 175, 60, 20);
			Global.setFontSizeText(txtlfakt);
			
			txtStravaKey = new Text(compkonfig, SWT.BORDER);
			txtStravaKey.setToolTipText(Messages.getString("konfig.strava_ttp")); 
			txtStravaKey.setBounds(60, 225, 120, 20);
			Global.setFontSizeText(txtStravaKey);
			
			txtTileserver = new Text(compkonfig, SWT.BORDER);
			txtTileserver.setToolTipText(Messages.getString("konfig.tileserver_ttp")); 
			txtTileserver.setBounds(120, 300, 180, 20);
			Global.setFontSizeText(txtTileserver);

			txtpulsrot = new Text(compkonfig, SWT.BORDER);
			txtpulsrot.setToolTipText(Messages.getString("konfig.txtpulsrottp")); 
			txtpulsrot.setBounds(80, 343, 40, 20);
			Global.setFontSizeText(txtpulsrot);

			txtpulsgelb = new Text(compkonfig, SWT.BORDER);
			txtpulsgelb.setToolTipText(Messages.getString("konfig.txtpulsgelbtp")); 
			txtpulsgelb.setBounds(80, 371, 40, 20);
			Global.setFontSizeText(txtpulsgelb);

			txtpulsgruen = new Text(compkonfig, SWT.BORDER);
			txtpulsgruen.setToolTipText(Messages.getString("konfig.txtpulsgruentp")); 
			txtpulsgruen.setBounds(80, 399, 40, 20);
			Global.setFontSizeText(txtpulsgruen);

			txtrpmrot = new Text(compkonfig, SWT.BORDER);
			txtrpmrot.setToolTipText(Messages.getString("konfig.txtrpmrottp")); 
			txtrpmrot.setBounds(210, 343, 40, 20);
			Global.setFontSizeText(txtrpmrot);

			txtrpmgelb = new Text(compkonfig, SWT.BORDER);
			txtrpmgelb.setToolTipText(Messages.getString("konfig.txtrpmgelbtp")); 
			txtrpmgelb.setBounds(210, 371, 40, 20);
			Global.setFontSizeText(txtrpmgelb);

			txtrpmgruen = new Text(compkonfig, SWT.BORDER);
			txtrpmgruen.setToolTipText(Messages.getString("konfig.txtrpmgruentp")); 
			txtrpmgruen.setBounds(210, 399, 40, 20);
			Global.setFontSizeText(txtrpmgruen);

			txtprot = new Text(compkonfig, SWT.BORDER);
			txtprot.setToolTipText(Messages.getString("konfig.txtprottp")); 
			txtprot.setBounds(340, 343, 40, 20);
			Global.setFontSizeText(txtprot);

			txtpgelb = new Text(compkonfig, SWT.BORDER);
			txtpgelb.setToolTipText(Messages.getString("konfig.txtpgelbtp")); 
			txtpgelb.setBounds(340, 371, 40, 20);
			Global.setFontSizeText(txtpgelb);

			txtpgruen = new Text(compkonfig, SWT.BORDER);
			txtpgruen.setToolTipText(Messages.getString("konfig.txtpgruentp")); 
			txtpgruen.setBounds(340, 399, 40, 20);
			Global.setFontSizeText(txtpgruen);

			txtergoip = new Text(compkonfig, SWT.BORDER);
			txtergoip.setToolTipText(Messages.getString("konfig.ip_eingeben")); 
			txtergoip.setBounds(310, 100, 100, 20);
			Global.setFontSizeText(txtergoip);

			txtcwa = new Text(compkonfig, SWT.BORDER);
			txtcwa.setToolTipText(Messages.getString("konfig.luftwiderstand")); 
			txtcwa.setBounds(310, 125, 40, 20);
			Global.setFontSizeText(txtcwa);
			
			txtk2 = new Text(compkonfig, SWT.BORDER);
			txtk2.setToolTipText(Messages.getString("konfig.konstante_nicht")); 
			txtk2.setBounds(400, 125, 40, 20);
			Global.setFontSizeText(txtk2);
			
			txtSteigungMin = new Text(compkonfig, SWT.BORDER);
			txtSteigungMin.setToolTipText(Messages.getString("konfig.min_steigung_tooltip")); 
			txtSteigungMin.setBounds(310, 150, 40, 20);
			Global.setFontSizeText(txtSteigungMin);
			
			txtSteigungMax = new Text(compkonfig, SWT.BORDER);
			txtSteigungMax.setToolTipText(Messages.getString("konfig.max_steigung_tooltip")); 
			txtSteigungMax.setBounds(400, 150, 40, 20);
			Global.setFontSizeText(txtSteigungMax);

			txtDynRPMNormal = new Text(compkonfig, SWT.BORDER);
			txtDynRPMNormal.setToolTipText(Messages.getString("konfig.obere_trittfrequenz")); 
			txtDynRPMNormal.setBounds(310, 175, 40, 20);
			Global.setFontSizeText(txtDynRPMNormal);
			
			txtDynRPMWiege = new Text(compkonfig, SWT.BORDER);
			txtDynRPMWiege.setToolTipText(Messages.getString("konfig.untere_trittfrequenz")); 
			txtDynRPMWiege.setBounds(400, 175, 40, 20);
			Global.setFontSizeText(txtDynRPMWiege);

			txtAutomatik1 = new Text(compkonfig, SWT.BORDER);
			txtAutomatik1.setToolTipText(Messages.getString("konfig.automatik1tp")); 
			txtAutomatik1.setBounds(310, 200, 40, 20);
			Global.setFontSizeText(txtAutomatik1);
			
			txtAutomatik2 = new Text(compkonfig, SWT.BORDER);
			txtAutomatik2.setToolTipText(Messages.getString("konfig.automatik2tp")); 
			txtAutomatik2.setBounds(400, 200, 40, 20);
			Global.setFontSizeText(txtAutomatik2);
			
			// Labels:
			Label labelname = new Label(compkonfig, SWT.NONE);		
			labelname.setText(Messages.getString("konfig.fahrername")); 
			labelname.setBounds(0, 0, 120, 20);
			Global.setFontSizeLabel(labelname);
			
			labelgewicht = new Label(compkonfig, SWT.NONE);
			labelgewicht.setText(Messages.getString("konfig.fahrergewicht_kg")); 
			labelgewicht.setBounds(0, 25, 120, 20);
			Global.setFontSizeLabel(labelgewicht);

			Label labelalter = new Label(compkonfig, SWT.NONE);		
			labelalter.setText(Messages.getString("konfig.fahreralter")); 
			labelalter.setBounds(0, 50, 120, 20);
			Global.setFontSizeLabel(labelalter);
			
			labelmaxleistung = new Label(compkonfig, SWT.NONE);		
			labelmaxleistung.setText(Messages.getString("konfig.max_leistung")); 
			labelmaxleistung.setBounds(0, 75, 120, 20);
			Global.setFontSizeLabel(labelmaxleistung);
			
			labelminleistung = new Label(compkonfig, SWT.NONE);		
			labelminleistung.setText(Messages.getString("konfig.min_leistung")); 
			labelminleistung.setBounds(0, 100, 120, 20);
			Global.setFontSizeLabel(labelminleistung);

			labelmaxpuls = new Label(compkonfig, SWT.NONE);		
			labelmaxpuls.setText(Messages.getString("konfig.max_puls")); 
			labelmaxpuls.setBounds(0, 125, 120, 20);
			Global.setFontSizeLabel(labelmaxpuls);
			
			labelbgewicht = new Label(compkonfig, SWT.NONE);		
			labelbgewicht.setText(Messages.getString("konfig.bikegewicht_kg")); 
			labelbgewicht.setBounds(0, 150, 120, 20);
			Global.setFontSizeLabel(labelbgewicht);
			
			Label labellfakt = new Label(compkonfig, SWT.NONE);		
			labellfakt.setText(Messages.getString("konfig.leistungsfaktor_proz")); 
			labellfakt.setBounds(0, 175, 120, 20);
			Global.setFontSizeLabel(labellfakt);
			
			Label labelstrava = new Label(compkonfig, SWT.NONE);		
			labelstrava.setText(Messages.getString("konfig.strava_label")); 
			labelstrava.setBounds(0, 225, 120, 20);
			Global.setFontSizeLabel(labelstrava);

			Label labelTileserver = new Label(compkonfig, SWT.NONE);		
			labelTileserver.setText(Messages.getString("konfig.labelTileserver")); 
			labelTileserver.setBounds(0, 300, 120, 20);
			Global.setFontSizeLabel(labelTileserver);
			
			labelpulsampel = new Label(compkonfig, SWT.NONE);		
			labelpulsampel.setText(Messages.getString("konfig.Pulsanzeige")); 
			labelpulsampel.setBounds(0, 320, 120, 20);
			Global.setFontSizeLabel(labelpulsampel);
			
			labelrpmampel = new Label(compkonfig, SWT.NONE);		
			labelrpmampel.setText(Messages.getString("konfig.RPM_Anzeige")); 
			labelrpmampel.setBounds(130, 320, 120, 20);
			Global.setFontSizeLabel(labelrpmampel);
			
			labelpampel = new Label(compkonfig, SWT.NONE);		
			labelpampel.setText(Messages.getString("konfig.Leistungsanzeige")); 
			labelpampel.setBounds(260, 320, 120, 20);
			Global.setFontSizeLabel(labelpampel);
			
			Label labeltrainer = new Label(compkonfig, SWT.NONE);		
			labeltrainer.setText(Messages.getString("konfig.trainer")); 
			labeltrainer.setBounds(200, 50, 100, 20);
			Global.setFontSizeLabel(labeltrainer);

			labelinterface = new Label(compkonfig, SWT.NONE);		
			labelinterface.setText(Messages.getString("konfig.interface")); 
			//labelinterface.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
			labelinterface.setBounds(200, 75, 100, 20);
			Global.setFontSizeLabel(labelinterface);

			labelergoip = new Label(compkonfig, SWT.NONE);		
			labelergoip.setText("(W)LAN IP"); 
			//labelergoip.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
			labelergoip.setBounds(200, 100, 100, 20);
			Global.setFontSizeLabel(labelergoip);
			
			labelcwa = new Label(compkonfig, SWT.NONE);		
			labelcwa.setText("cwa"); 
			labelcwa.setBounds(200, 125, 100, 20);
			Global.setFontSizeLabel(labelcwa);
			
			labelk2 = new Label(compkonfig, SWT.NONE);		
			labelk2.setText("k2"); 
			labelk2.setBounds(355, 125, 50, 20);
			Global.setFontSizeLabel(labelk2);
			
			labelSteigungMin = new Label(compkonfig, SWT.NONE);		
			labelSteigungMin.setText(Messages.getString("konfig.min_steigung")); 
			labelSteigungMin.setBounds(200, 150, 100, 20);
			Global.setFontSizeLabel(labelSteigungMin);

			labelSteigungMax = new Label(compkonfig, SWT.NONE);		
			labelSteigungMax.setText(Messages.getString("konfig.max_p")); 
			labelSteigungMax.setBounds(355, 150, 50, 20);
			Global.setFontSizeLabel(labelSteigungMax);
			
			labeldynnormal = new Label(compkonfig, SWT.NONE);		
			labeldynnormal.setText(Messages.getString("konfig.normal_rpm")); 
			labeldynnormal.setBounds(200, 175, 100, 20);
			Global.setFontSizeLabel(labeldynnormal);
			
			labeldynwiege = new Label(compkonfig, SWT.NONE);		
			labeldynwiege.setText(Messages.getString("konfig.wiege_p")); 
			labeldynwiege.setBounds(355, 175, 50, 20);
			Global.setFontSizeLabel(labeldynwiege);

			labelAutomatik2 = new Label(compkonfig, SWT.NONE);		
			labelAutomatik2.setText(Messages.getString("konfig.automatik2")); 
			labelAutomatik2.setBounds(355, 200, 50, 20);
			Global.setFontSizeLabel(labelAutomatik2);

			labelBelohnung = new Label(compkonfig, SWT.NONE);		
			labelBelohnung.setText(Messages.getString("konfig.labelbelohnung")); 
			labelBelohnung.setBounds(200, 225, 100, 20);
			Global.setFontSizeLabel(labelBelohnung);

			labelRahmenfarbe = new Label(compkonfig, SWT.NONE);		
			labelRahmenfarbe.setText(Messages.getString("konfig.labelRahmenfarbe")); 
			labelRahmenfarbe.setBackground(Rsmain.getRahmenfarbe());
			labelRahmenfarbe.setBounds(310, 252, 80, 20);
			Global.setFontSizeLabel(labelRahmenfarbe);

			// Images
			Image AmpelImage = new Image(Display.getCurrent(), "Ampel.png");
			imgpulsampel = new Label(compkonfig, SWT.NONE);		
			imgpulsampel.setImage(AmpelImage); 
			imgpulsampel.setBounds(0, 335, 65, 95);
			
			imgrpmampel = new Label(compkonfig, SWT.NONE);		
			imgrpmampel.setImage(AmpelImage); 
			imgrpmampel.setBounds(130, 335, 65, 95);

			imgpampel = new Label(compkonfig, SWT.NONE);		
			imgpampel.setImage(AmpelImage); 
			imgpampel.setBounds(260, 335, 65, 95);

			// Buttons
			butok = new Button(compkonfig, SWT.NONE);
			butok.setText(Messages.getString("konfig.close")); 
			butok.setToolTipText(Messages.getString("konfig.closetp")); 
			butok.setBounds(360, 430, 80, 25);
			Global.setFontSizeButton(butok);
			butok.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Rsmain.setImDialog(false);
			        updateProfil(biker, ergo);
			        saveProfil(biker, ergo);
					// ANT+ eingeschaltet?
					if (chkAntpPuls.getSelection() || chkAntpCadenceSpeed.getSelection() || chkAntpCadence.getSelection() || cmbinterface.getText().equals("ANT+")) {
						if (chkAntpPuls.getSelection())
							Rsmain.libant.setaktTHr(LibAnt.THr.antppuls);
						else
							Rsmain.libant.setaktTHr(LibAnt.THr.notset);
						
						if (chkAntpCadenceSpeed.getSelection()) 
							Rsmain.libant.setaktTCadence(LibAnt.TCadence.antpcadencespeed);
						else
							if (chkAntpCadence.getSelection()) 
								Rsmain.libant.setaktTCadence(LibAnt.TCadence.antpcadence);
							else
								Rsmain.libant.setaktTCadence(LibAnt.TCadence.notset);
						
						Rsmain.thisTrainer.antopen();
					} else {	// ANT+ - Puls und Cadence deaktivieren
						if (Rsmain.libant != null) {
							Rsmain.libant.setaktTHr(LibAnt.THr.notset);
							Rsmain.libant.setaktTCadence(LibAnt.TCadence.notset);
							// if (Rsmain.libant.isEin()) // dann ausschalten
							Rsmain.libant.stop();
						}			
					}
				    if (indcmbInterfaceOld != cmbinterface.getSelectionIndex() || 
				    	indcmbTrainerOld != cmbtrainer.getSelectionIndex() ||
				    	!txtErgoIPOld.equals(txtergoip.getText())) {	// bei Änderung an Interface alle Schnittstellen schliessen und init.
						Rsmain.thisTrainer.serclose();
						Rsmain.thisTrainer.netclose();
						Rsmain.thisTrainer.usbclose();
			        	
			        	Rsmain.initergokom();		        	
			        }
			        biker.setChanged(true);
			        Rsmain.cmbfahrer.setItems(Rsmain.setcmbfahrer());
			        Rsmain.cmbfahrer.select(Rsmain.cmbfahrer.indexOf(biker.getName()));
					Rsmain.atKurbel.setEnabled(2, true);  // Rsmain.textgang.setEnabled(true);
					if (Rsmain.isVlc_ein() && Rsmain.libvlc != null && Rsmain.aktstatus == Rsmain.Status.laeuft) {  // Video erstmal wieder auf Normalgeschw. stellen. (falls sich Dynamik geändert hat)
						Rsmain.libvlc.libvlc_media_player_set_rate(Rsmain.mediaplayer, (float) 1.0);
					}
					int ind = cmbtrainer.getSelectionIndex();
					if (ind == 3 || ind == 4  || ind == 7 || ind == 8 || ind == 9 || ind == 14 || ind == 17) // Rollentrainer?
						Rsmain.cmbwind.setEnabled(false);
					else
						Rsmain.cmbwind.setEnabled(true);

			    	Rsmain.setRahmenfarbe(labelRahmenfarbe.getBackground());
					OSMViewer.tileServer.setURL(txtTileserver.getText());

			    	if (chkSwitchXY.getSelection())
			    		OSMViewer.tileServer.setswitchXY(true);
			    	else
			    		OSMViewer.tileServer.setswitchXY(false);
			    		
			    	if (chkSwitchJpg.getSelection())
			    		OSMViewer.tileServer.setextImage(".jpg");
			    	else
			    		OSMViewer.tileServer.setextImage(".png");
			    		
			        sShell.setVisible(false);
				}
			});

			butreset = new Button(compkonfig, SWT.NONE);
			butreset.setText(Messages.getString("konfig.reset")); 
			butreset.setToolTipText(Messages.getString("konfig.resettp")); 
			butreset.setBounds(0, 430, 80, 25);
			Global.setFontSizeButton(butreset);
			butreset.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					resetAll2FactoryDefaults();
					Rsmain.setImDialog(false);
				}
			});
			
			butsuche = new Button(compkonfig, SWT.NONE);
			butsuche.setImage(new Image(Display.getCurrent(), "ergo.gif")); 
			butsuche.setToolTipText(Messages.getString("konfig.netzwerkscan")); 
			butsuche.setBounds(415, 98, 25, 25);
			Global.setFontSizeButton(butsuche);
			butsuche.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					txtergoip.setText(ergo.SucheErgometer());
				}
			});

			butcolor = new Button(compkonfig, SWT.NONE);
			butcolor.setText(Messages.getString("konfig.farbe"));
			butcolor.setToolTipText(Messages.getString("konfig.farbetp")); 
			butcolor.setBounds(400, 250, 40, 25);
			Global.setFontSizeButton(butcolor);
			butcolor.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// Color-Dialog erzeugen
			        ColorDialog dlg = new ColorDialog(sShell);

			        // aktuellle Rahmenfarbe setzen im Dialog
			        dlg.setRGB(Rsmain.getRahmenfarbe().getRGB());
			        // Überschrift setzen
			        dlg.setText(Messages.getString("konfig.colorpicker"));

			        // Color-Dialog öffnen und Farbe übergeben
			        RGB rgb = dlg.open();
			        if (rgb != null) {
			        	labelRahmenfarbe.setBackground(new Color(sShell.getDisplay(), rgb));
			        }
				}
			});
			
			// Checkboxen
			chkDynamik = new Button(compkonfig, SWT.CHECK);
			chkDynamik.setText(Messages.getString("konfig._dynamik")); 
			chkDynamik.setToolTipText(Messages.getString("konfig.dynamik_modus")); 
			chkDynamik.setSelection(dynamik);
			//chkDynamik.setEnabled(false);
			chkDynamik.setBounds(200, 0, 80, 20);
			Global.setFontSizeButton(chkDynamik);
			chkDynamik.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					enabledisableControls();
				}
			});

			chkAveraging = new Button(compkonfig, SWT.CHECK);
			chkAveraging.setText(Messages.getString("konfig.hoehendaten_glaetten")); 
			chkAveraging.setToolTipText(Messages.getString("konfig.hoehenprofil_mittelwert")); 
			chkAveraging.setSelection(averaging);
			chkAveraging.setBounds(310, 0, 140, 20);
			Global.setFontSizeButton(chkAveraging);
			chkAveraging.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkAveraging.getSelection())
						averaging = true;
					else
						averaging = false;
				}
			});
			
			chkTCXPower = new Button(compkonfig, SWT.CHECK);
			chkTCXPower.setText("GPS-Power");
			chkTCXPower.setToolTipText(Messages.getString("konfig.tcxpower_einaus"));
			//chkTCXPower.setSelection(tcxpower);
			chkTCXPower.setBounds(200, 25, 100, 20);
			Global.setFontSizeButton(chkTCXPower);
			chkTCXPower.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkTCXPower.getSelection())
						tcxpower = true;
					else
						tcxpower = false;
				}
			});

			// Gangautomatik
			chkAutomatik = new Button(compkonfig, SWT.CHECK);
			chkAutomatik.setText(Messages.getString("konfig.Automatik"));
			chkAutomatik.setToolTipText(Messages.getString("konfig.AutomatikTP"));
			chkAutomatik.setSelection(biker.isAutomatik());
			chkAutomatik.setBounds(200, 200, 100, 20);
			Global.setFontSizeButton(chkAutomatik);
			chkAutomatik.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkAutomatik.getSelection())
						biker.setAutomatik(true);
					else
						biker.setAutomatik(false);
					
					enabledisableControls();
				}
			});

			chkOverlay = new Button(compkonfig, SWT.CHECK);
			chkOverlay.setText(Messages.getString("konfig.overlay"));
			chkOverlay.setToolTipText(Messages.getString("konfig.overlayTP"));
			chkOverlay.setSelection(isNoOverlay());
			chkOverlay.setBounds(200, 250, 110, 20);
			Global.setFontSizeButton(chkOverlay);
			chkOverlay.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkOverlay.getSelection())
						setNoOverlay(true);
					else
						setNoOverlay(false);
				}
			});

			chkMap = new Button(compkonfig, SWT.CHECK);
			chkMap.setText(Messages.getString("konfig.chkosm"));
			chkMap.setToolTipText(Messages.getString("konfig.chkosmtp"));
			chkMap.setSelection(isShowmap());
			chkMap.setBounds(90, 250, 110, 20);
			Global.setFontSizeButton(chkMap);
			chkMap.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setMapchg(true);	// Rsmain muß showclientarea aufrufen im RefreshInfos
					if (chkMap.getSelection())
						setShowmap(true);
					else
						setShowmap(false);
				}
			});
			
			chkAntpPuls = new Button(compkonfig, SWT.CHECK);
			chkAntpPuls.setText(Messages.getString("konfig.AntPuls"));
			chkAntpPuls.setToolTipText(Messages.getString("konfig.AntPulsEinAus"));
			if (Platform.isLinux())		// kein libant.so verfügbar!
				chkAntpPuls.setVisible(false);
			if (Rsmain.libant != null) {
				chkAntpPuls.setSelection(Rsmain.libant.getaktTHr() == LibAnt.THr.antppuls);
			}
			chkAntpPuls.setBounds(0, 250, 110, 20);
			Global.setFontSizeButton(chkAntpPuls);

			chkAntpCadenceSpeed = new Button(compkonfig, SWT.CHECK);
			chkAntpCadenceSpeed.setText(Messages.getString("konfig.AntCadence"));
			chkAntpCadenceSpeed.setToolTipText(Messages.getString("konfig.AntCadenceSpeedEinAus"));
			if (Platform.isLinux())		// kein libant.so verfügbar!
				chkAntpCadenceSpeed.setVisible(false);
			if (Rsmain.libant != null) {
				chkAntpCadenceSpeed.setSelection(Rsmain.libant.getaktTCadence() == LibAnt.TCadence.antpcadencespeed);
			}
			chkAntpCadenceSpeed.setBounds(90, 275, 110, 20);
			Global.setFontSizeButton(chkAntpCadenceSpeed);
			chkAntpCadenceSpeed.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					chkAntpCadence.setSelection(false);
				}
			});

			chkAntpCadence = new Button(compkonfig, SWT.CHECK);
			chkAntpCadence.setText("ANT+ RPM");
			chkAntpCadence.setToolTipText(Messages.getString("konfig.AntCadenceEinAus"));
			if (Platform.isLinux())		// kein libant.so verfügbar!
				chkAntpCadence.setVisible(false);
			if (Rsmain.libant != null) {
				chkAntpCadence.setSelection(Rsmain.libant.getaktTCadence() == LibAnt.TCadence.antpcadence);
			}
			chkAntpCadence.setBounds(0, 275, 90, 20);
			Global.setFontSizeButton(chkAntpCadence);
			chkAntpCadence.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					chkAntpCadenceSpeed.setSelection(false);
				}
			});

			chkInfo = new Button(compkonfig, SWT.CHECK);
			chkInfo.setText(Messages.getString("konfig.showinfo"));
			chkInfo.setToolTipText(Messages.getString("konfig.showinfotp"));
			//chkInfo.setSelection(Rsmain.libant.isEin());
			chkInfo.setBounds(310, 275, 120, 20);
			Global.setFontSizeButton(chkInfo);

			chkAutoOK = new Button(compkonfig, SWT.CHECK);
			chkAutoOK.setText(Messages.getString("konfig.autoOK"));
			chkAutoOK.setToolTipText(Messages.getString("konfig.autoOKtp"));
			chkAutoOK.setBounds(200, 275, 120, 20);
			Global.setFontSizeButton(chkAutoOK);

			chkSwitchXY = new Button(compkonfig, SWT.CHECK);
			chkSwitchXY.setText(Messages.getString("konfig.switchXY"));
			chkSwitchXY.setToolTipText(Messages.getString("konfig.switchXYtp"));
			chkSwitchXY.setBounds(310, 300, 60, 20);
			Global.setFontSizeButton(chkSwitchXY);

			chkSwitchJpg = new Button(compkonfig, SWT.CHECK);
			chkSwitchJpg.setText(Messages.getString("konfig.switchJpg"));
			chkSwitchJpg.setToolTipText(Messages.getString("konfig.switchJpgtp"));
			chkSwitchJpg.setBounds(400, 300, 60, 20);
			Global.setFontSizeButton(chkSwitchJpg);

			// Comboboxen:
			cmbspart = new CCombo(compkonfig, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			cmbspart.setItems(SPArt);
			cmbspart.setToolTipText(Messages.getString("konfig.bike_auswaehlen")); 
			cmbspart.setBounds(310, 25, 130, 20);
			cmbspart.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeCCombo(cmbspart);
			cmbspart.addSelectionListener(new SelectionAdapter() {
			    public void widgetSelected(SelectionEvent e) {
			    	if (cmbspart.getSelectionIndex() == 0) { // MTB
			    		txtcwa.setText("0,35"); 
			    		txtk2.setText("0,01"); 
			    		txtbgewicht.setText("13"); 
			    	} else {
			        	if (cmbspart.getSelectionIndex() == 1) {// Rennrad
			        		txtcwa.setText("0,25"); 
			        		txtk2.setText("0,005"); 
			        		txtbgewicht.setText("8"); 
			        	} else {
				        	if (cmbspart.getSelectionIndex() == 2) {// Langlaufski
				        		txtcwa.setText("0,6"); 
				        		txtk2.setText("0,3"); 
				        		txtbgewicht.setText("2"); 
				        	}
			        	}
			    	}
			    }
			});

			cmbtrainer = new CCombo(compkonfig, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			cmbtrainer.setItems(trainer);
			cmbtrainer.setToolTipText(Messages.getString("konfig.trainer_auswahl")); 
			cmbtrainer.setBounds(310, 50, 130, 20);
			cmbtrainer.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeCCombo(cmbtrainer);
			cmbtrainer.addSelectionListener(new SelectionAdapter() {
			    public void widgetSelected(SelectionEvent e) {
			    	enabledisableControls();
			    	int ind = cmbtrainer.getSelectionIndex();
			    	if (ind <= 2 || ind == 5 || ind == 6 || ind == 12 || ind == 13)
			    		cmbinterface.select(0);
			    }
			});

			cmbinterface = new CCombo(compkonfig, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			cmbinterface.setItems(Interfaces);
			cmbinterface.setToolTipText(Messages.getString("konfig.ergo_interface_auswahl")); 
			cmbinterface.setBounds(310, 75, 130, 20);
			cmbinterface.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeCCombo(cmbinterface);
			cmbinterface.addSelectionListener(new SelectionAdapter() {
			    public void widgetSelected(SelectionEvent e) {
			    	enabledisableControls();
			    }
			});

			cmbLF = new CCombo(compkonfig, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			cmbLF.setItems(LFStufen);
			cmbLF.setToolTipText(Messages.getString("konfig.sgrad")); 
			cmbLF.setBounds(0, 200, 180, 20);
			cmbLF.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeCCombo(cmbLF);
			cmbLF.addSelectionListener(new SelectionAdapter() {
			    public void widgetSelected(SelectionEvent e) {
			    	if (cmbLF.getSelectionIndex() == 0)	// sehr leicht = 60%
			    		txtlfakt.setText("60");
			    	if (cmbLF.getSelectionIndex() == 1)	// leicht = 80%
			    		txtlfakt.setText("80");
			    	if (cmbLF.getSelectionIndex() == 2)	// normal = 100%
			    		txtlfakt.setText("100");
			    	if (cmbLF.getSelectionIndex() == 3)	// schwer = 120%
			    		txtlfakt.setText("120");
			    	if (cmbLF.getSelectionIndex() == 4)	// sehr schwer = 140%
			    		txtlfakt.setText("140");
			    }
			});

			cmbBelohnung = new CCombo(compkonfig, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			cmbBelohnung.setItems(Belohnung);
			cmbBelohnung.setToolTipText(Messages.getString("konfig.cmbbelohnungtp")); 
			cmbBelohnung.setBounds(310, 225, 130, 20);
			cmbBelohnung.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeCCombo(cmbBelohnung);
			initFelder(biker, ergo);
			initTextPruefungen();
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * Übernahme der Profil-(Fahrer)daten aus dem Bikerobjekt (das dem Konstruktor übergeben wurde)
	 * @param biker Fahrer
	 * @param ergo  Trainingsgerät
	 */
	private void initFelder(Fahrer biker, Trainer ergo) {
		DecimalFormat zfk3 = new DecimalFormat("#.###"); 
		DecimalFormat zfk1 = new DecimalFormat("#.#"); 
		DecimalFormat zfk0 = new DecimalFormat("#"); 

		try {
			txtfgewicht.setText(zfk0.format(new Double(biker.getFahrergewicht())));	 // Fahrergewicht für Leistungsberechnung
			txtbgewicht.setText(zfk0.format(new Double(biker.getBikegewicht())));	 // Bikegewicht für Leistungsberechnung
			txtmaxleistung.setText(zfk0.format(new Double(biker.getMaxleistung()))); // maximale Leistung, die vorgegeben werden darf
			txtminleistung.setText(zfk0.format(new Double(biker.getMinleistung()))); // minimale Leistung, die vorgegeben wird.
			txtmaxpuls.setText(zfk0.format(new Double(biker.getMaxpuls())));         // maximaler Puls des Fahrers
			txtcwa.setText(zfk3.format(new Double(biker.getCwa())));             	 // Oberfläche und cw-Wert - geschätzt
			txtk2.setText(zfk3.format(new Double(biker.getK2())));              	 // Konstante für Rollreibung
			txtalter.setText(zfk0.format(new Double(biker.getAlter())));             // Fahreralter
			txtname.setText(biker.getName());										 // Fahrername

			txtpulsrot.setText(zfk0.format(new Double(biker.getPulsrot())));         // Pulsgrenze roter Bereich
			txtpulsgelb.setText(zfk0.format(new Double(biker.getPulsgelb())));       // Pulsgrenze gelber Bereich
			txtpulsgruen.setText(zfk0.format(new Double(biker.getPulsgruen())));     // Pulsgrenze grüner Bereich

			txtrpmrot.setText(zfk0.format(new Double(biker.getRpmrot())));           // RPM-grenze roter Bereich
			txtrpmgelb.setText(zfk0.format(new Double(biker.getRpmgelb())));         // RPM-grenze gelber Bereich
			txtrpmgruen.setText(zfk0.format(new Double(biker.getRpmgruen())));       // RPM-grenze grüner Bereich

			txtprot.setText(zfk0.format(new Double(biker.getProt())));               // Leistungsgrenze roter Bereich
			txtpgelb.setText(zfk0.format(new Double(biker.getPgelb())));             // Leistungsgrenze gelber Bereich
			txtpgruen.setText(zfk0.format(new Double(biker.getPgruen())));           // Leistungsgrenze grüner Bereich

			txtergoip.setText(ergo.getErgoIP());                                     // IP vom Ergometer
			cmbinterface.setText(ergo.getErgoCom());                                 // Schnittstelle vom Ergometer
			chkAveraging.setSelection(averaging);
			chkTCXPower.setSelection(tcxpower);
			chkDynamik.setSelection(dynamik);
			chkOverlay.setSelection(nooverlay);
			if (Rsmain.libant != null) {
				chkAntpPuls.setSelection(Rsmain.libant.getaktTHr() == LibAnt.THr.antppuls);
				chkAntpCadenceSpeed.setSelection(Rsmain.libant.getaktTCadence() == LibAnt.TCadence.antpcadencespeed);
				chkAntpCadence.setSelection(Rsmain.libant.getaktTCadence() == LibAnt.TCadence.antpcadence);
			}
			chkMap.setSelection(showmap);
			chkInfo.setSelection(showInfo);
			chkAutoOK.setSelection(autoOK);
			chkSwitchXY.setSelection(OSMViewer.tileServer.isswitchXY());
			
			if (dynamik == false) {
				txtDynRPMNormal.setEnabled(false);
				txtDynRPMWiege.setEnabled(false);
				labeldynwiege.setEnabled(false);
				labeldynnormal.setEnabled(false);
			}

			String sTrainer = ergo.getTrainerModell();	
			if (sTrainer.equalsIgnoreCase("daum")) { 	// switch mit Strings geht erst ab Java 7 (irgendwann umstellen?)
				//rbDaum.setSelection(true);
				cmbtrainer.setText(trainer[0]);  // "Daum Ergometer"
			} else { 
				if (sTrainer.equalsIgnoreCase("daum2001")) { 
					//rbDaum2001.setSelection(true);
					cmbtrainer.setText(trainer[1]);  // "Daum Ergo. bis 2001"
				} else {
					if (sTrainer.equalsIgnoreCase("kettler")) { 
						//rbKettler.setSelection(true);		
						cmbtrainer.setText(trainer[2]);  // "Kettler Ergometer"
					} else {
						if (sTrainer.equalsIgnoreCase("tacx1")) { 
							cmbtrainer.setText(trainer[3]);  // "Tacx Rollentrainer iMagic"
						} else {
							if (sTrainer.equalsIgnoreCase("tacx2")) { 
								cmbtrainer.setText(trainer[4]);  // "Tacx Rollentrainer Fortius"
							} else {
								if (sTrainer.equalsIgnoreCase("daumcr")) { 
									cmbtrainer.setText(trainer[5]);  // Daum Crosstrainer
								} else {
									if (sTrainer.equalsIgnoreCase("kettlercr")) { 
										cmbtrainer.setText(trainer[6]);  // Kettler Crosstrainer
									} else {
										if (sTrainer.equalsIgnoreCase("tacxvortex")) { 
											cmbtrainer.setText(trainer[7]);  // TACX Vortex (ANT+) Rollentrainer
										} else {
											if (sTrainer.equalsIgnoreCase("tacxbushido")) { 
												cmbtrainer.setText(trainer[8]);  // TACX Bushido (ANT+) Rollentrainer
											} else {
												if (sTrainer.equalsIgnoreCase("ant+gsc10")) { 
													cmbtrainer.setText(trainer[9]);  // (ANT+) Rollentrainer mit RPM/Speed, Leistung manuell
												} else {
													if (sTrainer.equalsIgnoreCase("cyclus2_el")) { 
														cmbtrainer.setText(trainer[10]);  // Cyclus 2 Ergoline kompatibel
													}  else {
														if (sTrainer.equalsIgnoreCase("cyclus2")) { 
															cmbtrainer.setText(trainer[11]);  // Cyclus 2 native
														}  else {
															if (sTrainer.equalsIgnoreCase("ergofit")) { 
																cmbtrainer.setText(trainer[12]);  // Ergo-Fit Ergometer
															} else {
																if (sTrainer.equalsIgnoreCase("ergofitcr")) { 
																	cmbtrainer.setText(trainer[13]);  // Ergo-Fit Crosstrainer
																} else {
																	if (sTrainer.equalsIgnoreCase("antfec")) { 
																		cmbtrainer.setText(trainer[14]);  // ANT+ FE-C
																	} else {
																		if (sTrainer.equalsIgnoreCase("ergoline")) { 
																			cmbtrainer.setText(trainer[15]);  // Ergoline Ergoselect
																		} else {
																			if (sTrainer.equalsIgnoreCase("wahookickr")) { 
																				cmbtrainer.setText(trainer[16]);  // Wahoo KICKR
																			} else {
																				if (sTrainer.equalsIgnoreCase("ant+rpm")) { 
																					cmbtrainer.setText(trainer[17]);  // ANT+ Rolle mit RPM, Leistung manuell
																				}
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}												
								}						
							}
						}
					}
				}
			}
			if (biker.getCwa() > 0.3 && biker.getCwa() < 0.45) 
				cmbspart.setText(SPArt[0]);  // MTB
			else
				if (biker.getCwa() <= 0.3) 
					cmbspart.setText(SPArt[1]); // Rennrad
				else
					cmbspart.setText(SPArt[2]); // Langlauf
			
			cmbBelohnung.setText(Belohnung[biker.getBelohnungindex()]);
			
			enabledisableControls();
			txtDynRPMNormal.setText(zfk0.format(new Double(biker.getDynRPMNormal()))); // Fahrertrittfrequenz Normal
			txtDynRPMWiege.setText(zfk0.format(new Double(biker.getDynRPMWiege())));   // Fahrertrittfrequenz Wiegetritt
			double dlfaktproz = biker.getLfakt() * 100.0;
			String lfakt = zfk0.format(dlfaktproz);
			txtlfakt.setText(lfakt);       	// Fahrer Leistungsfaktor in Prozent
			if (dlfaktproz <= 60)
				cmbLF.setText(LFStufen[0]);	// sehr leicht bis 60%
			else if (dlfaktproz <= 80)
				cmbLF.setText(LFStufen[1]);	// leicht
			else if (dlfaktproz <= 100)
				cmbLF.setText(LFStufen[2]);	// normal
			else if (dlfaktproz <= 120)
				cmbLF.setText(LFStufen[3]);	// schwer(real)
			else 
				cmbLF.setText(LFStufen[4]);	// sehr schwer
			txtSteigungMax.setText(zfk1.format(new Double(biker.getMaxSteigung())));   	// max. Steigung bei Rollentrainer
			txtSteigungMin.setText(zfk1.format(new Double(biker.getMinSteigung())));   	// min. Steigung bei Rollentrainer
			
			txtAutomatik1.setText(zfk0.format(new Double(biker.getAutomatik1())));   	// untere Automatikschwelle
			txtAutomatik2.setText(zfk0.format(new Double(biker.getAutomatik2())));   	// obere Automatikschwelle
			//txtibl.setText(biker.getIbl_APIKey());									// I-B-L Api-Key
			txtStravaKey.setText(Global.stravaKey);
			txtTileserver.setText(""+OSMViewer.tileServer.getURL());
			chkSwitchXY.setSelection(OSMViewer.tileServer.isswitchXY());
			if (OSMViewer.tileServer.getextImage().equalsIgnoreCase(".jpg"))
				chkSwitchJpg.setSelection(true);
			else
				chkSwitchJpg.setSelection(false);
			txtErgoIPOld = txtergoip.getText();
			indcmbTrainerOld = cmbtrainer.getSelectionIndex();
			indcmbInterfaceOld = cmbinterface.getSelectionIndex();
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * initialisiert die Textprüfungen für die Textfelder des Konfigurationsdialogs.
	 * Es dürfen nur Zahlen bzw. Zahlen mit Komma eingegeben werden.
	 * Alles andere (außer Steuerzeichen Backspace und Delete) werden ignoriert.
	 *
	 */
	private void initTextPruefungen() {
		txtfgewicht.addVerifyListener(Global.VLZahlen);
		txtbgewicht.addVerifyListener(Global.VLZahlen);
		txtmaxleistung.addVerifyListener(Global.VLZahlen);
		txtminleistung.addVerifyListener(Global.VLZahlen);
		txtmaxpuls.addVerifyListener(Global.VLZahlen);
		txtalter.addVerifyListener(Global.VLZahlen);
		txtcwa.addVerifyListener(Global.VLZahlenUndKomma);
		txtk2.addVerifyListener(Global.VLZahlenUndKomma);
		txtergoip.addVerifyListener(Global.VLZahlenUndPunkt);
		txtDynRPMNormal.addVerifyListener(Global.VLZahlen);
		txtDynRPMWiege.addVerifyListener(Global.VLZahlen);
		txtlfakt.addVerifyListener(Global.VLZahlen);
		txtSteigungMax.addVerifyListener(Global.VLZahlenUndKomma);
		txtSteigungMin.addVerifyListener(Global.VLZahlenKommaMinus);
		txtpulsrot.addVerifyListener(Global.VLZahlen);
		txtpulsgelb.addVerifyListener(Global.VLZahlen);
		txtpulsgruen.addVerifyListener(Global.VLZahlen);
		txtrpmrot.addVerifyListener(Global.VLZahlen);
		txtrpmgelb.addVerifyListener(Global.VLZahlen);
		txtrpmgruen.addVerifyListener(Global.VLZahlen);
		txtprot.addVerifyListener(Global.VLZahlen);
		txtpgelb.addVerifyListener(Global.VLZahlen);
		txtpgruen.addVerifyListener(Global.VLZahlen);
		txtAutomatik1.addVerifyListener(Global.VLZahlen);
		txtAutomatik2.addVerifyListener(Global.VLZahlen);
	}
	
	/**
	 * Speichern der Profildaten in persönlicher XML-Datei
	 * @param biker  Fahrer
	 * @param ergo   Trainingsgerät
	 */
	public void saveProfil(Fahrer biker, Trainer ergo) {
		try {
			String datei;
			if (biker.getName().length() > maxNameLength)
				datei = Global.strPfad+biker.getName().substring(0, maxNameLength)+".xml"; 
			else
				datei = Global.strPfad+biker.getName()+".xml"; 
			createXMLFileSettings(Global.standardsettingsdatei, biker);
			createXMLFileBiker(datei, biker, ergo);
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * Laden der Profildaten aus persönlicher XML-Datei
	 * @param datei		XML Datei mit persönlichen Settings
	 * @param biker		Fahrer
	 * @param ergo		Trainingsgerät
	 */
	public void loadProfil(String datei, Fahrer biker, Trainer ergo) {  
		Mlog.info("loadProfil: "+datei); 
		try {
			parseXmlFileSettings(Global.standardsettingsdatei, biker);
			// hier den Dateinamen setzen auf bikernamen (aus settings.xml)
			datei = Global.strPfad+biker.getName()+".xml"; 
			parseXmlFileBiker(datei, biker, ergo);
			Rsmain.aktcr = Rsmain.sShell.getClientArea();
			Point point = Rsmain.sShell.getSize();
			Rsmain.sShell.setSize(point);
			Rsmain.aktcr.x = Rsmain.sShell.getLocation().x; 
			Rsmain.aktcr.y = Rsmain.sShell.getLocation().y;
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * Biker-Objekt und Trainer-Objekt mit neuen Profildaten updaten
	 * @param biker  Fahrer
	 * @param ergo   Trainingsgerät
	 *
	 */
	private void updateProfil(Fahrer biker, Trainer ergo) {
		Double automatik1;
		Double automatik2;
		
		try {
			Double wert = new Double(txtfgewicht.getText());			
			if (wert > 200.0 || wert < 40.0) {
    			Messages.errormessage(Messages.getString("konfig.dieses_fahrergewicht_"));    			 
    			biker.setFahrergewicht(80.0);
			} else 
				biker.setFahrergewicht(wert);
			
			wert = new Double(txtbgewicht.getText());			
			if (wert > 20.0 || wert < 1.0) {
    			Messages.errormessage(Messages.getString("konfig.dieses_bikegewicht_"));    			 
    			biker.setBikegewicht(13.0);
			} else 
				biker.setBikegewicht(wert);

			Double pmax = new Double(Double.parseDouble(txtmaxleistung.getText()));
			Double pmin = new Double(Double.parseDouble(txtminleistung.getText()));
			if (pmin >= pmax || pmin < 0) {
    			Messages.errormessage(Messages.getString("konfig.min_leistung_"));    			 
				biker.setMaxleistung(350.0);
				biker.setMinleistung(80.0);
			} else {
				biker.setMaxleistung(pmax);
				biker.setMinleistung(pmin);
			}
			if (chkAutomatik.getSelection()) {
				automatik1 = new Double(Double.parseDouble(txtAutomatik1.getText()));
				automatik2 = new Double(Double.parseDouble(txtAutomatik2.getText()));
				if (automatik1 < (pmin+10) || automatik2 > (pmax-10)) {
	    			Messages.errormessage(Messages.getString("konfig.min_leistung_"));    			 
					biker.setMaxleistung(350.0);
					biker.setMinleistung(80.0);
				} 
			}
			
			Double pulsmax = new Double(Double.parseDouble(txtmaxpuls.getText()));
			if (pulsmax < 80.0 || pulsmax > 250.0) {
    			Messages.errormessage(Messages.getString("konfig.max_puls_"));    			 
    			biker.setMaxpuls(160.0);
			} else 
				biker.setMaxpuls(pulsmax);

			wert = Double.parseDouble(txtcwa.getText().replace(',','.'));
			if (wert < 0.2 || wert > 0.7) {
    			Messages.errormessage(Messages.getString("konfig.cwa_muss_"));    			 
    			biker.setCwa(0.35);
			} else 
				biker.setCwa(wert);
			wert = Double.parseDouble(txtk2.getText().replace(',','.'));
			if (wert < 0.002 || wert > 0.3) {
    			Messages.errormessage(Messages.getString("konfig.k2_muss_"));    			 
    			biker.setK2(0.01);
			} else 
				biker.setK2(wert);
			wert = Double.parseDouble(txtalter.getText());	
			if (wert > 130) {
    			Messages.errormessage(Messages.getString("konfig.so_alt_"));    			 
    			biker.setAlter(30);				
			} else
				biker.setAlter(wert);
			ergo.setErgoIP(txtergoip.getText());
			ergo.setErgoCom(cmbinterface.getText());

			//Mlog.debug("debug: Selected index: " + cmbtrainer.getSelectionIndex());
			int ind = cmbtrainer.getSelectionIndex();
			ind = (ind < 0) ? 0 : ind;
			ergo.setTrainerModell(Trainertypen[ind]);
			if (ind == 3 || ind == 4 || ind == 7 || ind == 8 || ind == 9 || ind == 14 || ind == 17) 
				ergo.setTrainertyp(Trainer.typ.rolle);
			else {	// Ergometer / Crosstrainer
				if (ind == 5 || ind == 6 || ind == 13) 
					ergo.setTrainertyp(Trainer.typ.crosstrainer);
				else {
					ergo.setTrainertyp(Trainer.typ.ergo);
					if (cmbinterface.getText().compareTo("USB") == 0) {
						Messages.errormessage(Messages.getString("konfig.bei_ergometern_"));    							 
						ergo.setErgoCom(""); 
					}
				}
			}
			biker.setName(txtname.getText());
			biker.setBelohnungindex(cmbBelohnung.getSelectionIndex());
			
			Double dynnormal = new Double(Double.parseDouble(txtDynRPMNormal.getText()));
			Double dynwiege = new Double(Double.parseDouble(txtDynRPMWiege.getText()));
			if (dynwiege > dynnormal) {
    			Messages.errormessage(Messages.getString("konfig.wiegetritt_rpm_")); 
    			biker.setDynRPMNormal(dynwiege);
    			biker.setDynRPMWiege(dynnormal);
			} else {
    			biker.setDynRPMNormal(dynnormal);
    			biker.setDynRPMWiege(dynwiege);
			}

			Double lfakt = new Double(Double.parseDouble(txtlfakt.getText()));
			if (lfakt < 10.0 || lfakt > 200.0) {
    			Messages.errormessage(Messages.getString("konfig.leistungsfaktor_"));    			 
    			biker.setLfakt(1.0);
			} else 
				biker.setLfakt(lfakt/100.0);

			Double smax = new Double(Double.parseDouble(txtSteigungMax.getText().replace(',','.')));
			Double smin = new Double(Double.parseDouble(txtSteigungMin.getText().replace(',','.')));
			if (smin >= smax) {
    			Messages.errormessage(Messages.getString("konfig.min_steigung_"));    			 
			} else {
				if (smax > 20.0) {
	    			Messages.errormessage(Messages.getString("konfig.max_steigung_"));    
	    			smax = 20.0;
				}
				biker.setMaxSteigung(smax);
				biker.setMinSteigung(smin);
			}
			
			Double pulsrot = new Double(Double.parseDouble(txtpulsrot.getText()));
			Double pulsgelb = new Double(Double.parseDouble(txtpulsgelb.getText()));
			Double pulsgruen = new Double(Double.parseDouble(txtpulsgruen.getText()));
			if (pulsrot > pulsgelb && pulsgelb > pulsgruen && pulsrot < 250.0) {
    			biker.setPulsrot(pulsrot);
    			biker.setPulsgelb(pulsgelb);
    			biker.setPulsgruen(pulsgruen);
			} else {
    			Messages.errormessage(Messages.getString("konfig.pulswertefalsch")); 
    			biker.setPulsrot(0.0);	// Die Standards werden in den Getterfunktionen beim ersten Holen gesetzt
    			biker.setPulsgelb(0.0);
    			biker.setPulsgruen(0.0);
			}

			Double rpmrot = new Double(Double.parseDouble(txtrpmrot.getText()));
			Double rpmgelb = new Double(Double.parseDouble(txtrpmgelb.getText()));
			Double rpmgruen = new Double(Double.parseDouble(txtrpmgruen.getText()));
			if (rpmrot < rpmgelb && rpmgelb < rpmgruen) {
    			biker.setRpmrot(rpmrot);
    			biker.setRpmgelb(rpmgelb);
    			biker.setRpmgruen(rpmgruen);
			} else {
    			Messages.errormessage(Messages.getString("konfig.rpmwertefalsch")); 
    			biker.setRpmrot(40.0);
    			biker.setRpmgelb(60.0);
    			biker.setRpmgruen(80.0);
			}

			Double prot = new Double(Double.parseDouble(txtprot.getText()));
			Double pgelb = new Double(Double.parseDouble(txtpgelb.getText()));
			Double pgruen = new Double(Double.parseDouble(txtpgruen.getText()));
			if (prot > pgelb && pgelb > pgruen) {
    			biker.setProt(prot);
    			biker.setPgelb(pgelb);
    			biker.setPgruen(pgruen);
			} else {
    			Messages.errormessage(Messages.getString("konfig.pwertefalsch")); 
    			biker.setProt(300.0);
    			biker.setPgelb(200.0);
    			biker.setPgruen(80.0);
			}

			//liveprofil = chkLiveProfil.getSelection();
			averaging = chkAveraging.getSelection();
			tcxpower = chkTCXPower.getSelection();
			dynamik = chkDynamik.getSelection();
			if (dynamik && dynnormal == 0) {
    			Messages.errormessage(Messages.getString("konfig.normtritt_rpm_0")); 
    			biker.setDynRPMNormal(90);
			}
			nooverlay = chkOverlay.getSelection();
			if (Rsmain.libant != null) {
				Rsmain.libant.setEin(chkAntpPuls.getSelection() || chkAntpCadenceSpeed.getSelection() || chkAntpCadence.getSelection());
			}
			showmap = chkMap.getSelection();
			showInfo = chkInfo.getSelection();
			autoOK = chkAutoOK.getSelection();
			
			if (chkAutomatik.getSelection()) {
				automatik1 = new Double(Double.parseDouble(txtAutomatik1.getText()));
				automatik2 = new Double(Double.parseDouble(txtAutomatik2.getText()));
				if (automatik1 < automatik2 && automatik2 - automatik1 > 50 && automatik1 > pmin && automatik2 < pmax) {
					biker.setAutomatik1(automatik1);
					biker.setAutomatik2(automatik2);
				} else {
	    			Messages.errormessage(Messages.getString("konfig.automatikwertefalsch")); 
					biker.setAutomatik1(pmin + 20.0);
					biker.setAutomatik2(pmax - 20.0);
				}
			}
			//biker.setIbl_APIKey(txtibl.getText());
			Global.stravaKey = txtStravaKey.getText();
			
		} catch (Exception e) {
	        Mlog.error(Messages.getString("konfig.formatfehler")); 
	        MessageBox messageBox = new MessageBox(sShell, SWT.OK|SWT.ICON_WARNING);
	        messageBox.setMessage(Messages.getString("konfig.kein_gueltiges_Format_")); 
	        messageBox.open();
		}
	}
	
	/**
	 * allg. Settings - XML-Datei mittels DOM API parsen und DOM-Objekt erzeugen
	 * @param dateiname  Dateiname XML-Datei
	 * @param biker      Fahrer
	 *
	 */
	private void parseXmlFileSettings(String dateiname, Fahrer biker){
		// hole factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {			
			// mittels factory instance des document builder erzeugen
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			// DOM Representation des XML file erzeugen 
			File f = new File(Global.strPfad+dateiname);  
			URL u = f.toURI().toURL(); 
			dom = db.parse(u.toString());
			
            Element konfigele = dom.getDocumentElement();
            try {  // ignorieren, wenn nicht vorhanden!
            	setStrTourenpfad(konfigele.getAttribute("tourenpfad")); 
			} catch (Exception e) {
				setStrTourenpfad(Global.strProgramPfad+"touren"); 
				Mlog.info("kein Tourenpfad in "+f.toString()+" eingetragen, verwendet wird: "+getStrTourenpfad());  
			}

			regCode = konfigele.getAttribute("code");
			Global.RegDat.setRegdatfromCode(regCode);
			String snr = konfigele.getAttribute("snr");	// bis 3.14 wurde die SNR in den Settings abgelegt!
			if (regCode.isEmpty() && !snr.isEmpty())
				Global.RegDat.sNr = snr;
			Global.serverAdr = konfigele.getAttribute("server");
			Mlog.debug("Settings: server = "+konfigele.getAttribute("server"));
            liveprofil = new Boolean(konfigele.getAttribute("liveprofil")); 
            biker.setName(konfigele.getAttribute("biker")); 
			Mlog.debug("Settings: biker = "+konfigele.getAttribute("biker"));
            int x = new Integer(konfigele.getAttribute("win_x"));
			Mlog.debug("Settings: win_x = "+konfigele.getAttribute("win_x"));
            int y = new Integer(konfigele.getAttribute("win_y"));
			Mlog.debug("Settings: win_y = "+konfigele.getAttribute("win_y"));
            int w = new Integer(konfigele.getAttribute("win_weite"));
			Mlog.debug("Settings: win_weite = "+konfigele.getAttribute("win_weite"));
            int h = new Integer(konfigele.getAttribute("win_hoehe"));
			Mlog.debug("Settings: win_hoehe = "+konfigele.getAttribute("win_hoehe"));
            Rsmain.sShell.setBounds(x, y, w, h);
            Rsmain.anzmodus = new Integer(konfigele.getAttribute("anzmodus"));   
			Mlog.debug("Settings: anzmodus = "+konfigele.getAttribute("anzmodus"));
            Global.rennenUserID = new Integer(konfigele.getAttribute("userid"));   
            // Displayfarbe einlesen:
			int hex = Integer.parseInt(konfigele.getAttribute("display_rgb"), 16);
			Mlog.debug("Settings: display_rgb = "+konfigele.getAttribute("display_rgb"));
			int red = (0xff0000 & hex) >> 16;
			int green = (0x00ff00 & hex) >> 8;
			int blue = 0x0000ff & hex;
            Color displaycolor = new Color(Display.getCurrent(), red, green, blue);
            Rsmain.setRahmenfarbe(displaycolor);
            Global.stravaKey = konfigele.getAttribute("stravakey"); 
			Mlog.debug("Settings: stravakey = "+konfigele.getAttribute("stravakey"));
			String tile = konfigele.getAttribute("tile_url");
			if (!tile.isEmpty()) {
				OSMViewer.tileServer.setURL(tile);
				Mlog.debug("Settings: tile_url = "+tile);
				tile = konfigele.getAttribute("tile_sw_xy");
				OSMViewer.tileServer.setswitchXY(new Boolean(tile));
				Mlog.debug("Settings: tile_sw_xy = "+tile);
				tile = konfigele.getAttribute("tile_ext");
				OSMViewer.tileServer.setextImage(tile);
				Mlog.debug("Settings: tile_ext = "+tile);
			}
            
		}catch(ParserConfigurationException pce) {
			Mlog.ex(pce);
		}catch(SAXException se) {
			Mlog.ex(se);
		}catch(IOException ioe) {
			Mlog.error(Messages.getString("konfig.settings_nicht_")); 
			createXMLFileSettings(dateiname, biker);
		}catch(Exception e) {
			Mlog.error(Messages.getString("konfig.settings_fehlerhaft_")); 
			createXMLFileSettings(dateiname, biker);
		}
	}

	/**
	 * Liest einen numerischen Wert ein und frägt auf NaN ab.
	 * @param ele  Element
	 * @param tag  XML-Tag
	 * @return eingelesenen Wert als double
	 */
	private double getNumberAttr(Element ele, String tag) {
		String wert = ele.getAttribute(tag);
		double dwert = 0.0;
		
        try {
			if (wert.equals("NaN"))
				dwert = 0.0;
			else
				dwert = Double.parseDouble(wert);
		} catch (NumberFormatException e) {
			Mlog.ex(e);
		} 
		
        return (dwert);
	}
	
	/**
	 * Fahrer - XML-Datei mittels DOM API parsen und DOM-Objekt erzeugen
	 * @param dateiname  Dateiname XML-Datei
	 * @param biker      Fahrer
	 * @param ergo       Trainingsgerät
	 *
	 */
	private void parseXmlFileBiker(String dateiname, Fahrer biker, Trainer ergo){
		// hole factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {			
			// mittels factory instance des document builder erzeugen
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			// DOM Representation des XML file erzeugen 
			File f = new File(dateiname); 
			URL u = f.toURI().toURL(); 
			dom = db.parse(u.toString());
			
            Element elefahrer = dom.getDocumentElement();
            //Element elefahrer = (Element) konfigele.getElementsByTagName("fahrer").item(0);
			// Werte aus XML-Fahrerprofil übernehmen:
			Double wert = new Double(getNumberAttr(elefahrer, "fahrergewicht"));			 
			Mlog.debug("Biker: fahrergewicht = "+getNumberAttr(elefahrer, "fahrergewicht"));
            biker.setFahrergewicht(wert);
			wert = new Double(getNumberAttr(elefahrer, "bikegewicht"));			 
			Mlog.debug("Biker: bikegewicht = "+getNumberAttr(elefahrer, "bikegewicht"));
            biker.setBikegewicht(wert);
            wert = getNumberAttr(elefahrer, "maxleistung"); 
			Mlog.debug("Biker: maxleistung = "+getNumberAttr(elefahrer, "maxleistung"));
            biker.setMaxleistung(wert);
            wert = getNumberAttr(elefahrer, "minleistung"); 
			Mlog.debug("Biker: minleistung = "+getNumberAttr(elefahrer, "minleistung"));
            biker.setMinleistung(wert);
            wert = getNumberAttr(elefahrer, "maxpuls"); 
			Mlog.debug("Biker: maxpuls = "+getNumberAttr(elefahrer, "maxpuls"));
            biker.setMaxpuls(wert);
            wert = getNumberAttr(elefahrer, "cwa"); 
			Mlog.debug("Biker: cwa = "+getNumberAttr(elefahrer, "cwa"));
            biker.setCwa(wert);
            wert = getNumberAttr(elefahrer, "k2"); 
			Mlog.debug("Biker: k2 = "+getNumberAttr(elefahrer, "k2"));
            biker.setK2(wert);
            wert = getNumberAttr(elefahrer, "alter"); 
			Mlog.debug("Biker: alter = "+getNumberAttr(elefahrer, "alter"));
            biker.setAlter(wert);
            biker.setName(elefahrer.getAttribute("name")); 
            wert = getNumberAttr(elefahrer, "dynrpmnormal"); 
			Mlog.debug("Biker: dynrpmnormal = "+getNumberAttr(elefahrer, "dynrpmnormal"));
            biker.setDynRPMNormal(wert);
            wert = getNumberAttr(elefahrer, "dynrpmwiege"); 
			Mlog.debug("Biker: dynrpmwiege = "+getNumberAttr(elefahrer, "dynrpmwiege"));
            biker.setDynRPMWiege(wert);
			wert = getNumberAttr(elefahrer, "leistungsfaktor");			 
			Mlog.debug("Biker: leistungsfaktor = "+getNumberAttr(elefahrer, "leistungsfaktor"));
            biker.setLfakt(wert);
            wert = getNumberAttr(elefahrer, "maxsteigung"); 
			Mlog.debug("Biker: maxsteigung = "+getNumberAttr(elefahrer, "maxsteigung"));
            biker.setMaxSteigung(wert);
            wert = getNumberAttr(elefahrer, "minsteigung"); 
			Mlog.debug("Biker: minsteigung = "+getNumberAttr(elefahrer, "minsteigung"));
            biker.setMinSteigung(wert);
            wert = getNumberAttr(elefahrer, "pulsrot"); 
			Mlog.debug("Biker: pulsrot = "+getNumberAttr(elefahrer, "pulsrot"));
            biker.setPulsrot(wert);
            wert = getNumberAttr(elefahrer, "pulsgelb"); 
			Mlog.debug("Biker: pulsgelb = "+getNumberAttr(elefahrer, "pulsgelb"));
            biker.setPulsgelb(wert);
            wert = getNumberAttr(elefahrer, "pulsgruen"); 
			Mlog.debug("Biker: pulsgruen = "+getNumberAttr(elefahrer, "pulsgruen"));
            biker.setPulsgruen(wert);
            wert = getNumberAttr(elefahrer, "rpmrot"); 
			Mlog.debug("Biker: rpmrot = "+getNumberAttr(elefahrer, "rpmrot"));
            biker.setRpmrot(wert);
            wert = getNumberAttr(elefahrer, "rpmgelb"); 
			Mlog.debug("Biker: rpmgelb = "+getNumberAttr(elefahrer, "rpmgelb"));
            biker.setRpmgelb(wert);
            wert = getNumberAttr(elefahrer, "rpmgruen"); 
			Mlog.debug("Biker: rpmgruen = "+getNumberAttr(elefahrer, "rpmgruen"));
            biker.setRpmgruen(wert);
            wert = getNumberAttr(elefahrer, "prot"); 
			Mlog.debug("Biker: prot = "+getNumberAttr(elefahrer, "prot"));
            biker.setProt(wert);
            wert = getNumberAttr(elefahrer, "pgelb"); 
			Mlog.debug("Biker: pgelb = "+getNumberAttr(elefahrer, "pgelb"));
            biker.setPgelb(wert);
            wert = getNumberAttr(elefahrer, "pgruen"); 
			Mlog.debug("Biker: pgruen = "+getNumberAttr(elefahrer, "pgruen"));
            biker.setPgruen(wert);
            long lwert = Long.parseLong(elefahrer.getAttribute("trainingszeit"));
            biker.setGesTrainingszeit(lwert);
            wert = getNumberAttr(elefahrer, "strecke"); 
            biker.setGesStrecke(wert);
            wert = getNumberAttr(elefahrer, "hm"); 
            biker.setGesHM(wert);
            wert = getNumberAttr(elefahrer, "kcal"); 
            biker.setGesKCal(wert);
            lwert = Long.parseLong(elefahrer.getAttribute("anzahl"));
            biker.setGesAnzahl(lwert);
//            wert = getNumberAttr(elefahrer, "fw"); 
//            biker.setGesFitnesswert(wert);
            wert = getNumberAttr(elefahrer, "puls"); 
            biker.setGesPuls(wert);
            wert = getNumberAttr(elefahrer, "rpm"); 
            biker.setGesRPM(wert);
            wert = getNumberAttr(elefahrer, "leistung"); 
            biker.setGesLeistung(wert);            
			Mlog.debug("Biker: automatik = "+elefahrer.getAttribute("automatik"));
            biker.setAutomatik(new Boolean(elefahrer.getAttribute("automatik"))); 
            wert = getNumberAttr(elefahrer, "automatik1"); 
			Mlog.debug("Biker: automatik1 = "+getNumberAttr(elefahrer, "automatik1"));
            biker.setAutomatik1(wert);            
            wert = getNumberAttr(elefahrer, "automatik2"); 
			Mlog.debug("Biker: automatik2 = "+getNumberAttr(elefahrer, "automatik2"));
            biker.setAutomatik2(wert); 
            int iwert = Integer.parseInt(elefahrer.getAttribute("belohnung"));
            biker.setBelohnungindex(iwert);            
			Mlog.debug("Biker: averaging = "+elefahrer.getAttribute("averaging"));
            averaging = new Boolean(elefahrer.getAttribute("averaging")); 
			Mlog.debug("Biker: tcxpower = "+elefahrer.getAttribute("tcxpower"));
            tcxpower = new Boolean(elefahrer.getAttribute("tcxpower")); 
			Mlog.debug("Biker: dynamik = "+elefahrer.getAttribute("dynamik"));
            dynamik = new Boolean(elefahrer.getAttribute("dynamik")); 
			Mlog.debug("Biker: nooverlay = "+elefahrer.getAttribute("nooverlay"));
            nooverlay = new Boolean(elefahrer.getAttribute("nooverlay"));

            String attShow = elefahrer.getAttribute("showmap");
            if (attShow.isEmpty())
            	attShow = "true";					// Standard true, wenn nicht vorgegeben
			Mlog.debug("Biker: showmap = "+elefahrer.getAttribute("showmap"));
            showmap = new Boolean(attShow);
            
            attShow = elefahrer.getAttribute("showinfo");
            if (attShow.isEmpty())
            	attShow = "true";					// Standard true, wenn nicht vorgegeben
			Mlog.debug("Biker: showinfo = "+elefahrer.getAttribute("showinfo"));
            showInfo = new Boolean(attShow);
            
            attShow = elefahrer.getAttribute("autook");
            if (attShow.isEmpty())
            	attShow = "false";					// Standard false, wenn nicht vorgegeben
			Mlog.debug("Biker: autook = "+elefahrer.getAttribute("autook"));
            autoOK = new Boolean(attShow);

            boolean getAnt = new Boolean(elefahrer.getAttribute("antpuls"));
			Mlog.debug("Biker: antpuls = "+elefahrer.getAttribute("antpuls"));
            if (Rsmain.libant != null) {
            	Rsmain.libant.setEin(getAnt);
            	if (getAnt)
            		Rsmain.libant.setaktTHr(LibAnt.THr.antppuls);
            	else
            		Rsmain.libant.setaktTHr(LibAnt.THr.notset);  
            	
            	getAnt = new Boolean(elefahrer.getAttribute("antcadenceonly"));
    			Mlog.debug("Biker: antcadenceonly = "+elefahrer.getAttribute("antcadenceonly"));
            	Rsmain.libant.setEin(getAnt);
            	if (getAnt)
            		Rsmain.libant.setaktTCadence(LibAnt.TCadence.antpcadence);
            	else {
            		getAnt = new Boolean(elefahrer.getAttribute("antcadence"));
            		Mlog.debug("Biker: antcadence = "+elefahrer.getAttribute("antcadence"));
            		Rsmain.libant.setEin(getAnt);
            		if (getAnt)
            			Rsmain.libant.setaktTCadence(LibAnt.TCadence.antpcadencespeed);
            		else
            			Rsmain.libant.setaktTCadence(LibAnt.TCadence.notset); 
            	}
            }
			Mlog.debug("Biker: ergoip = "+elefahrer.getAttribute("ergoip"));
            ergo.setErgoIP(elefahrer.getAttribute("ergoip")); 
			Mlog.debug("Biker: ergocom = "+elefahrer.getAttribute("ergocom"));
            ergo.setErgoCom(elefahrer.getAttribute("ergocom")); 
			Mlog.debug("Biker: hersteller = "+elefahrer.getAttribute("hersteller"));
            ergo.setTrainerModell(elefahrer.getAttribute("hersteller")); 
            if (ergo.getTrainerModell().startsWith("tacx") || ergo.getTrainerModell().startsWith("ant"))
           		ergo.setTrainertyp(Trainer.typ.rolle);
           	else
           		if (ergo.getTrainerModell().endsWith("cr"))
               		ergo.setTrainertyp(Trainer.typ.crosstrainer);
           		else	
           			ergo.setTrainertyp(Trainer.typ.ergo);
			Mlog.debug("Biker: infourl = "+elefahrer.getAttribute("infourl"));
            Global.infoURL = elefahrer.getAttribute("infourl");
			Mlog.debug("Biker: GPXIndex = "+elefahrer.getAttribute("GPXIndex"));
            Global.lastTourGPSPkt = new Long(elefahrer.getAttribute("GPXIndex"));
			Mlog.debug("Biker: GPXFile = "+elefahrer.getAttribute("GPXFile"));
            Global.lastGPXFile = elefahrer.getAttribute("GPXFile");
            
		}catch(ParserConfigurationException pce) {
			Mlog.ex(pce);
		}catch(SAXException se) {
			Mlog.ex(se);
		}catch(IOException ioe) {
			Mlog.info("Profil: " + dateiname + " ist nicht vorhanden und wird mit Defaultwerten neu erzeugt!");  
			createXMLFileBiker(dateiname,biker,ergo);
		}catch(Exception e) {
			Mlog.info("Profil: " + dateiname + " ist fehlerhaft und wird neu erzeugt!");  
			createXMLFileBiker(dateiname,biker,ergo);
		}
	}
	
	
	/**
	 * allg. Settings-XML-Datei mittels DOM-Objekt erzeugen.
	 * @param dateiname  Name der Settingsdatei
	 * @param biker      Fahrer
	 *
	 */
	public void createXMLFileSettings(String dateiname, Fahrer biker){
		// hole factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {			
			// mittels factory instance des document builder erzeugen
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			// DOM Representation des XML file erzeugen 
			dom = db.newDocument();
			DOMSource source = new DOMSource(dom);

			Element elekonfig = dom.createElement("konfig"); 
			elekonfig.setAttribute("liveprofil", new Boolean(liveprofil).toString()); 
			elekonfig.setAttribute("biker", biker.getName()); 
			elekonfig.setAttribute("win_x", new Integer(Rsmain.sShell.getLocation().x).toString()); 
			elekonfig.setAttribute("win_y", new Integer(Rsmain.sShell.getLocation().y).toString()); 
			elekonfig.setAttribute("win_weite", new Integer(Rsmain.sShell.getSize().x).toString()); 
			elekonfig.setAttribute("win_hoehe", new Integer(Rsmain.sShell.getSize().y).toString()); 
			elekonfig.setAttribute("tourenpfad", getStrTourenpfad());
			elekonfig.setAttribute("server", Global.serverAdr);
			elekonfig.setAttribute("code", getRegCode());
			elekonfig.setAttribute("anzmodus", new Integer(Rsmain.anzmodus).toString());
			elekonfig.setAttribute("userid", new Integer(Global.rennenUserID).toString());
			RGB displayrgb = Rsmain.getRahmenfarbe().getRGB();
			elekonfig.setAttribute("display_rgb", Integer.toHexString(displayrgb.red)+Integer.toHexString(displayrgb.green)+Integer.toHexString(displayrgb.blue));
			elekonfig.setAttribute("stravakey", Global.stravaKey); 
			elekonfig.setAttribute("tile_url", OSMViewer.tileServer.getURL()); 
			elekonfig.setAttribute("tile_sw_xy", new Boolean(OSMViewer.tileServer.isswitchXY()).toString()); 
			elekonfig.setAttribute("tile_ext", OSMViewer.tileServer.getextImage()); 
			dom.appendChild(elekonfig);
			
			StreamResult result = new StreamResult(new File(Global.strPfad+dateiname)); 
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(source, result);

		} catch (Exception e) {
			Mlog.ex(e);
		}
	}	

	/**
	 * Fahrer-XML-Datei mittels DOM-Objekt erzeugen.
	 * @param dateiname  Dateiname pers. XML-Datei Settings
	 * @param biker      Fahrer
	 * @param ergo		 Trainingsgerät
	 *
	 */
	public void createXMLFileBiker(String dateiname, Fahrer biker, Trainer ergo){
		// hole factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {			
			// mittels factory instance des document builder erzeugen
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			// DOM Representation des XML file erzeugen 
			dom = db.newDocument();
			DOMSource source = new DOMSource(dom);

			Element elefahrer = dom.createElement("fahrer"); 
			elefahrer.setAttribute("fahrergewicht", new Double(biker.getFahrergewicht()).toString()); 
			elefahrer.setAttribute("bikegewicht", new Double(biker.getBikegewicht()).toString()); 
			elefahrer.setAttribute("maxleistung", new Double(biker.getMaxleistung()).toString()); 
			elefahrer.setAttribute("minleistung", new Double(biker.getMinleistung()).toString()); 
			elefahrer.setAttribute("maxpuls", new Double(biker.getMaxpuls()).toString()); 
			elefahrer.setAttribute("cwa", new Double(biker.getCwa()).toString()); 
			elefahrer.setAttribute("k2", new Double(biker.getK2()).toString()); 
			elefahrer.setAttribute("alter", new Double(biker.getAlter()).toString()); 
			elefahrer.setAttribute("name", biker.getName()); 
			elefahrer.setAttribute("dynrpmnormal", new Double(biker.getDynRPMNormal()).toString()); 
			elefahrer.setAttribute("dynrpmwiege", new Double(biker.getDynRPMWiege()).toString()); 
			elefahrer.setAttribute("leistungsfaktor", new Double(biker.getLfakt()).toString()); 
			elefahrer.setAttribute("maxsteigung", new Double(biker.getMaxSteigung()).toString()); 
			elefahrer.setAttribute("minsteigung", new Double(biker.getMinSteigung()).toString()); 
			elefahrer.setAttribute("pulsrot", new Double(biker.getPulsrot()).toString()); 
			elefahrer.setAttribute("pulsgelb", new Double(biker.getPulsgelb()).toString()); 
			elefahrer.setAttribute("pulsgruen", new Double(biker.getPulsgruen()).toString()); 
			elefahrer.setAttribute("rpmrot", new Double(biker.getRpmrot()).toString()); 
			elefahrer.setAttribute("rpmgelb", new Double(biker.getRpmgelb()).toString()); 
			elefahrer.setAttribute("rpmgruen", new Double(biker.getRpmgruen()).toString()); 
			elefahrer.setAttribute("prot", new Double(biker.getProt()).toString()); 
			elefahrer.setAttribute("pgelb", new Double(biker.getPgelb()).toString()); 
			elefahrer.setAttribute("pgruen", new Double(biker.getPgruen()).toString()); 
			elefahrer.setAttribute("trainingszeit", new Long(biker.getGesTrainingszeit()).toString()); 
			elefahrer.setAttribute("strecke", new Double(biker.getGesStrecke()).toString()); 
			elefahrer.setAttribute("hm", new Double(biker.getGesHM()).toString()); 
			elefahrer.setAttribute("kcal", new Double(biker.getGesKCal()).toString()); 
			elefahrer.setAttribute("anzahl", new Long(biker.getGesAnzahl()).toString()); 
//			elefahrer.setAttribute("fw", new Double(biker.getGesFitnesswert()).toString()); 
			elefahrer.setAttribute("puls", new Double(biker.getGesPuls()).toString()); 
			elefahrer.setAttribute("rpm", new Double(biker.getGesRPM()).toString()); 
			elefahrer.setAttribute("leistung", new Double(biker.getGesLeistung()).toString()); 
			elefahrer.setAttribute("automatik", new Boolean(biker.isAutomatik()).toString()); 
			elefahrer.setAttribute("automatik1", new Double(biker.getAutomatik1()).toString()); 
			elefahrer.setAttribute("automatik2", new Double(biker.getAutomatik2()).toString()); 
			elefahrer.setAttribute("belohnung", new Integer(biker.getBelohnungindex()).toString()); 

			elefahrer.setAttribute("averaging", new Boolean(averaging).toString()); 
			elefahrer.setAttribute("tcxpower", new Boolean(tcxpower).toString()); 
			elefahrer.setAttribute("ergoip", ergo.getErgoIP()); 
			elefahrer.setAttribute("ergocom", ergo.getErgoCom()); 
			elefahrer.setAttribute("hersteller", ergo.getTrainerModell()); 
			elefahrer.setAttribute("dynamik", new Boolean(dynamik).toString()); 
			elefahrer.setAttribute("nooverlay", new Boolean(nooverlay).toString()); 
			elefahrer.setAttribute("showmap", new Boolean(showmap).toString()); 
			elefahrer.setAttribute("GPXFile", Global.gPXfile);
			elefahrer.setAttribute("GPXIndex", new Long(Global.aktGPSPunkt).toString());
			
			if (Rsmain.libant != null) {
				boolean isAnt = false;
				if (Rsmain.libant.isEin() && Rsmain.libant.getaktTHr() == LibAnt.THr.antppuls)
					isAnt = true;
					
				elefahrer.setAttribute("antpuls", new Boolean(isAnt).toString()); 

				if (Rsmain.libant.isEin() && Rsmain.libant.getaktTCadence() != LibAnt.TCadence.notset) {
					isAnt = true;
					if (Rsmain.libant.getaktTCadence() == LibAnt.TCadence.antpcadence)
						elefahrer.setAttribute("antcadenceonly", new Boolean(isAnt).toString()); 
					
					if (Rsmain.libant.getaktTCadence() == LibAnt.TCadence.antpcadencespeed)
						elefahrer.setAttribute("antcadence", new Boolean(isAnt).toString()); 
				}
			}
			elefahrer.setAttribute("showinfo", new Boolean(showInfo).toString()); 
			elefahrer.setAttribute("autook", new Boolean(autoOK).toString()); 
			elefahrer.setAttribute("infourl", Global.infoURL); 

			dom.appendChild(elefahrer);
			
			StreamResult result = new StreamResult(new File(dateiname));
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(source, result);

		} catch (Exception e) {
			Mlog.ex(e);
		}
	}	

	/**
	 * initialisiert die String-Arrays aus den Message-Properties
	 */
	private void init() {
		try {
			// Belohnung:
			for (int i=0; i<AnzBelohnung; i++) 
				Belohnung[i] = Messages.getString("konfig.cmbbel"+(i+1));
			BelohnungkCal[0] = 350;		// Apfelkuchen
			BelohnungkCal[1] = 105;		// Banane
			BelohnungkCal[2] = 55;		// Dominostein
			BelohnungkCal[3] = 220;		// Hefeweizen
			BelohnungkCal[4] = 400;		// Käsekuchen
			BelohnungkCal[5] = 130;		// Muffin
			BelohnungkCal[6] = 490;		// Sardinenbrötchen
			BelohnungkCal[7] = 510;		// SchinkenKäseSandwich
			BelohnungkCal[8] = 105;		// Schokokuss"
			BelohnungkCal[9] = 945;		// Nussecke
			BelohnungkCal[10] = 193;	// Weisswurst
			
			
			// Trainer
			trainer[0]  = "Daum Ergometer";
			trainer[1]  = Messages.getString("konfig.daum_ergometer_2001");
			trainer[2]  = "Kettler Ergometer";
			trainer[3]  = "Tacx i-Magic/i-Flow";
			trainer[4]  = "Tacx Fortius";     
			trainer[5]  = "Daum Crosstrainer";     
			trainer[6]  = "Kettler Crosstrainer";
			trainer[7]  = "Tacx Vortex";
			trainer[8]  = "Tacx Bushido";
			trainer[9]  = "ANT+ (RPM/Speed)";
			trainer[10] = "Cyclus 2 (Ergoline)";
			trainer[11] = "Cyclus 2";
			trainer[12] = "Ergo-Fit Ergometer";
			trainer[13] = "Ergo-Fit Crosstrainer";
			trainer[14] = "ANT+ FE-C";
			trainer[15] = "---";		// ergoline - nicht voll funktionsfähig!
			trainer[16] = "wahoo KICKR";
			trainer[17] = "ANT+ (RPM)";

			// Sportart
			SPArt[0] = Messages.getString("konfig.mountainbike");
			SPArt[1] = Messages.getString("konfig.rennrad");  
			SPArt[2] = Messages.getString("konfig.langlauf");  

			// LFStufen
			for (int i=0; i<AnzLFStufen; i++)
				LFStufen[i] = Messages.getString("konfig.lf"+(i+1));
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}
}
