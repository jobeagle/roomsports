import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
 * Server.java: Server-Klasse von RoomSports
 *****************************************************************************
 *
 * Serverfunktionalitäten von RS für die Netzwerktrainings
 * Verwendet wird TCP-Kommunikation.
 * Übernommen vom MTBSRaceService des Onlinetrainings (dort wird Axis2 verwendet) am 8.5.2013
 */

public class Server {
    private static final String url = "jdbc:sqlite:"+Global.strPfad+"rsserver.sqlite"; 
    private static final String jdbc = "org.sqlite.JDBC";
    private static final int port = 4488;
    public static Connection con;
    
    private static SimpleDateFormat tfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final int[] WindPunkt = {0,0};
	private static final int[] WindStaerke = {0,0};
	private static final int Windbereich = 30;
	private static final int WindMAxGPSPunkt = 400;
	private static final int kulanzzeit = 15;				// Zeit in Minuten für verspäteten Anmeldung/Einstieg ins Rennen

	public  static final String eom = "*EOM*";				// markiert End of Message
	public  static final String trz  = "~";					// Trennzeichen LAN Kommunikation (~)

	class NetworkService extends Thread {
		private ServerSocket serverSocket;
		private ExecutorService pool;
		public NetworkService(ExecutorService pool, ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		    this.pool = pool;
		}
		public void run() {
			Mlog.debug("NetworkService startet... url:"+url);
			try {
				while (true) {
					Mlog.debug("Client Accept.");
					Socket clientSocket = serverSocket.accept();
					pool.execute(new ServerThread(serverSocket, clientSocket));
				}
			} catch (Exception e) {
				Mlog.error("Server Kommunikation: allg. Fehler!");
				Mlog.ex(e);
			}
		};				
	}
	
	/**
	 * Dieser Thread liest vom Clientsocket einen String, trennt die mit ~ getrennten Teile,
	 * führt den entsprechenden Befehl (Klassenmethode) aus und sendet die Antwort über den
	 * Clientsocket zurück.
	 * 
	 * Aufbau der Kommunikation:
	 * empfangen wird:
	 * Befehl~Parameter1~Parameter2;...
	 * z. B. "rennenliste~2010998423947234" oder "rennenliste~null"
	 * 
	 * gesendet wird:
	 * Antworten getrennt mit ~ \n und am Ende "*EOM*"
	 * z. B. Bei rennenliste:
	 * "OT Tauchersreuth mit Dynamik 30.01.2012.~1.01.2013 0:00~1~0\n"
	 * "Zielpulstraining Lillinghof 24.04.2012.~1.01.2013 0:00~1~0\n"
	 * "*EOM*"
	 */
	class ServerThread extends Thread {
		@SuppressWarnings("unused")
		private ServerSocket serverSocket;
		private Socket clientSocket = null;
		ServerThread(ServerSocket serverSocket,Socket client) { //Server/Client-Socket
			    this.clientSocket = client;
			    this.serverSocket = serverSocket;
		}
		public void run() {
			String[] kette = null;		// Befehlskette mit Parametern
			Vector<String> ergListe = null;
			String befehl;
			Mlog.debug("Server Thread gestartet!");
			PrintWriter out;
			try {
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String clientInput;
				
				while (!(clientInput = in.readLine()).equals(eom)) {					
					kette = clientInput.split(trz);
					befehl = kette[0];
					Mlog.debug("Server, read: " + clientInput);
					if (befehl.equalsIgnoreCase("rennenliste")) {
						ergListe = RennenListe(kette[1]);
						String ergListe0 = (String) ergListe.firstElement();
						if (ergListe0.startsWith("ERR"))
							Mlog.error("Server-Error: " + ergListe0);
						else {
							if (!ergListe0.equals("0")) { 
								Iterator<String> vIt = ergListe.iterator();
								while (vIt.hasNext()) {
									String rennen = vIt.next()+"";
									Mlog.debug("Server, Rennen: " + rennen);
									out.println(rennen);
								}
							}
						}
					} 
					if (befehl.equalsIgnoreCase("insertteilnahme")) {
						String erg = InsertTeilnahme(kette[1], kette[2]);
						if (erg.startsWith("ERR"))
							Mlog.error("Server-Error: " + erg);
						out.println(erg);
					}
					if (befehl.equalsIgnoreCase("deleteteilnahme")) {
						String erg = DeleteTeilnahme(kette[1], kette[2]);
						if (erg.startsWith("ERR"))
							Mlog.error("Server-Error: " + erg);
						out.println(erg);
					}
					if (befehl.equalsIgnoreCase("getkonfiguration")) {
						String erg = GetKonfiguration(kette[1], kette[2]);
						if (erg.startsWith("ERR"))
							Mlog.error("Server-Error: " + erg);
						out.println(erg);
					}
					if (befehl.equalsIgnoreCase("wartebisstart")) {
						String erg = WarteBisStart(kette[1], kette[2]);
						if (erg.startsWith("ERR"))
							Mlog.error("Server-Error: " + erg);
						out.println(erg);
					}
					if (befehl.equalsIgnoreCase("insertactpos")) {
						String erg = InsertActPos(kette[1], kette[2], kette[3], kette[4], kette[5], kette[6], kette[7], kette[8], kette[9], kette[10], kette[11], 
								kette[12], kette[13], kette[14], kette[15], kette[16]);
						if (erg.startsWith("ERR"))
							Mlog.error("Server-Error: " + erg);
						out.println(erg);
					}
					if (befehl.equalsIgnoreCase("getteilnahmeid")) {
						String erg = GetTeilnahmeID(kette[1], kette[2]);
						if (erg.startsWith("ERR"))
							Mlog.error("Server-Error: " + erg);
						out.println(erg);
					}
					if (befehl.equalsIgnoreCase("getrennenid")) {
						String erg = GetRennenID(kette[1], kette[2]);
						if (erg.startsWith("ERR"))
							Mlog.error("Server-Error: " + erg);
						out.println(erg);
					}					
					if (befehl.equalsIgnoreCase("actposliste")) {
						ergListe = ActPosListe(kette[1], kette[2]);
						String ergListe0 = (String) ergListe.firstElement();
						if (ergListe0.startsWith("ERR")) {
							Mlog.error("Server-Error: " + ergListe0);
							out.println(ergListe0);
						} else {
							if (!ergListe0.equals("0")) { 
								Iterator<String> vIt = ergListe.iterator();
								while (vIt.hasNext()) {
									String pos = vIt.next()+"";
									Mlog.debug("Server, Pos: " + pos);
									out.println(pos);
								}
							}
						}
					} 
					if (befehl.equalsIgnoreCase("insertuser")) {
						String erg = InsertUser(kette[1], kette[2], kette[3], kette[4], kette[5]);
						if (erg.startsWith("ERR"))
							Mlog.error("Server-Error: " + erg);
						out.println(erg);
					}
					if (befehl.equalsIgnoreCase("ergliste")) {
						ergListe = ergListe(kette[1], kette[2], kette[3]);
						String ergListe0 = (String) ergListe.firstElement();
						if (ergListe0.startsWith("ERR")) {
							Mlog.error("Server-Error: " + ergListe0);
							out.println(ergListe0);
						} else {
							//if (!ergListe0.equals("0")) { 
								Iterator<String> vIt = ergListe.iterator();
								while (vIt.hasNext()) {
									String erg = vIt.next()+"";
									Mlog.debug("Server, Erg: " + erg);
									out.println(erg);
								}
							//}
						}
					} 
				}
				out.println(eom);
				out.close(); 
				in.close(); 
				clientSocket.close();
			} catch (Exception e) {
				Mlog.error("Server Thread: allg. Fehler!");
				Mlog.ex(e);
			}
		}
		ServerThread(Socket s) { clientSocket = s; }
	}
	
	/**
	 * Konstruktor unserer Serverklasse.
	 * Hier wird der Threadpool initialisiert und der erste Kommunikationsthread gestartet.
	 */
	Server() {
		final ExecutorService pool;
		final ServerSocket serverSocket;
		Mlog.debug("Server startet...");
		try {
			serverSocket = new ServerSocket(port);
		    pool = Executors.newCachedThreadPool();

			Thread t1 = new Thread(new NetworkService(pool, serverSocket));
			Mlog.debug("Start NetworkService, Thread: "+Thread.currentThread());
			//Start der run-Methode von NetworkService: warten auf Client-request
			t1.start();
		} catch (Exception e) {
			Mlog.error("Server Thread: allg. Fehler!");
			Mlog.ex(e);
		}
	}
   
	/**
	 * Öffnet die SQLite Datenbank und gibt Connection zurück.
	 * @return Connection (klassenweit definiert)
	 */
	private Connection connectDB() {
        try {
			Class.forName(jdbc);
	        con = DriverManager.getConnection( url, "", "" );	
		} catch (Exception e) {
			Mlog.ex(e);
		}
        return con;
	}

    /**
     * Wenn keine Connection zur DB besteht, dann connecten!
     */
    public void reConnectDB() {
    	if (con == null)
    		con = connectDB();
    }
    
    /**
     * InsertUser trägt einen neuen User mit Seriennummer in die Datenbank ein.
     * TODO: später Hash-Code eintragen
     * @param mAC		MAC-Adresse des Mitfahrers
     * @param name		Name des Teilnehmers
     * @param ort		Wohnort das Teilnehmers
     * @param gebjahr	Geburtsjahr (evtl. für Altersklassen)
     * @param trainer	Trainingsgerät
     * @return OK oder Fehlermeldung
     */
    public String InsertUser(String mAC, String name, String ort, String gebjahr, String trainer) {
    	reConnectDB();
    	try {
			Statement st = con.createStatement();
			st.executeUpdate("INSERT INTO USER (MACADR, NAME, ORT, GEBURTSJAHR, TRAINER) VALUES('"+mAC+"','"+name+"','"+ort+"','"+gebjahr+"','"+trainer+"')");
			st.close();
    		return("OK");
    	} catch (Exception e) {
    		return("ERROR! "+e.toString()); 
        }
    }

    /**
     * InsertTeilnahme trägt eine neue Teilnehme mittels (codierter) Seriennummer und Rennenbezeichnung in die Datenbank ein.
     * Der User muß bereits eingetragen und das Rennen in der Zukunft (abzgl. Kulanzzeit) sein.
     * @param mAC		MAC-Adresse des Mitfahrers
     * @param Rennen	Bezeichnung des Rennens
     * @return OK oder Fehlermeldung
     */
    public String InsertTeilnahme(String mAC, String Rennen) {
    	reConnectDB();
    	try {
			Statement st = con.createStatement();
			ResultSet rsuser = st.executeQuery ("SELECT USER_ID FROM USER WHERE MACADR = '" + mAC + "'");
			if (!rsuser.next()) {
				rsuser.close();
				st.close();
				return("ERROR! User ist nicht vorhanden!");
			}
			long userid = rsuser.getLong("USER_ID");			
			
			//st.executeQuery ("SELECT RENN_ID, TEILNEHMER, TEILNEHMERLIMIT FROM RENNEN WHERE BEZEICHNUNG = '" + Rennen + "' AND STARTZEIT > DATE_SUB(NOW(), INTERVAL " + kulanzzeit + " MINUTE)");
			ResultSet rsrennen = st.executeQuery ("SELECT RENN_ID, TEILNEHMER, TEILNEHMERLIMIT FROM RENNEN WHERE BEZEICHNUNG = '" + Rennen + "' AND STARTZEIT > DATETIME('NOW', '-" + kulanzzeit + " MINUTE')");
			if (!rsrennen.next()) {
				rsrennen.close();
				st.close();
				return("ERROR! Das Rennen ist nicht vorhanden!");
			}
			long rennenid = rsrennen.getLong("RENN_ID");	
			short tlimit = rsrennen.getShort("TEILNEHMERLIMIT");
			short teiln = rsrennen.getShort("TEILNEHMER");

			if (++teiln > tlimit) 
				return("ERROR! Das Teilnehmerlimit ist erreicht!");
			
			st.executeUpdate("INSERT INTO TEILNAHME (TEIL_USER_ID, TEIL_RENN_ID, ANGEMELDET_AM) VALUES ('"+userid+"','"+rennenid+"',DATETIME('NOW'))");

			ResultSet rsteilnahme = st.executeQuery ("SELECT COUNT(*) TEILNEHMER FROM TEILNAHME WHERE TEIL_RENN_ID = '" + rennenid + "'");
			if (!rsteilnahme.next()) {
				rsteilnahme.close();
				st.close();
				return("ERROR! Fehler beim zaehlen der Teilnehmer!");
			}
			teiln = rsteilnahme.getShort("TEILNEHMER");

			st.executeUpdate("UPDATE RENNEN SET TEILNEHMER = "+teiln+" WHERE RENN_ID = '"+rennenid+"'");
			rsuser.close();
			rsrennen.close();
			st.close();
    		//return("OK");
			return(userid+"");
    	} catch (Exception e) {
    		return("ERROR! "+e.toString()); 
        }
    }

    /**
     * DeleteTeilnahme löscht eine TEILNAHME mittels (codierter) Seriennummer und Rennenbezeichnung aus der Datenbank.
     * Der User muß bereits eingetragen und das Rennen in der Zukunft sein.
     * @param mAC		MAC-Adresse des Mitfahrers
     * @param Rennen	Rennbezeichnung
     * @return OK oder Fehlermeldung
     */
    public String DeleteTeilnahme(String mAC, String Rennen) {
    	reConnectDB();
    	try {
			Statement st = con.createStatement();
			ResultSet rsuser = st.executeQuery ("SELECT USER_ID FROM USER WHERE MACADR = '" + mAC + "'");
			if (!rsuser.next()) {
				rsuser.close();
				st.close();
				return("ERROR! User ist nicht vorhanden!");
			}
			long userid = rsuser.getLong("USER_ID");			
			
			//st.executeQuery ("SELECT RENN_ID FROM RENNEN WHERE BEZEICHNUNG = '" + Rennen + "' AND STARTZEIT > NOW()");
			ResultSet rsrennen = st.executeQuery ("SELECT RENN_ID FROM RENNEN WHERE BEZEICHNUNG = '" + Rennen + "' AND STARTZEIT > DATETIME('NOW')");
			if (!rsrennen.next()) {
				rsrennen.close();
				st.close();
				return("ERROR! Das Rennen ist nicht vorhanden!");
			}
			long rennenid = rsrennen.getLong("RENN_ID");			

			st.executeUpdate("DELETE FROM TEILNAHME WHERE TEIL_USER_ID = "+userid+" AND TEIL_RENN_ID = "+rennenid);

			ResultSet rsteilnahme = st.executeQuery ("SELECT COUNT(*) TEILNEHMER FROM TEILNAHME WHERE TEIL_RENN_ID = '" + rennenid + "'");
			if (!rsteilnahme.next()) {
				rsteilnahme.close();
				st.close();
				return("ERROR! Fehler beim zaehlen der Teilnehmer!");
			}
			short teiln = rsteilnahme.getShort("TEILNEHMER");

			st.executeUpdate("UPDATE RENNEN SET TEILNEHMER = "+teiln+" WHERE RENN_ID = '"+rennenid+"'");
			rsuser.close();
			rsrennen.close();
			st.close();
    		return("OK");
    	} catch (Exception e) {
    		return("ERROR! "+e.toString()); 
        }
    }

    /**
     * GetTeilnahmeID ermittelt die ID der aktuellen Teilnahme mittels (codierter) Seriennummer und Rennenbezeichnung.
     * Die Teilnahme-Id wird im Client zum eintragen der Positionen verwendet.
     * @param mAC		MAC-Adresse des Mitfahrers
     * @param Rennen	Rennenbezeichnung
     * @return ID der Teilnahme
     */
    public String GetTeilnahmeID(String mAC, String Rennen) {
    	reConnectDB();
    	try {
			Statement st = con.createStatement();
			ResultSet rsuser = st.executeQuery ("SELECT USER_ID FROM USER WHERE MACADR = '" + mAC + "'");
			if (!rsuser.next()) {
				rsuser.close();
				st.close();
				return("ERROR! User ist nicht vorhanden!");
			}
			long userid = rsuser.getLong("USER_ID");			
			
			ResultSet rsrennen = st.executeQuery ("SELECT RENN_ID FROM RENNEN WHERE BEZEICHNUNG = '" + Rennen + "'");
			if (!rsrennen.next()) {
				rsrennen.close();
				st.close();
				return("ERROR! Das Rennen ist nicht vorhanden!");
			}
			long rennenid = rsrennen.getLong("RENN_ID");			

			ResultSet rs = st.executeQuery ("SELECT TEIL_ID FROM TEILNAHME WHERE TEIL_USER_ID = " + userid + " AND TEIL_RENN_ID = " + rennenid);
			if (!rs.next()) {
				rs.close();
				st.close();
				return("ERROR! Die Teilnahme an diesem Rennen ist nicht eingetragen!");
			}
			long teilnahmeid = rs.getLong("TEIL_ID");
			rs.close();
			rsuser.close();
			rsrennen.close();
			st.close();
    		return(teilnahmeid + "");
    	} catch (Exception e) {
    		return("ERROR! "+e.toString()); 
        }
    }

    /**
     * WarteBisStart ermittelt die Startzeit des eingetragenen Rennens mittels MAC-Adresse und Rennenbezeichnung.
     * Anschliessend wird bis zum Startzeitpunkt gewartet und eine Zufallszahl für die Anzahl der Wartesekunden zurückgegeben.
     * @param mAC		MAC-Adresse des Mitfahrers
     * @param Rennen	Rennbezeichnung
     * @return Zufallszahl 0 bei OK, oder Fehlermeldung
     */
    public String WarteBisStart(String mAC, String rennen) {
    	SimpleDateFormat tfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	reConnectDB();
    	try {
			Statement st = con.createStatement();
			//ResultSet rsuser = st.executeQuery ("SELECT USER_ID FROM USER WHERE SNR = '" + SNR + "'");
			ResultSet rsuser = st.executeQuery ("SELECT USER_ID, RENN_ID FROM USER JOIN TEILNAHME ON TEIL_USER_ID = USER_ID AND TEIL_RENN_ID = RENN_ID " +
					" JOIN RENNEN ON BEZEICHNUNG = '" + rennen + "' WHERE MACADR = '" + mAC + "'");
			if (!rsuser.next()) {
				rsuser.close();
				st.close();
				return("ERROR! Der Teilnehmer: " + mAC + " ist nicht angemeldet zum Rennen: " + rennen + "!");
			}
			//long userid = rsuser.getLong("USER_ID");			
			long rennid = rsuser.getLong("RENN_ID");
			
			// Bedingung für den Countdown: Start in max. 30 Min., der Start darf max. "kulanzzeit" zurückliegen und man muss angemeldet sein!
			//String sql = "SELECT RENN_ID, STARTZEIT, count(TEIL_ID) cnt FROM RENNEN join TEILNAHME on teil_renn_id = renn_id WHERE RENN_ID = '" + rennid + 
			//				"' AND STARTZEIT > DATETIME('NOW', '-" + kulanzzeit + " MINUTE') AND STARTZEIT < DATETIME('NOW', '+30 MINUTE') AND STATUS != 2";
			// wegen Wiedereinstieg wird keine Kulanzzeit mehr abgefragt!
			String sql = "SELECT RENN_ID, STARTZEIT, count(TEIL_ID) cnt FROM RENNEN join TEILNAHME on teil_renn_id = renn_id WHERE RENN_ID = '" + rennid + 
			"' AND STARTZEIT < DATETIME('NOW', '+30 MINUTE') AND STATUS != 2";
			
			ResultSet rsrennen = st.executeQuery (sql);
			// wegen count kommt jetzt immer ein Satz!
			if (!rsrennen.next()) {
				rsrennen.close();
				st.close();
				return("ERROR! Fehler beim Countdown-Select!");
			}
			long rennenid = rsrennen.getLong("RENN_ID");
			if (rennenid == 0)
				return("ERROR! Das Rennen ist nicht im aktuellen Zeitfenster!");
			int teilAnz = rsrennen.getInt("CNT");
			String date = rsrennen.getString ("STARTZEIT");
			Mlog.debug("Startzeit: " + date);
			java.util.Date startzeit = tfmt.parse(date);

			rsuser.close();
			rsrennen.close();

			java.util.Date utilDate = new java.util.Date();
			java.sql.Date sqlNow = new java.sql.Date(utilDate.getTime());
			Mlog.debug("startzeit: " + startzeit + " // " + "sqlNow.getTime(): " + sqlNow.getTime());
			
			long sek = (startzeit.getTime() - sqlNow.getTime()) / 1000;
			
			if (sek > 0) {
				st.close();	
				if ((sek % 30) == 0)
					return("angemeldet: "+teilAnz+" zuletzt: "+getLetzteAnmeldung(rennenid));
				else
					return(sek+"");						// Startfreigabe ist noch nicht erfolgt!
			}

			// Globalen für Gegenwind zurücksetzen
			if (sek <= 0 && (WindPunkt[0] != 0 || WindPunkt[1] != 0)) {
				WindPunkt[0] = 0;
				WindPunkt[1] = 0;
				WindStaerke[0] = 0;
				WindStaerke[1] = 0;				
			}
			
			st.executeUpdate("UPDATE RENNEN SET STATUS = 1 WHERE RENN_ID = "+rennenid+" AND STATUS = 0");
			st.close();			
			
    		return("0");						
    	} catch (Exception e) {
    		Mlog.ex(e);
    		return("ERROR! "+e.toString());	
        }
    }

    /**
     * GetRennenID ermittelt die ID des Rennens anhand der Rennenbezeichnung.
     * Die Renn-Id wird im Client zum ermitteln der Positionen aller Teilnehmer verwendet.
     * @param mAC		MAC-Adresse des Mitfahrers
     * @param Rennen	Rennenbezeichnung
     * @return ID des Rennens
     */
    public String GetRennenID(String mAC, String Rennen) {
    	reConnectDB();
    	try {
			Statement st = con.createStatement();
			ResultSet rsrennen = st.executeQuery ("SELECT RENN_ID FROM RENNEN WHERE BEZEICHNUNG = '" + Rennen + "'");
			if (!rsrennen.next()) {
				rsrennen.close();
				st.close();
				return("ERROR! Das Rennen ist nicht vorhanden!");
			}
			long rennenid = rsrennen.getLong("RENN_ID");			
			rsrennen.close();
			st.close();
    		return(rennenid + "");
    	} catch (Exception e) {
    		return("ERROR! "+e.toString()); 
        }
    }

    /**
     * GetKonfiguration ermittelt die Konfigurationswerte (MIN-Leistung etc.) anhand der Rennenbezeichnung.
     * @param mAC		MAC-Adresse des Mitfahrers
     * @param Rennen	Rennenbezeichnung
     * @return "MINLEISTUNG ; DYNAMIK" ...
     */
    public String GetKonfiguration(String mAC, String Rennen) {
		DecimalFormat zfk1 = new DecimalFormat("0.0");  
		DecimalFormat zfk3 = new DecimalFormat("0.000");  
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		zfk1.setDecimalFormatSymbols(dfs);
		zfk3.setDecimalFormatSymbols(dfs);
		
    	reConnectDB();
    	try {
			Statement st = con.createStatement();
			ResultSet rsrennen = st.executeQuery ("SELECT MINLEISTUNG, MAXLEISTUNG, DYNAMIK, TOUR, PULSMIN, PULSMAX, TYP, SCHIKANE, LEISTUNGSFAKTOR, TFNORMAL, TFMIN, KONSTANT_CW, KONSTANT_K2, GEWICHT_FAHRER, GEWICHT_BIKE FROM RENNEN WHERE BEZEICHNUNG = '" + Rennen + "'");
			if (!rsrennen.next()) {
				rsrennen.close();
				st.close();
				return("ERROR! Das Rennen ist nicht vorhanden!");
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
			rsrennen.close();
			st.close();
			// die letzten beiden Parameter stehen für 1. und letzten GPS-Punkt. Damit können bei Erweiterung auch Teilabschnitte gefahren werden
			// aktuell wird es nur im Onlinetraining unterstützt!
			String ret = minleistung+trz+dynamik+trz+tour+trz+pulsmin+trz+pulsmax+trz+maxleistung+trz+typ+trz+zfk1.format(lf)+trz + 
			             schikane+trz+nTF+trz+nTFmin+trz+zfk3.format(cw)+trz+zfk3.format(k2)+trz+zfk1.format(gewichtBike)+trz+zfk1.format(gewichtFahrer)+trz+"0"+trz+"0";			
    		return(ret);
    	} catch (Exception e) {
    		return("ERROR! "+e.toString()); 
        }
    }

    /**
     * InsertActPos trägt die aktuelle Position und weitere Daten in die Tabelle ACTPOS ein.
     * Das ist die neue Version mit Übertragung der Zielpulsstrecke.
     * @param mAC				MAC-Adresse des Mitfahrers
     * @param ID				Teilnahme-ID
     * @param Lat				Breitengrad
     * @param Long				Längengrad
     * @param Gps_Punkt			Index des GPS-Punktes
     * @param Wind				Windgeschwindigkeit
     * @param Gang				Gang
     * @param Energie			kCal
     * @param Puls				Pulsrate
     * @param RPM				Kurbelumdrehungen
     * @param Leistung			akt. Leistung
     * @param Geschwindigkeit	akt. Geschwindigkeit
     * @param Strecke			zurückgelegte Strecke
     * @param Steigung			akt. Steigung
     * @param Hoehe				akt. Höhe
     * @param ZPStrecke			zurückgelegte Strecke im Zielpulsbereich
     * @return OK oder Fehlermeldung
     */
    public String InsertActPos(String mAC, String ID, String Lat, String Long, String Gps_Punkt, String Wind, String Gang, String Energie, String Puls, String RPM, String Leistung, String Geschwindigkeit, String Strecke, String Steigung, String Hoehe, String ZPStrecke) {
    	reConnectDB();
    	try {
			Statement st = con.createStatement();
			String sql = "INSERT INTO ACTPOS (ACTP_TEIL_ID, ZEITPUNKT, LATITUDE, LONGITUDE, GPS_PUNKT, WIND, GANG, ENERGIE, PULS, RPM, LEISTUNG, GESCHWINDIGKEIT, STRECKE, STEIGUNG, HOEHE, ZPSTRECKE) VALUES ("+
			             ID+",DATETIME('NOW'),"+Lat+","+Long+","+Gps_Punkt+","+Wind+","+Gang+","+Energie+","+Puls+","+RPM+","+Leistung+","+Geschwindigkeit+","+Strecke+","+Steigung+","+Hoehe+","+ZPStrecke+")";
			st.executeUpdate(sql);
			st.close();
			if (Wind.contentEquals("1"))
				return(windautomatik(new Integer(Gps_Punkt)));
			
    		return("OK");
    	} catch (Exception e) {
    		return("ERROR! "+e.toString()); 
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
    		//st.executeQuery (sql);
    		ResultSet rslastTeiln = st.executeQuery(sql);
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
     * Die Windautomatik setzt beim ersten Aufruf zwei zufällige Punkte und zufällige Windstärken fest und
     * prüft anschliessend, ob der übergebene Punkt im Bereich (Punkt + 30) ist und liefert die zugewiesene Windstärke zurück.
     * 
     * @param punkt
     * @return Windstärke (1..6) als Zahl
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
     * RennenListe liefert eine mit trz-getrenntes Stringarry aller zukünftigen Rennen zurück.
     * Wird MAC-Adresse übergeben, dann wird ein Array mit allen Rennen des Users zurückgegeben.
     * @param mAC		MAC-Adresse des Mitfahrers
     * @return Vector aus Strings mit Inhalt aus Datenbank mit ";" getrennt.
     */
    public Vector<String> RennenListe(String mAC) {
    	Vector<String> vret = new Vector<String>();
    	Statement st;
    	ResultSet rs;
		int count = 0;
    	reConnectDB();
    	try {
    		if (mAC.equals("null")) { 	// alle Rennen zurückgeben
    			st = con.createStatement();   			
    			rs = st.executeQuery ("SELECT BEZEICHNUNG, TYP, TEILNEHMER, STARTZEIT FROM RENNEN WHERE STATUS != 2 ORDER BY STARTZEIT");
    			while (rs.next()) {
    				String bez = rs.getString ("BEZEICHNUNG");
    				int anzteil = rs.getInt ("TEILNEHMER");
    				String date = rs.getString ("STARTZEIT");
    				java.util.Date startzeit = tfmt.parse(date);
    				vret.addElement(bez + trz + tfmt.format(startzeit) + trz + anzteil);
    				count++;
    			};
    		} else {			// SNR wurde übergeben: alle Rennen des Teilnehmers zurückliefern
    			st = con.createStatement();
    			rs = st.executeQuery ("SELECT BEZEICHNUNG, TYP, TEILNEHMER, STARTZEIT FROM RENNEN r INNER JOIN TEILNAHME t ON t.TEIL_RENN_ID = r.RENN_ID INNER JOIN USER u ON u.USER_ID = t.TEIL_USER_ID where u.macadr = '"
    					+mAC+"'"+" AND STATUS != 2 ORDER BY STARTZEIT");
   				while (rs.next ()) {
    				String bez = rs.getString ("BEZEICHNUNG");
    				int anzteil = rs.getInt ("TEILNEHMER");
    				String date = rs.getString ("STARTZEIT");
    				java.util.Date startzeit = tfmt.parse(date);
    				vret.addElement(bez + trz + tfmt.format(startzeit) + trz + anzteil);
    				count++;
    			};
    		}
			if (count == 0)
				vret.addElement("0");
			rs.close ();
			st.close ();
		return(vret);
    	} catch (Exception e) {
    		if (vret.size() > 0)
    			vret.setElementAt("ERROR! "+e.toString(), 0) ;
    		else
				vret.addElement("ERROR! "+e.toString());
    		return(vret); 
    	}
    }

    /**
     * Rückgabe der aktuellen Positionen aller Teilnehmer eines Rennens als Stringarray.
     * @param mAC		MAC-Adresse des Mitfahrers
     * @param RennenId	Id des Rennens
     * @return Vector aus Datenbankwerten mit Trennzeichen (trz) getrennt.
     */
    public Vector<String> ActPosListe(String mAC, String RennenId) { 
    	Vector<String> vret = new Vector<String>();
    	String renntyp = "";
    	String sqlstrecke = "";
    	
    	reConnectDB();
    	try {
    		Statement st = con.createStatement();
    		ResultSet rsrennen = st.executeQuery ("SELECT TYP FROM RENNEN WHERE RENN_ID = " + RennenId);
    		if (!rsrennen.next()) {
    			rsrennen.close();
    			st.close();
    			vret.addElement("ERROR! Das Rennen ist nicht vorhanden!");
    			return(vret);
    		}
    		renntyp = rsrennen.getString("TYP");
    		rsrennen.close();

    		if (renntyp.matches("(.*)puls(.*)")) {						// Ergometer Pulstraining
    			sqlstrecke = "ZPSTRECKE";
    		} else {													// normales Training
    			sqlstrecke = "STRECKE";
    		}

    		String sql = "SELECT ACTP_TEIL_ID, ZEITPUNKT, LATITUDE, LONGITUDE, GPS_PUNKT, WIND, GANG, ENERGIE, PULS, RPM, LEISTUNG, GESCHWINDIGKEIT, STRECKE, ZPSTRECKE, STEIGUNG, HOEHE, NAME, VORNAME, USER_ID " +
    		"FROM (SELECT a.*, u.* FROM ACTPOS a INNER JOIN TEILNAHME t ON t.TEIL_ID = a.ACTP_TEIL_ID INNER JOIN USER u ON u.USER_ID = t.TEIL_USER_ID WHERE t.TEIL_RENN_ID = '" + RennenId + 
    		"' ORDER BY a.GPS_PUNKT) AS TMP GROUP BY ACTP_TEIL_ID ORDER BY " + sqlstrecke + " DESC, ZEITPUNKT";
    		ResultSet rs = st.executeQuery(sql);
    		while (rs.next()) {
    			// TODO ZEITPUNKT fehlt noch - wird evtl. hier nicht benötigt!
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
    			int userid = rs.getInt ("USER_ID");
    		    
    			vret.addElement(vorname + trz + name + trz + latitude + trz + longitude + trz + gps_punkt + trz + wind + trz + gang +
    					trz + energie + trz + puls + trz + rpm + trz + leistung + trz + geschwindigkeit +
    					trz + strecke + trz + steigung + trz + hoehe + trz + zpstrecke + trz + userid);
    		};
    		rs.close ();
    		st.close ();
    		return(vret);
    	} catch (Exception e) {
    		if (vret.size() > 0)
    			vret.setElementAt("ERROR! "+e.toString(), 0) ;
    		else
    			vret.addElement("ERROR! "+e.toString());
    		return(vret); 
    	}
    }

    /**
     * Rückgabe der aktuellen Rangliste aller Teilnehmer eines Rennens als Stringarray.
     * @param mAC		MAC-Adresse des Mitfahrers
     * @param rennenId	Id des Rennens
     * @param ende01	"1": Ziel erreicht, Ergebnis speichern / "0": nur Ergebnisliste zurückliefern
     * @return Vector aus Datenbankwerten mit Trennzeichen (trz) getrennt.
     */
    public Vector<String> ergListe(String mAC, String rennenId, String ende01) { 
    	Vector<String> vret = new Vector<String>();
    	String renntyp = "";
    	String sqlstrecke = "";
    	int teilId = 0;
    	int anzTeilnehmer = 0;
    	boolean zprennen = false;
    	
    	reConnectDB();
    	try {
    		Statement st = con.createStatement();
    		String sql = "SELECT TYP, TEIL_ID, TEILNEHMER FROM RENNEN JOIN TEILNAHME ON TEIL_RENN_ID = RENN_ID " +
				"JOIN USER ON USER_ID = TEIL_USER_ID WHERE RENN_ID = " + rennenId + " AND MACADR = '" + mAC + "'";
    		ResultSet rsrennen = st.executeQuery(sql);
    		if (!rsrennen.next()) {
    			rsrennen.close();
    			st.close();
    			vret.addElement("ERROR! Das Rennen ist nicht aktiv!");
    			return(vret);
    		}
    		renntyp = rsrennen.getString("TYP");
    		teilId = rsrennen.getInt("TEIL_ID");
    		anzTeilnehmer = rsrennen.getInt("TEILNEHMER");
    		rsrennen.close();

    		if (renntyp.equalsIgnoreCase("ZIELPULS")) {	// Ergometer Pulstraining
    			zprennen = true;
    			sqlstrecke = "ZPSTRECKE";
    		}  else {									// normales Training
    			sqlstrecke = "STRECKE";
    		}
    		
    		// hier prüfen, ob das eigene Ergebnis schon gespeichert wurde und wenn nicht, dann ohne Platzierung inserten
    		sql = "SELECT ERGE_ID FROM ERGEBNIS WHERE ERGE_TEIL_ID = " + teilId + " AND ERGE_RENN_ID = " + rennenId;
    		ResultSet rsergebnis = st.executeQuery(sql);
    		if (!rsergebnis.next()) {
    			rsergebnis.close();
    			Mlog.debug("kein Ergebnissatz vorhanden -> Insert!");
    			if (ende01.equals("1")) {
    				sql = "INSERT INTO ERGEBNIS (ERGE_TEIL_ID,ENERGIE,RPM_SCHNITT,PULS_SCHNITT,LEISTUNG_SCHNITT,ZEIT,ERGE_RENN_ID,ZPSTRECKE,PLATZ) " +
    					"SELECT ACTP_TEIL_ID, MAX(ENERGIE), AVG(rpm), AVG(puls), AVG(leistung), strftime('%s',max(zeitpunkt))-strftime('%s',STARTZEIT), " +
    					"RENN_ID, MAX("+sqlstrecke+"), 0 " +
    					"FROM ACTPOS join RENNEN on RENN_ID = " + rennenId + " WHERE ACTP_TEIL_ID = " + teilId;

    				st.executeUpdate(sql);
    			}
    		}
    		
    		// anschliessend das akt. Ergebnis selektieren ähnlich der Posliste!
    		sql = "SELECT ENERGIE, PULS_SCHNITT, RPM_SCHNITT, LEISTUNG_SCHNITT, ZPSTRECKE, ZEIT, NAME, PLATZ, USER_ID " +
    		"FROM ERGEBNIS INNER JOIN USER ON USER_ID = TEIL_USER_ID INNER JOIN TEILNAHME ON TEIL_ID = ERGE_TEIL_ID WHERE ERGE_RENN_ID = " + rennenId + " " +
    		"ORDER BY PLATZ DESC, ZEIT ASC";
    		ResultSet rs = st.executeQuery(sql);
    		while (rs.next()) {
    			int platz = rs.getInt("PLATZ");
    			String name = rs.getString("NAME");
    			String zeit = rs.getString("ZEIT");
    			int energie = rs.getInt("ENERGIE");
    			int puls = rs.getInt ("PULS_SCHNITT");
    			double rpm = rs.getDouble("RPM_SCHNITT");
    			double leistung = rs.getDouble("LEISTUNG_SCHNITT");
    			double zpstrecke = rs.getDouble("ZPSTRECKE");
    			int userid = rs.getInt ("USER_ID");

    			vret.addElement(platz + trz + zeit + trz + name + trz + energie + trz + puls + trz + rpm + 
    					trz + leistung + trz + zpstrecke + trz + userid);
    		};
    		rs.close ();
    		st.close ();
    		
    		raengeErmitteln(anzTeilnehmer, new Integer(rennenId), zprennen, false);
    		return(vret);
    	} catch (Exception e) {
    		if (vret.size() > 0)
    			vret.setElementAt("ERROR! "+e.toString(), 0) ;
    		else
    			vret.addElement("ERROR! "+e.toString());
    		return(vret); 
    	}
    }
    
    /**
     * Wenn alle Teilnehmer im Ziel sind, dann alle Platzierungen updaten und 
     * STATUS auf 2 (abgelaufen) setzen.
     * @param anzahl	Anzahl der gemeldeten Teilnehmer
     * @param rennId	RENN_ID aus der Datenbank
     * @param zpRennen	true bei Zielpulsrennen, sonst false
     * @param doit	bei true werden unabhängig von der Anzahl die Platzierungen upgedatet
     */
    public void raengeErmitteln(int anzahl, int rennId, boolean zpRennen, boolean doit) {
    	String sql;
    	int i = 0;
    	int anzErg = 0;
    	reConnectDB();
    	try {
    		Statement st = con.createStatement();
    		if (!doit) {	// anhand der Teilnehmeranzahl bestimmen ob alle im Ziel sind:
    			ResultSet rsanz = st.executeQuery ("SELECT COUNT(*) IMZIEL FROM ERGEBNIS WHERE ERGE_RENN_ID = " + rennId);
    			if (!rsanz.next()) {
    				rsanz.close();
    				st.close();
    				Mlog.error("ERROR! Fehler beim zaehlen der Teilnehmer im Ziel!");
    				return;
    			}
    			anzErg = rsanz.getInt("IMZIEL");    			
    		} 
    		if (anzErg == anzahl || doit) {
    			sql = "select ERGE_ID, ERGE_TEIL_ID from ERGEBNIS where ERGE_TEIL_ID in " +
    			"(select TEIL_ID from TEILNAHME where TEIL_RENN_ID = " + rennId + ") ";
    			if (zpRennen)
    				sql = sql + "order by ZPSTRECKE DESC, ZEIT, LEISTUNG_SCHNITT DESC";
    			else
    				sql = sql + "order by ZEIT, LEISTUNG_SCHNITT DESC";

    			ResultSet rs = st.executeQuery(sql);
    			while (rs.next()) {
    				i++;
    				int teilId = rs.getInt("ERGE_TEIL_ID");
    				int ergId = rs.getInt("ERGE_ID");
    				Mlog.info("Platz " + i + " wird für Teilnahme " + teilId + " geschrieben");
    	    		Statement st1 = con.createStatement();
    				st1.executeUpdate("update ERGEBNIS set PLATZ = " + i + " where ERGE_ID = " + ergId);
    	    		st1.close();
    			};
    			rs.close();
				st.executeUpdate("update RENNEN set STATUS = 2 where RENN_ID = " + rennId);	// Status auf abgelaufen setzen!
    		}
    		st.close();
    	} catch (Exception e) {
    		Mlog.error("Serverfehler beim ermitteln der Rangfolge!");
    		Mlog.ex(e);
    	}    	
    }
}
