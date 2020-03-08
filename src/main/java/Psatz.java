import java.util.Date;

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
 * Psatz.java: Datensatz für einen gefahrenen, geographischen Punkt. Wird 
 *   verwendet bei Import/Export (CSV, evtl. später: FIT)
 *****************************************************************************
 *
 */
public class Psatz {
		public	long index;
		public Date zeitpunkt;
		public long punkt; 
		public double work; 
		public double puls; 
		public double rpm; 
		public double leistung; 
		public double geschw; 
		public double strecke; 
		public double steigung; 
		public double hoehe;
		public double lat;
		public double lon;
		Psatz(
			long index,
			Date zeitpunkt,
			long punkt, 
			double work, 
			double puls, 
			double rpm, 
			double leistung, 
			double geschw, 
			double strecke, 
			double steigung, 
			double hoehe,
			double lat,
			double lon) {
			this.index = index;
			this.zeitpunkt = zeitpunkt;
			this.punkt = punkt;
			this.work = work;
			this.puls = puls;
			this.rpm = rpm;
			this.leistung = leistung;
			this.geschw = geschw;
			this.strecke = strecke;
			this.steigung = steigung;
			this.hoehe = hoehe;
			this.lat = lat;
			this.lon = lon;
		}
}

