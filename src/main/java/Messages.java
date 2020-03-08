import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jna_ext.User32ext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;

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
 * Messages.java: Messages für RoomSports
 *****************************************************************************
 *
 * Diese Klasse beinhaltet die Messageboxen für RoomSports und die
 * Einbindung weiterer Sprachen (akt. Englisch) über das ResourceBundle.
 */

public class Messages {
	public  static boolean isInitialized = false;
	private static Shell shell;
	private static String BUNDLE_NAME = "messages"; 
	private static int autoOKTimerMillisek = 5000;			// wenn in Konfiguration gesetzt, dann Infofenster nach dieser Zeit automatisch schliessen!
	private static String strCaption = "";
	private static String lastMessage = "";					// letzte Meldung speichern, um doppelte Fehlermeldungen zu unterdrücken
	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * @param bundle Der Bundle-Name, der gesetzt werden soll (z.B. "messages_en")
	 */
	public static void setbundle(String bundle) {
		BUNDLE_NAME = bundle;
		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	}

	private Messages() {
	}

	/**
	 * Liefert einen String in der entsprechenden Sprache.
	 * @param key  Key für Ressourcedateieintrag in entspr. Sprache
	 * @return  Eintrag als String in entspr. Sprache
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	/**
	 * Damit wird die Messageausgabe an die übergebene Shell gebunden
	 * @param pshell  Shell
	 */
	public static void startmessaging(Shell pshell) {
		shell = pshell;
		isInitialized = true;
	}

	/**
	 * Messagebox für RoomSports
	 * @param message  Text für MessageBox
	 */
	public static void errormessage(String message) {
	    Mlog.error(message);
		if (shell != null && !message.equals(lastMessage)) {
			lastMessage = message;
			Display display = shell.getDisplay();
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
			messageBox.setText("Error");
			messageBox.setMessage(message);
			
	        /* starte ggf timer */
			if (Rsmain.newkonfig != null)
				if (Rsmain.newkonfig.isAutoOK()) {
					strCaption = "Error";
					display.timerExec(autoOKTimerMillisek, timerautoOK);
				}
			messageBox.open();	
		}
	}

	/**
	 * Messagebox für RoomSports
	 * @param pshell	Parent shell
	 * @param message	Text für MessageBox
	 */
	public static void errormessage(Shell pshell, String message) {
	    Mlog.error(message);
	    if (pshell != null && !message.equals(lastMessage)) {
	    	lastMessage = message;
	    	MessageBox messageBox = new MessageBox(pshell, SWT.ICON_ERROR);
			Display display = pshell.getDisplay();
			messageBox.setText("Error");
	    	messageBox.setMessage(message);
	    	
	        /* starte ggf timer */
			if (Rsmain.newkonfig.isAutoOK()) {
				strCaption = "Error";
				display.timerExec(autoOKTimerMillisek, timerautoOK);
			}
	    	messageBox.open();	
	    }
	}

	/**
	 * Info-Messagebox für RoomSports
	 * @param message  Text für Info-Messagebox
	 */
	public static void infomessage(String message) {
		Mlog.debug(message);
		if (shell != null && !message.equals(lastMessage)) {
			lastMessage = message;
			Display display = shell.getDisplay();
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
			messageBox.setText("Info");
			messageBox.setMessage(message);
			
	        /* starte ggf timer */
			if (Rsmain.newkonfig.isAutoOK()) {
				strCaption = "Info";
				display.timerExec(autoOKTimerMillisek, timerautoOK);
			}
			messageBox.open();			
		}
	}

	/**
	 * Liefert die HWND (Window-Id) vom Window mit entsprechender caption (Titel)
	 * @param caption Titel des zu suchenden Fensters
	 * @return HWND
	 */ 
    private static HWND getCmdHwnd(String caption)
    {
            HWND hwnd;

            hwnd = User32ext.INSTANCE.FindWindow(null, caption);
            //Mlog.debug("HWND = "+ hwnd);
            return hwnd;
    }


    /*
     * Timer um Infomeldungen nach einer vorgewählten zeit automatisch zu schliessen
     */
    private static Runnable timerautoOK = new Runnable()
    {
        public void run()
        {
			HWND hwnd = getCmdHwnd(strCaption);
        	
   		    WPARAM wparam = new WPARAM(0x0000000D);
   		    LPARAM lparam = new LPARAM(0);
   	    	int WM_KEYDOWN = 0x0100;
   		    User32ext.INSTANCE.PostMessage(hwnd, WM_KEYDOWN, wparam, lparam); // Enter (0x0000000D) drücken
        }
    };
    

    /**
	 * Info-Messagebox für RoomSports
	 * @param pshell	Parent shell
	 * @param message   Text für MessageBox
	 */
	public static void infomessage(final Shell pshell, final String message) {
		Mlog.debug(message);
		if (pshell != null && !message.equals(lastMessage)) {
			lastMessage = message;
			MessageBox messageBox = new MessageBox(pshell, SWT.ICON_INFORMATION);
			Display display = pshell.getDisplay();
			messageBox.setText("Info");
			messageBox.setMessage(message);
			
	        /* starte ggf timer */
			if (Rsmain.newkonfig.isAutoOK()) {
				strCaption = "Info";
				display.timerExec(autoOKTimerMillisek, timerautoOK);
			}
			messageBox.open();
		}
	}
	
	/**
	 * Info-Messagebox ohne Button für zeitweise Einblendungen - einschalten
	 * @param message   Text für MessageBox
	 * @param weite		Breite der Messagebox
	 * @param hoehe     Höhe der Messagebox
	 * @param fontsize  Fontgröße
	 * @return Shell der Infobox zurückgeben
	 */
	public static Shell infomessage_on(String message, int weite, int hoehe, int fontsize) {
        FontData infoFont = new FontData("Arial",(int) (fontsize/Global.resolutionFactor),SWT.BOLD); 

		Display disp = Display.getCurrent();
	    Point pt = disp.getCursorLocation();
		Shell tmpShell = new Shell(disp, SWT.NONE);
		//tmpShell.setSize(400, 80);
		tmpShell.setSize(weite, hoehe);
		tmpShell.setMinimumSize(weite, hoehe);
		tmpShell.setLocation(pt.x, pt.y);
		tmpShell.setActive();
		Label lblMsg = new Label(tmpShell, SWT.NONE);
		lblMsg.setText(message);
		lblMsg.setFont(new Font(disp, infoFont));
		lblMsg.setBounds(10, 10, weite-20, hoehe-20);
		tmpShell.pack();
		tmpShell.open();
		
		return tmpShell;
	}

	/**
	 * Start-Info-Fenster mit HTML-Ausgabe der übergebenen URL
	 * @param url	URL-Adresse für HTML-Ausgabe
	 */
	public static void showHTMLImBrowser(String url) {
		Display disp = Display.getCurrent();
		Button butOk = null;
		Button chkInfo = null;
		final Shell infoShell = new Shell(disp, SWT.TITLE|SWT.BORDER|SWT.CLOSE|SWT.ON_TOP);
		infoShell.setImage(Rsmain.icon);

		infoShell.setSize(600, 600);
		infoShell.setMinimumSize(600, 600);
		infoShell.setText(Messages.getString("Rsmain.startinfocaption"));
		infoShell.setActive();
		Browser browser = new Browser(infoShell, SWT.BORDER);
		browser.setUrl(url);
		browser.setSize(600, 530);
		Rectangle rect = infoShell.getBounds ();
		
		int x = Rsmain.sShell.getLocation().x + (Rsmain.sShell.getSize().x - rect.width) / 2;
		int y = Rsmain.sShell.getLocation().y + (Rsmain.sShell.getSize().y - rect.height) / 2;
		
		infoShell.setLocation (x, y);

		chkInfo = new Button(infoShell, SWT.CHECK);
		chkInfo.setText(Messages.getString("messages.infoanzeige"));
		chkInfo.setToolTipText(Messages.getString("messages.infoanzeigeTP"));
		chkInfo.setBounds(10, 540, 250, 20);
		chkInfo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Rsmain.newkonfig.setShowInfo(false);
			}
		});

		butOk = new Button(infoShell, SWT.NONE);
		butOk.setText(Messages.getString("konfig._ok_"));
		butOk.setBounds(545, 540, 40, 25);
		butOk.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				infoShell.close();
			}
		});

		infoShell.pack();
		infoShell.open();
	}

	/**
	 * Info-Messagebox ohne Button für zeitweise Einblendungen - ausschalten
	 * @param shell	Parent shell
	 */
	public static void infomessage_off(Shell shell) {
		if (shell != null)
			shell.dispose();
	}
	
	/**
	 * allgemeine Ja/Nein Entscheidungs-Messagebox
	 * @param pshell		Shell
	 * @param pmessage		Text für MessageBox
	 * @return Ja Button ausgewählt?
	 */
	public static boolean entscheidungmessage(Shell pshell, String pmessage) {
		MessageBox messageBox = new MessageBox(pshell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setMessage(pmessage);
		int rc = messageBox.open();	
		
		if (rc == SWT.YES)
			return true;

		return false;
	}
	
	/**
	 * Anzeige "Ein" oder "Aus" bei Boolean ausgeben
	 * @param wert		true = Ein, false = Aus
	 * @return			true = Ein, false = Aus
	 */
	public static String einaus(boolean wert) {
		if (wert)
			return ("Ein");
		else
			return("Aus");
	}

}
