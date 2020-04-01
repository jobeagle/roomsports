import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
 * LoadGPXFile.java: Einlesen der GPS-Daten im GPX-Format
 *****************************************************************************
 *
 * Diese Klasse beinhaltet die Methoden zum Einlesen der GPS-Daten. Die Daten 
 * werden im GPX-Format (inkl. Koordinaten, Höhe und Zeit) erwartet. Beim einlesen
 * werden Abstände und Geschwindigkeiten berechnet und die Daten in eine Liste
 * geschrieben.
 *  
 */

public class LoadGPXFile {
	public static List<TrkPt> track;
	private Document   dom;
	private Calendar   startzeit;		  	// Startzeit = Zeitpunkt des ersten Trackpunktes
	private double     gesamtstrecke;     	// Gesamtstrecke
	private long       gesamtsek = 0;     	// Zeit in Sekunden des letzten Punktes
	private long       tsek = 0;          	// Sekunden des vorherigen Punktes
	private double     tlatitude = 0.0;   	// Längengrad des letzten Punktes
	private double     tlongitude = 0.0;  	// Breitengrad des letzten Punktes
	private double     thoehe = 0.0;      	// Höhe des letzten Punktes
	private double     tabstand = 0.0;    	// Abstand zum Startpunkt des letzten Punktes
	private double     tsteigung = 0.0;   	// Steigung des letzten Punktes
	private double     entfernung = 0.0;  	// Entfernung zum nächsten Punkt
	private double     kurs = 0.0;        	// Kurswinkel zum nächsten Punkt in Grad
	private boolean    gpsleistung = false;	// wurden Leistungswerte eingelesen?
	private double     gesamthm = 0.0;    	// Höhenmeter
	private double     maxSteigung = 8.0;	// Grenzwert für Höhendatenglättung
	
	/**
	 * @return the gpsleistung
	 */
	public boolean isGpsleistung() {
		return gpsleistung;
	}

	/**
	 * @param gpsleistung the gpsleistung to set
	 */
	public void setGpsleistung(boolean gpsleistung) {
		this.gpsleistung = gpsleistung;
	}

	/**
	 * @return the gesamtstrecke
	 */
	public double getGesamtstrecke() {
		return gesamtstrecke;
	}

	/**
	 * @param gesamtstrecke the gesamtstrecke to set
	 */
	public void setGesamtstrecke(double gesamtstrecke) {
		this.gesamtstrecke = gesamtstrecke;
	}

	/**
	 * @return the gesamtsek
	 */
	public long getGesamtsek() {
		return gesamtsek;
	}

	/**
	 * @param gesamtsek the gesamtsek to set
	 */
	public void setGesamtsek(long gesamtsek) {
		this.gesamtsek = gesamtsek;
	}

	/**
	 * @return the startzeit
	 */
	public Calendar getStartzeit() {
		return startzeit;
	}

	/**
	 * @param startzeit the startzeit to set
	 */
	public void setStartzeit(Calendar startzeit) {
		this.startzeit = startzeit;
	}

	/**
	 * @return the entfernung
	 */
	public double getEntfernung() {
		return entfernung;
	}

	/**
	 * @param entfernung the entfernung to set
	 */
	public void setEntfernung(double entfernung) {
		this.entfernung = entfernung;
	}

	/**
	 * @return the kurs
	 */
	public double getKurs() {
		return kurs;
	}

	/**
	 * @param kurs the kurs to set
	 */
	public void setKurs(double kurs) {
		this.kurs = kurs;
	}

	
	/**
	 * @return the gesamthm
	 */
	public double getGesamthm() {
		return gesamthm;
	}

	/**
	 * @param gesamthm the gesamthm to set
	 */
	public void setGesamthm(double gesamthm) {
		this.gesamthm = gesamthm;
	}

	/**
	 * erzeuge eine List für den GPS-Track (liegt im GPX-Format vor)
	 */
	public LoadGPXFile(){		
		track = new ArrayList<TrkPt>();
		startzeit = Calendar.getInstance();
	}
    
	/**
	 * GPS-Daten einlesen aus GPX-Datei bzw. TCX-Datei
	 * @param dateiname Dateiname GPX/TCX-Datei
	 * @param averaging Averagingfiler ein/aus
	 * @return Dateiname
	 */
	public String loadGPS(String dateiname, boolean averaging) {		
		// GPX (XML) Datei parsen und DOM-Objekt erzeugen
		dateiname = parseXmlFile(dateiname);
		
		// alle Trackpoints einlesen und gesamten Track aufbauen
		if (dateiname.toLowerCase().endsWith("tcx")) // Garmin TCX-Format ?
			parseDocument(averaging, "Trackpoint");
		else
			parseDocument(averaging, "trkpt");
	
		if (averaging) 
			calcDynSteigungsWerte();
		
		return (dateiname);		
	}
	
	/**
	 * GPX/TCX-(XML)-Datei mittels DOM API parsen und DOM-Objekt erzeugen.
	 * @param dateiname  Dateiname
	 * @return dateiname
	 *
	 */
	private String parseXmlFile(String dateiname){
		File f;
		URL  u;
		// hole factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {			
			// mittels factory instance den document builder erzeugen
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			// DOM Representation des XML file erzeugen 
			f = new File(dateiname); 
			if (f.exists()) {
				u = f.toURI().toURL();
				dom = db.parse(u.toString());
			} else { // noch auf GPX-Datei prüfen
				dateiname = dateiname.replaceFirst(".tcx", ".gpx");
				f = new File(dateiname); 
				if (f.exists()) {
					Mlog.debug("GPX-Datei: "+dateiname);
					u = f.toURI().toURL();
					dom = db.parse(u.toString());
				} else 
					Messages.errormessage(Messages.getString("loadGPXFile.NO_GPS_DATA")+dateiname+Messages.getString("loadGPXFile.NO_GPS_DATA2"));  				
			}
		} catch (Exception e) {
			Mlog.ex(e);
		}
		return(dateiname);
	}

	/**
	 * Document einlesen und Trackliste aufbauen.
	 * @param averaging    Averagingfilter ein/aus
	 * @param tagname      Bezeichnung im GPX/TCX (XML)
	 */
	private void parseDocument(boolean averaging, String tagname){
		int i;
		int gpsanzahl;
		TrkPt t;
		DecimalFormat zfk2 = new DecimalFormat("0.00");  
		// erzeuge root elememt
		Element docEle = dom.getDocumentElement();
		
		// nodelist der trackpoints (trkpt) erzeugen
		NodeList nl = docEle.getElementsByTagName(tagname); 
		gpsanzahl = nl.getLength();
			
		if(nl != null && gpsanzahl > 0) {
			for(i = 0 ; i < gpsanzahl;i++) {				
				// erzeuge trackpoint element
				Element el = (Element)nl.item(i);
				
				// trackpoint-objekt erzeugen
				if (tagname.compareToIgnoreCase("trackpoint") == 0)	// Garmin TCX-Format ?
					t = getTrackpoint(i, el, averaging, true, "LatitudeDegrees", "LongitudeDegrees", "AltitudeMeters", "Time");
				else // GPX-Format ?
					t = getTrackpoint(i, el, averaging, false, "lat", "lon", "ele", "time");

				// hinzufügen zur List
				track.add(t);
			}
			gesamtsek = track.get(--i).getAnzsek();
			gesamtstrecke = track.get(--i).getAbstand_m();
			Mlog.info("Tourdauer:"+gesamtsek+" Sek.");  
			Mlog.info("Tourlänge:"+zfk2.format(gesamtstrecke/1000)+" km");
		}
	}

	/**
	 * Ist die Glättung der Höhendaten aktiviert, dann wird hier die Sache etwas dynamisiert:
	 * Das Problem: Bei der normalen Mittelwertbildung vom aktuellen Punkt zum Vorgänger geht die Leistung an Kuppen (analog Senken)
	 * etwas verzögert hoch bzw. runter. Deshalb wird nun (in einem Loop über alle GPS-Pukte) am akt. Punkt die Steigung des folgenden 
	 * Punktes analysiert und bei überschreiten eines Grenzwertes der vorhandene Mittelwert überschrieben.
	 * Abhängig vom überschreiten von einem Grenzwert (maxSteigung z.B. 8%) wird nicht der Mittelwert zum
	 * Vorgänger sondern der Mittelwert von aktuellem Mittelwert, Steigung des akt. Punktes und Steigung beim Nachfolgepunkt berechnet.
	 */
	private void calcDynSteigungsWerte() {
		int    aktindex;
		double aktSteig, aktSteigNf1, neuSteigAv, aktSteigAv;
		for (TrkPt aktTrkPt : track) {
			aktindex = (int) aktTrkPt.getIndex();
			if (aktindex < track.size() - 1) {								// für den letzten Punkt können wir es nicht berechnen!
				aktSteig = aktTrkPt.getSteigung_proz();
				aktSteigNf1 = track.get(aktindex+1).getSteigung_proz();		// Steigung nächster GPS-Punkt
				aktSteigAv = aktTrkPt.getSteigungAv_proz();					// aktueller Mittelwert
				if ((Math.abs(aktSteig - aktSteigNf1) > maxSteigung)) {
					neuSteigAv = (aktSteigAv +  aktSteig + aktSteigNf1) / 3.0;
					//Mlog.debug("Dyn. Steigung: setze akt. Punkt: " + aktindex + " von " + aktSteigAv + " auf " + neuSteigAv);
					aktTrkPt.setSteigungAv_proz(neuSteigAv);
				}
			}			
		}		
	}
	
	/**
	 * Trackpointwerte einlesen, zusätzliche Werte berechnen
	 * und zurückgeben. In den t-Variablen werden die letzten Werte
	 * gespeichert. Es wird ein neuer Trackpunkt für die Liste angelegt.
	 * @param index     Index des Trackpunktes (1..n)
	 * @param trkpt1    Trackpunkt aus GPX-Datei
	 * @param averaging Averagingfiler ein/aus
	 * @param tcxflag   TCX-Datei (false: GPX-Datei)
	 * @param tagLat    Bezeichnung im GPX/TCX (XML) für Latitude
	 * @param tagLon    Bezeichnung im GPX/TCX (XML) für Longitude
	 * @param tagHoehe  Bezeichnung im GPX/TCX (XML) für Höhe
	 * @param tagZeit   Bezeichnung im GPX/TCX (XML) für Zeit
	 * @return Trackpunkt
	 */
	private TrkPt getTrackpoint(int index, Element trkpt1, boolean averaging, boolean tcxflag, String tagLat, String tagLon, String tagHoehe, String tagZeit) {
		double lat;
		double lon;
		double puls = 0.0;
		double leistung = 0.0;
		double rpm = 0.0;
		
		if (tcxflag) {	// bei TCX direkt aus den Tags holen
			lat = getNumberValue(trkpt1, tagLat); 	// <LatitudeDegrees>
			lon = getNumberValue(trkpt1, tagLon); 	// <LongitudeDegrees>	
			Element HREle = dom.getDocumentElement();			
			// nodelist der trackpoints (trkpt) erzeugen
			NodeList nl = HREle.getElementsByTagName("HeartRateBpm");			
			Element el = (Element)nl.item(index);
			puls = getNumberValue(el, "Value");
			rpm = getNumberValue(trkpt1, "Cadence");
			leistung = getNumberValue(trkpt1, "Watts");
			if (!gpsleistung && (leistung > 0))
				gpsleistung = true;
		}
		else { // bei GPX aus den Tag-Werten holen
			lat = getNumberTagValue(trkpt1, tagLat); 
			lon = getNumberTagValue(trkpt1, tagLon); 
			puls = getNumberValue(trkpt1, "gpxtpx:hr"); 
			if (puls == 0.0)
				puls = getNumberValue(trkpt1, "gpxx:hr"); 
			leistung = getNumberValue(trkpt1, "watt");
			if (!gpsleistung && (leistung > 0))
				gpsleistung = true;
		}
		double hoehe = getNumberValue(trkpt1, tagHoehe); 	// "ele"
		long anzsek = getSekValue(index, trkpt1, tagZeit); // "time"

		// neuen Trackpunkt erzeugen und Startwerte übergeben
		TrkPt tpkt = new TrkPt(lat,lon,hoehe,anzsek);
		
		// weitere Werte des Trackpunktes berechnen:
		tpkt.setAktsek(calcAktsek(tpkt, tsek));
		double abstand = calcAbstand(tpkt, tlatitude, tlongitude);
		tpkt.setAbstvorg_m(abstand);
		tpkt.setAbstand_m(abstand+tabstand);
		if (index > 0) {
			double hm = getGesamthm();
			if (hoehe > thoehe) {
				hm += hoehe - thoehe;
			}
			setGesamthm(hm);
			tpkt.setHm_m(hm);				
		}
		tpkt.setKurs(kurs);
		tpkt.setV_kmh(calcV_kmh(tpkt));
		tpkt.setSteigung_proz(calcSteigung(tpkt, thoehe, tsteigung, false));
		tpkt.setSteigungAv_proz(calcSteigung(tpkt, thoehe, tsteigung, true));
		tpkt.setIndex(index+1);
		tpkt.setPuls(puls);
		tpkt.setLeistung(leistung);
		tpkt.setRpm(rpm);
		
		// "Zwischenwerte" setzen für Berechnung von: aktsek, v_kmh und abstvorg_m
		tsek = anzsek;
		tlatitude = lat;
		tlongitude = lon;
		thoehe = hoehe;
		tabstand = tpkt.getAbstand_m();
		tsteigung = tpkt.getSteigung_proz();
		return tpkt;
	}

    
	/**
	 * Textinhalt wird für xml element und tag-name  ermittelt und zurück-
	 * gegeben.
	 * z.B. <trkpt><time>2006-05-03T16:21:06Z</time></trkpt> 
	 * gibt den String "2006-05-03T16:21:06Z" zurück.
	 * @param ele       Element
	 * @param tagName   Tag im XML
	 * @return String   Wert als Text
	 */
	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
		return textVal;
	}

	/**
	 * Attribut wird für xml element und tag-name  ermittelt und zurück-
	 * gegeben.
	 * z.B. <trkpt lat="49.562008381" lon="11.346344948"></trkpt> 
	 * gibt für ele trkpt (tagName="Lat") den String "49.562008381" zurück.
	 * @param ele      XML-Element
	 * @param tagName  tagName
	 * @return Inhalt als String
	 */
	private String getTextTagValue(Element ele, String tagName) {
		String textVal = ele.getAttribute(tagName);
		return textVal;
	}

	
	/**
	 * Ruft getTextValue auf und gibt Anzahl Sekunden (seit Start) als long zurück. 
	 * Das Datum wird in folg. Format erwartet:
	 * <time>2006-05-03T19:38:05Z</time>
	 * Beim ersten Aufruf wird die globale Variable startzeit gesetzt.
	 * @param index   Index
	 * @param ele     Element
	 * @param tagName XML-Tag
	 * @return (long) Anzahl der Sekunden
	 */
	private long getSekValue(int index, Element ele, String tagName) {
		int jahr = 0, monat = 0, tag = 0, stunde = 0, minute = 0, sekunde = 0;
		String strzeit = getTextValue(ele,tagName);
		try {
			jahr    = Integer.parseInt(strzeit.substring(0,4));
			monat   = Integer.parseInt(strzeit.substring(5,7));
			tag     = Integer.parseInt(strzeit.substring(8,10));
			stunde  = Integer.parseInt(strzeit.substring(11,13));
			minute  = Integer.parseInt(strzeit.substring(14,16));
			sekunde = Integer.parseInt(strzeit.substring(17,19));			
			} catch (Exception E){
				Mlog.error("Datum konnte nicht ermittelt werden!"); 
			}
		Calendar zeit = Calendar.getInstance();
		zeit.set(jahr, monat, tag, stunde, minute, sekunde);
		if (index == 0) {
			startzeit.set(jahr, monat, tag, stunde, minute, sekunde);
		}
		return((zeit.getTimeInMillis() - startzeit.getTimeInMillis()) / 1000);
	}

	/**
	 * Ruft getTextValue auf und gibt Wert als double zurück
	 * @param ele      Element
	 * @param tagName  Name des XML-Tags
	 * @return (double) Wert
	 */
	private double getNumberValue(Element ele, String tagName) {
		String textvalue;
		Double ret = 0.0;
		try {
			textvalue = getTextValue(ele,tagName);
			if (textvalue != null)
				ret = Double.parseDouble(textvalue);
		} catch (Exception e) {
			Mlog.ex(e);
		}
		return (ret);		
	}

	/**
	 * Ruft getTextTagValue auf und gibt Wert als double zurück
	 * @param ele      Element
	 * @param tagName  XML-Tag
	 * @return (double) Wert  
	 */
	private double getNumberTagValue(Element ele, String tagName) {
		String textvalue;
		Double ret = 0.0;
		try {
			textvalue = getTextTagValue(ele,tagName);
			if (textvalue != null && textvalue != "")
				ret = Double.parseDouble(textvalue);
		} catch (Exception e) {
			Mlog.ex(e);
		}
		return (ret);
	}

	/**
	 * Berechnung der Sekunden vom vorherigen zum aktuellen Punkt.
	 * @param tp    Trackpunkt
	 * @param ts    ts
	 * @return Sekunden
	 */
	private long calcAktsek(TrkPt tp, long ts){
		return(tp.getAnzsek() - ts);
	}
	
	/**
	 * Berechnung des Abstandes vom vorherigen zum aktuellen Punkt.
	 * @param tp     Trackpunkt
	 * @param vlat   Längengrad vorheriger Punkt
	 * @param vlon   Breitengrad vorheriger Punkt
	 * @return abstand
	 */
	private double calcAbstand(TrkPt tp, double vlat, double vlon){
		if (vlat == 0)
			return 0.0;
		else
			return(calcOrthodrome(vlat, tp.getLatitude(), vlon, tp.getLongitude()));
	}

	/**
	 * Die eigentliche Abstandsberechnung.
	 * Berechnungsformel Abstand: siehe: http://de.wikipedia.org/wiki/Orthodrome
	 * Kurswinkelberechnung: http://uafr.freeshell.org/fh/diplomarbeit/html/node64.html:
	 * 
	 * @param b1  Breitengrad 1
	 * @param b2  Breitengrad 2
	 * @param l1  Längengrad 1
	 * @param l2  Längengrad 2
	 * @return Abstand im Metern
	 */
	private double calcOrthodrome(double b1, double b2, double l1, double l2){
		double F;
		double G;
		double l;
		double S;
		double C;
		double w;
		double D;
		double a;
		double R;
		double H1;
		double H2;
		double abst = 0.0;
		double f;
		//double e;
		double nh;
		double cosw = 0.0;
		
		f = 1.0 / 298.257223563;
		a = 6378137.0;
		F = (b1+b2)/2.0;
		F = Math.PI*F/180.0;
		G = (b1-b2)/2.0;
		G = Math.PI*G/180.0;
		l = (l1-l2)/2.0;
		l = Math.PI*l/180.0;
		S = (Math.pow(Math.sin(G), 2) * Math.pow(Math.cos(l), 2)) + (Math.pow(Math.cos(F), 2) * Math.pow(Math.sin(l), 2));
		C = (Math.pow(Math.cos(G), 2) * Math.pow(Math.cos(l), 2)) + (Math.pow(Math.sin(F), 2) * Math.pow(Math.sin(l), 2));
		w = Math.atan(Math.sqrt(S/C));
		D = 2 * w * a;
		R = Math.sqrt(S*C) / w;
		H1 = (3 * R - 1.0) / (2 * C);
		H2 = (3 * R + 1.0) / (2 * S);
		abst = D * (1+f*H1*Math.pow(Math.sin(F), 2)*Math.pow(Math.cos(G), 2) - f*H2*Math.pow(Math.cos(F), 2)*Math.pow(Math.sin(G), 2));
		nh = Math.sqrt(Math.pow(l2-l1,2)+Math.pow(b2-b1, 2));
		if (nh != 0.0)
			cosw = (b2-b1) / nh; 
		kurs = Math.acos(cosw) * 180.0 / Math.PI;
		if (l1 > l2)
			kurs = 360 - kurs;
		
		if (Double.isNaN(abst))
			abst = 0.0;
		entfernung = abst;
		
		return(abst);
	}

	/**
	 * Berechnung der aktuellen Geschwindigkeit:
	 * v = strecke / zeit (umgerechnet in km/h)
	 * @param tp Trackpunkt
	 * @return Geschwindigkeit in km/h
	 */
	private double calcV_kmh(TrkPt tp){
		if (tp.getAbstvorg_m() == 0.0 || tp.getAktsek() == 0)
			return 0.0;
		
		double v = tp.getAbstvorg_m() / tp.getAktsek() * 3.6;  // Umrechnung in km/h
		return v;
	}
	
	/**
	 * Berechnung der aktuellen Steigung in Prozent nach folg. Formel:
	 * Steigung = Höhendifferenz / Abstand (*100 - da Rückgabe in Prozent)
	 * @param tp        Trackpunkt
	 * @param vhoehe    Höhe des vorherigen Trackpunktes
	 * @param vsteigung Steigung des letzten Trackpunktes (für Averaging)
	 * @param averaging Averagingfilter ein-/ausschalten
	 * @return          Steigung in Prozent
	 */
	private double calcSteigung(TrkPt tp, double vhoehe, double vsteigung, boolean averaging){
		if (tp.getAbstvorg_m() <= 0.5) // Wenn kein Abstand zum Vorgänger ist die Steigung auch 0! (akt. Schranke: 50 cm)
			return 0.0;
		
		double st = ((tp.getHoehe() - vhoehe) / tp.getAbstvorg_m()) * 100;

		if (averaging && (vsteigung != 0))
			st = (st + vsteigung) / 2.0;   // Averaging = Mittelwertberechnung von letzter und dieser Steigung
		
		return st;
	}
}
