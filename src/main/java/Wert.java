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
 * Wert.java: Beinhaltet Daten von Biker und Bike
 *****************************************************************************
 *
 * Klasse um einen beliebigen Wert zu halten und benötigte Berechnungen 
 * z. B. Mittelwert bereitzustellen.
 *
 */
public class Wert {
	private double	summe;
	private long	anzahl;
	private double	wert;
	private double	maxwert;
	private double	minwert;
	
	/**
	 * Setzt alles zurück.
	 */
	public void reset() {
		wert = 0;
		summe = 0;
		minwert = 0;
		maxwert = 0;
		anzahl = 0;		
	}
	
	/**
	 * Gibt die Summe zurück.
	 * @return Summe
	 */
	public double getSumme() {
		return summe;
	}

	/**
	 * Gibt die Anzahl zurück.
	 * @return the anzahl
	 */
	public long getAnzahl() {
		return anzahl;
	}

	/**
	 * Gibt den Maximalwert zurück
	 * @return the maxwert
	 */
	public double getMaxwert() {
		return maxwert;
	}

	/**
	 * Gibt den Minimalwert zurück
	 * @return the minwert
	 */
	public double getMinwert() {
		return minwert;
	}

	/**
	 * setzt den Wert
	 * @param pwert		Wert
	 */
	public void setWert(double pwert) {
		wert = pwert;
		summe += pwert;
		anzahl++;
		minwert = (minwert > pwert) ? pwert : minwert;
		maxwert = (maxwert < pwert) ? pwert : maxwert;
	}
	
	/**
	 * holt den Wert
	 * @return akt. Wert
	 */
	public double getWert() {
		return(wert);
	}
	
	/**
	 * Ermittelt den Mittelwert
	 * @return Average
	 */
	public double getAverage() {
		return(summe / anzahl);
	}
}
