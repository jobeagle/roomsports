import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import javax.xml.bind.DatatypeConverter;


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
 * MTBSRaceService.java: Server-services
 *****************************************************************************
 *
 * Serverfunktionalitäten des Onlinetrainings verwendet wird Axis2 als 
 * Webserverumgebung.
 * 
 */


public class MTBSRaceService {
    private static final String url = "jdbc:mysql://localhost/";
    private static final String classname = "com.mysql.jdbc.Driver";
    private static final String user = "<DB-user>";
    private static final String password = "<DB-Pw>";
    private static Connection con;
    private SimpleDateFormat tfmt = new SimpleDateFormat();
	private static final int[] WindPunkt = {0,0};
	private static final int[] WindStaerke = {0,0};
	private static final int Windbereich = 30;
	private static final int WindMAxGPSPunkt = 400;
	private static final int kulanzzeit = 30;				// Zeit in Minuten fuer verspaeteten Anmeldung/Einstieg ins Rennen
	private static long lastrennid = -1;					// hier wird fuer die Windautomatik die letzte Renn-Id gespeichert
	
    /**
     * Ueberpruefung der Seriennummer auf Gueltigkeit.
     * Aktuell wird nicht ueberprueft!
     * @param SNR		Seriennummer des MTB-Simulators
     * @return true oder false
     */
    private boolean chkSNR(String SNR) {
    			return (true);
    }
    
    /**
     * Connect zur Datenbank.
     * TODO: Anpassen an SQLite
     * @param db		Datenbank
     * @return OK oder Fehlermeldung
     */
    public String ConnectDB(String db) {
    	try {
    		Class.forName(classname);
    		con = DriverManager.getConnection(url+db, user, password);
    		return("OK");
    	} catch (Exception e) {
    		return("WEBSV-Fehler! "+e.toString()); 
        }
    }

    /**
     * Wenn keine Connection zur DB besteht, dann connecten!
     * @param db
     */
    private void reConnectDB(String db) {
    	if (con == null)
    		ConnectDB(db);
		else
			try {
				if (!con.isValid(0)) {
					ConnectDB(db);
				}
			} catch (SQLException e) {
				ConnectDB(db);
			}
    }
    
    /**
     * InsertUser traegt einen neuen User mit (codierter) Seriennummer in die Datenbank ein.
     * @param db		Datenbank
     * @param SNR		Seriennummer MTB-Simulator
     * @param name		Name des Teilnehmers
     * @param vorname	Vorname des Teilnehmers
     * @param ort		Wohnort das Teilnehmers
     * @param gebjahr	Geburtsjahr (evtl. fuer Altersklassen)
     * @param trainer	Trainingsgeraet
     * @return OK oder Fehlermeldung
     */
    public String InsertUser(String db, String SNR, String name, String vorname, String ort, String gebjahr, String trainer) {
    	if (!chkSNR(SNR))
    		return ("WEBSV-Fehler! Keine Berechtigung!"); 
    	
    	reConnectDB(db);
    	try {
			Statement st = con.createStatement();
			st.executeUpdate("INSERT INTO USER (SNR, NAME, VORNAME, ORT, GEBURTSJAHR, TRAINER) VALUES('"+SNR+"','"+name+"','"+vorname+"','"+ort+"','"+gebjahr+"','"+trainer+"')");
			st.close();
    		return("OK");
    	} catch (Exception e) {
    		return("WEBSV-Fehler! "+e.toString()); 
        }
    }

    /**
     * InsertTeilnahme traegt eine neue Teilnehme mittels Seriennummer und Rennenbezeichnung in die Datenbank ein.
     * Der User muss bereits eingetragen und das Rennen in der Zukunft sein.
     * @param db		Datenbank
     * @param SNR		Seriennummer MTB-Simulator
     * @param Rennen	Bezeichnung des Rennens
     * @param vnr		Versionsnummer des Clients (nur die Ziffern, z. B. 406)
     * @return User_id des Angemeldeten oder Fehlermeldung
     */
    public String InsertTeilnahme(String db, String SNR, String Rennen, String vnr) {
    	if (!chkSNR(SNR))
    		return ("WEBSV-Fehler! Keine Berechtigung!"); 
    	
    	reConnectDB(db);
    	try {
			Statement st = con.createStatement();
			st.executeQuery ("SELECT USER_ID FROM USER WHERE SNR = '" + SNR + "'");
			ResultSet rsuser = st.getResultSet();
			if (!rsuser.next()) {
				rsuser.close();
				st.close();
				return("WEBSV-Fehler! User ist nicht vorhanden!");
			}
			long userid = rsuser.getLong("USER_ID");			
			
			st.executeQuery ("SELECT RENN_ID, TEILNEHMER, TEILNEHMERLIMIT, VERSIONNR FROM RENNEN WHERE BEZEICHNUNG = '" + Rennen + "' AND STARTZEIT > DATE_SUB(NOW(), INTERVAL " + kulanzzeit + " MINUTE)");
			ResultSet rsrennen = st.getResultSet();
			if (!rsrennen.next()) {
				rsrennen.close();
				st.close();
				return("WEBSV-Fehler! Das Rennen ist nicht vorhanden!");
			}
			long rennenid = rsrennen.getLong("RENN_ID");	
			short tlimit = rsrennen.getShort("TEILNEHMERLIMIT");
			short teiln = rsrennen.getShort("TEILNEHMER");
			short versionnr = rsrennen.getShort("VERSIONNR");
			
			if (++teiln > tlimit) 
				return("WEBSV-Fehler! Das Teilnehmerlimit ist erreicht!");
			
			short clientVnr = 0;
			
			if (vnr == null) {	// Versionen bis 4.05 unterstuetzen diesen Parameter nicht, das wird hier abgefangen.
				if (versionnr > 0)
					return("WEBSV-Fehler! Die Version des MTB-Simulators ist veraltet, bitte vorher updaten!");
			} else {			
				if (!vnr.isEmpty())
					clientVnr = new Short(vnr);	
			
				if (clientVnr < versionnr)
					return("WEBSV-Fehler! Die Version des MTB-Simulators ist veraltet! Bitte Version ab " + versionnr + " verwenden.");
			}	
			st.executeUpdate("INSERT INTO TEILNAHME (TEIL_USER_ID, TEIL_RENN_ID, ANGEMELDET_AM) VALUES ('"+userid+"','"+rennenid+"',NOW())");

			st.executeQuery ("SELECT COUNT(*) TEILNEHMER FROM TEILNAHME WHERE TEIL_RENN_ID = '" + rennenid + "'");
			ResultSet rsteilnahme = st.getResultSet();
			if (!rsteilnahme.next()) {
				rsteilnahme.close();
				st.close();
				return("WEBSV-Fehler! Fehler beim zaehlen der Teilnehmer!");
			}
			teiln = rsteilnahme.getShort("TEILNEHMER");

			st.executeUpdate("UPDATE RENNEN SET TEILNEHMER = "+teiln+" WHERE RENN_ID = '"+rennenid+"'");
			rsuser.close();
			rsrennen.close();
			st.close();
    		// return("OK");
    		return(userid+"");
    	} catch (Exception e) {
    		return("WEBSV-Fehler! "+e.toString()); 
        }
    }

    /**
     * DeleteTeilnahme loescht eine TEILNAHME mittels (codierter) Seriennummer und Rennenbezeichnung aus der Datenbank.
     * Der User muss bereits eingetragen und das Rennen in der Zukunft sein.
     * @param db		Datenbank
     * @param SNR		Seriennummer MTB-Simulator
     * @param Rennen	Rennbezeichnung
     * @return OK oder Fehlermeldung
     */
    public String DeleteTeilnahme(String db, String SNR, String Rennen) {
    	if (!chkSNR(SNR))
    		return ("WEBSV-Fehler! Keine Berechtigung!"); 
    	
    	reConnectDB(db);
    	try {
			Statement st = con.createStatement();
			st.executeQuery ("SELECT USER_ID FROM USER WHERE SNR = '" + SNR + "'");
			ResultSet rsuser = st.getResultSet();
			if (!rsuser.next()) {
				rsuser.close();
				st.close();
				return("WEBSV-Fehler! User ist nicht vorhanden!");
			}
			long userid = rsuser.getLong("USER_ID");			
			
			st.executeQuery ("SELECT RENN_ID FROM RENNEN WHERE BEZEICHNUNG = '" + Rennen + "' AND STARTZEIT > NOW()");
			ResultSet rsrennen = st.getResultSet();
			if (!rsrennen.next()) {
				rsrennen.close();
				st.close();
				return("WEBSV-Fehler! Das Rennen ist nicht vorhanden!");
			}
			long rennenid = rsrennen.getLong("RENN_ID");			

			st.executeUpdate("DELETE FROM TEILNAHME WHERE TEIL_USER_ID = "+userid+" AND TEIL_RENN_ID = "+rennenid);

			st.executeQuery ("SELECT COUNT(*) TEILNEHMER FROM TEILNAHME WHERE TEIL_RENN_ID = '" + rennenid + "'");
			ResultSet rsteilnahme = st.getResultSet();
			if (!rsteilnahme.next()) {
				rsteilnahme.close();
				st.close();
				return("WEBSV-Fehler! Fehler beim zaehlen der Teilnehmer!");
			}
			short teiln = rsteilnahme.getShort("TEILNEHMER");

			st.executeUpdate("UPDATE RENNEN SET TEILNEHMER = "+teiln+" WHERE RENN_ID = '"+rennenid+"'");
			rsuser.close();
			rsrennen.close();
			st.close();
    		return("OK");
    	} catch (Exception e) {
    		return("WEBSV-Fehler! "+e.toString()); 
        }
    }

    /**
     * GetTeilnahmeID ermittelt die ID der aktuellen Teilnahme mittels (codierter) Seriennummer und Rennenbezeichnung.
     * Die Teilnahme-Id wird im Client zum eintragen der Positionen verwendet.
     * @param db		Datenbank
     * @param SNR		Seriennummer MTB-Simulator
     * @param Rennen	Rennenbezeichnung
     * @return ID der Teilnahme
     */
    public String GetTeilnahmeID(String db, String SNR, String Rennen) {
    	if (!chkSNR(SNR))
    		return ("WEBSV-Fehler! Keine Berechtigung!"); 
    	
    	reConnectDB(db);
    	try {
			Statement st = con.createStatement();
			st.executeQuery ("SELECT USER_ID FROM USER WHERE SNR = '" + SNR + "'");
			ResultSet rsuser = st.getResultSet();
			if (!rsuser.next()) {
				rsuser.close();
				st.close();
				return("WEBSV-Fehler! User ist nicht vorhanden!");
			}
			long userid = rsuser.getLong("USER_ID");			
			
			st.executeQuery ("SELECT RENN_ID FROM RENNEN WHERE BEZEICHNUNG = '" + Rennen + "'");
			ResultSet rsrennen = st.getResultSet();
			if (!rsrennen.next()) {
				rsrennen.close();
				st.close();
				return("WEBSV-Fehler! Das Rennen ist nicht vorhanden!");
			}
			long rennenid = rsrennen.getLong("RENN_ID");			

			st.executeQuery ("SELECT TEIL_ID FROM TEILNAHME WHERE TEIL_USER_ID = " + userid + " AND TEIL_RENN_ID = " + rennenid);
			ResultSet rs = st.getResultSet();
			if (!rs.next()) {
				rs.close();
				st.close();
				return("WEBSV-Fehler! Die Teilnahme an diesem Rennen ist nicht eingetragen!");
			}
			long teilnahmeid = rs.getLong("TEIL_ID");
			rs.close();
			rsuser.close();
			rsrennen.close();
			st.close();
    		return(teilnahmeid + "");
    	} catch (Exception e) {
    		return("WEBSV-Fehler! "+e.toString()); 
        }
    }

    /**
     * WarteBisStart ermittelt die Startzeit des eingetragenen Rennens mittels (codierter) Seriennummer und Rennenbezeichnung.
     * Anschliessend wird bis zum Startzeitpunkt gewartet und eine Zufallszahl fuer die Anzahl der Wartesekunden zurueckgegeben.
     * @param db		Datenbank
     * @param SNR		Seriennummer MTB-Simulator
     * @param Rennen	Rennbezeichnung
     * @return Zufallszahl (1..9) fuer die Sekunden die der Aufrufer warten soll bis zur ersten Datenuebertragung, oder Fehlermeldung
     */
    public String WarteBisStart(String db, String SNR, String Rennen) {
    	if (!chkSNR(SNR))
    		return ("WEBSV-Fehler! Keine Berechtigung!"); 
    	
    	reConnectDB(db);
    	try {
			Statement st = con.createStatement();
			st.executeQuery ("SELECT USER_ID FROM USER WHERE SNR = '" + SNR + "'");
			ResultSet rsuser = st.getResultSet();
			if (!rsuser.next()) {
				rsuser.close();
				st.close();
				return("WEBSV-Fehler! User ist nicht vorhanden!");
			}
			long userid = rsuser.getLong("USER_ID");			
			
			// Bedingung fuer den Countdown: Start in max. 30 Min., der Start darf max. "kulanzzeit" zurueckliegen und man muss angemeldet sein!
			String sql = "SELECT RENN_ID, STARTZEIT, count(TEIL_ID) cnt FROM RENNEN join TEILNAHME on teil_renn_id = renn_id WHERE BEZEICHNUNG = '" + Rennen + 
			"' AND STARTZEIT < DATE_ADD(NOW(), INTERVAL 30 MINUTE)";
			
			st.executeQuery (sql);
			ResultSet rsrennen = st.getResultSet();
			// wegen count kommt jetzt immer ein Satz!
			rsrennen.next();
			long rennenid = rsrennen.getLong("RENN_ID");
			if (rennenid == 0)
				return("WEBSV-Fehler! Das Rennen ist nicht im aktuellen Zeitfenster!");
			int teilAnz = rsrennen.getInt("CNT");
			Timestamp startzeit = rsrennen.getTimestamp("STARTZEIT");			

			st.executeQuery ("SELECT TEIL_ID FROM TEILNAHME WHERE TEIL_USER_ID = " + userid + " AND TEIL_RENN_ID = " + rennenid);
			ResultSet rs = st.getResultSet();
			if (!rs.next()) {
				rs.close();
				st.close();
				return("WEBSV-Fehler! Die Teilnahme an diesem Rennen ist nicht eingetragen!");
			}
			rs.close();
			rsuser.close();
			rsrennen.close();

			java.util.Date utilDate = new java.util.Date();
			java.sql.Date sqlNow = new java.sql.Date(utilDate.getTime());
			
			long sek = (startzeit.getTime() - sqlNow.getTime()) / 1000;
			
			if (sek > 0) {
				st.close();	
				if ((sek % 30) == 0)
					return("angemeldet: "+teilAnz+" zuletzt: "+getLetzteAnmeldung(rennenid));
				else
					return(sek+"");						// Startfreigabe ist noch nicht erfolgt!
			}

			// Globalen fuer Gegenwind zuruecksetzen
			// das muss einmalig pro Rennen erfolgen!
			if ((rennenid != lastrennid) && (WindPunkt[0] != 0 || WindPunkt[1] != 0)) {
				WindPunkt[0] = 0;
				WindPunkt[1] = 0;
				WindStaerke[0] = 0;
				WindStaerke[1] = 0;	
				lastrennid = rennenid;
			}
			
			st.executeUpdate("UPDATE RENNEN SET STATUS = 1 WHERE RENN_ID = "+rennenid+" AND STATUS = 0");
			st.close();			
			
    		return("0");						
    	} catch (Exception e) {
    		return("ERROR: "+e.toString());	
        }
    }

    /**
     * GetRennenID ermittelt die ID des Rennens anhand der Rennenbezeichnung.
     * Die Renn-Id wird im Client zum ermitteln der Positionen aller Teilnehmer verwendet.
     * @param db		Datenbank
     * @param SNR		Seriennummer MTB-Simulator
     * @param Rennen	Rennenbezeichnung
     * @return ID des Rennens
     */
    public String GetRennenID(String db, String SNR, String Rennen) {
    	if (!chkSNR(SNR))
    		return ("WEBSV-Fehler! Keine Berechtigung!"); 
    	
    	reConnectDB(db);
    	try {
			Statement st = con.createStatement();
			st.executeQuery ("SELECT RENN_ID FROM RENNEN WHERE BEZEICHNUNG = '" + Rennen + "'");
			ResultSet rsrennen = st.getResultSet();
			if (!rsrennen.next()) {
				rsrennen.close();
				st.close();
				return("WEBSV-Fehler! Das Rennen ist nicht vorhanden!");
			}
			long rennenid = rsrennen.getLong("RENN_ID");			
			rsrennen.close();
			st.close();
    		return(rennenid + "");
    	} catch (Exception e) {
    		return("WEBSV-Fehler! "+e.toString()); 
        }
    }

    /**
     * GetKonfiguration ermittelt die Konfigurationswerte (MIN-Leistung etc.) anhand der Rennenbezeichnung.
     * @param db		Datenbank
     * @param SNR		Seriennummer MTB-Simulator
     * @param Rennen	Rennenbezeichnung
     * @return "MINLEISTUNG ; DYNAMIK" ...
     */
    public String GetKonfiguration(String db, String SNR, String Rennen) {
		DecimalFormat zfk1 = new DecimalFormat("0.0");  
		DecimalFormat zfk3 = new DecimalFormat("0.000");  
    	if (!chkSNR(SNR))
    		return ("WEBSV-Fehler! Keine Berechtigung!"); 
    	
    	reConnectDB(db);
    	try {
			Statement st = con.createStatement();
			st.executeQuery ("SELECT MINLEISTUNG, MAXLEISTUNG, DYNAMIK, TOUR, PULSMIN, PULSMAX, TYP, SCHIKANE, LEISTUNGSFAKTOR, TFNORMAL, TFMIN, KONSTANT_CW, KONSTANT_K2, GEWICHT_FAHRER, GEWICHT_BIKE, ERST_GPS, LETZT_GPS FROM RENNEN WHERE BEZEICHNUNG = '" + Rennen + "'");
			ResultSet rsrennen = st.getResultSet();
			if (!rsrennen.next()) {
				rsrennen.close();
				st.close();
				return("WEBSV-Fehler! Das Rennen ist nicht vorhanden!");
			}
			int minleistung = rsrennen.getInt("MINLEISTUNG");	
			int maxleistung = rsrennen.getInt("MAXLEISTUNG");	
			int dynamik = rsrennen.getInt("DYNAMIK");
			String tour = rsrennen.getString("TOUR");
			int pulsmin = rsrennen.getInt("PULSMIN");
			int pulsmax = rsrennen.getInt("PULSMAX");
			String typ = rsrennen.getString("TYP");
			double lf = rsrennen.getDouble("LEISTUNGSFAKTOR");
			int schikane = rsrennen.getInt("SCHIKANE");
			int nTF = rsrennen.getInt("TFNORMAL");
			int nTFmin = rsrennen.getInt("TFMIN");
			double cw = rsrennen.getDouble("KONSTANT_CW");
			double k2 = rsrennen.getDouble("KONSTANT_K2");
			double gewichtBike = rsrennen.getDouble("GEWICHT_BIKE");
			double gewichtFahrer = rsrennen.getDouble("GEWICHT_FAHRER");
			int erstGPS = rsrennen.getInt("ERST_GPS");	
			int letztGPS = rsrennen.getInt("LETZT_GPS");	
			rsrennen.close();
			st.close();
			String ret = minleistung+";"+dynamik+";"+tour+";"+pulsmin+";"+pulsmax+";"+maxleistung+";"+typ+";"+zfk1.format(lf)+";" + 
			             schikane+";"+nTF+";"+nTFmin+";"+zfk3.format(cw)+";"+zfk3.format(k2)+";"+zfk1.format(gewichtBike)+";"+zfk1.format(gewichtFahrer)+";"+erstGPS+";"+letztGPS;
			
    		return(ret);
    	} catch (Exception e) {
    		return("WEBSV-Fehler! "+e.toString()); 
        }
    }

    /**
     * InsertActPos1 traegt die aktuelle Position und weitere Daten in die Tabelle ACTPOS ein.
     * Das ist die neue Version mit Uebertragung der Zielpulsstrecke.
     * @param db				Datenbank
     * @param SNR				Seriennummer MTB-Simulator
     * @param ID				Teilnahme-ID
     * @param Lat				Breitengrad
     * @param Long				Laengengrad
     * @param Gps_Punkt			Index des GPS-Punktes
     * @param Wind				Windgeschwindigkeit
     * @param Gang				Gang
     * @param Energie			kCal
     * @param Puls				Pulsrate
     * @param RPM				Kurbelumdrehungen
     * @param Leistung			akt. Leistung
     * @param Geschwindigkeit	akt. Geschwindigkeit
     * @param Strecke			zurueckgelegte Strecke
     * @param Steigung			akt. Steigung
     * @param Hoehe				akt. Hoehe
     * @param ZPStrecke			zurueckgelegte Strecke im Zielpulsbereich
     * @return OK oder Fehlermeldung
     */
    public String InsertActPos1(String db, String SNR, String ID, String Lat, String Long, String Gps_Punkt, String Wind, String Gang, String Energie, String Puls, String RPM, String Leistung, String Geschwindigkeit, String Strecke, String Steigung, String Hoehe, String ZPStrecke) {
    	if (!chkSNR(SNR))
    		return ("WEBSV-Fehler! Keine Berechtigung!"); 
    	
    	reConnectDB(db);
    	try {
			Statement st = con.createStatement();
			String sql = "INSERT INTO ACTPOS (ACTP_TEIL_ID, ZEITPUNKT, LATITUDE, LONGITUDE, GPS_PUNKT, WIND, GANG, ENERGIE, PULS, RPM, LEISTUNG, GESCHWINDIGKEIT, STRECKE, STEIGUNG, HOEHE, ZPSTRECKE) VALUES ('"+
			             ID+"',NOW(),'"+Lat+"','"+Long+"','"+Gps_Punkt+"','"+Wind+"','"+Gang+"','"+Energie+"','"+Puls+"','"+RPM+"','"+Leistung+"','"+Geschwindigkeit+"','"+Strecke+"','"+Steigung+"','"+Hoehe+"','"+ZPStrecke+"')";
			st.executeUpdate(sql);
			st.close();
			if (Wind.contentEquals("1"))
				return(windautomatik(new Integer(Gps_Punkt)));
			
    		return("OK");
    	} catch (Exception e) {
    		return("WEBSV-Fehler! "+e.toString()); 
        }
    }

    /**
     * getRegCode testet ob die SNR in der Tabelle REGDAT gueltig ist.
     * Wenn ja, dann wird der Keystring gebildet aus:
     * SNR~Name~Level~31.12.2100
     * und CRYPTCODE ermittelt und der Datensatz aktualisiert mit:
     * NAME=Name, FREI=0, ANZREG+1, REGDATE, CRYPTCODE
     * @param db		Datenbank
     * @param sNR		Seriennummer MTB-Simulator
     * @param name		Name/Pseudonym wie vom User eingetragen
     * @return CRYPTCODE zur Uebernahme in die MTBS-Settings
     */
    public String getRegCode(String db, String sNR, String name) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(); 
    	reConnectDB(db);
    	try {
			Statement st = con.createStatement();
			String snr = sNR.trim().replace(' ', 'x');	// replace gegen SQL-Injection
			st.executeQuery ("SELECT LEVEL, ANZREG, GUELTIG FROM REGDAT WHERE SNR = '" + snr + "'");
			ResultSet rsregdat = st.getResultSet();
			if (!rsregdat.next()) {
				rsregdat.close();
				st.close();
				return("WEBSV-Fehler! Seriennummer nicht vorhanden!");
			}
			int gueltig = rsregdat.getInt("GUELTIG");			
			int anzreg = rsregdat.getInt("ANZREG");	
			String level = rsregdat.getString("LEVEL");
			if (gueltig == 0) {
				rsregdat.close();
				st.close();
				return("WEBSV-Fehler! Seriennummer nicht gueltig!");				
			}

			String keycode = snr+"~"+name+"~"+level+"~"+"31.12.2100";
			Mtbscipher.encode(keycode.getBytes(), out);
			String cryptcode = DatatypeConverter.printBase64Binary(out.toByteArray());
			//Mlog.debug("codiert: " + s);	

			String sql = "UPDATE REGDAT SET NAME='"+name+"',REGDATE=NOW(),FREI=0,ANZREG="+(++anzreg)+",CRYPTCODE='"+cryptcode+"' WHERE SNR='"+snr+"'";
			st.executeUpdate(sql);

			rsregdat.close();
			st.close();
    		return(cryptcode);
    	} catch (Exception e) {
    		return("WEBSV-Fehler! "+e.toString()); 
        }
    }

    /**
     * getInfoURL testet ob die uebergebene URL der letzte Eintrag in der 
     * INFO Tabelle ist, wenn ja wird ein leerer String zurueckgegeben.
     * Damit wird erreicht, dass keine Meldungen mehrfach angezeigt werden.
     * Ist eine neuere URL vorhanden oder ist die uebergebene URL leer (erster
     * Aufruf des MTBS) dann wird die neueste URL zurueckgegeben.
     * @param 	db		Datenbank
     * @param 	url		letzte URL, die vom Client angezeigt wurde
     * @return 	url des neuesten Eintrags in INFO-Tabelle
     */
    public String getInfoURL(String db, String url) {
    	reConnectDB(db);
    	try {
			Statement st = con.createStatement();
			st.executeQuery ("SELECT INFO_ID, URL FROM INFO ORDER BY INFO_ID DESC");
			ResultSet rsregdat = st.getResultSet();
			if (!rsregdat.next()) {
				rsregdat.close();
				st.close();
				return("WEBSV-Fehler! kein Eintrag in INFO-Tabelle!");
			}
			String retURL = rsregdat.getString("URL");
			int infoId = rsregdat.getInt("INFO_ID");	
			
			rsregdat.close();
			if (url.equals(retURL)) {
				st.close();
				return "";
			}
			
			String sql = "UPDATE INFO SET VIEWS = VIEWS+1 WHERE INFO_ID = " + infoId;
			st.executeUpdate(sql);
			st.close();

    		return(retURL);
    	} catch (Exception e) {
    		return("WEBSV-Fehler! "+e.toString()); 
        }
    }

    /**
     * Ermittelt die letzte Anmeldung
     * @param rennenid
     * @return Name als String
     */
    private String getLetzteAnmeldung(long rennenid) {
    	Statement st;
		String sql = "SELECT name FROM TEILNAHME join USER on user_id = teil_user_id where teil_renn_id = " + rennenid +
		" and angemeldet_am in (select max(angemeldet_am) from TEILNAHME where teil_renn_id = " + rennenid + ")";
    	try {
    		st = con.createStatement();
    		st.executeQuery (sql);
    		ResultSet rslastTeiln = st.getResultSet();
    		if (!rslastTeiln.next()) {
    			rslastTeiln.close();
    			st.close();
    			return("Keiner");
    		}

    		String erg = rslastTeiln.getString("NAME");
			rslastTeiln.close();
			st.close();
    		return(erg);
    	} catch (SQLException e) {
    		return("Error");
    	}
    }
    
    /**
     * Die Windautomatik setzt beim ersten Aufruf zwei zufaellige Punkte und zufaellige Windstaerken fest und
     * prueft anschliessend, ob der uebergebene Punkt im Bereich (Punkt + 30) ist und liefert die zugewiesene Windstaerke zurueck.
     * @param punkt
     * @return Windstaerke (1..6) als Zahl
     */
    private String windautomatik(int punkt) {
		if (WindPunkt[0] == 0 && WindPunkt[1] == 0) {
			WindPunkt[0] = (int) (Math.random() * WindMAxGPSPunkt);
			WindPunkt[1] = (int) (Math.random() * WindMAxGPSPunkt);
			WindStaerke[0] = 1 + (int) (Math.random() * 6);
			WindStaerke[1] = 1 + (int) (Math.random() * 6);
		}
		
		if (punkt >= WindPunkt[0] && punkt < WindPunkt[0] + Windbereich) 
			return(WindStaerke[0] + "");

		if (punkt >= WindPunkt[1] && punkt < WindPunkt[1] + Windbereich) 
			return(WindStaerke[1] + "");

		return "0";
	}

	/**
     * Der WebService RennenListe liefert eine mit ";"-getrenntes Stringarry aller zukuenftigen Rennen zurueck.
     * Wird SNR uebergeben, dann wird ein Array mit allen Rennen des Users zurueckgegeben.
     * @param db		Datenbank
     * @param SNR		Seriennummer MTB-Simulator
     * @return String[] aus Datenbank mit ";" getrennt.
     */
    public String[] RennenListe(String db, String SNR) {
    	String[] ret = {"0"};

		tfmt.applyPattern("d.MM.yyy H:mm");
    	reConnectDB(db);
    	try {
    		if (SNR == null) { 	// alle Rennen zurueckgeben
    			Statement st = con.createStatement();
    			st.executeQuery ("SELECT BEZEICHNUNG, TYP, TEILNEHMER, STARTZEIT FROM RENNEN WHERE STATUS != 2 ORDER BY STARTZEIT");
    			ResultSet rs = st.getResultSet();
    			rs.last();
    			int anz = rs.getRow();
    			if (anz > 0) {
    				ret = new String[anz];
    				rs.first();
    				int count = 0;
    				do {
    					String bez = rs.getString ("BEZEICHNUNG");
    					int anzteil = rs.getInt ("TEILNEHMER");
    					Timestamp startzeit = rs.getTimestamp("STARTZEIT");
    					ret[count++] = bez + ";" + tfmt.format(startzeit) + ";" + anzteil;
    				} while (rs.next ());
    			}
    			rs.close ();
    			st.close ();
    			return(ret);
    		}
    		else {	// SNR wurde uebergeben: alle Rennen des Teilnehmers zurueckliefern
    			Statement st = con.createStatement();
    			st.executeQuery ("SELECT BEZEICHNUNG, TYP, TEILNEHMER, STARTZEIT, PLATZ, ZEIT FROM RENNEN r INNER JOIN TEILNAHME t ON t.TEIL_RENN_ID = r.RENN_ID INNER JOIN USER u ON u.USER_ID = t.TEIL_USER_ID where u.snr = '"+SNR+"'"+" AND STATUS != 2 ORDER BY STARTZEIT");
    			ResultSet rs = st.getResultSet();
    			rs.last();
    			int anz = rs.getRow();
    			if (anz > 0) {
    				ret = new String[anz];
    				rs.first();
    				int count = 0;
    				do {
    					String bez = rs.getString ("BEZEICHNUNG");
    					int anzteil = rs.getInt ("TEILNEHMER");
    					int platz = rs.getInt ("PLATZ");
    					Timestamp startzeit = rs.getTimestamp("STARTZEIT");
    					ret[count++] = bez + ";" + tfmt.format(startzeit) + ";" + anzteil + ";" + platz;
    				} while (rs.next ());
    			}
    			rs.close ();
    			st.close ();
    			return(ret);
    		}
    	} catch (Exception e) {
    		ret[0] = "WEBSV-Fehler! "+e.toString();
    		return(ret); 
    	}
    }

    /**
     * Rueckgabe der aktuellen Positionen aller Teilnehmer eines Rennens als Stringarray.
     * 25.11.2011: erweitert auf Zielpulstraining
     * 28.12.2011: Zielpulsstrecke wird extra zurueckgegeben
     * @param db		Datenbank
     * @param SNR		Seriennummer MTB-Simulator
     * @param RennenId	Id des Rennens
     * @return String[] aus Datenbank mit ";" getrennt.
     */
    public String[] ActPosListe(String db, String SNR, String RennenId) {
    	String[] ret = {"0"};
    	String renntyp = "";
    	String sqlstrecke = "";
    	
    	if (!chkSNR(SNR)) {
    		ret[0] =  ("WEBSV-Fehler! Keine Berechtigung!"); 
    		return ret;
    	}
    	reConnectDB(db);
    	try {
    			Statement st = con.createStatement();

    			st.executeQuery ("SELECT TYP FROM RENNEN WHERE RENN_ID = " + RennenId);
    			ResultSet rsrennen = st.getResultSet();
    			if (!rsrennen.next()) {
    				rsrennen.close();
    				st.close();
    	    		ret[0] = "WEBSV-Fehler! Das Rennen ist nicht vorhanden!";
    				return(ret);
    			}
				renntyp = rsrennen.getString("TYP");
				rsrennen.close();

				if (renntyp.equalsIgnoreCase("ERGOMETERZIELPULSTRAINING")) {	// Ergometer Pulstraining
    				sqlstrecke = "ZPSTRECKE";
    			} else {													// normales Training
    				sqlstrecke = "STRECKE";
    			}
    			
    			String sql = "SELECT ACTP_TEIL_ID, ZEITPUNKT, LATITUDE, LONGITUDE, GPS_PUNKT, WIND, GANG, ENERGIE, PULS, RPM, LEISTUNG, GESCHWINDIGKEIT, STRECKE, ZPSTRECKE, STEIGUNG, HOEHE, NAME, VORNAME, USER_ID " +
    					"FROM (SELECT a.*, u.* FROM ACTPOS a INNER JOIN TEILNAHME t ON t.TEIL_ID = a.ACTP_TEIL_ID INNER JOIN USER u ON u.USER_ID = t.TEIL_USER_ID WHERE t.TEIL_RENN_ID = '" + RennenId + 
    					"' ORDER BY a.ACTP_ID DESC) AS TMP GROUP BY ACTP_TEIL_ID ORDER BY " + sqlstrecke + " DESC, ZEITPUNKT";
    			st.executeQuery (sql);
    			ResultSet rs = st.getResultSet();
    			rs.last();
    			int anz = rs.getRow();
    			if (anz > 0) {
    				ret = new String[anz];
    				rs.first();
    				int count = 0;
    				do {
    					// TODO: ZEITPUNKT  fehlt noch
    					String vorname = rs.getString("VORNAME");
    					String name = rs.getString("NAME");    					
    					double latitude = rs.getDouble("LATITUDE");
    					double longitude = rs.getDouble("LONGITUDE");
    					long gps_punkt = rs.getLong("GPS_PUNKT");
    					int wind = rs.getInt ("WIND");
    					int gang = rs.getInt ("GANG");
    					int energie = rs.getInt ("ENERGIE");
    					int puls = rs.getInt ("PULS");
    					double rpm = rs.getDouble("RPM");
    					double leistung = rs.getDouble("LEISTUNG");
    					double geschwindigkeit = rs.getDouble("GESCHWINDIGKEIT");
    					double strecke = rs.getDouble("STRECKE");
    					double steigung = rs.getDouble("STEIGUNG");
    					double hoehe = rs.getDouble("HOEHE");   					
    					double zpstrecke = rs.getDouble("ZPSTRECKE");
    					int userID = rs.getInt ("USER_ID");
    					ret[count++] = vorname + ";" + name + ";" + latitude + ";" + longitude + ";" + gps_punkt + ";" + wind + ";" + gang +
    						";" + energie + ";" + puls + ";" + rpm + ";" + leistung + ";" + geschwindigkeit +
    						";" + strecke + ";" + steigung + ";" + hoehe + ";" + zpstrecke + ";" + userID;
    				} while (rs.next());
    			}
    			rs.close ();
    			st.close ();
    			return(ret);
    	} catch (Exception e) {
    		ret[0] = "WEBSV-Fehler! "+e.toString();
    		return(ret); 
    	}
    }
}
