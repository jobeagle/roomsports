import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
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
 * RegistrierungRace.java: Beinhaltet die Registrierung von Online-
 * rennen und LAN-Rennen. 
 *****************************************************************************
 *
 */
public class RegistrierungRace extends RsErwDialog {
	private Text txtName = null;
	private Text txtOrt = null;
	private Text txtGebJahr = null;
	private Label lblName = null;
	private Label lblInfo = null;
	private Combo cmbTrainer = null;
	public String name = null;
	public String ort = null;
	public String gebJahr = null;
	public String trainer = null;
	private String [] trainingsgeraete = {"", "Daum Serie 8008", "Daum Design Line", 
			"Daum Premium Serie", "Daum andere", "Kettler Ergoracer", 
			"Kettler Ergorace", "Kettler Racer 8,9", "Kettler andere (E,X,MX,PX,XTR ...)", 
			"Ergo-Fit Cycle", "Ergo-Fit andere",
			"Rollentrainer (ANT+)", "Ergoline",
			"Tacx Rollentrainer", "andere Rollentrainer", "Cyclus 2", "anderes Gerät"};
	
	public RegistrierungRace() {
		super();					// Aufruf des RsErwDialog-Konstruktors
		
		shl.setText(Messages.getString("OnlineRennen.userreg"));
		txtName = new Text(cmp, SWT.BORDER);
		txtName.setBounds(10, 30, 140, 21);
		txtName.setTextLimit(11);
		txtName.setFocus();
		
		txtOrt = new Text(cmp, SWT.BORDER);
		txtOrt.setBounds(10, 78, 140, 21);
		txtOrt.setTextLimit(17);
		
		txtGebJahr = new Text(cmp, SWT.BORDER);
		txtGebJahr.setBounds(206, 78, 139, 21);
		txtGebJahr.setTextLimit(4);
		
		Label lblTrainer = new Label(cmp, SWT.NONE);
		lblTrainer.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblTrainer.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD)); 
		lblTrainer.setBounds(10, 105, 140, 15);
		lblTrainer.setText("Trainingsgerät:"); 
		
		Label lblOrt = new Label(cmp, SWT.NONE);
		lblOrt.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblOrt.setBounds(10, 57, 140, 15);
		lblOrt.setText(Messages.getString("OnlineRennen.ort"));  
		
		Label lblGebJahr = new Label(cmp, SWT.NONE);
		lblGebJahr.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblGebJahr.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD)); 
		lblGebJahr.setBounds(205, 57, 101, 15);
		lblGebJahr.setText(Messages.getString("OnlineRennen.geburtsjahr"));  
		
		lblName = new Label(cmp, SWT.NONE);
		lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblName.setText(Messages.getString("OnlineRennen.namepseudo")); 
		lblName.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD)); 
		lblName.setBounds(10, 9, 140, 15);
		
		lblInfo = new Label(cmp, SWT.WRAP);
		lblInfo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblInfo.setText(Messages.getString("OnlineRennen.lblInfo.text")); 
		lblInfo.setBounds(10, 167, 218, 37);
		
		cmbTrainer = new Combo(cmp, SWT.NONE);
		cmbTrainer.setItems(trainingsgeraete);
		cmbTrainer.setBounds(10, 126, 335, 23);
		cmbTrainer.setTextLimit(36);
	}

	/**
	 * Überprüfung einiger Muss-Felder.
	 * @return True oder False
	 */
	private boolean regispruefung() {
		if (txtName.getText().equals("")) { 
			Messages.errormessage(shl, Messages.getString("OnlineRennen.fehlernamenichtgef")); 
			return false;
		}
		if (txtGebJahr.getText().equals("")) { 
			Messages.errormessage(shl, Messages.getString("OnlineRennen.fehlergebjahrnichtgef")); 
			return false;
		}
		if (cmbTrainer.getText().equals("")) { 
			Messages.errormessage(shl, Messages.getString("OnlineRennen.fehlertrainernichtgef")); 
			return false;
		}

		String smessage = Messages.getString("OnlineRennen.deruser") + txtName.getText() + Messages.getString("OnlineRennen.wirdmit") +  
						//Messages.getString("OnlineRennen.vornamedp") + txtVorname.getText() + "\n" +   
						Messages.getString("OnlineRennen.geburtsjahr") + txtGebJahr.getText() + "\n" +  
						Messages.getString("OnlineRennen.ortdp") + txtOrt.getText() + "\n" +  
						Messages.getString("OnlineRennen.trainerdp") + cmbTrainer.getText(); 
		return Messages.entscheidungmessage(shl, smessage);
	}

	/**
	 * Diese Methode wird beim betätigen des OK-Buttons aufgerufen.
	 * Der OK-Button ist in der abstrakten Klasse RsErwDialog definiert.
	 */
	@Override
	void doOk() {
		name = txtName.getText();
		ort = txtOrt.getText();
		gebJahr = txtGebJahr.getText();
		trainer = cmbTrainer.getText();
		
		if (regispruefung()) {
			setErgebnisOK(true);
			off();
			//shl.setVisible(false);
		} else {
			on();
		}
	}

	/**
	 * Diese Methode wird beim betätigen des Abbruch-Buttons aufgerufen.
	 * Der OK-Button ist in der abstrakten Klasse RsErwDialog definiert.
	 */
	@Override
	void doAbbruch() {
		name = null;
		ort = null;
		gebJahr = null;
		trainer = null;
		setErgebnisOK(false);		
		//shl.setVisible(false);
		off();
	}
}
