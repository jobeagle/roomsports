
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
 * Gegnerzp.java: Daten des "Gegners" zu einem Zeitpunkt
 *****************************************************************************
 *
 * Diese Klasse beinhaltet die Daten des "Gegners" bei CSV-Rennen zu einem Zeitpunkt. Bzw.
 * einen Satz eines protokollierten Trainings mit Zeit, Ort und aktuellen Trainingsdaten 
 * (Puls, RPM etc.). Beim Einlesen des Trainingsprotokolls wird die Zeit normalisiert
 * auf den Start des Rennens.
 */

public class GegnerZP {
	private long   sekseitstart;
	private long   punkt;
	private int    wind;
	private int    gang;
	private double energie;
	private int    puls;
	private double rpm;
	private double leistung;
	private double geschwindigkeit;
	private double strecke;
	private double steigung;
	private double hoehe;
	private double latitude;
	private double longitude;
	
	/**
	 * @return die Anzahl der Sekunden seit dem Start
	 */
	public long getSekseitStart() {
		return sekseitstart;
	}
	/**
	 * @param sekunden Anzahl Sekunden seit Start setzen
	 */
	public void setSekseitStart(long sekunden) {
		sekseitstart = sekunden;
	}
	/**
	 * @return the punkt
	 */
	public long getPunkt() {
		return punkt;
	}
	/**
	 * @param punkt the punkt to set
	 */
	public void setPunkt(long punkt) {
		this.punkt = punkt;
	}
	/**
	 * @return the wind
	 */
	public int getWind() {
		return wind;
	}
	/**
	 * @param wind the wind to set
	 */
	public void setWind(int wind) {
		this.wind = wind;
	}
	/**
	 * @return the gang
	 */
	public int getGang() {
		return gang;
	}
	/**
	 * @param gang the gang to set
	 */
	public void setGang(int gang) {
		this.gang = gang;
	}
	/**
	 * @return the energie
	 */
	public double getEnergie() {
		return energie;
	}
	/**
	 * @param energie the energie to set
	 */
	public void setEnergie(double energie) {
		this.energie = energie;
	}
	/**
	 * @return the puls
	 */
	public int getPuls() {
		return puls;
	}
	/**
	 * @param puls the puls to set
	 */
	public void setPuls(int puls) {
		this.puls = puls;
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
	 * @return the geschwindigkeit
	 */
	public double getGeschwindigkeit() {
		return geschwindigkeit;
	}
	/**
	 * @param geschwindigkeit the geschwindigkeit to set
	 */
	public void setGeschwindigkeit(double geschwindigkeit) {
		this.geschwindigkeit = geschwindigkeit;
	}
	/**
	 * @return the strecke
	 */
	public double getStrecke() {
		return strecke;
	}
	/**
	 * @param strecke the strecke to set
	 */
	public void setStrecke(double strecke) {
		this.strecke = strecke;
	}
	/**
	 * @return the steigung
	 */
	public double getSteigung() {
		return steigung;
	}
	/**
	 * @param steigung the steigung to set
	 */
	public void setSteigung(double steigung) {
		this.steigung = steigung;
	}
	/**
	 * @return the hoehe
	 */
	public double getHoehe() {
		return hoehe;
	}
	/**
	 * @param hoehe the hoehe to set
	 */
	public void setHoehe(double hoehe) {
		this.hoehe = hoehe;
	}
	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
