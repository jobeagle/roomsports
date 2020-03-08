import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
 * Gegner.java: Daten des "Gegners"
 *****************************************************************************
 *
 * Diese Klasse beinhaltet die Daten des "Gegners" bei CSV-Rennen. 
 */

public class Gegner {
	private String name;
	private double rpmwiege;
	private double rpmnorm;
	private double gewicht;
	private double pmax;
	private double pmin;
	private boolean averaging;
	public List<GegnerZP> gegnerdaten;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the rpmwiege
	 */
	public double getRpmwiege() {
		return rpmwiege;
	}
	/**
	 * @param rpmwiege the rpmwiege to set
	 */
	public void setRpmwiege(double rpmwiege) {
		this.rpmwiege = rpmwiege;
	}
	/**
	 * @return the rpmnorm
	 */
	public double getRpmnorm() {
		return rpmnorm;
	}
	/**
	 * @param rpmnorm the rpmnorm to set
	 */
	public void setRpmnorm(double rpmnorm) {
		this.rpmnorm = rpmnorm;
	}
	/**
	 * @return the gewicht
	 */
	public double getGewicht() {
		return gewicht;
	}
	/**
	 * @param gewicht the gewicht to set
	 */
	public void setGewicht(double gewicht) {
		this.gewicht = gewicht;
	}
	/**
	 * @return the pmax
	 */
	public double getPmax() {
		return pmax;
	}
	/**
	 * @param pmax the pmax to set
	 */
	public void setPmax(double pmax) {
		this.pmax = pmax;
	}
	/**
	 * @return the pmin
	 */
	public double getPmin() {
		return pmin;
	}
	/**
	 * @param pmin the pmin to set
	 */
	public void setPmin(double pmin) {
		this.pmin = pmin;
	}
	/**
	 * @return the averaging
	 */
	public boolean isAveraging() {
		return averaging;
	}
	/**
	 * @param averaging the averaging to set
	 */
	public void setAveraging(boolean averaging) {
		this.averaging = averaging;
	}
	
	/**
	 * Erzeuge eine ArrayList für die CSV-Daten und lese die komplette CSV-Datei ein.
	 * Dabei wird der Zeitpunkt normalisiert auf die Startzeit
	 * @param csvFile  Name der CSV-Datei
	 * @return sind Fehler aufgetreten? 
	 */
	@SuppressWarnings("deprecation")
	public boolean loadCSVFile(String csvFile){		
		gegnerdaten = new ArrayList<GegnerZP>();
		Date startzeit = new Date();
		Date satzzeit = new Date();
		FileReader reader = null;
		BufferedReader in = null;
		boolean datumerkannt = false;
		boolean allesok = false;
		int indsemicol = 0;
		int indsemicol1 = 0;
		int counter = 0;
		
		try 
		{ 
		  reader = new FileReader(csvFile); 
		  in = new BufferedReader(reader);
		  
		  String zeile = null;
		  String feld = null;
		  while ((zeile = in.readLine()) != null) {
			  counter++;
			  indsemicol = zeile.indexOf(';', 0);
			  if (indsemicol > 0) {
				  if (datumerkannt == false) {
					  feld = zeile.substring(0, indsemicol);
					  if (feld.matches("[0-9][0-9]:[0-9][0-9]:[0-9][0-9]")) {  // handelt es sich um eine Uhrzeit?
						  int stunde = Integer.parseInt(feld.substring(0,2));
						  int minute = Integer.parseInt(feld.substring(3,5));
						  int sekunde = Integer.parseInt(feld.substring(6,8));
						  startzeit = new Date(1970, 1, 1, stunde, minute, sekunde);
						  datumerkannt = true;
						  satzzeit = new Date(0);
					  }
				  } else {  // erstes Datumsfeld wurde erkannt, es kommmen Daten...
					  feld = zeile.substring(0, zeile.indexOf(';', 0));
					  if (feld.matches("[0-9][0-9]:[0-9][0-9]:[0-9][0-9]")) {  // handelt es sich um eine Uhrzeit?
						  int stunde = Integer.parseInt(feld.substring(0,2));
						  int minute = Integer.parseInt(feld.substring(3,5));
						  int sekunde = Integer.parseInt(feld.substring(6,8));
						  satzzeit = new Date(1970, 1, 1, stunde, minute, sekunde);
						  satzzeit = new Date(satzzeit.getTime() - startzeit.getTime());
					  }				  
				  }
			  
				  if (datumerkannt) { // dann den Rest der Zeile einlesen:
					  GegnerZP gzp = new GegnerZP();
					  // zuerst die normalisierte Zeit eintragen in das gegnerzp-objekt
					  gzp.setSekseitStart(satzzeit.getTime()/1000);
					  // dann akt. Punkt einlesen
					  indsemicol = zeile.indexOf(';', 9);
					  feld = zeile.substring(9, indsemicol);
					  gzp.setPunkt(Long.parseLong(feld));
					  indsemicol1 = zeile.indexOf(';', indsemicol+1);
					  // Wind erstmal überspringen...
					  indsemicol = zeile.indexOf(';', indsemicol1+1);
					  feld = zeile.substring(indsemicol1+1, indsemicol);
					  gzp.setGang(Integer.parseInt(feld));
					  // Energie einlesen...
					  indsemicol1 = zeile.indexOf(';', indsemicol+1);
					  feld = zeile.substring(indsemicol+1, indsemicol1);
					  gzp.setEnergie(Double.parseDouble(feld.replace(',', '.')));
					  // Puls einlesen...
					  indsemicol = zeile.indexOf(';', indsemicol1+1);
					  feld = zeile.substring(indsemicol1+1, indsemicol);
					  gzp.setPuls(Integer.parseInt(feld));
					  // RPM einlesen...
					  indsemicol1 = zeile.indexOf(';', indsemicol+1);
					  feld = zeile.substring(indsemicol+1, indsemicol1);
					  gzp.setRpm(Double.parseDouble(feld.replace(',', '.')));
					  // Leistung einlesen...
					  indsemicol = zeile.indexOf(';', indsemicol1+1);
					  feld = zeile.substring(indsemicol1+1, indsemicol);
					  gzp.setLeistung(Integer.parseInt(feld));
					  // Geschwindigkeit einlesen...
					  indsemicol1 = zeile.indexOf(';', indsemicol+1);
					  feld = zeile.substring(indsemicol+1, indsemicol1);
					  gzp.setGeschwindigkeit(Double.parseDouble(feld.replace(',', '.')));
					  // Strecke einlesen...
					  indsemicol = zeile.indexOf(';', indsemicol1+1);
					  feld = zeile.substring(indsemicol1+1, indsemicol);
					  gzp.setStrecke(Double.parseDouble(feld.replace(',', '.')));
					  // Steigung einlesen...
					  indsemicol1 = zeile.indexOf(';', indsemicol+1);
					  feld = zeile.substring(indsemicol+1, indsemicol1);
					  gzp.setSteigung(Integer.parseInt(feld));
					  // Höhe einlesen...
					  indsemicol = zeile.indexOf(';', indsemicol1+1);
					  feld = zeile.substring(indsemicol1+1, indsemicol);
					  //gzp.setHoehe(Integer.parseInt(feld));
					  gzp.setHoehe(Double.parseDouble(feld.replace(',', '.')));
					  // Breitengrad einlesen...
					  indsemicol1 = zeile.indexOf(';', indsemicol+1);
					  feld = zeile.substring(indsemicol+1, indsemicol1);
					  gzp.setLatitude(Double.parseDouble(feld.replace(',', '.')));
					  // Längengrad einlesen...
					  feld = zeile.substring(indsemicol1+1);
					  gzp.setLongitude(Double.parseDouble(feld.replace(',', '.')));

					  gegnerdaten.add(gzp);
				  }
			  }
		  }
		  allesok = true;
		  Mlog.info("Es wurden "+counter+" Sätze eingelesen.");		  
		} 		
		catch ( Exception e ) { 
			Mlog.error("Fehler beim einlesen der CSV-Datei aufgetreten");
			Mlog.ex(e);
		} 
		finally { 
		  try {
			  in.close();
			  reader.close();
		  } catch ( Exception e ) { } 
		}	
		return (allesok);
	}
}
