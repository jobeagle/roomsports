import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXVersion;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import ch.ntb.usb.Device;
import ch.ntb.usb.LibusbJava;
import ch.ntb.usb.USB;
import ch.ntb.usb.USBException;
import ch.ntb.usb.Usb_Config_Descriptor;
import ch.ntb.usb.Usb_Interface;

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
 * Trainer.java: Schnittstelle zum Ergometer/Rollentrainer für RoomSports
 *****************************************************************************
 *
 * Diese Klasse bildet die Schnittstelle von RoomSports zum Trainingsgerät und
 * beinhaltet die Methoden zur Konfiguration, Kommunikation und Steuerung der
 * verwendeten Daum- und Kettler-Trainingsgeräte mit serieller bzw. Netzwerkschnittstelle.
 * Die USB-Schnittstelle der Kettlergeräte wird über serielle Kommunikation versorgt.
 * Tacx-Rollentrainer werden über einen USB-Filtertreiber (LibUSBJava) angesteuert.
 *  
 */

public class Trainer {
    public enum usbCommands { kommunikation };
    public enum kommunikation { keine, netzwerk, seriell, usb, ant };
    public enum Commands { version, trainingszeit, puls, geschwindigkeit, steigung, leistung, rpm, getadress, check_cockpit, init, 
    						getpower, reset, lesedaten, getid, setpc, setpc_alt, setpc_aus, sp0, start, stop, cassette, rings, gangHinten };
    public enum typ { rolle, ergo, crosstrainer };
    private typ trainertyp = typ.ergo;
	private String ergoIP = new String(); 
	private String Com = new String();
	private byte ergoAdresse = 0;
	private Socket ergoSocket;
    private InputStream in;
    private OutputStream out;	                
    private int NetTimout = 1000;               // Netzwerktimeout bei Daum im Milleskunden
    private long kettlerwait = 150; 			// Kettler: Millisekungen Wartezeit zwischen senden und empfangen!
    private long ergofitwait = 200; 			// Ergo-Fit: Millisekungen Wartezeit zwischen senden und empfangen!
    private long kettlerresetwait = 2000; 		// Kettler: nach Reset etwas warten
    private long daumwait = 150; 				// Millisekungen Wartezeit zwischen senden und empfangen!
    private long daum2001wait = 10; 			// Daum-2001: Millisekungen Wartezeit zwischen senden und empfangen jedes Zeichens!
    private SerialPort serialPort;
    public  String trainerModell = "daum";		// alle Typen siehe Konfiguration!
    											
    private int gang = 5;
    private boolean deepdebug = false;	
    private Device dev;
    private double bikergewicht = 90.0;			// wird bei Tacx benötigt
	private byte[] readData = new byte[64];		// Tacx: data read from the device   
	private byte[] writeData = new byte[] { 0x01, 0x08, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x54, 0x10, 0x04 }; // Tacx: Datenbuffer für neue Cockpits iMagic und Fortius
	private byte[] wDataAlt = new byte[] { (byte) 0x80, 0x00 }; // Tacx: Datenbuffer für alte Cockpits 
	private int steigungcode = 0;				// Tacx: temp. für Steigung
	private double tacxvcorrect1 = 1.05;			// Korrekturfaktor bei der Geschwindigkeit Tacx iMagic vorher: 1.9 (1.53: 2.0)
	private double tacxvcorrect2 = 1.05;			// Korrekturfaktor bei der Geschwindigkeit Tacx Fortius (vorher 1.3)
	private double tacxpcorrect = 600.0;		// Korrekturfaktor bei der Leistungsermittlung (vorher 400)
	private String Antwort = new String();		// für Kommunikationsantworten von Kettlergeräten
	private String kettlerCockpit = new String(); // Cockpitversion z. B. SJ10
	private boolean tacxalt = false;			// Flag für die alten grünen und blauen Cockpits
	private int aktpow = 0;						// letzte Power merken um ST-Kommando bei Kettler zu vermeiden
	private String[] cyclus2Data15 = null;
	private String cyclus2CassetteMTB = "11,12,14,16,18,21,24,28,32";	// 9-fach Kassette bei MTB
	private String cyclus2CassetteRR = "11,12,13,14,15,17,19,21,24,28";	// 10-fach Kassette Rennrad
	private String cyclus2RingsMTB = "22,32,44";	// 3-fach Übersetzung MTB
	private String cyclus2RingsRR = "39,53";		// 2-fach Übersetzung Rennrad
		
    /**
	 * @return the trainertyp
	 */
	public typ getTrainertyp() {
		return trainertyp;
	}

	/**
	 * @param trainertyp the trainertyp to set
	 */
	public void setTrainertyp(typ trainertyp) {
		this.trainertyp = trainertyp;
	}

	/**
	 * @return the kettlerwait
	 */
	public long getKettlerwait() {
		return kettlerwait;
	}

	/**
	 * @param kettlerwait the kettlerwait to set
	 */
	public void setKettlerwait(long kettlerwait) {
		this.kettlerwait = kettlerwait;
	}

	/**
	 * @return the netTimout
	 */
	public int getNetTimout() {
		return NetTimout;
	}

	/**
	 * @param netTimout the netTimout to set
	 */
	public void setNetTimout(int netTimout) {
		NetTimout = netTimout;
	}

	/**
	 * @return gibt das Flag deepdebug zurück
	 */
	public boolean isDeepdebug() {
		return deepdebug;
	}

	/**
	 * @param deepdebug setze das Flag deepdebug
	 */
	public void setDeepdebug(boolean deepdebug) {
		this.deepdebug = deepdebug;
	}

	/**
	 * @return gibt den eingelegten Gang zurück
	 */
	public int getGang() {
		return gang;
	}

	/**
	 * @param gang setzt den eingelegten Gang
	 */
	public void setGang(int gang) {
		this.gang = gang;
	}

	/**
	 * @return the ergoHersteller
	 */
	public String getTrainerModell() {
		return trainerModell;
	}

	/**
	 * @param trainerModell setze trainerModell
	 */
	public void setTrainerModell(String trainerModell) {
		this.trainerModell = trainerModell;
	}

    /**
	 * Getter-Funktion für ergoIP
	 * @return ergoIP als String
	 */
	public String getErgoIP() {
		return ergoIP;
	}

	/**
	 * Setter-Funktion für ergoIP
	 * @param ergoIP 	IP Adresse für Ergometer (z.B. Daum P8i)
	 */
	public void setErgoIP(String ergoIP) {
		this.ergoIP = ergoIP;
	}

	/**
	 * Getter-Funktion für ergoCom = Schnittstelle (LAN, COM1 etc.) 
	 * @return Schnittstelle
	 */
	public String getErgoCom() {
		return Com;
	}

	/**
	 * Setter-Funktion für ergoCom (Schnittstelle)
	 * @param com	COM-Schnittstelle
	 */
	public void setErgoCom(String com) {
		this.Com = com;
	}

	/**
	 * Getter-Funktion für ergoAdresse
	 * @return Adresse (ser. Schnittstelle)
	 */
	public byte getErgoAdresse() {
		return ergoAdresse;
	}

	/**
	 * Setter-Funktion für ergoAdresse
	 * @param ergoAdresse		ergoAdresse
	 */
	public void setErgoAdresse(byte ergoAdresse) {
		this.ergoAdresse = ergoAdresse;
	}


	/**
	 * @param kettlerCockpit the kettlerCockpit to set
	 */
	public void setKettlerCockpit(String kettlerCockpit) {
		this.kettlerCockpit = kettlerCockpit;
	}

	/**
	 * @return the kettlerCockpit
	 */
	public String getKettlerCockpit() {
		return kettlerCockpit;
	}

	/**
	 * @return the tacxalt
	 */
	public boolean isTacxalt() {
		return tacxalt;
	}

	/**
	 * @param tacxalt the tacxalt to set
	 */
	public void setTacxalt(boolean tacxalt) {
		this.tacxalt = tacxalt;
	}

	/**
     * prüfe, über welche Verbindung der Ergometer angeschlossen ist
     * @return	Schnittstelle, über die die Kommunikation läuft
     */
    public kommunikation hatverbindung() {
    	if (Com.isEmpty()) {
    		return kommunikation.keine;
    	} else if (Com.equalsIgnoreCase("LAN")) {
    		return kommunikation.netzwerk;    		
    	} else if (Com.equalsIgnoreCase("USB")) {
    		return kommunikation.usb;    		
    	} else if (Com.equalsIgnoreCase("ANT+")) {
    		return kommunikation.ant;    		
    	} else
    		return kommunikation.seriell;    		    		
    }

    /**
     * Allgemeine Kommunikation mit dem Ergometer. Die Entscheidung über welche Schnittstelle 
     * erfolgt mittels "hatverbindung" (s.o.)
     * @param Kommando		Kommando an Trainingsgerät
     * @param wert			ggf. zusätzlicher Wert
     * @return Rückgabestring
     */
    public String talk(Commands Kommando, double wert) {
  	  int ergint = 0;
  	  int[] erg;
  	  byte[] berg;
  	  String rueckgabe = new String();
  	  int erbL;
  	  int erbH;

	  if (isDeepdebug())
			Mlog.debug("(talk) Kommando:"+Kommando+" Wert:"+wert);

  	  switch (hatverbindung()) {
  	  	case keine:		// auch die ANT+ Werte liefern, wenn möglich (vielleicht Cadence/Speed und manuelle Rolle?)
  	  	case ant:
			if (Rsmain.libant == null)
				return rueckgabe;
  	  		switch (Kommando) {
  		    case version:
  	  		    rueckgabe = "" + Rsmain.libant.getDevSNR();
  	  		    if (trainerModell.equals("wahookickr") && rueckgabe.equals("0"))
  	  		    		rueckgabe = "KICKR";
  	  		    Mlog.info("Device SNR = " + rueckgabe);  	  		    		
  		    	break;
  		    case getpower:
  		    	rueckgabe = "" + Rsmain.libant.getPower();
  		    	//Mlog.debug("Watt = " + rueckgabe);
  		    	break;		    	
  		    case puls:
  		    	rueckgabe = "" + Rsmain.libant.getPuls();
  		    	break;
  		    case rpm:
  		    	rueckgabe = "" + Rsmain.libant.getRpm();
  		    	//Mlog.debug("(talk) rpm = " + rueckgabe);
  		    	break;
  		    case geschwindigkeit:
  		    	rueckgabe = "" + Rsmain.libant.getSpeed();
  		    	//Mlog.debug("(talk) v = " + rueckgabe);
  		    	break;
		    case steigung:
		    	Rsmain.libant.setantWertChg(true);
		    	Rsmain.libant.setSlope((int)(wert*10.0));
  		    	//Mlog.debug("Steigung = " + wert);
		    	break;
		    case leistung:
		    	Rsmain.libant.setantWertChg(true);
		    	Rsmain.libant.setVorgabePower((int)(wert));
  		    	//Mlog.debug("Power = " + Rsmain.libant.getVorgabePower());
  		    	break;
			default:
				break;
  		    }
  	  		break;
  	  		
  	  	case netzwerk:
  	  		if (trainerModell.equals("daum") || trainerModell.equals("daumcr")) {
  	  			switch (Kommando) {
  	  			case version:
  	  				rueckgabe = nettalkDaum(Trainer.Commands.version, wert);
  	  				if (rueckgabe.isEmpty())
  	  					rueckgabe = "0";
 	  				break;
  	  			case getpower:
  	  				rueckgabe = nettalkDaum(Trainer.Commands.getpower, wert);
  	  				if (rueckgabe.isEmpty())
  	  					rueckgabe = "0";
 	  				break;
  	  			case leistung:
  	  				nettalkDaum(Trainer.Commands.leistung, wert);
  	  				break;		    	
  	  			case puls:
  	  				rueckgabe = nettalkDaum(Trainer.Commands.puls, wert);
  	  				if (rueckgabe.isEmpty())
  	  					rueckgabe = "0";
  	  				break;
  	  			case rpm:
  	  				rueckgabe = nettalkDaum(Trainer.Commands.rpm, wert);
  	  				if (rueckgabe.isEmpty())
  	  					rueckgabe = "0";
  	  				break;
				default:
					break;
  	  			}
  	  		}
  	  		if (trainerModell.equals("cyclus2_el")) {		// Cyclus 2 Ergoline-Modus
  	  			switch (Kommando) {
  	  			case setpc:
  	  				rueckgabe = nettalkCyclus2_El(Trainer.Commands.setpc, (byte) 0);
  			        Mlog.debug("Cyclus2.setpc(ergo=1): " + rueckgabe);
  	  				break;
  	  			case setpc_aus:
  	  				rueckgabe = nettalkCyclus2_El(Trainer.Commands.setpc_aus, (byte) 0);
  			        Mlog.debug("Cyclus2.setpc_aus(ergo=0): " + rueckgabe);
  	  				break;
  	  			case version:
  	  				rueckgabe = nettalkCyclus2_El(Trainer.Commands.version, (byte) 0);
  			        Mlog.debug("Cyclus2.Version: " + rueckgabe);
  	  				break;
  	  			case leistung:
  	  				Double Power = new Double(wert);
  	  				aktpow = Power.intValue();
  	  				rueckgabe = nettalkCyclus2_El(Trainer.Commands.leistung, aktpow);
  	  				rueckgabe = ""; // wird eh nicht verwendet!
  	  				break;
  	  			case puls:
  	  				rueckgabe = nettalkCyclus2_El(Trainer.Commands.puls, (byte) 0);
  	  				break;
  	  			case rpm:
  	  				rueckgabe = nettalkCyclus2_El(Trainer.Commands.rpm, (byte) 0);
  	  				break;
				default:
					break;
  	  			}
  	  		}
  	  		if (trainerModell.equals("cyclus2")) {		// Cyclus 2 Standardmodus
  	  			switch (Kommando) {
  	  			case setpc:
  	  				rueckgabe = nettalkCyclus2(Trainer.Commands.setpc, 0);
  			        Mlog.debug("Cyclus2.setpc(slave=4): " + rueckgabe);
  	  				rueckgabe = nettalkCyclus2(Trainer.Commands.steigung, 0);
  			        Mlog.debug("Cyclus2.steigung(load=6,0): " + rueckgabe);
  	  				rueckgabe = nettalkCyclus2(Trainer.Commands.init, 0);
  			        Mlog.debug("Cyclus2.init(data=13): " + rueckgabe);
  			        if (Rsmain.biker.getCwa() <= 0.3)	{ // Rennrad ?
  			        	rueckgabe = nettalkCyclus2(Trainer.Commands.cassette, 1.0);
  			        	Mlog.debug("Cyclus2.cassette(RR=2): " + rueckgabe);
  			        	rueckgabe = nettalkCyclus2(Trainer.Commands.rings, 1.0);
  			        	Mlog.debug("Cyclus2.rings(RR=10): " + rueckgabe);
  			        } else {
  			        	rueckgabe = nettalkCyclus2(Trainer.Commands.cassette, 0.0);
  			        	Mlog.debug("Cyclus2.cassette(MTB=3): " + rueckgabe);  			        	
  			        	rueckgabe = nettalkCyclus2(Trainer.Commands.rings, 0.0);
  			        	Mlog.debug("Cyclus2.rings(MTB=9): " + rueckgabe);
  			        }
  	  				rueckgabe = nettalkCyclus2(Trainer.Commands.start, 0);
  			        Mlog.debug("Cyclus2.start(ctrl=1): " + rueckgabe);
  	  				break;
  	  			case setpc_aus:
  	  				rueckgabe = nettalkCyclus2(Trainer.Commands.stop, 0);
  			        Mlog.debug("Cyclus2.stop(ctrl=0): " + rueckgabe);
  	  				rueckgabe = nettalkCyclus2(Trainer.Commands.setpc_aus, 0);
  			        Mlog.debug("Cyclus2.setpc_aus(slave=0): " + rueckgabe);
  	  				break;
  	  			case version:
  	  				rueckgabe = nettalkCyclus2(Trainer.Commands.version, 0);
  			        Mlog.debug("Cyclus2.Version: " + rueckgabe);
  	  				break;
  	  			case puls:
  	  				// Datenabfrage nur wenn keine vorhanden:
  	  				rueckgabe = readCyclus2Data(false,6);
  	  				Mlog.debug("Puls: "+rueckgabe);
  	  				break;
  	  			case rpm:
  	  				// Hier werden die Daten immer abgefragt
  	  				rueckgabe = readCyclus2Data(true, 5);
  	  				Mlog.debug("RPM: "+rueckgabe);
  	  				break;
  	  			case steigung:
  	  				rueckgabe = nettalkCyclus2(Trainer.Commands.steigung, wert);
  	  				Mlog.debug("Steigung: "+rueckgabe);
  	  				break;  
  	  			case geschwindigkeit:
  	  				// Datenabfrage nur wenn keine vorhanden:
  	  				rueckgabe = readCyclus2Data(false, 7);
  	  				Mlog.debug("Geschwindigkeit: "+rueckgabe);
  	  				break;
  	  			case getpower:
  	  				// Datenabfrage immer:
  	  				rueckgabe = readCyclus2Data(true, 10);
  	  				Mlog.debug("Leistung: "+rueckgabe);
  	  				break;
  	  			case gangHinten:
  	  				// Datenabfrage nur wenn keine vorhanden:
  	  				rueckgabe = readCyclus2Data(false, 14);
  	  				Mlog.debug("Gang hinten: "+rueckgabe);
  	  				break;
				default:
					break;
  	  			}
  	  		}
  	  		break;
  	  	case usb:  // aktuell nur Tacx-Rollentrainer
  	  		switch (Kommando) {
  		    case version:
  		    	if (isTacxalt()) {
  		    		// Hier letzte Stellen der SNr ausgeben
  			    	USBTalkTacx(Trainer.usbCommands.kommunikation, wDataAlt);  		    		
  	  		    	if (readData[0] == 0)
  	  		    		USBTalkTacx(Trainer.usbCommands.kommunikation, wDataAlt);  // nochmal versuchen...
  	  		    	
  	  		    	if (readData[0] == 0)
  	  		    		rueckgabe = "0";
  	  		    	else {
  	  		    		erbL = (readData[19] < 0) ? 256 + readData[19] : readData[19];
  	  		    		erbH = (readData[18] < 0) ? 256 + readData[18] : readData[18];
  	  		    		int snr = erbH * 256 + erbL;
  	  		    		//Rueckgabe = ""+Integer.toHexString(readData[19] & 0xff)+Integer.toHexString(readData[20] & 0xff);	// = Teil von SNR?
  	  		    		rueckgabe = ""+snr;
 	  		    	}
 		    	} else {
  		    		// beim Tacx-Rollentrainer wird das Gesamtgewicht bei der Version übergeben und hier gespeichert
  		    		bikergewicht = wert; 
  		    		writeData[9] = new Double(bikergewicht).byteValue();
  		    		//writeData[5] = new Double(bikergewicht).byteValue();
  			    	USBTalkTacx(Trainer.usbCommands.kommunikation, writeData);
  	  		    	if (readData[0] == 0 && readData[1] == 0)
  	  		    		USBTalkTacx(Trainer.usbCommands.kommunikation, writeData);  // nochmal versuchen...

  	  		    	if (readData[0] == 0 && readData[1] == 0) 
  	  		    		rueckgabe = "0";  	  		    		
  	  		    	else
  	  		    		rueckgabe = ""+Integer.toHexString(readData[0] & 0xff)+Integer.toHexString(readData[1] & 0xff);
  		    	}

  		    	Mlog.debug("Tacx.version: " + rueckgabe);
  		    	break;
  		    case steigung:
  		    	if (isTacxalt()) {
  		    		double dwert = wert * 226.0/3.0;		// 226 = 3% Steigung - durch Versuche ermittelt
  		    		dwert = (dwert < 0) ? 0 : dwert;
  		    		wDataAlt[0] = (byte) ((dwert > 226) ? 226 : (byte) dwert);	// 226
  	  		    	USBTalkTacx(Trainer.usbCommands.kommunikation, wDataAlt);
  		    	} else {
  		    		writeData[9] = new Double(bikergewicht).byteValue();
  		    		wert = wert < -24.0 ? 24.0 : wert;
  		    		wert = wert > 24.0 ? 24.0 : wert; // begrenzen, sonst kommts zu einem Überlauf!
  		    		steigungcode = new Double(wert * 650.0).intValue();
  		    		writeData[4] = new Integer(steigungcode & 0xFF).byteValue();
  		    		writeData[5] = new Integer((steigungcode & 0xFF00) / 256).byteValue();
  	  		    	USBTalkTacx(Trainer.usbCommands.kommunikation, writeData);
  		    	}
  		    	break;
  		    case leistung:
  		    	break;		    	
  		    case puls:
  		    	if (isTacxalt()) {
  		    		USBTalkTacx(Trainer.usbCommands.kommunikation, wDataAlt);
  		    		ergint = (readData[4] < 0) ? 256 + readData[4] : readData[4];  		    		
  		    	} else {
  		    		USBTalkTacx(Trainer.usbCommands.kommunikation, writeData);  
  		    		ergint = (readData[12] < 0) ? 256 + readData[12] : readData[12]; // sonst Überlauf bei 128
  		    	}
	  	  		rueckgabe = ""+ergint;
  		    	break;
  		    case rpm:
  		    	if (isTacxalt()) {
  		    		USBTalkTacx(Trainer.usbCommands.kommunikation, wDataAlt);
  		    		ergint = (readData[3] < 0) ? 256 + readData[3] : readData[3]; 		    		
  		    	} else {
  		    		USBTalkTacx(Trainer.usbCommands.kommunikation, writeData);
  		    		ergint = (readData[44] < 0) ? 256 + readData[44] : readData[44]; // sonst Überlauf bei 128
  		    	}
	    		rueckgabe = ""+ergint;
	    		//Mlog.debug("TACX: rpm = " + ergint);
  		    	break;
  		    case geschwindigkeit:
  		    	double v1 = 0.0;
  		    	if (isTacxalt()) {
  		    		//USBTalkTacx(Trainer.usbCommands.kommunikation, wDataAlt);
  		    		erbL = (readData[1] < 0) ? 256 + readData[1] : readData[1];
  		    		erbH = (readData[2] < 0) ? 256 + readData[2] : readData[2];
  		    		v1 = (erbH * 256 + erbL) * 0.09;	// = (0.085 durch Tests ermittelt)
  		    		if (getTrainerModell().endsWith("2"))  // Geschwindigkeitskorrektur
  		    			v1 /= tacxvcorrect2;  			// Korrekturfaktor Fortius
  		    		else
  		    			v1 /= tacxvcorrect1;  			// Korrekturfaktor iMagic
  		    		
  		    	} else {
  		    		USBTalkTacx(Trainer.usbCommands.kommunikation, writeData);  
  		    		ergint = (int) readData[33];
  		    		v1 = (double) ergint;
  		    		if (getTrainerModell().endsWith("2"))  // Geschwindigkeitskorrektur
  		    			v1 /= tacxvcorrect2;  // Korrekturfaktor Fortius
  		    		else
  		    			v1 /= tacxvcorrect1;  // Korrekturfaktor iMagic
  		    	}
	    		rueckgabe = ""+v1;
  		    	break;
  		    case getpower:
  		    	if (isTacxalt()) {
  		    		USBTalkTacx(Trainer.usbCommands.kommunikation, wDataAlt);
  		    		ergint = (readData[1] < 0) ? 256 + readData[1] : readData[1];	// muß das noch mit einem Faktor multipliziert werden?
  		    	} else {
  		    		USBTalkTacx(Trainer.usbCommands.kommunikation, writeData); 
  		    		ergint = (int) readData[39]*256;
  		    		ergint += (int) readData[38];  // Kraft ermitteln (s.u.)
  		    		ergint *= (int) readData[33];  // multipliziert mit der Geschwindigkeit
  		    		ergint /= tacxpcorrect;           // Schätzwert: ~ 4677 (max Kraft) * 30 (Geschw.) / 400 (W)
  		    	}
	    		rueckgabe = ""+ergint;
			default:
				break;
   		    }
  	  		break;
  	  	case seriell:  // USB-Kettler gehen auch über seriell!
  	  		if (trainerModell.equalsIgnoreCase("daum") || trainerModell.equalsIgnoreCase("daumcr")) {
  	  			Double Power;
  	  			switch (Kommando) {
  	  			case leistung:
  	  				Power = new Double(wert / 5.0);
  	  				berg = sertalk(Trainer.Commands.leistung, getErgoAdresse(), Power.byteValue());
  	  				break;
  	  			case puls:
  	  				berg = sertalk(Trainer.Commands.lesedaten, getErgoAdresse(), (byte) 0);
	            	//ergint = berg[14]; 
  	  				ergint = (berg[14] < 0) ? 256 + berg[14] : berg[14]; // sonst Überlauf bei 128
  	  				rueckgabe = ""+ergint;
  	  				break;
  	  			case rpm:
  	  				berg = sertalk(Trainer.Commands.lesedaten, getErgoAdresse(), (byte) 0);
  	  				//ergint = berg[6];
  	  				ergint = (berg[6] < 0) ? 256 + berg[6] : berg[6];
  	  				rueckgabe = ""+ergint;
  	  				break;
  	  			case getpower:
  	  				berg = sertalk(Trainer.Commands.lesedaten, getErgoAdresse(), (byte) 0);
	            	//ergint = berg[5]; 
  	  				ergint = (berg[5] < 0) ? 256 + berg[5] : berg[5]; // sonst Überlauf bei 128
  	  				ergint *= 5;
  	  				rueckgabe = ""+ergint;
  	  				break;
  	  			case getadress:
  	  				berg = sertalk(Trainer.Commands.getadress, (byte) 0, (byte) 0);
  	  				setErgoAdresse((byte) berg[1]);		
  	  				ergint = berg[1];
  			        Mlog.debug("Daum.getadress: " + berg.toString());  			      
  	  				rueckgabe = ""+ergint;
  	  				break;
  	  			case check_cockpit:
  	  				berg = sertalk(Trainer.Commands.check_cockpit, (byte) 0, (byte) 0);
  	  				ergint = berg[2];
  			        Mlog.debug("Daum.check_cockpit: " + berg.toString());
  	  				rueckgabe = ""+ergint;
  	  				break;
  	  			case version:
  	  				berg = sertalk(Trainer.Commands.version, getErgoAdresse(), (byte) 0);
  			        Mlog.debug("Daum.version: " + berg.toString());
  	  	  			if (berg[0] > 4) {
  	  	  				rueckgabe = String.format("%c%c%c%c%c%c%c%c", berg[2]-4, berg[3]-4, berg[4]-4, berg[5]-4, berg[6]-4, berg[7]-4, berg[8]-4, berg[9]-4);					
  	  				} else {
  	  					rueckgabe = "0";
  	  				}
  	  				break;
				default:
					break;
  	  			}
  	  		}
  	  		if (trainerModell.equalsIgnoreCase("daum2001")) {
  	  			switch (Kommando) {
  	  			case leistung:
  	  				Double Power = new Double(wert / 5.0);
  	  				erg = sertalk2001(Trainer.Commands.leistung, getErgoAdresse(), Power.byteValue());
  	  				break;
  	  			case puls:
  	  				erg = sertalk2001(Trainer.Commands.lesedaten, getErgoAdresse(), (byte) 0);
  	  				ergint = erg[14];
  	  				rueckgabe = ""+ergint;
  	  				break;
  	  			case rpm:
  	  				erg = sertalk2001(Trainer.Commands.lesedaten, getErgoAdresse(), (byte) 0);
  	  				ergint = erg[6];
  	  				rueckgabe = ""+ergint;
  	  				break;
  	  			case getpower:
  	  				erg = sertalk2001(Trainer.Commands.lesedaten, getErgoAdresse(), (byte) 0);
  	  				ergint = erg[5] * 5;
  	  				rueckgabe = ""+ergint;
  	  				break;
  	  			case getadress:
  	  				erg = sertalk2001(Trainer.Commands.getadress, (byte) 0, (byte) 0);
  	  				setErgoAdresse((byte) erg[1]);		
  	  				ergint = erg[1];
  	  				rueckgabe = ""+ergint;
  	  				break;
  	  			case check_cockpit:
  	  				erg = sertalk2001(Trainer.Commands.check_cockpit, (byte) 0, (byte) 0);
  	  				ergint = erg[2];
  	  				rueckgabe = ""+ergint;
  	  				break;
  	  			case version:
  	  				erg = sertalk2001(Trainer.Commands.version, getErgoAdresse(), (byte) 0);
  	  				if (erg[0] != -1) {
  	  					rueckgabe = String.format("%c%c%c%c%c%c%c%c", erg[2]-4, erg[3]-4, erg[4]-4, erg[5]-4, erg[6]-4, erg[7]-4, erg[8]-4, erg[9]-4);					
  	  				} else {
  	  					rueckgabe = "0";
  	  				}
  	  				break;
				default:
					break;
  	  			}
  	  		}
  	  		if (trainerModell.equalsIgnoreCase("kettler") || trainerModell.equalsIgnoreCase("kettlercr")) {
  	  			switch (Kommando) {
  	  			case puls:
  	  				if (Antwort.isEmpty()){	// nur wenn keine Antwort gespeichert wurde, neu einlesen
  	  	  				Antwort = sertalkKettler(Trainer.Commands.leistung, aktpow);
  	  					// vorher: Antwort = sertalkKettler(Trainer.serCommands.lesedaten, 0);
  	  				}
  	  				rueckgabe = readKettlerSingleValue(Antwort, 1);
   	  		    	if (rueckgabe.isEmpty())
  	  		    		rueckgabe = "0";
  	  				break;
  	  			case rpm:
  	  				if (Antwort.isEmpty()) {	// nur wenn keine Antwort gespeichert wurde, neu einlesen
  	  	  				Antwort = sertalkKettler(Trainer.Commands.leistung, aktpow);
  	  				}
  	  				rueckgabe = readKettlerSingleValue(Antwort, 2);
  	  		    	if (rueckgabe.isEmpty())
  	  		    		rueckgabe = "0";
  	  				Antwort = "";	// sonst kommt er nie aus der Pause!
 	  				break;
  	  			case getpower:
  	  				if (Antwort.isEmpty()) {	// nur wenn keine Antwort gespeichert wurde, neu einlesen
  	  	  				Antwort = sertalkKettler(Trainer.Commands.leistung, aktpow);
  	  				}
  	  				rueckgabe = readKettlerSingleValue(Antwort, 8);
  	  		    	if (rueckgabe.isEmpty())
  	  		    		rueckgabe = "0";
  	  				Antwort = "";	// sonst kommt er nie aus der Pause!
 	  				break;
  	  			case init:
  	  				// Ergebnis = sertalkKettler(trainer.serCommands.reset, 0);  // 11.10.2011. macht Kommunikationsproblem mit AR1S !
  	  				rueckgabe = sertalkKettler(Trainer.Commands.setpc, 0);
  			        Mlog.debug("Kettler.setpc: " + rueckgabe + "(" + rueckgabe.length() + ")");
  			        if (rueckgabe.equals("ERROR")) {	// bei ERROR nochmal mit CM versuchen
  			        	rueckgabe = sertalkKettler(Trainer.Commands.setpc_alt, 0);
  			        	Mlog.debug("Kettler.setpc_alt: " + rueckgabe);
  			        }
  	  				if ((byte) rueckgabe.charAt(0) == -1)
  	  					return "0"; // kein Kettler erreichbar!
  	  				//Rueckgabe = sertalkKettler(Trainer.serCommands.db, 0);
  	  				//Mlog.debug("Kettler.db: " + Rueckgabe);
  	  				rueckgabe = sertalkKettler(Trainer.Commands.sp0, 0);   				
  			        Mlog.debug("Kettler.sp0: " + rueckgabe);
  	  				break;
  	  			case reset:
  	  				rueckgabe = sertalkKettler(Trainer.Commands.reset, 0); 
  			        Mlog.debug("Kettler.reset: " + rueckgabe);
					Global.sleep(kettlerresetwait);	// etwas warten...

  	  				if ((byte) rueckgabe.charAt(0) == -1)
  	  					return "0"; // kein Kettler erreichbar!
  	  				break;
  	  			case version:
  	  				rueckgabe = sertalkKettler(Trainer.Commands.getid, 0);
  			        Mlog.debug("Kettler.version: " + rueckgabe);
  			        if (rueckgabe.equals("ERROR")) {	// bei ERROR nochmal versuchen
  	  	  				rueckgabe = sertalkKettler(Trainer.Commands.getid, 0);
  	  			        Mlog.debug("-Kettler.version: " + rueckgabe);  			        	
  			        }  			        
  	  				if (rueckgabe.charAt(0) != '0') {
  	  					if (rueckgabe.length() > 4)
  	  						rueckgabe = rueckgabe.substring(0, 4); // nur die ersten 4 Zeichen übergeben
  	  					setKettlerCockpit(rueckgabe);
  	  				}
  	  				break;
  	  			case leistung:
  	  				Antwort = "";	
  	  				Double Power = new Double(wert);
  	  				aktpow = Power.intValue();
  	  				Antwort = sertalkKettler(Trainer.Commands.leistung, aktpow);
  	  				rueckgabe = ""; // wird eh nicht verwendet!
  	  				break;
				default:
					break;
  	  			}
  	  		}
  	  		if (trainerModell.equalsIgnoreCase("ergofit") || trainerModell.equalsIgnoreCase("ergofitcr")) {
  	  			switch (Kommando) {
  	  			case check_cockpit:
  	  				rueckgabe = sertalkErgofit(Trainer.Commands.setpc, (byte) 0);
  			        //Mlog.debug("ERGO-FIT.setpc(ergo=1): " + rueckgabe);
  	  				break;
  	  			case version:
  	  				rueckgabe = sertalkErgofit(Trainer.Commands.version, (byte) 0);
  			        //Mlog.debug("ERGO-FIT.Version(Typ): " + rueckgabe);
  			        if (rueckgabe.equals("0")) {
  	  	  				rueckgabe = sertalkErgofit(Trainer.Commands.version, (byte) 0);
  	  			        Mlog.debug("(2)ERGO-FIT.Version(Typ): " + rueckgabe);  			        	
  			        }
  	  				break;
  	  			case leistung:
  	  				Antwort = "";	
  	  				Double Power = new Double(wert);
  	  				aktpow = Power.intValue();
  	  				Antwort = sertalkErgofit(Trainer.Commands.leistung, aktpow);
  	  				rueckgabe = ""; // wird eh nicht verwendet!
  	  				break;
  	  			case puls:
  	  				rueckgabe = sertalkErgofit(Trainer.Commands.puls, (byte) 0);
  	  				// nur die ersten drei Zeichen werden übernommen:
  	  				rueckgabe = rueckgabe.substring(0, 3);
  			        //Mlog.debug("ERGO-FIT.puls: " + rueckgabe);
  	  				break;
  	  			case rpm:
  	  				rueckgabe = sertalkErgofit(Trainer.Commands.rpm, (byte) 0);
  	  				// nur dier ersten drei Zeichen werden übernommen:
  	  				rueckgabe = rueckgabe.substring(0, 3);
  			        //Mlog.debug("ERGO-FIT.RPM: " + rueckgabe);
  	  				break;
  	  			case getpower:
  	  				rueckgabe = sertalkErgofit(Trainer.Commands.getpower, (byte) 0);
  	  				// nur dier ersten drei Zeichen werden übernommen:
  	  				rueckgabe = rueckgabe.substring(0, 3);
  			        //Mlog.debug("ERGO-FIT.getpower: " + rueckgabe);
  	  				break;
				default:
					break;
  	  			}
  	  		}
  	  		if (trainerModell.equalsIgnoreCase("ergoline")) {
  	  			switch (Kommando) {
  	  			case version:
  	  				rueckgabe = sertalkErgoline(Trainer.Commands.version, (byte) 0);
  			        Mlog.debug("Ergoline.Version: " + rueckgabe);
  	  				break;
  	  			case leistung:
  	  				Double Power = new Double(wert);
  	  				aktpow = Power.intValue();
  	  				rueckgabe = sertalkErgoline(Trainer.Commands.leistung, aktpow);
  	  				rueckgabe = ""; // wird eh nicht verwendet!
  	  				break;
  	  			case puls:
  	  				rueckgabe = sertalkErgoline(Trainer.Commands.puls, (byte) 0);
  	  				break;
  	  			case rpm:
  	  				rueckgabe = sertalkErgoline(Trainer.Commands.rpm, (byte) 0);
  	  				break;
  	  			case getpower:
  	  				rueckgabe = sertalkErgoline(Trainer.Commands.getpower, (byte) 0);
  	  				break;
				default:
					break;
  	  			}
  	  		}
  	  		if (trainerModell.equals("cyclus2_el")) {		// Cyclus 2 Ergoline-Modus
  	  			switch (Kommando) {
  	  			case setpc:
  	  				rueckgabe = sertalkCyclus2_El(Trainer.Commands.setpc, (byte) 0);
 			        Mlog.debug("Cyclus2.setpc(ergo=1): " + rueckgabe);
  	  				break;
  	  			case setpc_aus:
  	  				rueckgabe = sertalkCyclus2_El(Trainer.Commands.setpc_aus, (byte) 0);
  			        Mlog.debug("Cyclus2.setpc_aus(ergo=0): " + rueckgabe);
  	  				break;
  	  			case version:
  	  				rueckgabe = sertalkCyclus2_El(Trainer.Commands.version, (byte) 0);
  			        Mlog.debug("Cyclus2.Version: " + rueckgabe);
  	  				break;
  	  			case leistung:
  	  				Double Power = new Double(wert);
  	  				aktpow = Power.intValue();
  	  				rueckgabe = sertalkCyclus2_El(Trainer.Commands.leistung, aktpow);
  	  				rueckgabe = ""; // wird eh nicht verwendet!
  	  				break;
  	  			case puls:
  	  				rueckgabe = sertalkCyclus2_El(Trainer.Commands.puls, (byte) 0);
  	  				break;
  	  			case rpm:
  	  				rueckgabe = sertalkCyclus2_El(Trainer.Commands.rpm, (byte) 0);
  	  				break;
  	  			case getpower:
  	  				rueckgabe = sertalkCyclus2_El(Trainer.Commands.getpower, (byte) 0);
  			        //Mlog.debug("Ist-Leistung: " + rueckgabe);
  	  				break;
				default:
					break;
  	  			}
  	  		}
  	  		if (trainerModell.equals("cyclus2")) {		// Cyclus 2 Standardmodus
  	  			switch (Kommando) {
  	  			case setpc:
  	  				rueckgabe = sertalkCyclus2(Trainer.Commands.setpc, 0);
  			        Mlog.debug("Cyclus2.setpc(slave=4): " + rueckgabe);
  	  				rueckgabe = sertalkCyclus2(Trainer.Commands.steigung, 0);
  			        Mlog.debug("Cyclus2.steigung(load=6,0): " + rueckgabe);
  	  				rueckgabe = sertalkCyclus2(Trainer.Commands.init, 0);
  			        Mlog.debug("Cyclus2.init(data=13): " + rueckgabe);
  			        if (Rsmain.biker.getCwa() <= 0.3)	{ // Rennrad ?
  			        	rueckgabe = sertalkCyclus2(Trainer.Commands.cassette, 1.0);
  			        	Mlog.debug("Cyclus2.cassette(RR=2): " + rueckgabe);
  			        	rueckgabe = sertalkCyclus2(Trainer.Commands.rings, 1.0);
  			        	Mlog.debug("Cyclus2.rings(RR=10): " + rueckgabe);
  			        } else {
  			        	rueckgabe = sertalkCyclus2(Trainer.Commands.cassette, 0.0);
  			        	Mlog.debug("Cyclus2.cassette(MTB=3): " + rueckgabe);  			        	
  			        	rueckgabe = sertalkCyclus2(Trainer.Commands.rings, 0.0);
  			        	Mlog.debug("Cyclus2.rings(MTB=9): " + rueckgabe);
  			        }
  	  				rueckgabe = sertalkCyclus2(Trainer.Commands.start, 0);
  			        Mlog.debug("Cyclus2.start(ctrl=1): " + rueckgabe);
  	  				break;
  	  			case setpc_aus:
  	  				rueckgabe = sertalkCyclus2(Trainer.Commands.stop, 0);
  			        Mlog.debug("Cyclus2.stop(ctrl=0): " + rueckgabe);
  	  				rueckgabe = sertalkCyclus2(Trainer.Commands.setpc_aus, 0);
  			        Mlog.debug("Cyclus2.setpc_aus(slave=0): " + rueckgabe);
  	  				break;
  	  			case version:
  	  				rueckgabe = sertalkCyclus2(Trainer.Commands.version, 0);
  			        Mlog.debug("Cyclus2.Version: " + rueckgabe);
  	  				break;
  	  			case puls:
  	  				// Datenabfrage nur wenn keine vorhanden:
  	  				rueckgabe = readCyclus2Data(false,6);
  	  				Mlog.debug("Puls: "+rueckgabe);
  	  				break;
  	  			case rpm:
  	  				// Hier werden die Daten immer abgefragt
  	  				rueckgabe = readCyclus2Data(true, 5);
  	  				Mlog.debug("RPM: "+rueckgabe);
  	  				break;
  	  			case steigung:
  	  				rueckgabe = sertalkCyclus2(Trainer.Commands.steigung, wert);
  	  				Mlog.debug("Steigung: "+rueckgabe);
  	  				break;  
  	  			case geschwindigkeit:
  	  				// Datenabfrage nur wenn keine vorhanden:
  	  				rueckgabe = readCyclus2Data(false, 7);
  	  				Mlog.debug("Geschwindigkeit: "+rueckgabe);
  	  				break;
  	  			case getpower:
  	  				// Datenabfrage immer
  	  				rueckgabe = readCyclus2Data(true, 10);
  	  				Mlog.debug("Ist-Leistung: "+rueckgabe);
  	  				break;
  	  			case gangHinten:
  	  				// Datenabfrage nur wenn keine vorhanden:
  	  				rueckgabe = readCyclus2Data(false, 14);
  	  				Mlog.debug("Gang hinten: "+rueckgabe);
  	  				break;
				default:
					break;
  	  			}
  	  		}

  	  		break;
  	  } 
  	  
  	  return rueckgabe;
  	}

	/*
	 * Ermittelt den Wert an der Stelle index in der Kettler-Antwort. D.h. es wird der Wert nach
	 * dem n-ten (=index) Tab zurückgegeben.
	 * Teststring: "094\t000\t000\t003\t155\t0045\t01:01\t155\t"
	 * 1: heart rate as bpm (beats per minute)
     * 2: rpm (revolutions per minute)
     * 3: speed as 10*km/h -> 074=7.4 km/h
     * 4: distance in 100m steps
     * 5: power in Watt, may be configured in PC mode with "pw x[Watt]"
     * 6: energy in kJoule (display on trainer may be kcal, note kcal = kJ * 0.2388)
     * 7: time minutes:seconds, 
     * 8: current power on eddy current brake
     * 
	 * 	@params kettvals Antwort, die vom Kettler-Trainer geliefert wurde
	 *  @params index    Wert, der zurückgegeben werden soll (s.u.)
	 *  @return          Wert als String
	 *  
	 */
	public static String readKettlerSingleValue(String kettvals, int index) {
		String rueckgabe = "0";
		int itab;
		String[] arr;
		
		if (index < 1 || index > 8) {
			Mlog.debug("Kettler-Kom. (falscher Index): " + index); 
			return rueckgabe;
		}
		itab = kettvals.indexOf(0x09,1); // Pos. des ersten Tabs 
		if (itab == -1) {
				Mlog.debug("Kettler-Kom. (kein erster TAB): " + kettvals); 
				rueckgabe = "0";
		} else {
				arr = kettvals.split("\t");
				if (index <= arr.length)
					rueckgabe = arr[index-1];
				else
					Mlog.debug("Kettler-Kom. (abgeschnittener Eingangsstring!): " + kettvals); 
		}		
		return rueckgabe;
	}

    /**
     * Einlesen der Daten vom Cyclus 2 (native). 
     * 
     * @param always	true: Immer einlesen, false: nur wenn keine Werte vorhanden
     * @param index		Index im data-Format 3 (siehe Schnittstellendoku Format 3)
     * @return			Wert als String im Format 2.34
     */
    private String readCyclus2Data(boolean always, int index) {
    	String rueckgabe = "0";
		if (always || cyclus2Data15 == null) {
			if (hatverbindung() == kommunikation.netzwerk)
				rueckgabe = nettalkCyclus2(Trainer.Commands.lesedaten, 0);
			else	// seriell
				rueckgabe = sertalkCyclus2(Trainer.Commands.lesedaten, 0);
	  		Mlog.debug("rueckgabe1: "+rueckgabe+" index: "+index);
	  		if (rueckgabe.length() > 50)		
	  			if (rueckgabe.substring(0,5).equals("data:"))
	  				cyclus2Data15 = rueckgabe.split(","); 
		}
		
		if (cyclus2Data15 != null)
			if (cyclus2Data15.length >= index)
				rueckgabe = cyclus2Data15[index];
		
		return rueckgabe;
    }

    /**
     * serielle Kommunikation mit dem Ergoline Ergometer (ErgoLine 800 Betrieb)
     * @param Kommando		Kommando an trainingsgerät
     * @param wert			ggf. zus. Wert
     * @return String mit Antwort
     */
    public String sertalkErgoline(Commands Kommando, int wert) {
    	int i = 0;
    	//int j = 0;
    	int k = 0;
    	int rest = 0;
  		char puff[] = new char[256];
	    //byte befehl[] = new byte[10];
        int sendcnt = 0;
        int receivecnt = 0;
  		byte clear[] = new byte[256];
  		//byte cs = 0;
  		String sBefehl = null;
  		String Ergebnis = null;
  		
    	try {
		    char befehl[] = new char[1];
    		switch (Kommando) {
    		case version:
    			sBefehl = "I\r";
		    	break;   			
    		case leistung:
    			sBefehl = "W"+wert+"\r";
		    	break;   			
    		case rpm:
    			sBefehl = "D\r";
		    	break;   			
    		case puls:
    			sBefehl = "H\r";
		    	break;
    		case getpower:
    			sBefehl = "B\r";
		    	break;
			default:
				break;   			
    		}
		    befehl = sBefehl.toCharArray();
	    	sendcnt = sBefehl.length();
	    	if (sendcnt == 0) {
	    		Mlog.error("kein gültiger Befehl!");
	    		return "0";
	    	}
	    	
    		// "überflüssige" Zeichen zuerst einlesen (Puffer leeren)
    		rest = in.available();

    		if (rest > 0)
    			rest = (char) in.read(clear);
     		
    		if (isDeepdebug())
    			Mlog.info("gelöscht aus Puffer: "+rest);   	// Debug!     

	    	for (i=0; i<sendcnt; i++) {
	    		out.write((int) befehl[i]);
	    		if (isDeepdebug())
	    			Mlog.debug("s: 0x"+Integer.toHexString(befehl[i] & 0xff));  	// Debug!
	    	}	
    		if (isDeepdebug())
    			Mlog.debug("Send: "+new String(befehl));  	// Debug!
    		out.flush();
    		Global.sleep(kettlerwait);						// etwas warten...
    		
    		// receivecnt dynamisch ermitteln
    		receivecnt = in.available();

    		if (receivecnt <= 0) {
    			Mlog.debug("keine Antwort vom Ergoline cnt = "+receivecnt); 
    			return "0";
    		}
    		if (isDeepdebug())
    			Mlog.debug("receivecnt:"+receivecnt);      // Debug!   
    		
    		for (i=0; i<receivecnt; i++) {
    			puff[i] = (char) in.read();
    			if (puff[i] == '?')	{						// fehlerhafte Zeichen überspringen
    	        	Mlog.info("r:?"); 	        	
    				i--;
    			}
    		}

    		receivecnt--;		// CR abziehen
			Ergebnis = new String(puff).substring(0, receivecnt);

    		if (isDeepdebug()) {
    			Mlog.debug("Zeichen gelesen: "+receivecnt);   	// Debug!     
        		for (k=0; k<receivecnt; k++) {   				// Debug!     
        			Mlog.debug("r:"+puff[k]);        			// Debug!      	        	
        		}   											// Debug!     
    		}
		} catch (IOException e) {
			Mlog.error("serieller Kommunikationsfehler Ergoline: IO-Fehler");
			Mlog.ex(e);
		}
		
		//String ret = String.valueOf(puff).substring(3, 3);
		//return ret;
		// Rückgabe formatieren:
	    switch (Kommando) {
		case version:
			return Ergebnis;
		case rpm:
			if (Ergebnis.substring(0,1).equals("n")){
				//Mlog.debug("rpm erkannt");
				return Ergebnis.substring(1, 4);				
			}
		case puls:
			if (Ergebnis.substring(0,1).equals("H")){
				//Mlog.debug("Puls erkannt");
				return Ergebnis.substring(1, 4);				
			}
		case getpower:
			if (Ergebnis.substring(0,1).equals("B")){
				//Mlog.debug("act. Leistung erkannt");
				return Ergebnis.substring(1, 4);				
			}
		default:
			break;
	    }
		return "0";
    }
    
    /**
     * serielle Kommunikation mit dem Cyclus2 Ergometer (ErgoLine 800 Betrieb)
     * @param Kommando		Kommando an Trainingsgerät
     * @param wert			ggf. zus. Wert
     * @return String mit Antwort
     */
    public String sertalkCyclus2_El(Commands Kommando, int wert) {
    	int i = 0;
    	int k = 0;
    	int rest = 0;
  		char puff[] = new char[256];
        int sendcnt = 0;
        int receivecnt = 0;
  		byte clear[] = new byte[256];
  		String sBefehl = null;
  		String Ergebnis = null;
  		
    	try {
		    char befehl[] = new char[1];
    		switch (Kommando) {
    		case setpc:
			    sBefehl = "slave=1\rergo=1\ra100\rtext=RoomSports -> Cyclus2(Ergoline)\rs\r";
		    	break;   			
    		case setpc_aus:
			    sBefehl = "f\rergo=0\rslave=0\r";
		    	break;   			
    		case version:
    			sBefehl = "i\r";
		    	break;   			
    		case leistung:
    			sBefehl = "w"+wert+"\r";
		    	break;   			
    		case rpm:
    			sBefehl = "d\r";
		    	break;   			
    		case puls:
    			sBefehl = "h\r";
		    	break;
    		case getpower:
    			sBefehl = "b\r";
		    	break;
			default:
				break;   			
    		}
		    befehl = sBefehl.toCharArray();
	    	sendcnt = sBefehl.length();
	    	if (sendcnt == 0) {
	    		Mlog.error("kein gültiger Befehl!");
	    		return "0";
	    	}
	    	
    		// "überflüssige" Zeichen zuerst einlesen (Puffer leeren)
    		rest = in.available();

    		if (rest > 0)
    			rest = (char) in.read(clear);
     		
    		if (isDeepdebug())
    			Mlog.info("gelöscht aus Puffer: "+rest);   	// Debug!     

	    	for (i=0; i<sendcnt; i++) {
	    		out.write((int) befehl[i]);
	    		if (isDeepdebug())
	    			Mlog.debug("s: 0x"+Integer.toHexString(befehl[i] & 0xff));  	// Debug!
	    	}	
    		if (isDeepdebug())
    			Mlog.debug("Send: "+new String(befehl));  	// Debug!
    		out.flush();
    		Global.sleep(kettlerwait);						// etwas warten...
    		
    		// receivecnt dynamisch ermitteln
    		receivecnt = in.available();

    		if (receivecnt <= 0) {
    			Mlog.debug("keine Antwort vom Ergoline cnt = "+receivecnt); 
    			return "0";
    		}
    		if (isDeepdebug())
    			Mlog.debug("receivecnt:"+receivecnt);      // Debug!   
    		
    		for (i=0; i<receivecnt; i++) {
    			puff[i] = (char) in.read();
    			if (puff[i] == '?')	{						// fehlerhafte Zeichen überspringen
    	        	Mlog.debug("r:?"); 	        	
    				i--;
    			}
    		}

    		receivecnt--;		// CR abziehen
			Ergebnis = new String(puff).substring(0, receivecnt);

    		if (isDeepdebug()) {
    			Mlog.debug("Zeichen gelesen: "+receivecnt);   	// Debug!     
        		for (k=0; k<receivecnt; k++) {   				// Debug!     
        			Mlog.debug("r:"+puff[k]);        			// Debug!      	        	
        		}   											// Debug!     
    		}
		} catch (IOException e) {
			Mlog.error("serieller Kommunikationsfehler Cyclus2 (Ergoline): IO-Fehler");
			Mlog.ex(e);
		}
		
		// Rückgabe formatieren:
	    switch (Kommando) {
		case version:
			return Ergebnis;
		case rpm:
			if (Ergebnis.substring(0,1).equals("n")){
				//Mlog.debug("rpm erkannt");
				return Ergebnis.substring(1, 4);				
			}
		case puls:
			if (Ergebnis.substring(0,1).equals("H")){
				//Mlog.debug("Puls erkannt");
				return Ergebnis.substring(1, 4);				
			}
		case getpower:
			if (Ergebnis.substring(0,1).equals("B")){
				//Mlog.debug("akt. Leistung erkannt");
				return Ergebnis.substring(1, 4);				
			}
		default:
			break;
	    }
		return "0";
    }

    /**
     * serielle Kommunikation mit dem Cyclus2 Ergometer (nativer Cyclus2 Betrieb)
     * @param Kommando		Kommando an Trainingsgerät
     * @param wert			ggf. zus. Wert
     * @return String mit Antwort
     */
    public String sertalkCyclus2(Commands Kommando, double wert) {
    	int i = 0;
    	int k = 0;
    	int rest = 0;
  		char puff[] = new char[256];
        int sendcnt = 0;
        int receivecnt = 0;
  		byte clear[] = new byte[256];
  		String sBefehl = null;
  		String Ergebnis = null;
  		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
  		dfs.setDecimalSeparator('.');
  		DecimalFormat zfk2 = new DecimalFormat("#0.00", dfs);
  		
    	try {
		    char befehl[] = new char[1];
    		switch (Kommando) {
    		case setpc:
    			sBefehl = "slave=4\r";
    			break;   			
    		case setpc_aus:
    			sBefehl = "slave=0\r";
    			break; 
    		case version:
    			sBefehl = "vers?\r";
    			break;   			
    		case init:
    			sBefehl = "data=13\r";	// erst ab Version 4.2!
    			break;   			
    		case start:
    			sBefehl = "ctrl=1\r";
    			break;   			
    		case stop:
		    	sBefehl = "ctrl=0\r";
		    	break;   			
    		case steigung:
    			if (wert == 0.0)
    				sBefehl = "load=6,0\r";
    			else
    				sBefehl = ("load=6,"+zfk2.format(wert)+"\r");
    			break; 
    		case lesedaten:
    			sBefehl = "data?\r";
    			break;  
    		case cassette:
    			if (wert == 0.0)	// MTB 9-fach
    				//befehl = "cassette=11,12,14,16,18,21,24,28,32\r".toCharArray();
    				sBefehl = ("cassette="+cyclus2CassetteMTB+"\r");
    			else				// RR 10-fach
    				//befehl = "cassette=11,12,13,14,15,17,19,21,24,28\r".toCharArray(); 
    				sBefehl = ("cassette="+cyclus2CassetteRR+"\r");
    				break;
    		case rings:
    			if (wert == 0.0)	// MTB 3-fach
    				//befehl = "rings=22,32,44\r".toCharArray();
    				sBefehl = ("rings="+cyclus2RingsMTB+"\r");
    			else				// RR 2-fach
    				//befehl = "rings=39,53\r".toCharArray();
    				sBefehl = ("rings="+cyclus2RingsRR+"\r");  
    			break;
    		default:
    			break;
    		}
   		
    		befehl = sBefehl.toCharArray();
	    	sendcnt = sBefehl.length();
	    	if (sendcnt == 0) {
	    		Mlog.error("kein gültiger Befehl!");
	    		return "0";
	    	}
	    	
    		// "überflüssige" Zeichen zuerst einlesen (Puffer leeren)
    		rest = in.available();

    		if (rest > 0)
    			rest = (char) in.read(clear);
     		
    		if (isDeepdebug())
    			Mlog.info("gelöscht aus Puffer: "+rest);   	// Debug!     

	    	for (i=0; i<sendcnt; i++) {
	    		out.write((int) befehl[i]);
	    		if (isDeepdebug())
	    			Mlog.debug("s: 0x"+Integer.toHexString(befehl[i] & 0xff));  	// Debug!
	    	}	
    		if (isDeepdebug())
    			Mlog.debug("Send: "+new String(befehl));  	// Debug!
    		out.flush();
    		Global.sleep(kettlerwait);						// etwas warten...
    		
    		// receivecnt dynamisch ermitteln
    		receivecnt = in.available();

    		if (receivecnt <= 0) {
    			Mlog.info("keine Antwort vom Cyclus2 cnt = "+receivecnt); 
    			return "0";
    		}
    		if (isDeepdebug())
    			Mlog.debug("receivecnt:"+receivecnt);      // Debug!   
    		
    		for (i=0; i<receivecnt; i++) {
    			puff[i] = (char) in.read();
    			if (puff[i] == '?')	{						// fehlerhafte Zeichen überspringen
    	        	Mlog.info("r:?"); 	        	
    				i--;
    			}
    		}

    		receivecnt--;		// CR abziehen
			Ergebnis = new String(puff).substring(0, receivecnt);

    		if (isDeepdebug()) {
    			Mlog.debug("Zeichen gelesen: "+receivecnt);   	// Debug!     
        		for (k=0; k<receivecnt; k++) {   				// Debug!     
        			Mlog.debug("r:"+puff[k]);        			// Debug!      	        	
        		}   											// Debug!     
    		}
		} catch (IOException e) {
			Mlog.error("serieller Kommunikationsfehler Cyclus2: IO-Fehler");
			Mlog.ex(e);
		}
		
		return Ergebnis;
    }

    /**
     * serielle Kommunikation mit dem ERGO-FIT Ergometer/Crosstrainer
     * @param Kommando	Kommando an Trainingsgerät
     * @param wert		ggf. zus. Wert
     * @return String mit Antwort
     */
    public String sertalkErgofit(Commands Kommando, int wert) {
    	int i = 0;
    	int k = 0;
    	int rest = 0;
  		char puff[] = new char[256];
	    byte befehl[] = new byte[10];
        int sendcnt = 0;
        int receivecnt = 0;
  		byte clear[] = new byte[256];
  		byte cs = 0;
        
    	try {
    		switch (Kommando) {
    		case setpc:
		    	befehl[0] = 1;		// SOH
		    	befehl[1] = 'R';	// 82
		    	befehl[2] = 2;		// STX
		    	befehl[3] = 0;		// nur Tasten gesperrt
		    	befehl[4] = 3;		// ETX
		    	befehl[5] = 82;		// CS (Prüfsumme: alles bis dahin XOR)
		    	befehl[6] = 23;		// ETB
		    	sendcnt = 7;
		    	break;   			
    		case version:
		    	befehl[0] = 1;		// SOH
		    	befehl[1] = 'V';	// 86
		    	befehl[2] = 3;		// ETX
		    	befehl[3] = 84;		// CS (Prüfsumme: alles bis dahin XOR)
		    	befehl[4] = 23;		// ETB
		    	sendcnt = 5;
		    	break;   			
    		case getpower:
		    	befehl[0] = 1;		// SOH
		    	befehl[1] = 'L';	// 
		    	befehl[2] = 3;		// ETX
		    	befehl[3] = 78;		// CS (Prüfsumme: alles bis dahin XOR)
		    	befehl[4] = 23;		// ETB
		    	sendcnt = 5;
		    	break;   			
    		case leistung:
    			String pow = ""+wert;
		    	befehl[0] = 1;		// SOH
		    	befehl[1] = 'W';	// 87
		    	befehl[2] = 2;		// STX
		    	befehl[3] = (byte) pow.charAt(0);
	    		if (wert > 9) {	    	
	    			befehl[4] = (byte) pow.charAt(1);
		    		if (wert > 99)
		    			befehl[5] = (byte) pow.charAt(2);
		    		else
		    			befehl[5] = '0';
	    		} else
	    			befehl[4] = '0';

	    		befehl[6] = 3;		// ETX
	    		for (i=0; i<7; i++)
	    			cs ^= befehl[i]; 
		    	befehl[7] = cs;		// CS (Prüfsumme: alles bis dahin XOR)
		    	befehl[8] = 23;		// ETB
    			sendcnt = 9;	    			
		    	break;   			
    		case rpm:
		    	befehl[0] = 1;		// SOH
		    	befehl[1] = 'D';	// 68
		    	befehl[2] = 3;		// ETX
		    	befehl[3] = 70;		// CS (Prüfsumme: alles bis dahin XOR)
		    	befehl[4] = 23;		// ETB
		    	sendcnt = 5;
		    	break;   			
    		case puls:
		    	befehl[0] = 1;		// SOH
		    	befehl[1] = 'P';	// 80
		    	befehl[2] = 3;		// ETX
		    	befehl[3] = 82;		// CS (Prüfsumme: alles bis dahin XOR)
		    	befehl[4] = 23;		// ETB
		    	sendcnt = 5;
		    	break;
			default:
				break;   			
    		}

    		// "überflüssige" Zeichen zuerst einlesen (Puffer leeren)
    		rest = in.available();

    		if (rest > 0)
    			rest = (char) in.read(clear);
     		
    		if (isDeepdebug())
    			Mlog.info("gelöscht aus Puffer: "+rest);   	// Debug!     

	    	for (i=0; i<sendcnt; i++) {
	    		out.write((int) befehl[i]);
	    		if (isDeepdebug())
	    			Mlog.debug("s: 0x"+Integer.toHexString(befehl[i] & 0xff));  	// Debug!
	    	}	
    		if (isDeepdebug())
    			Mlog.debug("Send: "+new String(befehl));  	// Debug!
    		out.flush();
    		Global.sleep(ergofitwait);						// etwas warten...
    		
    		// receivecnt dynamisch ermitteln
    		receivecnt = in.available();

    		if (receivecnt <= 0) {
    			Mlog.info("keine Antwort vom ERGO-FIT cnt = "+receivecnt); 
    			return "0";
    		}
    		
    		for (i=0; i<receivecnt; i++) {
    			puff[i] = (char) in.read();
    			if (puff[i] == '?')	{						// fehlerhafte Zeichen überspringen
    	        	Mlog.debug("r:?"); 	        	
    				i--;
    			}
    		}

    		if (isDeepdebug()) {
    			Mlog.debug("Zeichen gelesen: "+receivecnt);   	// Debug!     
        		for (k=0; k<receivecnt; k++) {   				// Debug!     
        			Mlog.debug("r:"+puff[k]);        			// Debug!      	        	
        		}   											// Debug!     
    		}
		} catch (IOException e) {
			Mlog.error("serieller Kommunikationsfehler (ERGO-FIT): IO-Fehler");
			Mlog.ex(e);
		}
		
		String ret = String.valueOf(puff);
		// Bis 0x03 (ETX) zurückgeben und die ersten drei Zeichen weglassen:
		int etxpos = ret.indexOf(0x03);
		if (etxpos < 3)
			return "0";
		ret = ret.substring(3, etxpos);
		return ret;
    }

    /**
     * serielle Kommunikation mit dem Kettler Ergometer
     * @param Kommando	Kommando an Trainingsgerät
     * @param wert		ggf. zus. Wert
     * @return String mit Antwort
     */
    public String sertalkKettler(Commands Kommando, int wert) {
    	int i = 0;
    	//int j = 0;
    	int k = 0;
    	int rest = 0;
  		char puff[] = new char[256];
	    byte befehl[] = new byte[10];
        int sendcnt = 0;
        int receivecnt = 0;
  		byte clear[] = new byte[256];
        
    	try {
    		switch (Kommando) {
    		case setpc:
		    	befehl[0] = 'C';
		    	//befehl[1] = 'M';
		    	befehl[1] = 'D';
		    	befehl[2] = 0x0D;
		    	befehl[3] = 0x0A;
		    	sendcnt = 4;
		    	break;   			
    		case setpc_alt:
		    	befehl[0] = 'C';
		    	befehl[1] = 'M';
		    	befehl[2] = 0x0D;
		    	befehl[3] = 0x0A;
		    	sendcnt = 4;
		    	break;   			
    		case reset:
		    	befehl[0] = 'R';
		    	befehl[1] = 'S';
		    	befehl[2] = 0x0D;
		    	befehl[3] = 0x0A;
		    	sendcnt = 4;
		    	break;   			
    		case getid:
		    	befehl[0] = 'I';
		    	befehl[1] = 'D';
		    	befehl[2] = 0x0D;
		    	befehl[3] = 0x0A;
		    	sendcnt = 4;
		    	break;   			
    		case leistung:
    			String pow = ""+wert;
		    	befehl[0] = 'P';
		    	befehl[1] = 'W';
		    	befehl[2] = ' ';
		    	befehl[3] = (byte) pow.charAt(0);
	    		if (wert > 9) {		    	
	    			befehl[4] = (byte) pow.charAt(1);
		    		if (wert > 99) {
		    			befehl[5] = (byte) pow.charAt(2);
		    			befehl[6] = 0x0D;
				    	befehl[7] = 0x0A;
		    			sendcnt = 8;
		    		} else {
		    			befehl[5] = 0x0D;
				    	befehl[6] = 0x0A;
		    			sendcnt = 7;	    			
		    		}
	    		} else {
	    			befehl[4] = 0x0D;
	    			befehl[5] = 0x0A;
	    			sendcnt = 6;	    			
	    		} 
		    	break;   			
    		case lesedaten:
		    	befehl[0] = 'S';
		    	befehl[1] = 'T';
		    	befehl[2] = 0x0D;
		    	befehl[3] = 0x0A;
		    	sendcnt = 4;
		    	break;   			
    		case sp0:
		    	befehl[0] = 'S';
		    	befehl[1] = 'P';
		    	befehl[2] = '0';
		    	befehl[3] = 0x0D;
		    	befehl[4] = 0x0A;
		    	sendcnt = 5;
		    	break; 
			default:
				break;   			
    		}

    		// "überflüssige" Zeichen zuerst einlesen (Puffer leeren)
    		rest = in.available();

    		if (rest > 0)
    			rest = (char) in.read(clear);
     		
    		if (isDeepdebug())
    			Mlog.info("gelöscht aus Puffer: "+rest);   	// Debug!     

	    	for (i=0; i<sendcnt; i++) {
	    		out.write((int) befehl[i]);
	    		if (isDeepdebug())
	    			Mlog.debug("s: 0x"+Integer.toHexString(befehl[i] & 0xff));  	// Debug!
	    	}	
    		if (isDeepdebug())
    			Mlog.debug("Send: "+new String(befehl));  	// Debug!
    		out.flush();
    		Global.sleep(kettlerwait);						// etwas warten...
    		
    		// receivecnt dynamisch ermitteln
    		receivecnt = in.available();

    		if (receivecnt <= 0) {
    			Mlog.info("keine Antwort vom Ergometer cnt = "+receivecnt); 
    			return "0";
    		}
    		if (isDeepdebug())
    			Mlog.debug("receivecnt:"+receivecnt);      // Debug!   
    		
    		for (i=0; i<receivecnt; i++) {
    			puff[i] = (char) in.read();
    			if (puff[i] == '?')	{						// fehlerhafte Zeichen überspringen
    	        	Mlog.info("r:?"); 	        	
    				i--;
    			}
    		}

    		if (isDeepdebug()) {
    			Mlog.debug("Zeichen gelesen: "+receivecnt);   	// Debug!     
        		for (k=0; k<receivecnt; k++) {   				// Debug!     
        			Mlog.debug("r:"+puff[k]);        			// Debug!      	        	
        		}   											// Debug!     
    		}
		} catch (IOException e) {
			Mlog.error("serieller Kommunikationsfehler (Kettler): IO-Fehler");
			Mlog.ex(e);
		}
		String ret = String.valueOf(puff).substring(0, receivecnt);
		ret = ret.replace("\r\n", "");	// Zeilenumbrüche raus
		return ret;
    }
    
    /**
     * serielle Kommunikation mit dem Daum Ergometer
     * @param Kommando	Kommando an Trainingsgerät
     * @param adresse	Adresse
     * @param wert		ggf. zus. Wert
     * @return Integer-Array mit Antwort
     */
    public byte[] sertalk(Commands Kommando, byte adresse, byte wert) {
    	int i = 0;
    	int rest = 0;
    	int ih;
  		byte puff[] = new byte[25];
	    byte befehl[] = new byte[4];
        int sendcnt = 0;
        int receivecnt = 0;
    	
        try {                                  
        	switch (Kommando) {
        	case getadress:
        		befehl[0] = 0x11;    		    	
        		sendcnt = 1;
        		break;
        	case leistung:
        		befehl[0] = 0x51;
        		befehl[1] = adresse;
        		befehl[2] = wert;
        		sendcnt = 3;
        		break;
        	case lesedaten:
        		befehl[0] = 0x40;
        		befehl[1] = adresse;
        		sendcnt = 2;
        		break;
        	case version:
        		befehl[0] = 0x73;
        		befehl[1] = adresse;
        		sendcnt = 2;
        		break;
        	case check_cockpit:
        		befehl[0] = 0x10;
        		befehl[1] = adresse;
        		sendcnt = 2;
        		break;
        	default:
        		break;
        	}

    		// "überflüssige" Zeichen zuerst einlesen (Puffer leeren)
    		rest = in.available();
    		in.read(puff, 0, rest);   		
    		out.write(befehl, 0, sendcnt);
    		out.flush();
    		
    		if (isDeepdebug()) {
    			Mlog.info("gelöscht aus Puffer: "+rest);   	// Debug!     
   				for (i=0; i<sendcnt; i++) {
    				ih = befehl[i] & 0xff;
	    			Mlog.info("s:"+ih);
    			}
    		}
    		
    		Global.sleep(daumwait);						
    		
    		// receivecnt dynamisch ermitteln
    		receivecnt = in.available();
    		in.read(puff, 0, receivecnt);
    		
    		if (isDeepdebug()) {
    			Mlog.info("Zeichen gelesen: "+receivecnt);   	// Debug!     
        		for (i=0; i<receivecnt; i++) {   				// Debug!     
    				ih = puff[i] & 0xff;
        			Mlog.info("r:"+ih);        				// Debug!      	        	
        		}   											// Debug!     
    		}

		} catch (IOException e) {
			Mlog.error("serieller Kommunikationsfehler (Daum): IO-Fehler");
			Mlog.ex(e);
		} catch (Exception e) {
			Mlog.error("serieller Kommunikation (Daum): anderer Fehler!");
			Mlog.ex(e);
		}
		return puff;
    }
    
    /**
     * serielle Kommunikation mit dem Daum Ergometer
     * altes Schnittstellenprotokoll bis 2001
     * @param Kommando	Kommando an Trainingsgerät
     * @param adresse	Adresse
     * @param wert		ggf. Wert
     * @return Integer-Array mit Antwort
     */
    public int[] sertalk2001(Commands Kommando, byte adresse, byte wert) {
    	int i = 0;
  		int puff[] = new int[25];
	    byte befehl[] = new byte[3];
        int sendcnt = 0;
        int receivecnt = 0;
    	
        try {                                  
        	switch (Kommando) {
        	case getadress:
        		befehl[0] = 0x11;    		    	
        		befehl[1] = 0x00;   // RS-232 2001
        		//sendcnt = 1;
        		sendcnt = 2;   // RS-232 2001
        		receivecnt = 2;
        		break;
        	case leistung:
        		befehl[0] = 0x51;
        		befehl[1] = adresse;
        		befehl[2] = wert;
        		sendcnt = 3;
        		receivecnt = 3;
        		break;
        	case lesedaten:
        		befehl[0] = 0x40;
        		befehl[1] = adresse;
        		sendcnt = 2;
        		//receivecnt = 19;  
        		receivecnt = 17;  // RS-232 2001
        		break;
        	case version:
        		befehl[0] = 0x73;
        		befehl[1] = adresse;
        		sendcnt = 2;
        		receivecnt = 11;
        		// RS-232 2001:
        		puff[0] = '-'+4; 
        		puff[1] = '-'+4; 
        		puff[2] = 'C'+4; 
        		puff[3] = 'O'+4; 
        		puff[4] = 'M'+4; 
        		puff[5] = '-'+4; 
        		puff[6] = '2'+4; 
        		puff[7] = '0'+4; 
        		puff[8] = '0'+4; 
        		puff[9] = '1'+4; 
        		return puff;
        		//break;
        	case check_cockpit:
        		befehl[0] = 0x10;
        		befehl[1] = adresse;
        		sendcnt = 2;
        		receivecnt = 3;
        		break;
        	default:
        		break;
        	}

	    	for (i=0; i<sendcnt; i++) {
	    		out.write((int) befehl[i]);
	    		out.flush();
	    		Global.sleep(daum2001wait);						// die älteren Cockpits brauchen etwas Pause ?
	    	}

    		for (i=0; i<receivecnt; i++)
    			puff[i] = (int) in.read();
    		
    		// 2 Zeichen (CR-LF?) zusätzlich lesen...
		    puff[i+1] = (int) in.read();
		    puff[i+2] = (int) in.read();

    		if (isDeepdebug()) {
    			Mlog.info("Zeichen gelesen: "+receivecnt);
    			for (i=0; i<receivecnt; i++) { // nur für Testzwecke!
    				Mlog.info("r"+puff[i]);       	        	
    			}
    		}
		} catch (IOException e) {
			Mlog.error("serieller Kommunikationsfehler (D): IO-Fehler");
			Mlog.ex(e);
		}
    	return puff;
    }

    /**
     * Öffnet die serielle Kommunikation zum Ergometer 
     * @param port		COM-Port
     * @param baud   	Baudrate
     */
    public void seropen(String port, int baud) {
    	Mlog.info(RXTXVersion.getVersion());
        CommPortIdentifier portIdentifier;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(port);
	        if ( portIdentifier.isCurrentlyOwned() )
	        {
	        	Mlog.error("Port ist blockiert!");
	        }
	        else
	        {
	            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
	            
	            if ( commPort instanceof SerialPort )
	            {
	                serialPort = (SerialPort) commPort;
		            serialPort.setSerialPortParams(baud,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
	      	  		
	      	  		serialPort.setDTR(true); 
	                serialPort.setRTS(true);
	                                
	                in = serialPort.getInputStream();
	                out = serialPort.getOutputStream();        
	            }
	            else
	            {
	                Mlog.error("Nur serielle Ports werden hier behandelt.");
	            }
	        }     
		} catch (NoSuchPortException e) {
			Mlog.error("serieller Kommunikationsfehler: Port nicht vorhanden");
			setErgoCom("");
		} catch (PortInUseException e) {
			Mlog.error("serieller Kommunikationsfehler: Port nicht frei");
			setErgoCom("");
		} catch (UnsupportedCommOperationException e) {
			Mlog.error("serieller Kommunikationsfehler: Operation wird nicht unterstützt");
			Mlog.ex(e);
		} catch (IOException e) {
			Mlog.error("serieller Kommunikationsfehler: IO-Fehler");
			Mlog.ex(e);
		}
    }

    /**
     * schliessen der seriellen Verbindung
     *
     */
    public void serclose() {
        try {
        	if (in != null) {
        		out.close();
        		in.close();
        		serialPort.close();
        	}
		} catch (IOException e) {
			Mlog.error("serieller Kommunikationsfehler: IO-Fehler");
			Mlog.ex(e);
		}
    }
    
    /**
     * Öffnet die Netzwerkschnittstelle für die Daum-Kommunikation
     * @param timeout	Timout
     * @param port		Portadresse
     */
    public void netopen(int timeout, int port) {
  	  	ergoSocket = new Socket();
  	  	try { 	  
  	  		InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(ergoIP), port);
  	  		//ergoSocket.connect(socketAddress, timeout);
  	  		ergoSocket.connect(socketAddress);
  	  		ergoSocket.setSoTimeout(timeout);
		
  	  	} catch (IOException e) {
			Mlog.error("Kommunikationsfehler bei netopen (kein connect)!");
  	  	}
    }

    /**
     * Schliesst die Netzwerkschnittstelle der Daum-Kommunikation
     */
    public void netclose() {
		try {
			if (ergoSocket != null) 
				ergoSocket.close();
		} catch (IOException e) {
			Mlog.error("Kommunikationsfehler bei netclose!");
		}
    }

    /**
     * Versucht mit mehreren Produkt-IDs unter der Vendor-ID 3561H die USB-Schnittstelle zu öffnen
     * @return gefundene PID oder 0, wenn nicht gefunden
     */
    public short usbopen() {
    	int i = 0;
    	short vid;
    	short pidi = 0;
    	short[] pid = new short[10];
  		pid[0] = (short) 0x1902;
  		pid[1] = (short) 0x1904;
  		pid[2] = (short) 0x1952;
  		pid[3] = (short) 0xE6BE;
  		pid[4] = (short) 0x1932;
  		pid[5] = (short) 0x1942;
  		vid    = (short) 0x3561;
  		short anz = 6;
    	boolean usbfound = false;
		// init USB Zeugs
	    LibusbJava.usb_init();
	    LibusbJava.usb_find_busses(); 	// find all usb busses
	    LibusbJava.usb_find_devices(); 	// find all devices
	    
	    for (i=0; i<anz; i++) {
	    	pidi = pid[i];
			Mlog.debug("USB-Open: pruefe USB Device "+i+" PID: "+Integer.toHexString(pidi & 0x0000FFFF));
		    dev = USB.getDevice(vid, pidi);
		    try {
				dev.open(1, 0, -1);	// neues Interface
				//dev.open(1, 0, 1);	// altes Interface
				usbfound = true;
				
				Usb_Config_Descriptor[] cfD = dev.getConfigDescriptors();
				Usb_Interface[] iF = cfD[0].getInterface();
				Mlog.debug("Interfacetyp (alt=3,neu=1): "+iF[0].getNumAltsetting());	// alt: 3 - neu: 1
				if (iF[0].getNumAltsetting() > 1) {			// dann handelt es sich um ein altes Cockpit (grün)
					dev.close();
					dev.open(1, 0, 1);
					setTacxalt(true);
				}
				break;
			} catch (USBException e) {
				Mlog.debug("USB-Open: USB Device nicht erkannt, PID: "+Integer.toHexString(pidi & 0x0000FFFF)+" Ex: "+e.toString());
			}
	    }
	    if (usbfound) {
	    	Mlog.info("USB-Open: Device erkannt, PID: "+Integer.toHexString(pidi & 0x0000FFFF));
	    	return pidi;
	    } else {
		    Mlog.error("USB-Open: Device nicht gefunden!");
		    return 0;
	    }
    }

    /**
     * Schliesst die Tacx-USB-Schnittstelle!
     */
    public void usbclose() {
		try {
			if (dev != null)
				dev.close();
		} catch (USBException e) {
			Mlog.error("USB Kommunikationsfehler (Tacx) bei close");
			Mlog.ex(e);
		}
    }
    
    /**
     * Netzwerkkommunikation mit dem Daum Ergometer
     * @param Kommando	Kommando an Trainingsgerät
     * @param wert		ggf. Wert
     * @return Rückgabestring vom Ergometer
     */
    public String nettalkDaum(Commands Kommando, double wert) {
	  DecimalFormat df = new DecimalFormat("00000");
	  String Ergebnis = new String();
	  int prfsum = 0;
	  int cnt = 0;
	  int buffersize = 50;
      try {
      		char puff[] = new char[buffersize];
	        PrintStream os = new PrintStream(ergoSocket.getOutputStream());
	        BufferedReader in = new BufferedReader(
	                new InputStreamReader(ergoSocket.getInputStream()));
		    char befehl[] = new char[1];
		    switch (Kommando) {
		    case version:
			    befehl = new char[7];
		    	// 01 56 30 30 38 32 17 schicken
		    	befehl[0] = 0x01;	// SOH
		    	befehl[1] = 0x56;	// S1 = V
		    	befehl[2] = 0x30;	// Z1 = 0
		    	befehl[3] = 0x30;	// Z2 = 0
		    	befehl[4] = 0x38;	// Prüfsumme 1
		    	befehl[5] = 0x32;	// Prüfsumme 2
		    	befehl[6] = 0x17;   // ETB
		    	break;
		    case leistung:
		    	return "0";			// Leistungsvorgabe sollte nun über getpower ebenso laufen!
		    case getpower:
		    	// S23<%5.2f>
		    	// 01 53 32 33 W1 W2 W3 W4 W5 . 30 30 P1 P2 17 schicken
			    befehl = new char[15];
		    	String Stringwert = df.format(wert);
		    	befehl[0] = 0x01;	// SOH
		    	befehl[1] = 0x53;	// S1 = S
		    	befehl[2] = 0x32;	// Z1 = 2
		    	befehl[3] = 0x33;	// Z2 = 3
		    	befehl[4] = Stringwert.charAt(0);
		    	befehl[5] = Stringwert.charAt(1);
		    	befehl[6] = Stringwert.charAt(2);
		    	befehl[7] = Stringwert.charAt(3);
		    	befehl[8] = Stringwert.charAt(4);
		    	befehl[9] = 0x2E;   // "."
		    	befehl[10] = 0x30;
		    	befehl[11] = 0x30;
		    	for (int i=1; i<=11; i++)
		    		prfsum += (int) befehl[i];
		    	prfsum = prfsum % 100;
		        int tmp = (prfsum / 10) + 0x30;
		    	befehl[12] = (char) tmp;  // Prüfsumme 1
		        tmp = (prfsum % 10) + 0x30;
		    	befehl[13] = (char) tmp;   // Prüfsumme 2
		    	befehl[14] = 0x17;  // ETB
		    	break;		    	
		    case puls:
			    befehl = new char[7];
		    	befehl[0] = 0x01;	// SOH
		    	befehl[1] = 0x50;	// S1 = P
		    	befehl[2] = 0x30;	// Z1 = 0
		    	befehl[3] = 0x31;	// Z2 = 1
		    	befehl[4] = 0x37;	// Prüfsumme 1  (50H+30H+31H=B1H=177 -> 7)
		    	befehl[5] = 0x37;	// Prüfsumme 2  (50H+30H+31H=B1H=177 -> 7)
		    	befehl[6] = 0x17;   // ETB
		    	break;
		    case rpm:
			    befehl = new char[7];
		    	befehl[0] = 0x01;	// SOH
		    	befehl[1] = 0x53;	// S1 = S
		    	befehl[2] = 0x32;	// Z1 = 2
		    	befehl[3] = 0x31;	// Z2 = 1
		    	befehl[4] = 0x38;	// Prüfsumme 1  (53H+32H+31H=B6H=182 -> 8)
		    	befehl[5] = 0x32;	// Prüfsumme 2  (53H+32H+31H=B6H=182 -> 2)
		    	befehl[6] = 0x17;   // ETB
		    	break;
			default:
				return "0";
		    }

    		// "überflüssige" Zeichen zuerst einlesen (Puffer leeren)
    		if (in.ready()) {
    		  in.read(puff, 0, buffersize);
      			if (isDeepdebug())
      				Mlog.info("Puffer geleert!");
    		}
    		
    		if (isDeepdebug())
    				Mlog.debug("(nettalk) befehl:"+befehl.toString());

    		// zuerst 06, dann ...
            os.print((char) 0x06);
            // ... Befehl senden
		    os.print(befehl); 
		    
		    cnt = in.read(puff, 0, buffersize);
		    // wenn hier nur eine 1 gelesen wird, dann nochmal lesen ansonsten erstes Zeichen löschen

    		if (isDeepdebug())
    			Mlog.debug("cnt1: "+cnt+" - "+(new String(puff)));
    		
		    if (cnt == 1) {
		    	cnt = in.read(puff, 0, buffersize);
			    Ergebnis = new String(puff);
	    		if (isDeepdebug())
	    			Mlog.debug("cnt2+: "+cnt+" - "+Ergebnis);
		    } else {
		    	cnt--;
		    	Ergebnis = new String(puff).substring(1);
	    		if (isDeepdebug())
	    			Mlog.debug("cnt2-: "+cnt+" - "+Ergebnis);
		    }
    		if (isDeepdebug()) {
    			Mlog.debug("Zeichen gelesen: "+cnt);
    			for (int i=1; i<cnt-1; i++)
    				Mlog.debug("r:"+puff[i]);
    		}
    		
		} catch (SocketTimeoutException sex) {
			Mlog.debug("Socket Timeout");
			return "0";
		} catch (Exception ex) {
			Mlog.error("Kommunikationsfehler!");
//    		Mlog.ex(ex);
			return "0";
		}
		
		// Ausgabe formatieren:
		if (cnt > 3)
			return Ergebnis.substring(4, cnt-3);
		else
			return "0";
	}
	
    /**
     * Netzwerkkommunikation mit dem Cyclus2 Ergometer: Ergoline-Schnittstellenprotokoll
     * @param cmd		Kommando an Trainingsgerät
     * @param wert		ggf. Wert
     * @return Rückgabestring vom Ergometer
     */
    public String nettalkCyclus2_El(Commands cmd, double wert) {
	  String Ergebnis = new String();
	  int intwert = (int) wert;
	  int cnt = 0;
	  int buffersize = 50;
      try {
      		char puff[] = new char[buffersize];
	        PrintStream os = new PrintStream(ergoSocket.getOutputStream());
	        BufferedReader in = new BufferedReader(new InputStreamReader(ergoSocket.getInputStream()));
		    char befehl[] = new char[1];
		    switch (cmd) {
    		case setpc:
			    befehl = "ergo=1\r".toCharArray();
		    	break;   			
    		case setpc_aus:
			    befehl = "ergo=0\r".toCharArray();
		    	break;   			
    		case version:
			    befehl = "i\r".toCharArray();
		    	break;   			
    		case leistung:
			    befehl = ("w"+intwert+"\r").toCharArray();
		    	break;   			
    		case rpm:
			    befehl = "d\r".toCharArray();
		    	break;   			
    		case puls:
			    befehl = "h\r".toCharArray();
		    	break;
			default:
				break;   			
    		}

    		// "überflüssige" Zeichen zuerst einlesen (Puffer leeren)
    		if (in.ready()) {
    		  in.read(puff, 0, buffersize);
      			if (isDeepdebug())
      				Mlog.info("Puffer geleert!");
    		}
    		
            // ... Befehl senden
    		if (isDeepdebug()) {
    			for (int i=0;i<befehl.length;i++) {
    				Mlog.debug("s: " + befehl[i]);
    			}
    		}
		    os.print(befehl); 
		    
		    if (cmd == Commands.leistung)	// Hier kommt keine Antwort vom Cyclus2
		    	return "0";
		    
		    cnt = in.read(puff, 0, buffersize);
		    cnt--;		// CR abziehen
			Ergebnis = new String(puff).substring(0, cnt);

    		if (isDeepdebug()) {
    			Mlog.debug("Zeichen gelesen: "+cnt);
    			for (int i=0; i<cnt; i++)
    				Mlog.debug("r:"+puff[i]);
    		}    			
    		
		} catch (SocketTimeoutException sex) {
			Mlog.debug("Socket Timeout");
			return "0";
		} catch (Exception ex) {
			Mlog.error("Kommunikationsfehler!");
			//Mlog.ex(ex);
			return "0";
		}
		
		// Rückgabe formatieren:
	    switch (cmd) {
		case setpc:
		case setpc_aus:
		case version:
			return Ergebnis;
		case rpm:
			if (Ergebnis.substring(0,1).equals("n")){
				//Mlog.debug("rpm erkannt");
				return Ergebnis.substring(1, 4);				
			}
		case puls:
			if (Ergebnis.substring(0,1).equals("H")){
				//Mlog.debug("Puls erkannt");
				return Ergebnis.substring(1, 4);				
			}
		default:
			break;
	    }
		return "0";
	}

    /**
     * Netzwerkkommunikation mit dem Cyclus2 Ergometer: Cyclus2-Schnittstellenprotokoll
     * @param cmd		Kommando an Trainingsgerät
     * @param wert		ggf. Wert
     * @return Rückgabestring vom Ergometer
     */
    public String nettalkCyclus2(Commands cmd, double wert) {
	  String Ergebnis = new String();
	  //int intwert = (int) wert;
	  int cnt = 0;
	  int buffersize = 100;
	  DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
	  dfs.setDecimalSeparator('.');
	  DecimalFormat zfk2 = new DecimalFormat("#0.00", dfs);
	  
      try {
      		char puff[] = new char[buffersize];
	        PrintStream os = new PrintStream(ergoSocket.getOutputStream());
	        BufferedReader in = new BufferedReader(new InputStreamReader(ergoSocket.getInputStream()));
		    char befehl[] = new char[25];
		    switch (cmd) {
    		case setpc:
			    befehl = "slave=4\r".toCharArray();
		    	break;   			
    		case setpc_aus:
			    befehl = "slave=0\r".toCharArray();
		    	break; 
    		case version:
			    befehl = "vers?\r".toCharArray();
		    	break;   			
    		case init:
			    befehl = "data=13\r".toCharArray();
		    	break;   			
    		case start:
			    befehl = "ctrl=1\r".toCharArray();
		    	break;   			
    		case stop:
			    befehl = "ctrl=0\r".toCharArray();
		    	break;   			
    		case steigung:
			    if (wert == 0.0)
				    befehl = "load=6,0\r".toCharArray();
			    else
			    	befehl = ("load=6,"+zfk2.format(wert)+"\r").toCharArray();
		    	break; 
    		case lesedaten:
			    befehl = "data?\r".toCharArray();
		    	break;  
    		case cassette:
    			if (wert == 0.0)	// MTB 9-fach
    				//befehl = "cassette=11,12,14,16,18,21,24,28,32\r".toCharArray();
    				befehl = ("cassette="+cyclus2CassetteMTB+"\r").toCharArray();
    			else				// RR 10-fach
    				//befehl = "cassette=11,12,13,14,15,17,19,21,24,28\r".toCharArray(); 
    				befehl = ("cassette="+cyclus2CassetteRR+"\r").toCharArray();
    			break;
    		case rings:
    			if (wert == 0.0)	// MTB 3-fach
    				//befehl = "rings=22,32,44\r".toCharArray();
    				befehl = ("rings="+cyclus2RingsMTB+"\r").toCharArray();
    			else				// RR 2-fach
    				//befehl = "rings=39,53\r".toCharArray();
    				befehl = ("rings="+cyclus2RingsRR+"\r").toCharArray();  
    			break;
			default:
				break;
    		}

    		// "überflüssige" Zeichen zuerst einlesen (Puffer leeren)
    		if (in.ready()) {
    			int anz = in.read(puff, 0, buffersize);
      			Mlog.debug("Puffer geleert: "+anz+" Inhalt: "+new String(puff));
    		}
    		
            // ... Befehl senden
    		if (isDeepdebug()) {
    			for (int i=0;i<befehl.length;i++) {
    				Mlog.debug("s: " + befehl[i]);
    			}
    		}
		    os.print(befehl); 
		    
		    cnt = in.read(puff, 0, buffersize);
		    cnt--;			// CR entfernen am Ende
			Ergebnis = new String(puff).substring(0, cnt);

    		if (isDeepdebug()) {
    			Mlog.debug("Zeichen gelesen: "+cnt);
    			for (int i=0; i<cnt; i++)
    				Mlog.debug("r:"+puff[i]);
    		}    			
    		
		} catch (SocketTimeoutException sex) {
			Mlog.debug("Socket Timeout");
			return "0";
		} catch (Exception ex) {
			Mlog.error("Kommunikationsfehler!");
			//Mlog.ex(ex);
			return "0";
		}
		
		return Ergebnis;
	}

    /**
     * Daum-Ergometer wird im LAN gesucht in dem Adressbereich mit 256 Adressen in dem auch der
     * PC ist.
     * @return IP-Adresse des Daum-Ergometers
     */
	public String SucheErgometer() {
		String PcIp = new String();
		//Cursor busyCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_WAIT);
		try {
			PcIp = InetAddress.getLocalHost().getHostAddress().toString();
			Mlog.info("eigene IP: "+ PcIp);
		} catch (UnknownHostException e) {
			Mlog.error("IP-Adresse konnte nicht ermittelt werden!");
			//e.printStackTrace();
		}
		for (int i=0; i<256; i++) {
			PcIp = PcIp.substring(0,PcIp.lastIndexOf('.')+1)+i;
			Mlog.debug("teste: "+PcIp);
			this.ergoIP = PcIp;
			this.netopen(300, 51955);
            String ergoVersion = this.nettalkDaum(Trainer.Commands.version, 0.0);
            this.netclose();
            if (ergoVersion != "0") {
            	Mlog.info("ermittelt: "+PcIp);
            	this.ergoIP = PcIp;
        		return PcIp;
            }
		}
		
		Messages.errormessage("Die IP-Adresse konnte nicht ermittelt werden. Ist der Ergometer eingeschaltet und angeschlossen?");
		this.ergoIP = "0";
		return "0";
	}

	public void USBTalkTacx(usbCommands Kommando, byte[] wData) {	         	
		try {
			switch (Kommando) {
			case kommunikation:
				dev.writeBulk(0x02, wData, wData.length, 1000, false);

				Global.sleep(100);
				if (isDeepdebug()) 
					logData(wData, "write");

				dev.readBulk(0x82, readData, 64, 1000, false);
				if (isDeepdebug()) 
					logData(readData, "read");
			}
		} catch (USBException e) {
			Mlog.error("USB Kommunikationsfehler (Tacx)");
			Mlog.ex(e);
		} catch (Exception e) {
			Mlog.error("USB allg. Kommunikationsfehler (Tacx)");
			Mlog.ex(e);
		}
		//return readData;
	}

	/**
	 * Debugprotokoll für TACX-Kommunikation
	 * @param data data
	 * @param dir  dir
	 */
	private static void logData(byte[] data, String dir) {
		Mlog.debug("Data: "+dir);
		for (int i = 0; i < data.length; i++) {
			Mlog.debug((i+1)+": 0x" + Integer.toHexString(data[i] & 0xff) + " ");
		}
	}
	
	/**
	 * Öffnen der ANT+ Kommunikation entsprechend dem aktiven Modell
	 */
	public void antopen() {
		if (Rsmain.libant == null)
			return;
//		if (isDeepdebug())
//			Rsmain.libant.setDebug(true);
		
		Rsmain.libant.stop();
		if (getTrainerModell().equals("tacxvortex"))
			Rsmain.libant.setaktTDev(LibAnt.TDev.tacxvortex);
		else if (getTrainerModell().equals("tacxbushido"))
			Rsmain.libant.setaktTDev(LibAnt.TDev.tacxbushido);
		else if (getTrainerModell().equals("wahookickr"))
			Rsmain.libant.setaktTDev(LibAnt.TDev.wahookickr);
		else if (getTrainerModell().equals("antfec"))
			Rsmain.libant.setaktTDev(LibAnt.TDev.antfec);
		Rsmain.libant.start();
	}
	
	/**
	 * Schliessen der ANT+ Kommunikation
	 */
	/*
	public void antclose() {
		if (Rsmain.libant != null)
			Rsmain.libant.stop();		
	}	
*/
	
	/**
	 * Berechnet den virtuellen Gang (z.B. 1..9) aus 
	 * @param aktZaehne		Zähne, die hinten aktuell gefahren werden
	 * @return	int			virtueller Gang (1..9), 9 falls Gang nicht ermittelt werden konnte
	 */
	public int mapCyclus2Gang(int aktZaehne) {
		String[] ring;
		if (Rsmain.biker.getCwa() <= 0.3)	{ 	// Rennrad ?
			ring = cyclus2CassetteRR.split(",");
		} else {								// MTB !
			ring = cyclus2CassetteMTB.split(",");			
		}
		for (int i = 0; i < ring.length; i++) {
			int iring = new Integer(ring[i]);
			if (iring == aktZaehne)
				return 9 - i;
		}
		return 9;
	}

}
