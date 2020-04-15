import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

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
 * RsErwDialog.java: 
 *
 * Auf dieser Klasse bauen die (neueren) erweiterten Dialoge auf.
 * Als Standard sind OK und Abbruchbutton vorhanden. Diese Buttons rufen 
 * entsprechende abstrakte Methoden auf.
 * Klassen, die diese Klasse implementieren, müssen die abstrakten Methoden 
 * implementieren.
 * Der Dialog wartet, bis OK oder Abbruch betätigt wird.
 *****************************************************************************
 */
public abstract class RsErwDialog {
	protected Shell shl;
	protected Composite cmp;
	protected Button butOk;
	protected Button butAbbruch;
	protected Display display;
	protected int weite = 360, hoehe = 240;
	private boolean ergebnisOK = false;
	private boolean exit = false;

	// Die folgenden Methoden müssen in der abgeleiteten Klasse definiert werden:
	abstract void doOk();
	abstract void doAbbruch();
	
	/**
	 * @return the ergebnis
	 */
	public boolean isErgebnisOK() {
		return ergebnisOK;
	}
	
	/**
	 * @param ergebnis the ergebnis to set
	 */
	public void setErgebnisOK(boolean ergebnis) {
		this.ergebnisOK = ergebnis;
	}
	
	/**
	 * @return the exit
	 */
	public boolean isExit() {
		return exit;
	}
	
	/**
	 * @param exit the exit to set
	 */
	public void setExit(boolean exit) {
		this.exit = exit;
	}
	
	/**
	 * Konstruktor des erweiterten Dialogs.
	 * Es werden die Shell, Composite und die beiden Buttons konfiguriert.
	 */
	public RsErwDialog() {
		shl = new Shell(Rsmain.sShell.getDisplay(), SWT.TITLE|SWT.BORDER|SWT.APPLICATION_MODAL);	    
		shl.setLayout(null);
		cmp = new Composite(shl, SWT.NONE);
		cmp.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cmp.setBounds(0, 0, weite-6, hoehe-30);
		display = shl.getDisplay();

		// Buttons
		butOk = new Button(cmp, SWT.NONE);
		butOk.setText(Messages.getString("konfig._ok_"));
		butOk.setBounds(weite-55, hoehe-65, 40, 25);
		Global.setFontSizeButton(butOk);
		butOk.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doOk();
				setExit(true);
			}
		});
		
		butAbbruch = new Button(cmp, SWT.NONE);
		butAbbruch.setText(Messages.getString("OnlineRennen.abbruch"));  
		butAbbruch.setBounds(weite-126, hoehe-65, 60, 25);
		Global.setFontSizeButton(butAbbruch);
		butAbbruch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doAbbruch();
				setExit(true);
			}
		});
	}
	
	/**
	 * Anzeige des erweiterten Dialogs.
	 * Die Größe wird vorgegeben
	 * @param weite		Breite des Fensters
	 * @param hoehe		Höhe des Fensters
	 */
	public void on(int weite, int hoehe) {
		Rsmain.setImDialog(true);
		setErgebnisOK(false);
		this.weite=weite;this.hoehe=hoehe;
		shl.setSize(weite, hoehe+33);
		shl.setBounds((Rsmain.aktcr.width-weite)/2+Rsmain.aktcr.x, (Rsmain.aktcr.height-hoehe)/2+Rsmain.aktcr.y, weite, hoehe);
		cmp.setBounds(0, 0, weite-6, hoehe-30);
		butOk.setBounds(weite-55, hoehe-65, 40, 25);
		butAbbruch.setBounds(weite-126, hoehe-65, 60, 25);
		shl.open();
		while (!isExit()) {
			try {
				Thread.sleep(10);
				display.readAndDispatch();
			} catch (InterruptedException e) {
				Mlog.error("RsErwDialog: interrupted sleep!");
				Mlog.ex(e);
			}
		}
	}

	public void on() {
		Rsmain.setImDialog(true);
		setErgebnisOK(false);
		shl.open();
		while (!isExit()) {
			try {
				Thread.sleep(10);
				display.readAndDispatch();
			} catch (InterruptedException e) {
				Mlog.error("RsErwDialog: interrupted sleep!");
				Mlog.ex(e);
			}
		}
	}

	public void off() {
		shl.setVisible(false);
		Rsmain.setImDialog(false);
	}
}
