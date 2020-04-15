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
 * trkpt.java: Datenformat eines Trackpunktes der Trackliste
 *****************************************************************************
 *
 * Diese Klasse beinhaltet die Koordinaten eines Trackpunktes und zusätzliche
 * berechnete Daten von RoomSports.
 *  
 */

public class TrkPt {
	private long     index;               // 1..n
	private double   latitude;            // Breitengrad			
	private double   longitude;           // Längengrad
	private double   hoehe;               // Höhe über NN
	private long     anzsek;              // Sekunden seit erstem Punkt 
	private long     aktsek;              // Sekunden seit letztem Punkt 
	private double   v_kmh;               // berechnete aktuelle Geschwindigkeit
	private double   abstand_m;           // Abstand zum ersten Punkt in Metern
	private double   abstvorg_m;          // Abstand zum Punkt vorher in Metern
	private double   steigung_proz;       // Steigung in Prozent
	private double   steigungAv_proz;     // Steigung in Prozent geglättet
	private double   kurs;                // aktueller Kurswinkel
	private double   puls;                // akt. Puls aus TCX-Datei
	private double   leistung;            // akt. Leistung aus TCX-Datei
	private double   rpm;                 // akt. Kurbelumdrehungen aus TCX-Datei
	private double   hm_m;                // Höhenmeter seit erstem Punkt
	private long     zeitpunkt;           // Zeitpunkt (in Millisek. seit 1.1.1970)
	
	/**
	 * @return the zeitpunkt
	 */
	public long getZeitpunkt() {
		return zeitpunkt;
	}

	/**
	 * @param zeitpunkt the zeitpunkt to set
	 */
	public void setZeitpunkt(long zeitpunkt) {
		this.zeitpunkt = zeitpunkt;
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
	 * Getterfunktion für die Steigung in %
	 * @return Steigung
	 */
	public double getSteigung_proz() {
		return steigung_proz;
	}

	/**
	 * Setterfunktion für die Steigung in %
	 * @param steigung_proz		Steigung in Prozent
	 */
	public void setSteigung_proz(double steigung_proz) {
		this.steigung_proz = steigung_proz;
	}

	/**
	 * Getterfunktion für die gemittelte Steigung in %
	 * @return Steigung	(gemittelte Steigung in %)
	 */
	public double getSteigungAv_proz() {
		return steigungAv_proz;
	}

	/**
	 * Setterfunktion für die gemittelte Steigung in %
	 * @param steigung_proz		Steigung in Prozent
	 */
	public void setSteigungAv_proz(double steigung_proz) {
		this.steigungAv_proz = steigung_proz;
	}

	/**
	 * Getterfunktion für den Abstand zum Vorgänger
	 * @return abstvorg_m 
	 */
	public double getAbstvorg_m() {
		return abstvorg_m;
	}

	/**
	 * Setterfunktion für den Abstand zum Vorgänger
	 * @param abstvorg_m	Abstand zum Vorgängerpunkt
	 */
	public void setAbstvorg_m(double abstvorg_m) {
		this.abstvorg_m = abstvorg_m;
	}

	/**
	 * Getterfunktion für die aktuelle Geschwindigkeit
	 * @return v_kmh
	 */
	public double getV_kmh() {
		return v_kmh;
	}
	
	/**
	 * Setterfunktion für die aktuelle Geschwindigkeit
	 * @param v_kmh		akt. Geschwindigkeit in km/h
	 */
	public void setV_kmh(double v_kmh) {
		this.v_kmh = v_kmh;
	}

	/**
	 * Getterfunktion für die aktuelle Höhe
	 * @return hoehe
	 */
	public double getHoehe() {
		return hoehe;
	}

	/**
	 * Setterfunktion für die aktuelle Höhe
	 * @param hoehe		akt. Höhe
	 */
	public void setHoehe(double hoehe) {
		this.hoehe = hoehe;
	}

	/**
	 * Getterfunktion für Latitude (Breitengrad)
	 * @return latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Setterfunktion für Latitude (Breitengrad)
	 * @param latitude	Breitengrad
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Getterfunktion für Longitude (Längengrad)
	 * @return longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Setterfunktion für Longitude (Längengrad)
	 * @param longitude		Längengrad
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * Getterfunktion für die Anzahl Sekunden seit Start des Videos
	 * @return anzsek
	 */
	public long getAnzsek() {
		return anzsek;
	}

	/**
	 * Setterfunktion für die Anzahl Sekunden seit Start des Videos
	 * @param anzsek	Anzahl Sekunden seit Start des Videos
	 */
	public void setAnzsek(long anzsek) {
		this.anzsek = anzsek;
	}

	/**
	 * Getterfunktion für die Anzahl Sekunden seit dem letzten Punkt
	 * @return aktsek
	 */
	public long getAktsek() {
		return aktsek;
	}
	
	/**
	 * Setterfunktion für die Anzahl Sekunden seit dem letzten Punkt
	 * @param aktsek	Anzahl Sekunden seit Start des Videos
	 */
	public void setAktsek(long aktsek) {
		this.aktsek = aktsek;
	}
	
	/**
	 * Getterfunktion für Abstand zum 1. Punkt
	 * @return abstand_m
	 */
	public double getAbstand_m() {
		return abstand_m;
	}

	/**
	 * Setterfunktion für Abstand zum 1. Punkt
	 * @param abstand_m		Abstand zum 1. GPS-Punkt in m
	 */
	public void setAbstand_m(double abstand_m) {
		this.abstand_m = abstand_m;
	}

	/**
	 * Getterfunktion für den aktuellen Index
	 * @return index
	 */
	public long getIndex() {
		return index;
	}

	/**
	 * Setterfunktion für den aktuellen Index
	 * @param index		Index des akt. Punktes
	 */
	public void setIndex(long index) {
		this.index = index;
	}

	/**
	 * @return the puls
	 */
	public double getPuls() {
		return puls;
	}

	/**
	 * @param puls the puls to set
	 */
	public void setPuls(double puls) {
		this.puls = puls;
	}

	/**
	 * @return the leistung
	 */
	public double getLeistung() {
		return leistung;
	}

	/**
	 * @param leistung the leistung to set
	 */
	public void setLeistung(double leistung) {
		this.leistung = leistung;
	}

	/**
	 * @return the rpm
	 */
	public double getRpm() {
		return rpm;
	}

	/**
	 * @param rpm the rpm to set
	 */
	public void setRpm(double rpm) {
		this.rpm = rpm;
	}

	/**
	 * @return the hm_m
	 */
	public double getHm_m() {
		return hm_m;
	}

	/**
	 * @param hm_m the hm_m to set
	 */
	public void setHm_m(double hm_m) {
		this.hm_m = hm_m;
	}
	
	/**
	 * Setzt die Koordinaten, Höhe und Zeitpunkt des aktuellen Track-Punktes
	 * @param latitude		Breitengrad als double
	 * @param longitude		Längengrad als double
	 * @param hoehe			Höhe
	 * @param anzsek		anz. Sekunden nach Start
	 */
	public TrkPt(double latitude, double longitude, double hoehe, long anzsek, long zeitpunkt) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.hoehe = hoehe;
		this.anzsek = anzsek;
		this.zeitpunkt = zeitpunkt;
	}
}
