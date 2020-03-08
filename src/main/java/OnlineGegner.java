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
 * OnlineGegner.java: Beinhaltet Daten der Onlinegegner
 *****************************************************************************
 *
 * Aktuelle Daten der Online- und virtuellen Gegner
 *
 */
public class OnlineGegner implements Comparable<OnlineGegner> {
	private String vorname;
	private String nachname;
	private Date zeitpunkt;
	private long punkt; 
	private String wind; 
	private int gang; 
	private double work; 
	private double puls; 
	private double rpm; 
	private double leistung; 
	private double geschw; 
	private double strecke; 
	private double steigung; 
	private double hoehe;
	private double lat;
	private double lon;
	private double zpstrecke; 
	@SuppressWarnings("unused")
	private int    platz;
	private int    userid;

	OnlineGegner(
			String vorname,
			String nachname,
			Date zeitpunkt,
			long punkt, 
			String wind, 
			int gang, 
			double work, 
			double puls, 
			double rpm, 
			double leistung, 
			double geschw, 
			double strecke, 
			double steigung, 
			double hoehe,
			double lat,
			double lon,
			double zpstrecke,
			int platz,
			int userid) {
		this.vorname = vorname;
		this.nachname = nachname;
		this.zeitpunkt = zeitpunkt;
		this.punkt = punkt;
		this.wind = wind;
		this.gang = gang;
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
		this.zpstrecke = zpstrecke;
		this.platz = platz;
		this.userid = userid;
	}
	
    @Override
    public int compareTo(OnlineGegner o) {	// Es wird immer die Zielpulsstrecke verglichen! Nur wenn identisch, dann zählt die kürzere Zeit!
    	int ret = ((Double)o.zpstrecke).compareTo((Double)zpstrecke);
    	if (ret == 0) {	// beide sind identisch, dann zählt die Zeit!
    		if (zeitpunkt == null) {
    			Mlog.debug("Nullvergleich!");
    			return 0;
    		} else
    			return ((zeitpunkt).compareTo(o.zeitpunkt));
    	} else 
    		return ret;
    }

    /**
	 * @return the vorname
	 */
	public String getVorname() {
		return vorname;
	}
	/**
	 * @param vorname the vorname to set
	 */
	public void setVorname(String vorname) {
		this.vorname = vorname;
	}
	/**
	 * @return the nachname
	 */
	public String getNachname() {
		return nachname;
	}
	/**
	 * @param nachname the nachname to set
	 */
	public void setNachname(String nachname) {
		this.nachname = nachname;
	}
	/**
	 * @return the zeitpunkt
	 */
	public Date getZeitpunkt() {
		return zeitpunkt;
	}
	/**
	 * @param zeitpunkt the zeitpunkt to set
	 */
	public void setZeitpunkt(Date zeitpunkt) {
		this.zeitpunkt = zeitpunkt;
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
	public String getWind() {
		return wind;
	}
	/**
	 * @param wind the wind to set
	 */
	public void setWind(String wind) {
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
	 * @return the work
	 */
	public double getWork() {
		return work;
	}
	/**
	 * @param work the work to set
	 */
	public void setWork(double work) {
		this.work = work;
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
	 * @return the geschw
	 */
	public double getGeschw() {
		return geschw;
	}
	/**
	 * @param geschw the geschw to set
	 */
	public void setGeschw(double geschw) {
		this.geschw = geschw;
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
	 * @return the lat
	 */
	public double getLat() {
		return lat;
	}
	/**
	 * @param lat the lat to set
	 */
	public void setLat(double lat) {
		this.lat = lat;
	}
	/**
	 * @return the lon
	 */
	public double getLon() {
		return lon;
	}
	/**
	 * @param lon the lon to set
	 */
	public void setLon(double lon) {
		this.lon = lon;
	}

	/**
	 * @return the zpstrecke
	 */
	public double getZpstrecke() {
		return zpstrecke;
	}

	/**
	 * @param zpstrecke the zpstrecke to set
	 */
	public void setZpstrecke(double zpstrecke) {
		this.zpstrecke = zpstrecke;
	}

	/**
	 * @return the userid
	 */
	public int getUserid() {
		return userid;
	}

	/**
	 * @param userid the userid to set
	 */
	public void setUserid(int userid) {
		this.userid = userid;
	}
}

