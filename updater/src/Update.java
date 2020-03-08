import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

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
 * Update.java
 *****************************************************************************
 *
 * Diese Klasse beinhaltet den automatischen Update.
 * Um die eigenen Klassen (und EXE-Dateien problemlos überschreiben zu können
 * ist der Update in ein extra Jar-Archiv ausgelagert.
 * 
 */

public class Update {
	private final static String zipFile = "rsupdate.zip";
	private final static String downloadURL = "https://www.mtbsimulator.de/rs/download/"+zipFile;
	private static Thread worker;
	private final static String root = "update/";
	private static boolean logDebug = true;			// Debugstatus für das Logging ein/aus
	private static boolean updateComplete = false;
	private static Shell shell;
	private static Label lblInfo = null;
	private static String infoText = new String("");
	private static String javaRT = System.getProperty("java.home") + "\\bin\\javaw.exe";	
	
	/**
	 * Alle Aktionen werden hier in einem extra Thread durchgeführt.
	 */
	private static void download()
	{
		worker = new Thread(
				new Runnable(){
					public void run()
					{
						try {
							Mlog.info("Update startet mit Download");
							downloadFile(downloadURL);
							Mlog.debug("Unzip...");
							unzip();
							Mlog.debug("kopiere Dateien");
							copyFiles(new File(root),new File("").getAbsolutePath());
							infoText = "aufräumen";
							Mlog.debug("aufraeumen");
							cleanup();
							infoText = "Update beendet";
							Mlog.debug(infoText);
							startRs();
							Mlog.info("RS gestartet");
							updateComplete = true;
						} catch (Exception ex) {
							updateComplete = true;
							String fehler = "Beim Updatevorgang ist ein Fehler aufgetreten!";
							Mlog.error(fehler);
							Mlog.ex(ex);
							startRs();
							Mlog.info("RS gestartet (ex)");
						}
					}
				});
		worker.start();
	}

	/**
	 * RoomSports wieder starten.
	 */
	private static void startRs() {
    	Mlog.debug("Starte RoomSports...");
        String[] run = {javaRT,"-jar","rs.jar"};
        try {
            Runtime.getRuntime().exec(run);
        } catch (Exception ex) {
            Mlog.ex(ex);;
        }
	}
	
	/**
	 * ZIP-Datei und Updateverzeichnis werden gelöscht.
	 */
	private static void cleanup()
	{
		Mlog.debug("Cleanup wird durchgeführt...");
		File f = new File(zipFile);
		f.delete();
		remove(new File(root));
		new File(root).delete();
	}

	/**
	 * löscht rekursiv Dateien bzw. Verzeichnisse
	 * @param f
	 */
	private static void remove(File f)
	{
		File[]files = f.listFiles();
		for(File ff:files)
		{
			if(ff.isDirectory())
			{
				remove(ff);
				ff.delete();
			}
			else
			{
				ff.delete();
			}
		}
	}

	/**
	 * Kopiert die entzippten Dateien vom Verzeichnis update in das Programmverzeichnis
	 * von RoomSports.
	 * @param f		Pfad des Quellverzeichnisses (update)
	 * @param dir	Zielverzeichnisname (Programmverzeichnis)
	 * @throws IOException
	 */
	private static void copyFiles(File f,String dir) throws IOException
	{
		File[]files = f.listFiles();
		for(File ff:files)
		{
			if(ff.isDirectory()){
				new File(dir+"/"+ff.getName()).mkdir();
				copyFiles(ff,dir+"/"+ff.getName());
			}
			else
			{
				copy(ff.getAbsolutePath(),dir+"/"+ff.getName());
			}
		}
	}

	/**
	 * Ein einfacher Dateikopierer.
	 * @param srFile	Quellverzeichnis
	 * @param dtFile	Zielverzeichnis
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void copy(String srFile, String dtFile) throws FileNotFoundException, IOException{

		File f1 = new File(srFile);
		File f2 = new File(dtFile);

		InputStream in = new FileInputStream(f1);

		OutputStream out = new FileOutputStream(f2);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0){
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * Das "zipfile" wird entzipped.
	 * @throws IOException
	 */
	private static void unzip() throws IOException
	{
		int BUFFER = 2048;
		BufferedOutputStream dest = null;
		BufferedInputStream is = null;
		ZipEntry entry;
		ZipFile zipfile = new ZipFile(zipFile);
		Enumeration<? extends ZipEntry> e = zipfile.entries();
		(new File(root)).mkdir();
		while(e.hasMoreElements()) {
			entry = (ZipEntry) e.nextElement();
			infoText = "Extrahiere: " +entry;
			Mlog.debug(infoText);
			if(entry.isDirectory())
				(new File(root+entry.getName())).mkdir();
			else{
				(new File(root+entry.getName())).createNewFile();
				is = new BufferedInputStream
						(zipfile.getInputStream(entry));
				int count;
				byte data[] = new byte[BUFFER];
				FileOutputStream fos = new
						FileOutputStream(root+entry.getName());
				dest = new
						BufferedOutputStream(fos, BUFFER);
				while ((count = is.read(data, 0, BUFFER))
						!= -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				is.close();
			}
		}
		zipfile.close();
	}

	/**
	 * Lädt die Datei aus der übergebenen URL herunter.
	 * @param link
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static void downloadFile(String link) throws MalformedURLException, IOException
	{
		URL url = new URL(link);
		URLConnection conn = url.openConnection();
		InputStream is = conn.getInputStream();
		long max = conn.getContentLength();
		infoText = "download " + max + " Bytes";
		Mlog.debug("Download Datei ...Update Größe(komprimiert): " + max + " Bytes");
		BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(new File(zipFile)));
		byte[] buffer = new byte[32 * 1024];
		int bytesRead = 0;
		while ((bytesRead = is.read(buffer)) != -1) {
			fOut.write(buffer, 0, bytesRead);
		}
		fOut.flush();
		fOut.close();
		is.close();
		Mlog.debug("Download abgeschlossen");
	}

	/**
	 * Anzeige des aktuellen Status im Label
	 * @param info
	 */
	private static void showInfoText(String info) {
		lblInfo.setText(info);
		lblInfo.redraw();
	}
	
	/**
	 * Hauptprogramm des Updaters
	 * @param args
	 */
	public static void main(String[] args) {
		FontData wertKlFont = new FontData("Arial",(int) (14),SWT.BOLD); 
		Display display = new Display ();
		shell = new Shell(display);
		shell.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event event) {
				Mlog.init();
				Mlog.setDebugstatus(logDebug);
				download();
			}
		});
		shell.setSize(310, 113);
		shell.setBounds(120, 10, 310, 80);
		shell.setText("Update");
		lblInfo = new Label(shell, SWT.NONE);
		lblInfo.setBounds(10, 10, 290, 20);
		lblInfo.setFont(new Font(display, wertKlFont));

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				showInfoText(infoText);
				display.sleep();
				if (updateComplete) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Mlog.ex(e);
					}
					shell.dispose();
				}
			}
		}
		display.dispose();
	}
}
