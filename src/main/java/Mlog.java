import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import com.sun.jna.Platform;


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
 * Log.java: Klasse fürs Logging von RoomSports
 *****************************************************************************
 *
 * Klasse fürs Logging. Übernommen aus debug und umgestellt auf log4j am 14.9.2012
 */

public final class Mlog {
	private static boolean debugstatus = false;
	private static String logdateiname = new String(Platform.isWindows() ? System.getenv("APPDATA")+"\\roomsports\\"+"roomsports.log" : System.getProperty("user.home") + "/roomsports/roomsports.log");
	public  static Logger logger = null;
	  
	private Mlog() {
	    super();
	}
	 
	public static Logger getInstance() {
		if (logger == null)
			init();
	    return logger;
	}
	 

	/**
	 * Getterfunktion für Debugstatus
	 * @return debugstatus
	 */
	public static boolean isDebugstatus() {
		return debugstatus;
	}

	/**
	 * Setterfunktion für den Debugstatus
	 * @param pdebugstatus	Debug-logging aktivieren?
	 */
	public static void setDebugstatus(boolean pdebugstatus) {
		Mlog.debugstatus = pdebugstatus;
		if (pdebugstatus)
		     logger.setLevel(org.apache.log4j.Level.ALL);
	}

	/**
	 * Info gibt Infoausgabe auf Std-Output aus oder auch nicht (wenn debugstatus = false).
	 * Die Ausgabe wird immer zusätzlich in die Logdatei roomsports.log geschrieben.
	 * Zusätzlich wird ein Zeitstempel ausgegeben.
	 * 14.9.2012: Umstellung auf log4j
	 * @param ausgabe	Logging-String für Info-Level-Logging
	 */
	public static void info(String ausgabe) {
		logger.info(ausgabe);
	}

	/**
	 * Logging Debuglevel
	 * @param ausgabe	Logging-String für Debug-Level-Logging
	 */
	public static void debug(String ausgabe) {
		logger.debug(ausgabe);
	}

	/**
	 * Loggong Errorlevel
	 * @param ausgabe	Logging-String für Error-Level-Logging
	 */
	public static void error(String ausgabe) {
		logger.error(ausgabe);
	}

	/**
	 * log4j Ausgabe von Exceptions
	 * @param e  Exception
	 */
	public static void ex(Throwable e) {	
		logger.error("RS-Java Exception:", e);
	}

	/**
	 * Initialisiert das schreiben in die Logdatei, Zeitformat etc.
	 */
	public static void init() {
		try {
	      logger = Logger.getLogger(Mlog.class);
	      PatternLayout appenderLayout = new PatternLayout();
	      appenderLayout.setConversionPattern("%d[%t]%p-> %m%n");
	      RollingFileAppender appfile = new RollingFileAppender(appenderLayout, logdateiname);
	      ConsoleAppender appcons = new ConsoleAppender(appenderLayout);
	      appfile.setMaxFileSize("1MB");
	      appfile.setMaxBackupIndex(3);
	      logger.addAppender(appcons);
	      logger.addAppender(appfile);
	      logger.setLevel(org.apache.log4j.Level.INFO);
	      logger.info("\n--------------------- Start -----------------------");
	    }
	    catch (IOException ex)
	    {
	      logger.error("Cannot access log file: " + ex.getLocalizedMessage());
	    }
	    catch (Exception ex)
	    {
	      logger.error("Unknown exception: " + ex.getLocalizedMessage());
	    }
	}
}
