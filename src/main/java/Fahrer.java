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
 * Fahrer.java: Beinhaltet Daten von Biker und Bike
 *****************************************************************************
 * 
 * Diese Daten werden über Konfigurationsdialog einstellbar gemacht. Im ersten Schritt
 * werden Konstanten gesetzt.
 *
 */

public class Fahrer {
	private double fahrergewicht = 80.0;	// Fahrergewicht für Leistungsberechnung
	private double bikegewicht = 13.0;	    // Bikegewicht für Leistungsberechnung (Fully)
	private double maxleistung = 350.0;     // maximale Leistung, die vorgegeben werden darf
	private double minleistung = 80.0;      // minimale Leistung, die vorgegeben wird.
	private double cwa = 0.35;	            // Oberfläche und cw-Wert - geschätzt
	private double k2 = 0.01;	            // Konstante für Rollreibung
	private double alter = 32.0;            // Alter des Fahrers (für Herzfrequenzbereiche)
	private double maxpuls = 160.0;         // maximaler Puls, wenn überschritten, dann wird min. Leistung vorgegeben.
	private String name = new String("Standard");	// Name des Fahrers (für versch. Profile)
    private boolean changed = false;
    private double dynRPMNormal = 90.0;		// Dynamik-Modus: Normale Trittfrequenz für 100% Videogeschwindigkeit
    private double dynRPMWiege = 45.0;		// Dynamik-Modus: Wiegetrittfrequenz für 100% Videogeschwindigkeit
    private double lfakt = 1.0;             // Leistungsfaktor
    private double maxSteigung = 20.0;		// max. Steigung bei Rollentrainerbetrieb
    private double minSteigung = -20.0;		// min. Steigung bei Rollentrainerbetrieb um Grundlast zu erzeugen
    private double pulsrot = 0.0;			// ab dem Wert wird der Puls rot hinterlegt Std.: (220 - Alter) * 0.8
    private double pulsgelb = 0.0;			// ab dem Wert wird der Puls gelb hinterlegt Std.: (220 - Alter) * 0.7
    private double pulsgruen = 0.0;			// ab dem Wert wird der Puls gruen hinterlegt Std.: (220 - Alter) * 0.55
    private double rpmrot = 40.0;			// Ab dem Wert ist die RPM-Ampel rot
    private double rpmgelb = 60.0;			// Ab dem Wert ist die RPM-Ampel gelb
    private double rpmgruen = 80.0;			// Ab dem Wert ist die RPM-Ampel grün
    private double prot = 300.0;			// Ab dem Wert ist die Watt-Ampel rot
    private double pgelb = 200.0;			// Ab dem Wert ist die Watt-Ampel gelb
    private double pgruen = 80.0;			// Ab dem Wert ist die Watt-Ampel grün
    private long   gesTrainingszeit = 0;	// Gesamtwerte für Auswertung
    private long   gesAnzahl = 0;
    private double gesStrecke = 0.0;
    private double gesHM = 0.0;
    private double gesKCal = 0.0;
//    private double gesFitnesswert = 0.0;
    private double gesPuls = 0.0;
    private double gesRPM = 0.0;
    private double gesLeistung = 0.0;
    private double automatik1 = 120.0;		// untere Schaltschwelle bei Gangautomatik
    private double automatik2 = 280.0;		// obere Schaltschwelle bei Gangautomatik
    private boolean automatik = false;		// Gangautomatik
    private int    belohnungindex = 0;		// Standardbelohnung: Apfelkuchen
    
    /**
	 * @return the maxSteigung
	 */
	public double getMaxSteigung() {
		return maxSteigung;
	}

	/**
	 * @param maxSteigung the maxSteigung to set
	 */
	public void setMaxSteigung(double maxSteigung) {
		this.maxSteigung = maxSteigung;
	}

	/**
	 * @return the minSteigung
	 */
	public double getMinSteigung() {
		return minSteigung;
	}

	/**
	 * @param minSteigung the minSteigung to set
	 */
	public void setMinSteigung(double minSteigung) {
		this.minSteigung = minSteigung;
	}

	/**
	 * @return the lfakt
	 */
	public double getLfakt() {
		return lfakt;
	}

	/**
	 * @param lfakt the lfakt to set
	 */
	public void setLfakt(double lfakt) {
		this.lfakt = lfakt;
	}

	/**
     * Getter für normale Trittfrequenz im Dynamik-Modus
	 * @return dynRPMNormal
	 */
	public double getDynRPMNormal() {
		return dynRPMNormal;
	}

	/**
     * Setter für normale Trittfrequenz im Dynamik-Modus
	 * @param dynRPMNormal Trittfrequenz für "Normalgeschwindigkeit" (z.B. 80 = 100% Videogeschwindigkeit)
	 */
	public void setDynRPMNormal(double dynRPMNormal) {
		this.dynRPMNormal = dynRPMNormal;
	}

	/**
     * Getter für Wiegetrittfrequenz im Dynamik-Modus
	 * @return dynRPMWiege
	 */
	public double getDynRPMWiege() {
		return dynRPMWiege;
	}

	/**
     * Setter für Wiegetrittfrequenz im Dynamik-Modus
	 * @param dynRPMWiege Trittfrequenz für "Wiegetrittgeschwindigkeit" (z.B. 40 = 100% Videogeschwindigkeit-wenn aktiv)
	 */
	public void setDynRPMWiege(double dynRPMWiege) {
		this.dynRPMWiege = dynRPMWiege;
	}

	/**
     * Getter für maximal zulässigen Puls des Fahrers
     * @return maxpuls
     */
    public double getMaxpuls() {
		return maxpuls;
	}

    /**
     * Setter für maximal zulässigen Puls.
     * @param maxpuls	max. Puls (wenn überschritten geht Leistung auf Minimum)
     */
	public void setMaxpuls(double maxpuls) {
		this.maxpuls = maxpuls;
	}

    /**
     * Liefert changed-Flag für Fahrerkonfiguration
     * @return changed
     */
	public boolean isChanged() {
		return changed;
	}
	
	/**
	 * Setzt changed-Flag für Fahrerkonfiguration
	 * @param changed Konfiguration wurde geändert
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	/**
	 * Getter für Fahrername
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Setter für Fahrername
	 * @param name  Name des Fahrers
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Getter für Alter des Fahrers
	 * @return alter
	 */
	public double getAlter() {
		return alter;
	}
	
	/**
	 * Setter für Alter des Fahrers
	 * @param alter Alter des Fahrers
	 * TODO: besser später Geburtsdatum verwenden, muss nicht angepasst werden!
	 */
	public void setAlter(double alter) {
		this.alter = alter;
	}
	
	/**
	 * Getter für cw-Wert
	 * @return cwa
	 */
	public double getCwa() {
		return cwa;
	}
	
	/**
	 * Setter für cw-Wert
	 * @param cwa  CW-Wert für Gesamtwindwiderstand
	 */
	public void setCwa(double cwa) {
		this.cwa = cwa;
	}
	
	/**
	 * Getter für Fahrergewicht
	 * @return fahrergewicht
	 */
	public double getFahrergewicht() {
		return fahrergewicht;
	}
	
	/**
	 * Setter für Fahrergewicht
	 * @param fahrergewicht  Gewicht des Fahrers
	 */
	public void setFahrergewicht(double fahrergewicht) {
		this.fahrergewicht = fahrergewicht;
	}
	
	/**
	 * Getter für Bikegewicht
	 * @return Bikeegewicht
	 */
	public double getBikegewicht() {
		return bikegewicht;
	}
	
	/**
	 * Setter für Bikegewicht
	 * @param bikegewicht  Gewicht des Bikes
	 */
	public void setBikegewicht(double bikegewicht) {
		this.bikegewicht = bikegewicht;
	}
	
	/**
	 * Getter für k2-Wert
	 * @return k2
	 */
	public double getK2() {
		return k2;
	}
	
	/**
	 * Setter für k2-Wert
	 * @param k2  Konstante für Leistungs Berechnung
	 */
	public void setK2(double k2) {
		this.k2 = k2;
	}
	
	/**
	 * Getter für maximal vorgegebene Leistung
	 * @return maxleistung
	 */
	public double getMaxleistung() {
		return maxleistung;
	}
	
	/**
	 * Setter für Maximal vorgegebene Leistung
	 * @param maxleistung  max. Leistung die Trainingsgerät vorgegeben wird
	 */
	public void setMaxleistung(double maxleistung) {
		this.maxleistung = maxleistung;
	}
	
	/**
	 * Getter für minimal vorgegebene Leistung
	 * @return minleistung
	 */
	public double getMinleistung() {
		return minleistung;
	}
	
	/**
	 * Setter für minimal vorgegebene Leistung
	 * @param minleistung  max. Leistung die Trainingsgerät vorgegeben wird
	 */
	public void setMinleistung(double minleistung) {
		this.minleistung = minleistung;
	}

	/**
	 * @return the pulsrot
	 */
	public double getPulsrot() {
		if (pulsrot == 0.0)
			pulsrot = (220 - alter) * 0.8;
		return pulsrot;
	}

	/**
	 * @param pulsrot the pulsrot to set
	 */
	public void setPulsrot(double pulsrot) {
		this.pulsrot = pulsrot;
	}

	/**
	 * @return the pulsgelb
	 */
	public double getPulsgelb() {
		if (pulsgelb == 0.0)
			pulsgelb = (220 - alter) * 0.7;
		return pulsgelb;
	}

	/**
	 * @param pulsgelb the pulsgelb to set
	 */
	public void setPulsgelb(double pulsgelb) {
		this.pulsgelb = pulsgelb;
	}

	/**
	 * @return the pulsgruen
	 */
	public double getPulsgruen() {
		if (pulsgruen == 0.0)
			pulsgruen = (220 - alter) * 0.55;
		return pulsgruen;
	}

	/**
	 * @param pulsgruen the pulsgruen to set
	 */
	public void setPulsgruen(double pulsgruen) {
		this.pulsgruen = pulsgruen;
	}

	/**
	 * @return the rpmrot
	 */
	public double getRpmrot() {
		return rpmrot;
	}

	/**
	 * @param rpmrot the rpmrot to set
	 */
	public void setRpmrot(double rpmrot) {
		this.rpmrot = rpmrot;
	}

	/**
	 * @return the rpmgelb
	 */
	public double getRpmgelb() {
		return rpmgelb;
	}

	/**
	 * @param rpmgelb the rpmgelb to set
	 */
	public void setRpmgelb(double rpmgelb) {
		this.rpmgelb = rpmgelb;
	}

	/**
	 * @return the rpmgruen
	 */
	public double getRpmgruen() {
		return rpmgruen;
	}

	/**
	 * @param rpmgruen the rpmgruen to set
	 */
	public void setRpmgruen(double rpmgruen) {
		this.rpmgruen = rpmgruen;
	}

	/**
	 * @return the prot
	 */
	public double getProt() {
		return prot;
	}

	/**
	 * @param prot the prot to set
	 */
	public void setProt(double prot) {
		this.prot = prot;
	}

	/**
	 * @return the pgelb
	 */
	public double getPgelb() {
		return pgelb;
	}

	/**
	 * @param pgelb the pgelb to set
	 */
	public void setPgelb(double pgelb) {
		this.pgelb = pgelb;
	}

	/**
	 * @return the pgruen
	 */
	public double getPgruen() {
		return pgruen;
	}

	/**
	 * @param pgruen the pgruen to set
	 */
	public void setPgruen(double pgruen) {
		this.pgruen = pgruen;
	}

	/**
	 * @return the gesTreainingszeit
	 */
	public long getGesTrainingszeit() {
		return gesTrainingszeit;
	}

	/**
	 * @param gesTrainingszeit setze Gesamttrainingszeit
	 */
	public void setGesTrainingszeit(long gesTrainingszeit) {
		this.gesTrainingszeit = gesTrainingszeit;
	}

	/**
	 * @return the gesAnzahl
	 */
	public long getGesAnzahl() {
		return gesAnzahl;
	}

	/**
	 * @param gesAnzahl the gesAnzahl to set
	 */
	public void setGesAnzahl(long gesAnzahl) {
		this.gesAnzahl = gesAnzahl;
	}

	/**
	 * @return the gesStrecke
	 */
	public double getGesStrecke() {
		return gesStrecke;
	}

	/**
	 * @param gesStrecke the gesStrecke to set
	 */
	public void setGesStrecke(double gesStrecke) {
		this.gesStrecke = gesStrecke;
	}

	/**
	 * @return the gesHM
	 */
	public double getGesHM() {
		return gesHM;
	}

	/**
	 * @param gesHM the gesHM to set
	 */
	public void setGesHM(double gesHM) {
		this.gesHM = gesHM;
	}

	/**
	 * @return the gesKCal
	 */
	public double getGesKCal() {
		return gesKCal;
	}

	/**
	 * @param gesKCal the gesKCal to set
	 */
	public void setGesKCal(double gesKCal) {
		this.gesKCal = gesKCal;
	}

	/**
	 * @return the gesFitnesswert
	 */
	/*
	public double getGesFitnesswert() {
		return gesFitnesswert;
	}
	*/
	/**
	 * @param gesFitnesswert the gesFitnesswert to set
	 */
	/*
	public void setGesFitnesswert(double gesFitnesswert) {
		this.gesFitnesswert = gesFitnesswert;
	}
	*/
	/**
	 * @return the gesPulsSchnitt
	 */
	public double getGesPuls() {
		return gesPuls;
	}

	/**
	 * @param gesPuls setze gesPuls
	 */
	public void setGesPuls(double gesPuls) {
		this.gesPuls = gesPuls;
	}

	/**
	 * @return the gesRPM
	 */
	public double getGesRPM() {
		return gesRPM;
	}

	/**
	 * @param gesRPM the gesRPM to set
	 */
	public void setGesRPM(double gesRPM) {
		this.gesRPM = gesRPM;
	}

	/**
	 * @return the gesLeistung
	 */
	public double getGesLeistung() {
		return gesLeistung;
	}

	/**
	 * @param gesLeistung the gesLeistung to set
	 */
	public void setGesLeistung(double gesLeistung) {
		this.gesLeistung = gesLeistung;
	}

	/**
	 * @return unterer Watt-Schwellwert der Gangautomatik
	 */
	public double getAutomatik1() {
		return automatik1;
	}

	/**
	 * @param automatik1 unterer Watt-Schwellwert der Gangautomatik
	 */
	public void setAutomatik1(double automatik1) {
		this.automatik1 = automatik1;
	}

	/**
	 * @return oberer Watt-Schwellwert der Gangautomatik
	 */
	public double getAutomatik2() {
		return automatik2;
	}

	/**
	 * @param automatik2 oberer Watt-Schwellwert der Gangautomatik
	 */
	public void setAutomatik2(double automatik2) {
		this.automatik2 = automatik2;
	}

	/**
	 * @return Gangautomatik gesetzt?
	 */
	public boolean isAutomatik() {
		return automatik;
	}

	/**
	 * @param automatik Gangautomatik ein/aus
	 */
	public void setAutomatik(boolean automatik) {
		this.automatik = automatik;
	}

	/**
	 * @return the belohnungindex
	 */
	public int getBelohnungindex() {
		return belohnungindex;
	}

	/**
	 * @param belohnungindex the belohnungindex to set
	 */
	public void setBelohnungindex(int belohnungindex) {
		this.belohnungindex = belohnungindex;
	}
}
