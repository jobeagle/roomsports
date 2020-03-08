import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;

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
 * UpdateDialog.java: Dialog, ob Update durchgeführt werden soll oder nicht.
 * Zur Erklärung der Erlangung der UAC (Adminrechte) wird zusätzlich zur 
 * Textinfo ein Bild dargestellt.
 *****************************************************************************
 */
public class UpdateDialog extends RsErwDialog {
	private Label lblInfo = null;
	private Label lblImage = null;
	private Image img = null;
	
	/**
	 * Konstruktor des Updatedialogs
	 */
	public UpdateDialog() {
		super();					// Aufruf des RsErwDialog-Konstruktors
		this.shl.setText(Messages.getString("update.caption"));	
		String updMessage = Messages.getString("update.message1") + "\n" + Messages.getString("update.aktversion") +
				Global.versioncode + ", " + Messages.getString("update.neuversion")  + Rsmain.latestVersion + "\n" + Messages.getString("update.message2") +
				" " + Messages.getString("update.message3");
		lblInfo = new Label(cmp, SWT.WRAP);
		lblInfo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblInfo.setText(updMessage); 
		lblInfo.setBounds(10, 5, 460, 90);		
		Global.setFontSizeLabel(lblInfo);
		if (Global.os.equals("Windows XP"))
			img = new Image(this.shl.getDisplay(),"uac_xp.png");
		else
			img = new Image(this.shl.getDisplay(),"uac_w7.png");
		lblImage = new Label(cmp, SWT.NONE);
		lblImage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblImage.setBounds(10, 90, 455, 313);
		lblImage.setImage(img);
	}


	/**
	 * Diese Methode wird beim betätigen des OK-Buttons aufgerufen.
	 * Der OK-Button ist in der abstrakten Klasse RsErwDialog definiert.
	 */
	@Override
	void doOk() {
		setErgebnisOK(true);
		off();
	}

	/**
	 * Diese Methode wird beim betätigen des Abbruch-Buttons aufgerufen.
	 * Der OK-Button ist in der abstrakten Klasse RsErwDialog definiert.
	 */
	@Override
	void doAbbruch() {
		setErgebnisOK(false);		
		off();
	}
	
	/**
	 * Startet den eigentlichen Updatevorgang. 
	 * Das downloaden, entpacken und überschreiben der Dateien ist in der Klasse update.jar ausgelagert.
	 */
    public void launch()
    {
    	Mlog.debug("starte den updater...");
        try {
        	Mlog.debug("Java-Runtime: " + Global.javaRT);
        	executeAsAdministrator(Global.javaRT, "-jar update.jar");
        } catch (Exception ex) {
            Mlog.ex(ex);
        }
    }
    
    /**
     * Führt das übergebene Kommando mit Adminrechten aus. Verwendet wird Shell32X.SHELLEXECUTEINFO().
     * @param command		Betriebssystemkommando
     * @param args			ggf. zus. Argumente
     */
    public static void executeAsAdministrator(String command, String args)
    {
        Shell32X.SHELLEXECUTEINFO execInfo = new Shell32X.SHELLEXECUTEINFO();
        execInfo.lpFile = new WString(command);
        if (args != null)
            execInfo.lpParameters = new WString(args);
        execInfo.nShow = Shell32X.SW_SHOWDEFAULT;
        execInfo.fMask = Shell32X.SEE_MASK_NOCLOSEPROCESS;
        execInfo.lpVerb = new WString("runas");
        boolean result = Shell32X.INSTANCE.ShellExecuteEx(execInfo);

        if (!result)
        {
            int lastError = Kernel32.INSTANCE.GetLastError();
            String errorMessage = Kernel32Util.formatMessageFromLastErrorCode(lastError);
            throw new RuntimeException("Error performing elevation: " + lastError + ": " + errorMessage + " (apperror=" + execInfo.hInstApp + ")");
        }
    }
}
