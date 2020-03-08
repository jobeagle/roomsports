import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.ws.axis2.ActPosListe;
import org.apache.ws.axis2.ActPosListeResponse;
import org.apache.ws.axis2.ConnectDB;
import org.apache.ws.axis2.ConnectDBResponse;
import org.apache.ws.axis2.DeleteTeilnahme;
import org.apache.ws.axis2.DeleteTeilnahmeResponse;
import org.apache.ws.axis2.GetKonfiguration;
import org.apache.ws.axis2.GetKonfigurationResponse;
import org.apache.ws.axis2.GetRennenID;
import org.apache.ws.axis2.GetRennenIDResponse;
import org.apache.ws.axis2.GetTeilnahmeID;
import org.apache.ws.axis2.GetTeilnahmeIDResponse;
import org.apache.ws.axis2.InsertActPos1;
import org.apache.ws.axis2.InsertActPos1Response;
import org.apache.ws.axis2.InsertTeilnahme;
import org.apache.ws.axis2.InsertTeilnahmeResponse;
import org.apache.ws.axis2.InsertUser;
import org.apache.ws.axis2.InsertUserResponse;
import org.apache.ws.axis2.MTBSRaceServiceStub;
import org.apache.ws.axis2.RennenListe;
import org.apache.ws.axis2.RennenListeResponse;
import org.apache.ws.axis2.WarteBisStart;
import org.apache.ws.axis2.WarteBisStartResponse;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
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
 * Wettkampf.java: Beinhaltet die Konfiguration und Start von Online-
 * Training, Rennen geg. virt. Gegner, LAN-Rennen und Rennen gegen CSV-Datei.
 *****************************************************************************
 *
 */
public class Wettkampf {
	public  static final String eom = "*EOM*";				// markiert End of Message
	public  static final String nv  = "n.v.";				// Kennung für nicht vorhanden in DB
	public  static final String trz  = "~";					// Trennzeichen LAN Kommunikation (~)
	
	private String mACAdr;									// eigene MAC-Adresse dient als Key beim Netzwerktraining
	
	private RegistrierungRace regRace = null;				// Registrierungsdialog
	public  VerwaltungRace vwRace = null;					// Dialog zur Verwaltung der LAN-Rennen mittels SQLite-DB
	
	public  Shell shlRsWettkampf;
	private Composite cmpOnlineRennen = null;
	private String CSVDatei = null;
	private Button butOk = null;
	private Button butAbbr = null;
	private Button chkOnlineRennen = null;
	private Button chkLANRennen = null;
	private Button chkLANRennenAuto = null;
	private Button chkRennenGegenCsv = null;
	// private Button chkIBL = null;
	private Button chkVirtGeg = null;
	private Button chkvirtZP;
	private Button butCsvDatei = null;
	private Button butStartfreigabe = null;
	private Button butAbmelden = null;
	private Button butAnmelden = null;
	private Button butLanRaceDef = null;
	private Text txtStatus;
	// private Text txtibl = null;
	public  CCombo cmbOnlineRennen;
	private Text txtAngemeldet;
	private Text txtServerAdr;
	private String db = Global.db;  
	public  String aktivesRennen = null;
	private String[] rennliste = null;
	private String[] anmeldeliste = null;
	public  String[] rennennamen = null;
	private long teilnahmeID = 0;
	private long rennenID = 0;
	private boolean aktiv = false;
	private boolean csvAktiv = false;
	private boolean changed = false;
	private boolean zprace = false;
	private boolean schikane = false;
    // private boolean ibl = false;
    private boolean virtgeg = false;
    private boolean virtZP = false;
    private boolean onlRace = false;
    private boolean lanRace = false;
    private boolean lanRaceAuto = false;
    public  boolean lanRaceServer = false;
    private boolean showDialog = true;
	
	public  List<OnlineGegner> gegliste;
	public  List<OnlineGegner> ergliste;
	public  OnlineGegner gegner;
	private Text txtSNR;
	private Text txtCsvDatei;
	private Text txtcountdown;
	private Text txtTyp;
	private DecimalFormat zfk0 = new DecimalFormat("#");  
	private Table tblVirtGeg;
	private String[] header = {"Name", "Zeit", "Watt", "Puls", " ", "RPM", " ", "Trainer"};
	
	private Connection cn;
	public  TreeMap<String, OnlineGegner> posliste;
	public  ArrayList<Integer> teilIds;
	
	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	/**
	 * @return the csvAktiv
	 */
	public boolean isCsvAktiv() {
		return csvAktiv;
	}

	/**
	 * @param csvAktiv the csvAktiv to set
	 */
	public void setCsvAktiv(boolean csvAktiv) {
		this.csvAktiv = csvAktiv;
	}

	/**
	 * @return the aktiv
	 */
	public boolean isAktiv() {
		return aktiv;
	}

	/**
	 * @param aktiv the aktiv to set
	 */
	public void setAktiv(boolean aktiv) {
		this.aktiv = aktiv;
	}

	/**
	 * @return the zprace
	 */
	public boolean isZprace() {
		return zprace;
	}

	/**
	 * @param zprace the zprace to set
	 */
	public void setZprace(boolean zprace) {
		this.zprace = zprace;
	}

	/**
	 * @return the schikane
	 */
	public boolean isSchikane() {
		return schikane;
	}

	/**
	 * @param schikane the schikane to set
	 */
	public void setSchikane(boolean schikane) {
		this.schikane = schikane;
	}

	/**
	 * @return VirtGeg 
	 */
	public boolean isVirtGeg() {
		return virtgeg;
	}

	/**
	 * @param virtgeg setze VirtGeg
	 */
	public void setVirtGeg(boolean virtgeg) {
		this.virtgeg = virtgeg;
	}

	/**
	 * @return VirtZP 
	 */
	public boolean isVirtZP() {
		return virtZP;
	}

	/**
	 * @param virtzp setze VirtZP
	 */
	public void setVirtZP(boolean virtzp) {
		this.virtZP = virtzp;
	}
	
	/**
	 * @return the onlRace
	 */
	public boolean isOnlRace() {
		return onlRace;
	}

	/**
	 * @param onlRace the onlRace to set
	 */
	public void setOnlRace(boolean onlRace) {
		this.onlRace = onlRace;
	}

	/**
	 * @return the lanRace
	 */
	public boolean isLanRace() {
		return lanRace;
	}

	/**
	 * @param lanRace the lanRace to set
	 */
	public void setLanRace(boolean lanRace) {
		this.lanRace = lanRace;
	}

	/**
	 * @return the lanRaceAuto
	 */
	public boolean isLanRaceAuto() {
		return lanRaceAuto;
	}

	/**
	 * @param lanRaceAuto the lanRaceAuto to set
	 */
	public void setLanRaceAuto(boolean lanRaceAuto) {
		this.lanRaceAuto = lanRaceAuto;
	}

	/**
	 * Einstiegspunkt: Darstellung und Aufbau des Online-Rennen Dialogs.
	 * @param	server	Netzwerkadresse server
	 */
	public void createOnlineRennen(String server) {
		if (vwRace == null)
			vwRace = new VerwaltungRace();

		if (!(server == null)) {				// wurde server übergeben?
			lanRace = true;
			showDialog = false;
			if (server.equalsIgnoreCase("localhost")) {
				Mlog.debug("Netzwerktraining wird als Server initialisiert");
				lanRaceServer = true;
			} else {
				Mlog.debug("Netzwerktraining wird als Client initialisiert");			
			}
		} else
			showDialog = true;
		Rsmain.setImDialog(true);
		if (shlRsWettkampf != null) {	// Shell schon vorhanden?
			if (showDialog) {
				shlRsWettkampf.setVisible(true);
				shlRsWettkampf.setFocus();
			}
		} else {
			shlRsWettkampf = new Shell(Display.getCurrent(), SWT.TITLE|SWT.BORDER);	    
			shlRsWettkampf.setSize(560, 670);
			shlRsWettkampf.setBounds((Rsmain.aktcr.width-560)/2, (Rsmain.aktcr.height-670)/2, 560, 670);
			shlRsWettkampf.setLayout(null);
			shlRsWettkampf.setText(Messages.getString("OnlineRennen.shlRsWettkampf.text")); 
			mACAdr = Global.getMacAddress();
			
			cmpOnlineRennen = new Composite(shlRsWettkampf, SWT.NONE);
			cmpOnlineRennen.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cmpOnlineRennen.setBounds(0, 0, 554, 646);

			// Buttons
			butOk = new Button(cmpOnlineRennen, SWT.NONE);
			butOk.setText(Messages.getString("OnlineRennen._ok_"));  
			butOk.setToolTipText(Messages.getString("OnlineRennen.olrdialogschliessen"));  
			butOk.setBounds(500, 610, 40, 25);
			Global.setFontSizeButton(butOk);
			butOk.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {

					if (chkLANRennenAuto.getSelection()) {
						Rsmain.initNetzwerktraining(txtServerAdr.getText());
					} else if (chkVirtGeg.getSelection()) {
						loadVirtGeg(false);
					} else if (chkvirtZP.getSelection()) {
						loadVirtGeg(true);
					} 
					if (!Global.serverAdr.equals(txtServerAdr.getText())) {	// Dann die neue Serveradresse in Settings speichern
						Global.serverAdr = txtServerAdr.getText();
						Rsmain.newkonfig.saveProfil(Rsmain.biker, Rsmain.thisTrainer);
					}
					goBack(true);
				}
			});

			butAbbr = new Button(cmpOnlineRennen, SWT.NONE);
			butAbbr.setToolTipText(Messages.getString("OnlineRennen.butAbbruchTP"));
			butAbbr.setText(Messages.getString("OnlineRennen.abbruch")); 
			butAbbr.setBounds(443, 610, 55, 25);
			Global.setFontSizeButton(butAbbr);
			butAbbr.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					chkOnlineRennen.setSelection(false);
					setOnlRace(false);
					setLanRace(false);
					goBack(false);
				}
			});

			chkRennenGegenCsv = new Button(cmpOnlineRennen, SWT.CHECK);
			chkRennenGegenCsv.setText(Messages.getString("OnlineRennen.chkRennenGegenCsvdatei.text")); 
			chkRennenGegenCsv.setToolTipText(Messages.getString("OnlineRennen.chkRennenGegenCsvdatei.tooltyptext"));
			Global.setFontSizeButton(chkRennenGegenCsv);
			chkRennenGegenCsv.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkRennenGegenCsv.getSelection()) {
						chkOnlineRennen.setSelection(false);
						setOnlRace(false);
						setLanRace(false);
						setAktiv(false);
						// chkIBL.setSelection(false);
						chkLANRennenAuto.setSelection(false);
						chkvirtZP.setSelection(false);
						//setIbl(false);
						setLanRaceAuto(false);
						butCsvDatei.setEnabled(true);
						setCsvAktiv(true);
					}
					else {
						butCsvDatei.setEnabled(false);
						setCsvAktiv(false);					
					}
				}
			});
			chkRennenGegenCsv.setBounds(10, 10, 165, 16);
			chkRennenGegenCsv.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			chkRennenGegenCsv.setSelection(isCsvAktiv());
			
			// LAN-Rennen
			chkLANRennen = new Button(cmpOnlineRennen, SWT.CHECK);
			chkLANRennen.setBounds(10, 31, 165, 16);
			chkLANRennen.setText(Messages.getString("OnlineRennen.chkLANRennen.text")); 
			chkLANRennen.setToolTipText(Messages.getString("OnlineRennen.chkLANRennen.TP"));
			chkLANRennen.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeButton(chkLANRennen);
			chkLANRennen.setSelection(lanRace);
			chkLANRennen.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkLANRennen.getSelection()) {						
						Global.serverAdr = txtServerAdr.getText();
						if (Global.serverAdr.isEmpty()) {
							Messages.errormessage(shlRsWettkampf, Messages.getString("OnlineRennen.keineserveradr"));
							chkLANRennen.setSelection(false);
							return;
						}
						setOnlRace(false);
						setLanRace(true);
						chkRennenGegenCsv.setSelection(false);
						setCsvAktiv(false);
						// chkIBL.setSelection(false);
						chkLANRennenAuto.setSelection(false);
						chkVirtGeg.setSelection(false);
						chkvirtZP.setSelection(false);
						// setIbl(false);
						setLanRaceAuto(false);
						chkOnlineRennen.setSelection(false);
						cmbOnlineRennen.setEnabled(true);
						txtAngemeldet.setEnabled(true);	
						aktualisiereAusDB(0);					
					}
					else {
						setLanRace(false);
						cmbOnlineRennen.setEnabled(false);
						txtAngemeldet.setEnabled(false);
						butAnmelden.setEnabled(false);
						butAbmelden.setEnabled(false);
						butStartfreigabe.setEnabled(false);					
					}
				}
			});

			
			// LAN-Rennen Autostart
			chkLANRennenAuto = new Button(cmpOnlineRennen, SWT.CHECK);
			chkLANRennenAuto.setText(Messages.getString("OnlineRennen.chkLANRennenAuto.text"));
			chkLANRennenAuto.setToolTipText(Messages.getString("OnlineRennen.chkLANRennenAuto.TP"));
			chkLANRennenAuto.setSelection(lanRaceAuto);
			chkLANRennenAuto.setBounds(10, 52, 165, 16);
			chkLANRennenAuto.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeButton(chkLANRennenAuto);
			chkLANRennenAuto.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkLANRennenAuto.getSelection()) {
						Global.serverAdr = txtServerAdr.getText();
						if (Global.serverAdr.isEmpty()) {
							Messages.errormessage(shlRsWettkampf, Messages.getString("OnlineRennen.keineserveradr"));
							chkLANRennenAuto.setSelection(false);
							return;
						}
						chkRennenGegenCsv.setSelection(false);
						chkOnlineRennen.setSelection(false);
						chkLANRennen.setSelection(false);
						setOnlRace(false);
						setLanRace(false);
						chkVirtGeg.setSelection(false);
						chkvirtZP.setSelection(false);
						setLanRaceAuto(true);
					} else {
						setLanRaceAuto(false);
					}
				}
			});

			chkOnlineRennen = new Button(cmpOnlineRennen, SWT.CHECK);
			chkOnlineRennen.setBounds(10, 73, 165, 20);
			chkOnlineRennen.setText(Messages.getString("OnlineRennen.chkOnlineRennen.text")); 
			chkOnlineRennen.setToolTipText(Messages.getString("OnlineRennen.chkOnlineRennen.TP"));
			chkOnlineRennen.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeButton(chkOnlineRennen);
			chkOnlineRennen.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkOnlineRennen.getSelection()) {
						setOnlRace(true);
						setLanRace(false);
						chkRennenGegenCsv.setSelection(false);
						setCsvAktiv(false);
						// chkIBL.setSelection(false);
						chkLANRennenAuto.setSelection(false);
						chkVirtGeg.setSelection(false);
						chkvirtZP.setSelection(false);
						chkLANRennen.setSelection(false);
						// setIbl(false);
						setLanRaceAuto(false);
						cmbOnlineRennen.setEnabled(true);
						txtAngemeldet.setEnabled(true);					
						aktualisiereAusDB(0);					
					}
					else {
						setOnlRace(false);
						cmbOnlineRennen.setEnabled(false);
						txtAngemeldet.setEnabled(false);
						butAnmelden.setEnabled(false);
						butAbmelden.setEnabled(false);
						butStartfreigabe.setEnabled(false);					
					}
				}
			});
			chkOnlineRennen.setSelection(isAktiv());
					
			// Rennen gegen virtuellen Gegner fahren?
			chkVirtGeg = new Button(cmpOnlineRennen, SWT.CHECK);
			chkVirtGeg.setText(Messages.getString("OnlineRennen.virtgeg_check"));
			chkVirtGeg.setToolTipText(Messages.getString("OnlineRennen.virtgeg_check_ttp"));
			chkVirtGeg.setSelection(isVirtGeg());
			chkVirtGeg.setBounds(10, 94, 165, 20);
			chkVirtGeg.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeButton(chkVirtGeg);
			chkVirtGeg.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkVirtGeg.getSelection()) {
						chkRennenGegenCsv.setSelection(false);
						chkOnlineRennen.setSelection(false);
						chkLANRennen.setSelection(false);
						setOnlRace(false);
						setLanRace(false);
						//chkIBL.setSelection(false);
						chkLANRennenAuto.setSelection(false);
						setLanRaceAuto(false);
						chkvirtZP.setSelection(false);
						setVirtGeg(true);
						leseVirtGeg("Ergometertraining");
					} else {
						setVirtGeg(false);
					}
				}
			});

			chkvirtZP = new Button(cmpOnlineRennen, SWT.CHECK);
			chkvirtZP.setToolTipText(Messages.getString("OnlineRennen.chkvirtZP.toolTipText")); 
			chkvirtZP.setText(Messages.getString("OnlineRennen.chkvirtZP.text")); 
			chkvirtZP.setSelection(isVirtZP());
			chkvirtZP.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			chkvirtZP.setBounds(181, 94, 165, 20);
			Global.setFontSizeButton(chkvirtZP);
			chkvirtZP.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkvirtZP.getSelection()) {
						chkRennenGegenCsv.setSelection(false);
						chkOnlineRennen.setSelection(false);
						setOnlRace(false);
						setLanRace(false);
						//chkIBL.setSelection(false);
						chkLANRennenAuto.setSelection(false);
						setLanRaceAuto(false);						
						chkVirtGeg.setSelection(false);
						setVirtZP(true);
						leseVirtGeg("Ergometerzielpulstraining");
					} else {
						setVirtZP(false);
					}
				}
			});

			cmbOnlineRennen = new CCombo(cmpOnlineRennen, SWT.NONE | SWT.BORDER);
			cmbOnlineRennen.setEnabled(false);
			cmbOnlineRennen.setBounds(10, 328, 331, 23);
			Global.setFontSizeCCombo(cmbOnlineRennen);

			txtStatus = new Text(cmpOnlineRennen, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
			txtStatus.setBounds(10, 504, 331, 131);
			txtStatus.setEditable(false);
			Global.setFontSizeText(txtStatus);
			
			Label lblAdresse = new Label(cmpOnlineRennen, SWT.NONE);
			lblAdresse.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblAdresse.setBounds(181, 31, 100, 15);
			Global.setFontSizeLabel(lblAdresse);
			lblAdresse.setText(Messages.getString("OnlineRennen.lblAdresse.text")); 

			txtServerAdr = new Text(cmpOnlineRennen, SWT.BORDER);
			//txtServerAdr.setEnabled(false);
			txtServerAdr.setToolTipText(Messages.getString("OnlineRennen.serveradrtp"));
			txtServerAdr.setText(Global.serverAdr);
			// txtServerAdr.setBounds(181, 29, 308, 20);
			txtServerAdr.setBounds(335, 29, 154, 20);
			Global.setFontSizeText(txtServerAdr);
		
			Label lblStatus = new Label(cmpOnlineRennen, SWT.NONE);
			lblStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblStatus.setBounds(10, 483, 200, 15);
			Global.setFontSizeLabel(lblStatus);
			lblStatus.setText(Messages.getString("OnlineRennen.lblStatus.text")); 

			Label lblAktuelleRennen = new Label(cmpOnlineRennen, SWT.NONE);
			lblAktuelleRennen.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblAktuelleRennen.setBounds(10, 307, 200, 15);
			Global.setFontSizeLabel(lblAktuelleRennen);
			lblAktuelleRennen.setText(Messages.getString("OnlineRennen.lblAktuelleRennen.text")); 

			txtAngemeldet = new Text(cmpOnlineRennen, SWT.BORDER | SWT.V_SCROLL);
			txtAngemeldet.setEnabled(false);
			txtAngemeldet.setEditable(false);
			txtAngemeldet.setBounds(10, 389, 331, 68);
			Global.setFontSizeText(txtAngemeldet);

			Label lblAngemeldetFr = new Label(cmpOnlineRennen, SWT.NONE);
			lblAngemeldetFr.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblAngemeldetFr.setBounds(10, 368, 200, 15);
			Global.setFontSizeLabel(lblAngemeldetFr);
			lblAngemeldetFr.setText(Messages.getString("OnlineRennen.lblAngemeldetFr.text")); 

			butLanRaceDef = new Button(cmpOnlineRennen, SWT.NONE);
			butLanRaceDef.setToolTipText(Messages.getString("OnlineRennen.butLanRaceDefTP"));
			butLanRaceDef.setText(Messages.getString("OnlineRennen.butLanRaceDef")); 
			butLanRaceDef.setBounds(362, 298, 178, 25);
			Global.setFontSizeButton(butLanRaceDef);
			butLanRaceDef.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					//if (vwRace == null) {
						vwRace.on(800, 500);
					//} else
					//	vwRace.on();
				}
			});
		
			butAnmelden = new Button(cmpOnlineRennen, SWT.NONE);
			butAnmelden.setEnabled(false);
			butAnmelden.setBounds(362, 328, 75, 25);
			butAnmelden.setText(Messages.getString("OnlineRennen.btnAnmelden.text")); 
			butAnmelden.setToolTipText(Messages.getString("OnlineRennen.anmeld1")); 
			Global.setFontSizeButton(butAnmelden);
			butAnmelden.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (txtSNR.getText().isEmpty()) {
						Statusmeldung(Messages.getString("OnlineRennen.snrungueltig"));  
						Messages.errormessage(shlRsWettkampf, Messages.getString("OnlineRennen.snrungueltig")); 
					} else {
						rennanmeldung(null);
						aktualisiereAusDB(cmbOnlineRennen.getSelectionIndex());
					}
				}
			});

			butAbmelden = new Button(cmpOnlineRennen, SWT.NONE);
			butAbmelden.setEnabled(false);
			butAbmelden.setBounds(465, 328, 75, 25);
			butAbmelden.setText(Messages.getString("OnlineRennen.btnAbmelden.text")); 
			butAbmelden.setToolTipText(Messages.getString("OnlineRennen.abmeld1")); 
			Global.setFontSizeButton(butAbmelden);
			butAbmelden.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					rennabmeldung();
					aktualisiereAusDB(cmbOnlineRennen.getSelectionIndex());
				}
			});

			butStartfreigabe = new Button(cmpOnlineRennen, SWT.NONE);
			butStartfreigabe.setEnabled(false);
			butStartfreigabe.setBounds(362, 358, 178, 25);
			butStartfreigabe.setText(Messages.getString("OnlineRennen.btnStartfreigabe.text")); 
			butStartfreigabe.setToolTipText(Messages.getString("OnlineRennen.nachstartfreigabe")); 
			Global.setFontSizeButton(butStartfreigabe);
			butStartfreigabe.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					//String letzteInfo = "";
					//boolean resetflag = false;
					aktivesRennen = rennennamen[cmbOnlineRennen.getSelectionIndex()];
					if (konfigonline() == false) {
						Messages.infomessage(shlRsWettkampf, Messages.getString("OnlineRennen.fehlerconfig")); 
						return;
					}
					Rsmain.toolbar.disableItems4Race();
					Rsmain.toolbar.actionStart.setEnabled(false);
					Rsmain.toolbar.actionStop.setEnabled(false);
					warteStartfreigabe();
					Rsmain.toolbar.actionStart.setEnabled(true);	// Startbutton enablen
					Rsmain.toolbar.actionStop.setEnabled(true);	// Stopbutton enablen
				}
			});

			txtSNR = new Text(cmpOnlineRennen, SWT.BORDER | SWT.RIGHT);
			txtSNR.setEditable(false);
			txtSNR.setEnabled(false);
			txtSNR.setVisible(false);	// nicht mehr anzeigen wegen der ActionCam!
			//txtSNR.setText(Rsmain.newkonfig.getSnr()); 
			txtSNR.setText(Global.RegDat.sNr);
			txtSNR.setBounds(355, 32, 134, 21);

			txtCsvDatei = new Text(cmpOnlineRennen, SWT.BORDER | SWT.READ_ONLY | SWT.RIGHT);
			txtCsvDatei.setBounds(181, 7, 308, 21);		
			Global.setFontSizeText(txtCsvDatei);

			butCsvDatei = new Button(cmpOnlineRennen, SWT.NONE);
			butCsvDatei.setBounds(500, 6, 40, 25);
			butCsvDatei.setEnabled(false);
			butCsvDatei.setImage(new Image(Display.getCurrent(), "open.png")); 

			txtcountdown = new Text(cmpOnlineRennen, SWT.BORDER | SWT.CENTER);
			txtcountdown.setEditable(false);
			txtcountdown.setFont(SWTResourceManager.getFont("Arial", (int) (42/Global.resolutionFactor), SWT.BOLD)); 
			txtcountdown.setBounds(362, 536, 178, 68);

			Label lblSekundenBisZum = new Label(cmpOnlineRennen, SWT.NONE);
			lblSekundenBisZum.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblSekundenBisZum.setBounds(362, 615, 75, 15);
			Global.setFontSizeLabel(lblSekundenBisZum);
			lblSekundenBisZum.setText(Messages.getString("OnlineRennen.lblSekundenBisZum.text")); 

			txtTyp = new Text(cmpOnlineRennen, SWT.BORDER | SWT.WRAP);
			txtTyp.setEditable(false);
			txtTyp.setBounds(362, 389, 178, 142);
			Global.setFontSizeText(txtTyp);

			Label lblOnlinetraining = new Label(cmpOnlineRennen, SWT.NONE);
			lblOnlinetraining.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblOnlinetraining.setBounds(79, 287, 160, 18);
			Global.setFontSizeLabel(lblOnlinetraining);
			lblOnlinetraining.setText(Messages.getString("OnlineRennen.lblOnlinetraining.text")); 

			Label lblRennenGegenVirtuelle = new Label(cmpOnlineRennen, SWT.NONE);
			lblRennenGegenVirtuelle.setText(Messages.getString("OnlineRennen.lblRennenGegenVirtuelle.text")); 
			lblRennenGegenVirtuelle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblRennenGegenVirtuelle.setBounds(79, 117, 248, 18);
			Global.setFontSizeLabel(lblRennenGegenVirtuelle);

			Label lblLine = new Label(cmpOnlineRennen, SWT.SEPARATOR | SWT.HORIZONTAL);
			lblLine.setBounds(10, 295, 530, 2);

			Label lblLine1 = new Label(cmpOnlineRennen, SWT.SEPARATOR | SWT.HORIZONTAL);
			lblLine1.setBounds(10, 125, 530, 2);

			tblVirtGeg = new Table(cmpOnlineRennen, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK);
			tblVirtGeg.setBounds(10, 141, 530, 142);
			tblVirtGeg.setHeaderVisible(true);
			tblVirtGeg.setLinesVisible(true);
			Global.setFontSizeTable(tblVirtGeg);
			for (int i = 0; i < header.length; i++) {
				TableColumn column = new TableColumn(tblVirtGeg, SWT.NONE);
				column.setText(header[i]);
			}

			butCsvDatei.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String CSVDatei = initcsvrace();
					if (CSVDatei != null)
						txtCsvDatei.setText(CSVDatei.substring(CSVDatei.lastIndexOf(Global.ptz)+1)); 
				}
			});

			shlRsWettkampf.open();
			if (!showDialog) {
				shlRsWettkampf.setVisible(false);
			}
		}
	}

	/**
	 * Hier wird auf die Startfreigabe vom Webservice gewartet.
	 * Es wird die Anzahl der Sekunden bis zum Start vom Webservice empfangen und angezeigt.
	 * Weitere Meldungen (Anmeldungen) werden im Statusfeld angezeigt.
	 */
	public void warteStartfreigabe() {
		String letzteInfo = "";
		String erg;
		boolean resetflag = false;
		Vector<String> vret = new Vector<String>();
		Display display = shlRsWettkampf.getDisplay();
		Shell info = null;

		try {
			if (isOnlRace()) {
				MTBSRaceServiceStub stub;
				stub = new MTBSRaceServiceStub();
				ConnectDB connect = new ConnectDB();
				connect.setDb(db);
				//ConnectDBResponse res = stub.connectDB(connect);
				stub.connectDB(connect);

				WarteBisStart warte = new WarteBisStart();
				warte.setDb(db);
				warte.setSNR(txtSNR.getText());

				String aktRennen = rennennamen[cmbOnlineRennen.getSelectionIndex()];
				warte.setRennen(aktRennen);
				WarteBisStartResponse warteres;
				Statusmeldung(Messages.getString("OnlineRennen.warteaufautom")); 
				do {
					if (!isOnlRace())
						return;
					for (int i=0; i<100; i++) {
						Global.sleep(10);				// etwas warten...				    	
						display.readAndDispatch();
					}
					warteres = stub.warteBisStart(warte);
					erg = warteres.get_return();
					if (!erg.startsWith("WEBSV-")) { 
						if (erg.length() > 4) {			// dann ists eine andere Meldung
							if (!erg.equals(letzteInfo)) {	// nur einmal ausgeben
								Statusmeldung("Info: "+erg);
								letzteInfo = erg;
							}
						}
						else {
							// automatischen Reset (bei Kettler)
							try {
								Integer sekunden = new Integer(erg);
								if (sekunden.intValue() <= 5 && !resetflag) {
									resetflag = true;
									Rsmain.thisTrainer.talk(Trainer.Commands.reset, 0.0);  // Reset für Kettler
									Rsmain.thisTrainer.talk(Trainer.Commands.init, 0.0);  
								}
							} catch (NumberFormatException e1) {
								Mlog.info("NumberformatException bei Countdown!");
							}
							txtcountdown.setText(erg);
							txtcountdown.update();
						}
					}
					display.update();
					butOk.setEnabled(false);
				} while (!erg.startsWith("0") && !erg.startsWith("WEBSV-"));  

				butOk.setEnabled(true);

				if (erg.startsWith("WEBSV-")) {  
					Mlog.error("Web-Servicefehler: "+erg);  
					Statusmeldung("Web-Servicefehler: "+erg);  
				} else {
					Mlog.info("Startfreigabe erteilt!");
					setAktiv(true);
					goBack(true);
					Rsmain.toolbar.actionStart.run();
				}
			} else
				if (isLanRace() || isLanRaceAuto()) {
					do {
						if (!chkLANRennen.getSelection() && !chkLANRennenAuto.getSelection())
							return;
						for (int i=0; i<100; i++) {
							Global.sleep(10);					// etwas warten...				    	
							display.readAndDispatch();
						}
						Mlog.debug("Client, sende wartebisstart : " + Global.serverAdr + " - " + mACAdr + " - " + aktivesRennen);
						vret = clientSend("wartebisstart", Global.serverAdr, mACAdr, aktivesRennen);
						erg = vret.firstElement();
						Mlog.debug("Client, zurückgeliefert wartebisstart: " + erg);
						if (!erg.startsWith("ERR")) { 			// Serverfehler ?
							if (erg.length() > 4) {				// dann ists eine andere Meldung
								if (!erg.equals(letzteInfo)) {	// nur einmal ausgeben
									Statusmeldung("Info: "+erg);
									letzteInfo = erg;
								}
							} else {
								if (isLanRaceAuto()) {
									Messages.infomessage_off(info);
									info = Messages.infomessage_on(erg,80,80,42);
								} else {
									txtcountdown.setText(erg);
									txtcountdown.update();	
								}
							}
						} 
						
						display.update();
						butOk.setEnabled(false);
					} while (!erg.startsWith("0") && !erg.startsWith("ERR"));  				
					butOk.setEnabled(true);

					if (erg.startsWith("ERR")) { 			// Serverfehler ?
						Mlog.error("Serverfehler: "+erg);  
						Statusmeldung("Serverfehler: "+erg);  
					} else {
						Mlog.info("Startfreigabe erteilt!");
						setAktiv(true);
						goBack(true);
						if (!isLanRaceAuto())
							Rsmain.toolbar.actionStart.run();
						else 
							Messages.infomessage_off(info);
					}
				}
		} catch (Exception e1) {
			Mlog.ex(e1);
		}
	}

	/**
	 * Verlassen des Online-(Wettkampf-)Dialogs
	 */
	private void goBack(boolean changed) {
		shlRsWettkampf.setVisible(false);
		setChanged(changed);
		if (txtCsvDatei.getText() == null)
			setCsvAktiv(false);
        Rsmain.setImDialog(false);
	}
	

    /**
     * Initialisiert das Rennen gegen eine CSV-Datei.
     * Dabei wird die Datei ausgesucht und eingelesen. Anschliessend wird das Flag gesetzt
     * (und die Meldung ausgegeben, daß einige Parameter temporär umgestellt wurden?)
     * @return	CSV-Datei
     */
    public String initcsvrace() {
		FileDialog dialog = new FileDialog(shlRsWettkampf, SWT.OPEN);
		dialog.setFilterNames(new String [] {Messages.getString("Rsmain.csv_protokolldateien")});  
		dialog.setFilterExtensions(new String [] {"*.csv"}); //Windows wild cards  
		dialog.setFilterPath(Global.strPfad); // TODO! hier noch auf den Tournamen eingrenzen!
		CSVDatei = dialog.open();
		if (CSVDatei != null) {
			Rsmain.aktgegner = new Gegner();
			if (Rsmain.aktgegner.loadCSVFile(CSVDatei)) {
				Rsmain.setCsvrennen(true);
				Mlog.info(Messages.getString("OnlineRennen.csvinitgeg")+CSVDatei);  
				Statusmeldung(Messages.getString("OnlineRennen.csvinitgeg")+CSVDatei); 
			} else
				Messages.errormessage(shlRsWettkampf, Messages.getString("Rsmain.die_csv_datei")+CSVDatei+Messages.getString("Rsmain.konnte_nicht_gel")); 
		}
		return CSVDatei;
    }

	/** 
	 * Aktualisierung der angezeigten Daten in:
	 * Rennliste, angemeldete Rennen und Statusbereich.
	 * @param pActOLRIndex		pActOLRIndex?
	 */
	public void aktualisiereAusDB(int pActOLRIndex) {
		int i;
    	Vector<String> vret = new Vector<String>();
		
		// füllen der Combobox mit der Liste der bevorstehenden Rennen
		if (isOnlRace()) {
			//String[] posliste = null;
			String[] ergzeile = null;
			String[] ergliste = null;
			chkRennenGegenCsv.setSelection(false);
			//Statusmeldung(Messages.getString("OnlineRennen.aktuellolrabf")); 
			try {
				MTBSRaceServiceStub stub = new MTBSRaceServiceStub();
				ConnectDB connect = new ConnectDB();
				connect.setDb(db);
				ConnectDBResponse res = stub.connectDB(connect);
				Mlog.debug("Rückgabe Connect: "+res.get_return());		
				RennenListe rl = new RennenListe();
				rl.setDb(db);
				//rl.setSNR("1234567890");
				RennenListeResponse rlr;
				rlr = stub.rennenListe(rl);
				rennliste = rlr.get_return();
				ergliste = rennliste.clone();	// String-Elemente erzeugen!
				rennennamen = rennliste.clone();	// String-Elemente erzeugen!
				if (!rennliste[0].equals("0")) { 
					for (i=0; i<rennliste.length; i++) {
						//System.out.println("Rückgabe Rennen-Liste: "+rennliste[i]);
						ergzeile = rennliste[i].split(";"); 
						//ergliste[i] = ergzeile[0] + " am " + ergzeile[1] + " Teiln.: " + ergzeile[2];  
						ergliste[i] = ergzeile[0];  
						rennennamen[i] = ergzeile[0];
					}
					if (i > 0) {
						butAnmelden.setEnabled(true);
						butAbmelden.setEnabled(true);
					}
					cmbOnlineRennen.removeAll();
					cmbOnlineRennen.setItems(ergliste);
					cmbOnlineRennen.select(pActOLRIndex);
				}
				//Statusmeldung(Messages.getString("OnlineRennen.aktuellanmeldafr")); 
				//txtStatus.update();
				rl.setDb(db);
				rl.setSNR(txtSNR.getText());
				RennenListeResponse rlranmeld;
				rlranmeld = stub.rennenListe(rl);
				anmeldeliste = rlranmeld.get_return();
				ergliste = anmeldeliste;	// String-Elemente erzeugen!
				txtAngemeldet.setText(""); 

				if (!anmeldeliste[0].equals("0")) { 
					for (i=0; i<anmeldeliste.length; i++) {
						//System.out.println("Rückgabe Anmelde-Liste: "+anmeldeliste[i]);
						ergzeile = anmeldeliste[i].split(";"); 
						//ergliste[i] = ergzeile[0] + " am " + ergzeile[1] + " Teiln.: " + ergzeile[2];  
						ergliste[i] = ergzeile[0];  
						txtAngemeldet.append(ergliste[i] + "\n"); 
					}
					if (i > 0)
						butStartfreigabe.setEnabled(true);
				}
			} catch (Exception e) {
				Mlog.ex(e);
			}
		} else {
			if (isLanRace()) {		// LAN-Rennen?
				// kommende definierte Rennen anzeigen
				//String[] posliste = null;
				String[] ergzeile = null;
				String[] ergliste = null;
				chkRennenGegenCsv.setSelection(false);
				//Statusmeldung(Messages.getString("OnlineRennen.aktuelllanrabf")); 
				vret = clientSend("rennenListe", Global.serverAdr, null, null);
				Iterator<String> vIt = vret.iterator();
				i = 0;
				ergliste = new String[vret.size()];
				rennennamen = new String[vret.size()];
				while (vIt.hasNext()) {
					String rennen = vIt.next()+"";
					Mlog.debug("Client, zurückgeliefert Rennen: " + rennen);
					if (rennen.startsWith("ERROR!")) {
						chkLANRennen.setSelection(false);
						Statusmeldung(rennen); 
						return;						
					}
					ergzeile = rennen.split(trz);
					ergliste[i] = ergzeile[0];  
					rennennamen[i++] = ergzeile[0];  
				}
				if (i > 0) {
					butAnmelden.setEnabled(true);
					butAbmelden.setEnabled(true);
				}

				cmbOnlineRennen.removeAll();
				cmbOnlineRennen.setItems(ergliste);
				cmbOnlineRennen.select(0);
				
				// nun die aktuellen Anmeldungen anzeigen
				//Statusmeldung(Messages.getString("OnlineRennen.aktuellanmeldafr")); 
				vret = clientSend("rennenListe", Global.serverAdr, mACAdr, null);
				vIt = vret.iterator();
				i = 0;
				anmeldeliste = new String[vret.size()];
				txtAngemeldet.setText(""); 
				while (vIt.hasNext()) {
					String anmeld = vIt.next()+"";
					Mlog.debug("Client, zurückgeliefert Anmeldungen: " + anmeld);
					ergzeile = anmeld.split(trz);
					anmeldeliste[i] = ergzeile[0];  
					txtAngemeldet.append(anmeldeliste[i++] + "\n"); 
				}
				if (i > 0)
					butStartfreigabe.setEnabled(true);
			}
		}
	}

	/**
	 * Es wird ein Kommando zum Server gesendet.
	 * Das Kommando besteht aus dem Befehl und mehreren Parametern
	 * Kommandos werden mit *EOM* abgeschlossen.
	 * @param befehl
	 * @param serverIP
	 * @param par1
	 * @param par2
	 * @return
	 */
	private Vector<String> clientSend(String befehl, String serverIP, String par1, String par2) {
		String rueckgabe;
    	Vector<String> vret = new Vector<String>();

		try {
    		Socket echoSocket = new Socket(serverIP, 4488);
    		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
    		if (par2 == null)
    			out.println(befehl + trz + par1);
    		else
    			out.println(befehl + trz + par1 + trz + par2);
    			
    		out.println(eom);
    		
    		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

    		while (!(rueckgabe = in.readLine()).equals(eom)) {	
	    		//Mlog.debug("Client, Rückgabe: "+rueckgabe);
				vret.add(rueckgabe);
			}

			out.close();
			in.close();
			echoSocket.close();
    		return vret;
    		
    	} catch (ConnectException e1) {
			Mlog.error("Fehler bei clientSend: kein Connect möglich!");
    		Messages.errormessage(shlRsWettkampf, Messages.getString("OnlineRennen.noconnect")); 	// Der Server antwortet nicht!
    		if (vret.size() > 0)
    			vret.setElementAt("ERROR! no connect!", 0) ;
    		else
				vret.addElement("ERROR! no connect!");
    		return(vret); 
    	}
    	catch (UnknownHostException e2) {
    		Mlog.error("Fehler bei clientSend: Der Server ist unbekannt!");
    		Messages.errormessage(shlRsWettkampf, Messages.getString("OnlineRennen.serverunknown")); 	// Der Server ist unbekannt
    		if (vret.size() > 0)
    			vret.setElementAt("ERROR! server unknown!", 0) ;
    		else
    			vret.addElement("ERROR! server unknown!");
    		return(vret); 
    	}
		catch (Exception e3) {
			Mlog.error("Fehler bei clientSend: anderer Fehler!");
			Mlog.ex(e3);
    		if (vret.size() > 0)
    			vret.setElementAt("ERROR! "+e3.toString(), 0) ;
    		else
				vret.addElement("ERROR! "+e3.toString());
    		return(vret); 
    	}
	}

	/**
	 * Es wird ein Kommando zum Server gesendet.
	 * Das Kommando besteht aus dem Befehl und mehreren Parametern
	 * Kommandos werden mit *EOM* abgeschlossen.
	 * @param befehl
	 * @param serverIP
	 * @param par1
	 * @param par2..par16
	 * @return
	 */
	private Vector<String> clientSend(String befehl, String serverIP, String par1, String par2, String par3, String par4, String par5, String par6
			, String par7, String par8, String par9, String par10, String par11, String par12, String par13, String par14, String par15, String par16) {
		String rueckgabe;
    	Vector<String> vret = new Vector<String>();

		try {
    		Socket echoSocket = new Socket(serverIP, 4488);
    		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
    		out.println(befehl + trz + par1 + trz + par2 + trz + par3 + trz + par4 + trz + par5 + trz + par6 + trz + par7 + trz + par8 + trz + par9 + trz + par10
    				 + trz + par11 + trz + par12 + trz + par13 + trz + par14 + trz + par15 + trz + par16);
    			
    		out.println(eom);
    		
    		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
    		//String rueckgabe = in.readLine();
			while (!(rueckgabe = in.readLine()).equals(eom)) {	
	    		//Mlog.debug("Client, Rückgabe: "+rueckgabe);
				vret.add(rueckgabe);
			}

			out.close();
			in.close();
			echoSocket.close();
    		return vret;    		
    	} catch (Exception e1) {
			Mlog.error("Fehler bei clientSend16: anderer Fehler!");
			Mlog.ex(e1);
    		if (vret.size() > 0)
    			vret.setElementAt("ERROR! "+e1.toString(), 0) ;
    		else
				vret.addElement("ERROR! "+e1.toString());
    		return(vret); 
    	}
	}

	/**
	 * Es wird ein Kommando zum Server gesendet.
	 * Das Kommando besteht aus dem Befehl und mehreren Parametern
	 * Kommandos werden mit *EOM* abgeschlossen.
	 * @param befehl
	 * @param serverIP
	 * @param par1
	 * @param par2..par5
	 * @return
	 */
	private Vector<String> clientSend(String befehl, String serverIP, String par1, String par2, String par3, String par4, String par5) {
		String rueckgabe;
    	Vector<String> vret = new Vector<String>();

		try {
    		Socket echoSocket = new Socket(serverIP, 4488);
    		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
    		out.println(befehl + trz + par1 + trz + par2 + trz + par3 + trz + par4 + trz + par5);
    			
    		out.println(eom);
    		
    		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			while (!(rueckgabe = in.readLine()).equals(eom)) {	
	    		//Mlog.debug("Client, Rückgabe: "+rueckgabe);
				vret.add(rueckgabe);
			}

			out.close();
			in.close();
			echoSocket.close();
    		return vret;    		
    	} catch (Exception e1) {
			Mlog.error("Fehler bei clientSend5: anderer Fehler!");
			Mlog.ex(e1);
    		if (vret.size() > 0)
    			vret.setElementAt("ERROR! "+e1.toString(), 0) ;
    		else
				vret.addElement("ERROR! "+e1.toString());
    		return(vret); 
    	}
	}

	/**
	 * Hier werden die Daten (aktuell immer beim Wechsel auf den nächsten GPS-Punkt) 
	 * an den Webservice "insertActPos" übertragen.
	 * @param latitude		Breitengrad
	 * @param longitude		Längengrad
	 * @param gps_punkt		Index des GPS-Punktes
	 * @param wind			Windstärke
	 * @param gang			eingel. Gang
	 * @param energie		Energie in kCal
	 * @param puls			Puls
	 * @param rpm			Kurbelumdrehungen
	 * @param leistung		Leistung
	 * @param geschwindigkeit	akt. Geschwindigkeit
	 * @param strecke		akt. Strecke
	 * @param steigung		akt. Steigung
	 * @param hoehe			akt. Höhe
	 * @param zpstrecke 	Zielpulsstrecke
	 */
	public void sendData(double latitude, double longitude, long gps_punkt, 
			int wind, int gang, int energie, int puls, double rpm,
			double leistung, double geschwindigkeit, double strecke,
			double steigung, double hoehe, double zpstrecke) {
		int index;
		String ret = null;
		String gwind = null;
		if (aktivesRennen == null)
			return;
		try {
			if (isSchikane())
				gwind = "1";
			else
				gwind = "0";

			if (isOnlRace()) {	
				MTBSRaceServiceStub stub;
				stub = new MTBSRaceServiceStub();
				if (teilnahmeID == 0) {
					GetTeilnahmeID get = new GetTeilnahmeID();
					get.setDb(db);
					get.setSNR(txtSNR.getText());
					get.setRennen(aktivesRennen);
					GetTeilnahmeIDResponse getres = stub.getTeilnahmeID(get);
					teilnahmeID = new Long(getres.get_return());
				}

				InsertActPos1 ins = new InsertActPos1();
				ins.setDb(db);
				ins.setSNR(txtSNR.getText());
				ins.setID(teilnahmeID+""); 
				ins.setEnergie(energie+""); 
				ins.setGang(gang+""); 
				ins.setGeschwindigkeit(geschwindigkeit+""); 
				ins.setGps_Punkt(gps_punkt+""); 
				ins.setHoehe(hoehe+""); 
				ins.setLat(latitude+""); 
				ins.setLong(longitude+""); 
				ins.setLeistung(leistung+""); 
				ins.setPuls(puls+""); 
				ins.setRPM(rpm+""); 
				ins.setSteigung(steigung+""); 
				ins.setStrecke(strecke+""); 
				ins.setWind(gwind);
				ins.setZPStrecke(zpstrecke+"");
				InsertActPos1Response insres = stub.insertActPos1(ins);
				ret = insres.get_return();
			} else
				if (isLanRace()) {
					Vector<String> vret;
					if (teilnahmeID == 0) {
						vret = clientSend("getteilnahmeid", Global.serverAdr, mACAdr, aktivesRennen);
						ret = vret.firstElement();
						if (ret.startsWith("ERR")) { 	// Serverfehler 
							Mlog.error("Serverfehler bei getteilnahmeid: " + ret); 						
						} else {
//							Mlog.debug("getteilnahmeid: " + ret); 
							teilnahmeID = new Long(ret);
						}
					}
					vret = clientSend("insertactpos", Global.serverAdr, mACAdr, teilnahmeID+"", latitude+"", longitude+"", gps_punkt+"", gwind, gang+"", 
							energie+"", puls+"", rpm+"", leistung+"", geschwindigkeit+"", 
							strecke+"", steigung+"", hoehe+"", zpstrecke+"");
					ret = vret.firstElement();
					if (ret.startsWith("ERR")) { 	// Serverfehler 
						Mlog.error("Serverfehler bei sendData: " + ret); 						
					} else {
						Mlog.debug("sendData: " + ret); 
					}
				}
			if (ret.matches("[0123456]")) {
				index = new Integer(ret);
				index = Math.abs(index - 6);
				if (index != Rsmain.cmbwind.getSelectionIndex())
					Rsmain.setWindAndShow(index);
			}

		} catch (Exception e) {
			Mlog.ex(e);
		}
	}
	
	/**
	 * receiveData() frägt die Rennen-Id ab, wenn sie noch nicht gefüllt ist und
	 * füllt die Liste der Teilnehmer mit der akt. Position im Rennen mittels Webservice ActPosListe.
	 */
	public void receiveData() {
		String[] posliste = null;
		String[] ergzeile = null;
		String ret = null;
		//int i;

		if (aktivesRennen == null)
			return;

		try {	
			if (isOnlRace()) {
				MTBSRaceServiceStub stub;
				stub = new MTBSRaceServiceStub();
				if (rennenID == 0) {
					GetRennenID get = new GetRennenID();
					get.setDb(db);
					get.setSNR(txtSNR.getText());
					get.setRennen(aktivesRennen);
					GetRennenIDResponse getres = stub.getRennenID(get);
					Mlog.info("GetRennenID: "+getres.get_return()); 
					rennenID = new Long(getres.get_return());
				}

				ActPosListe apl = new ActPosListe();
				apl.setDb(db);
				apl.setSNR(txtSNR.getText());
				apl.setRennenId(rennenID+""); 
				ActPosListeResponse aplr = stub.actPosListe(apl);
				posliste = aplr.get_return();

				if (!posliste[0].equals("0")) { 
					gegliste = new ArrayList<OnlineGegner>();
					for (int i=0; i<posliste.length; i++) {
						ergzeile = posliste[i].split(";"); 
						gegner = new OnlineGegner(ergzeile[0], ergzeile[1], null, new Long(ergzeile[4]).longValue(), ergzeile[5], new Integer(ergzeile[6]).intValue(), 
								new Double(ergzeile[7]).doubleValue(), new Double(ergzeile[8]).doubleValue(), new Double(ergzeile[9]).doubleValue(), 
								new Double(ergzeile[10]).doubleValue(), new Double(ergzeile[11]).doubleValue(), new Double(ergzeile[12]).doubleValue(), 
								new Double(ergzeile[13]).doubleValue(), new Double(ergzeile[14]).doubleValue(), new Double(ergzeile[2]).doubleValue(), 
								new Double(ergzeile[3]).doubleValue(), new Double(ergzeile[15]).doubleValue(), 0,  new Integer(ergzeile[16]).intValue());
						gegliste.add(gegner);
					}
				}
			} else
				if (isLanRace()) {
					Vector<String> vret;
					if (rennenID == 0) {
						vret = clientSend("getrennenid", Global.serverAdr, mACAdr, aktivesRennen);
						ret = vret.firstElement();
						if (ret.startsWith("ERR")) { 	// Serverfehler 
							Mlog.error("Serverfehler bei getrennenid: " + ret); 						
						} else {
//							Mlog.debug("getrennenid: " + ret); 
							rennenID = new Long(ret);
						}
					}
					vret = clientSend("actposliste", Global.serverAdr, mACAdr, rennenID+"");
					ret = vret.firstElement();
	
					if (ret.startsWith("ERR")) { 	// Serverfehler 
						Mlog.error("Serverfehler bei actposliste: " + ret); 						
					} else {
//						Mlog.debug("actposliste: " + ret); 
						Iterator<String> vIt = vret.iterator();
						gegliste = new ArrayList<OnlineGegner>();
						//int i = 0;
						posliste = new String[vret.size()];
						txtAngemeldet.setText(""); 
						while (vIt.hasNext()) {
							String pos = vIt.next()+"";
							Mlog.debug("Client, zurückgeliefert Position: " + pos);
							ergzeile = pos.split(trz);
							gegner = new OnlineGegner(ergzeile[0], ergzeile[1], null, new Long(ergzeile[4]).longValue(), ergzeile[5], new Integer(ergzeile[6]).intValue(), 
									new Double(ergzeile[7]).doubleValue(), new Double(ergzeile[8]).doubleValue(), new Double(ergzeile[9]).doubleValue(), 
									new Double(ergzeile[10]).doubleValue(), new Double(ergzeile[11]).doubleValue(), new Double(ergzeile[12]).doubleValue(), 
									new Double(ergzeile[13]).doubleValue(), new Double(ergzeile[14]).doubleValue(), new Double(ergzeile[2]).doubleValue(), 
									new Double(ergzeile[3]).doubleValue(), new Double(ergzeile[15]).doubleValue(), 0, new Integer(ergzeile[16]).intValue());
							gegliste.add(gegner);
						}
					}					
				}
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * endeRennen() frägt die Rennen-Id ab, wenn sie noch nicht gefüllt ist und
	 * füllt die Ergebnisliste mit den aktuell gemeldeten Ergebnissen.
	 * @param ende01	"1": Ende setzen (Ziel erreicht), "0" nur Ergebnisse holen
	 */
	public void getRaceErgebnis(String ende01) {
		String[] ergzeile = null;
		String ret = null;
		SimpleDateFormat sdfSek = new SimpleDateFormat("ss");

		if (aktivesRennen == null)
			return;

		try {	
			if (isLanRace()) {
				Vector<String> vret;
				if (rennenID == 0) {
					vret = clientSend("getrennenid", Global.serverAdr, mACAdr, aktivesRennen);
					ret = vret.firstElement();
					if (ret.startsWith("ERR")) { 	// Serverfehler 
						Mlog.error("Serverfehler bei getrennenid: " + ret); 						
					} else {
//						Mlog.debug("getrennenid: " + ret); 
						rennenID = new Long(ret);
					}
				}
				Mlog.info("Ziellinie überschritten!");
				vret = clientSend("ergliste", Global.serverAdr, mACAdr, rennenID+"", ende01, "", "");
				ret = vret.firstElement();

				if (ret.startsWith("ERR")) { 	// Serverfehler 
					Mlog.error("Serverfehler bei ergliste: " + ret); 						
				} else {
					Mlog.debug("ergliste: " + ret); 
					Iterator<String> vIt = vret.iterator();
					ergliste = new ArrayList<OnlineGegner>();
					while (vIt.hasNext()) {
						String erg = vIt.next()+"";
						Mlog.debug("Client, zurückgeliefert Ergebnis: " + erg);
						ergzeile = erg.split(trz);
						gegner = new OnlineGegner("", ergzeile[2], sdfSek.parse(ergzeile[1]), 0, "", 0, 
								new Double(ergzeile[3]).doubleValue(), new Double(ergzeile[4]).doubleValue(), new Double(ergzeile[5]).doubleValue(), 
								new Double(ergzeile[6]).doubleValue(), 0.0, 0.0, 
								0.0, 0.0, 0.0, 
								0.0, new Double(ergzeile[7]).doubleValue(), new Integer(ergzeile[0]).intValue(), new Integer(ergzeile[8]).intValue());
						ergliste.add(gegner);
					}
				}					
			}
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * konfigonline() ermittelt die Konfigurationsdaten des Rennens mittels Webservice GetKonfiguration()
	 * und stellt sie in RoomSports ein.
	 * @return Fehler aufgetreten?
	 */
	public boolean konfigonline() {
		String ergzeile = null;
		String[] ergzeilen = null;
    	Vector<String> vret = new Vector<String>();
    	
		if (aktivesRennen == null)
			return false;

		if (Rsmain.thisTrainer.hatverbindung() == Trainer.kommunikation.keine) {
			if (!txtSNR.getText().startsWith("2010778466A999")) {	// nur der TRON und das MTBS-Team dürfen im Demomodus fahren!
				Messages.infomessage(shlRsWettkampf, Messages.getString("OnlineRennen.offline"));
				return false;
			}
		}
		if (isOnlRace()) {
			MTBSRaceServiceStub stub;
			try {
				stub = new MTBSRaceServiceStub();

				GetKonfiguration konfig = new GetKonfiguration();
				konfig.setDb(db);
				konfig.setRennen(aktivesRennen);
				konfig.setSNR(txtSNR.getText());
				GetKonfigurationResponse konfigres = stub.getKonfiguration(konfig);
				ergzeile = konfigres.get_return();
			} catch (Exception e) {
				Mlog.ex(e);
				return false;
			}
			Mlog.info("Konfiguration: "+ergzeile); 
			Statusmeldung("Konfig: " + ergzeile);  

			ergzeilen = ergzeile.split(";"); 
		} else 
			if (isLanRace()) {
				vret = clientSend("getkonfiguration", Global.serverAdr, mACAdr, aktivesRennen);
				ergzeile = (String) vret.firstElement();
				Mlog.debug("Client, zurückgeliefert konfig: " + ergzeile);
				if (ergzeile.startsWith("ERR")) { 	// Serverfehler 
					Mlog.error("Serverfehler bei konfigonline: " + ergzeile); 						
				} else {
					ergzeilen = ergzeile.split(trz);
					Mlog.info("Konfiguration: "+ergzeile); 
					Statusmeldung("Konfig: " + ergzeile); 
				}
			}

		double minleistung = new Double(ergzeilen[0]).doubleValue();
		Rsmain.biker.setMinleistung(minleistung);
		double maxleistung = new Double(ergzeilen[5]).doubleValue();
		if (maxleistung > 0.0)
			Rsmain.biker.setMaxleistung(maxleistung);
		else
			Rsmain.biker.setMaxleistung(400);	// Defaultwert wenn nicht vorgegeben

		double normTF = new Double(ergzeilen[9]).doubleValue();
		Rsmain.biker.setDynRPMNormal(normTF);	// Normaltrittfrequenz 
		Rsmain.biker.setDynRPMWiege(0);
		Rsmain.biker.setAutomatik(false); 	// Gangautomatik immer aus
		Rsmain.newkonfig.setDynamik(ergzeilen[1].contentEquals("1")); 
		Rsmain.newkonfig.setaveraging(true);	// Höhendaten immer glätten
		double lf = new Double(ergzeilen[7]).doubleValue();
		double tfmin = new Double(ergzeilen[10]).doubleValue();
		double cw = new Double(ergzeilen[11]).doubleValue();
		double k2 = new Double(ergzeilen[12]).doubleValue();
		double gewbike = new Double(ergzeilen[13]).doubleValue();
		double gewfahrer = new Double(ergzeilen[14]).doubleValue();
		int gpsPkt = new Integer(ergzeilen[15]).intValue();
		if (gpsPkt > 0)
			Rsmain.setStartGPSPunkt(gpsPkt);
		gpsPkt = new Integer(ergzeilen[16]).intValue();
		if (gpsPkt > 0)
			Rsmain.setZielGPSPunkt(new Integer(ergzeilen[16]).intValue());
		
		if (cw > 0.0)
			Rsmain.biker.setCwa(cw);
		if (k2 > 0.0)
			Rsmain.biker.setK2(k2);
		if (lf > 0.0)
			Rsmain.biker.setLfakt(lf);		// Leistungsfaktor
		if (tfmin > 0.0)
			Rsmain.drpmmin = tfmin;			// Mindestdrehzahl bei Rennen
		if (gewbike > 0.0)
			Rsmain.biker.setBikegewicht(gewbike);
		if (gewfahrer > 0.0)
			Rsmain.biker.setFahrergewicht(gewfahrer);

		double minpuls = new Double(ergzeilen[3]).doubleValue();
		double maxpuls = new Double(ergzeilen[4]).doubleValue();
		if (minpuls > 0.0) {
			Rsmain.biker.setPulsgruen(minpuls);
			Rsmain.biker.setPulsgelb(maxpuls+1);
		}
		if (!Global.strTourvideo.startsWith(ergzeilen[2])) {
			Statusmeldung(Messages.getString("OnlineRennen.falscheTour"));  
			Mlog.info(Messages.getString("OnlineRennen.falscheTour")); 
			return false;
		}
		if (ergzeilen[6].matches("(.*)puls(.*)"))	// Bei Netzwerktraining steht nur "Zielpuls" drin
			setZprace(true);
		else
			setZprace(false);

		if (ergzeilen[8].contentEquals("1"))	// Schikane ein oder ausschalten
			setSchikane(true);
		else
			setSchikane(false);

		String bikeTyp;
		if (cw == 0.25)
			bikeTyp = "Rennrad";
		else
			bikeTyp = "MTB";

		String sTyp = ergzeilen[6] + "\n" +
		Messages.getString("Auswertung.leistung") + ": " + zfk0.format(minleistung) + " - " + zfk0.format(maxleistung) + " W\n" +
		Messages.getString("Auswertung.puls") + ": " + zfk0.format(minpuls) + " - " + zfk0.format(maxpuls) + " bpm\n" +
		Messages.getString("OnlineRennen.dynamik") + ": " + Messages.einaus(Rsmain.newkonfig.isDynamik()) + "\n" +
		Messages.getString("Onlinerennen.nTF") + ": " + zfk0.format(normTF) + "\n" + 
		Messages.getString("Onlinerennen.wind") + ": " + Messages.einaus(isSchikane()) + "\n" + 
		Messages.getString("Onlinerennen.LF") + ": " + zfk0.format(lf * 100) + "\n" + 
		Messages.getString("Onlinerennen.bike") + ": " + bikeTyp + "\n" + 
		Messages.getString("Onlinerennen.gewichte") + ": " + zfk0.format(gewfahrer) + "kg + " + zfk0.format(gewbike) + "kg\n" + 
		Messages.getString("Onlinerennen.TFmin") + ": " + zfk0.format(tfmin);

		txtTyp.setText(sTyp);

		return true;
	}


	/**
	 * Darstellung und Aufbau des Online-Rennen Registrierungs-Dialogs.
	 * @return True: Registrierung erfolgreich, False: Fehler bei Registrierung
	 */
	private void DoRegistrierung() {
		Vector<String> vret = new Vector<String>();
		Mlog.info("User-Registrierung"); 

		//if (SNRGueltig(txtSNR.getText()) == false)
		if (txtSNR.getText().isEmpty())
			Messages.errormessage(shlRsWettkampf, Messages.getString("OnlineRennen.snrungueltig")); 

		if (regRace == null) {
			regRace = new RegistrierungRace();
			regRace.on(360, 240);
		} else
			regRace.on();
		
		if (isOnlRace()) {
			if (regRace.isErgebnisOK()) {
				try {
					MTBSRaceServiceStub stub = new MTBSRaceServiceStub();
					ConnectDB connect = new ConnectDB();
					connect.setDb(db);
					stub.connectDB(connect);

					InsertUser insuser = new InsertUser();
					insuser.setDb(db);
					insuser.setSNR(txtSNR.getText());
					insuser.setName(regRace.name);
					insuser.setVorname("");
					insuser.setOrt(regRace.ort);
					insuser.setGebjahr(regRace.gebJahr);
					insuser.setTrainer(regRace.trainer);
					InsertUserResponse insuserres = stub.insertUser(insuser);
					String ret = insuserres.get_return();
					Mlog.info("InsertUser: "+ret); 
					if (ret.equalsIgnoreCase("OK")) { 
						Statusmeldung(Messages.getString("OnlineRennen.registrierungerfolgreich")); 
					} else {
						Statusmeldung(Messages.getString("OnlineRennen.fehglerbeiregis"));							 
					}
				} catch (Exception e1) {
					Mlog.ex(e1);
				}
				rennanmeldung(null);
				aktualisiereAusDB(0);
				//shlRegistrierung.setVisible(false);			
			}
		} else {
			if (isLanRace()) {
				if (regRace.isErgebnisOK()) {
					vret = clientSend("insertuser", Global.serverAdr, mACAdr, regRace.name, regRace.ort, regRace.gebJahr, regRace.trainer);
					String ret = vret.firstElement();
					if (ret.startsWith("ERR")) { 	// Serverfehler 
						Mlog.error("Serverfehler bei insertuser: " + ret); 						
					} else {
						Mlog.debug("insertuser: " + ret); 
						Statusmeldung(Messages.getString("OnlineRennen.registrierungerfolgreich")); 
						rennanmeldung(null);
						aktualisiereAusDB(0);
					}
				}
			}
		}
	}
	
	/**
	 * neue Statuszeile anfügen im Statusfenster
	 * @param text
	 */
	private void Statusmeldung(String text) {
		txtStatus.append(text + "\n"); 
		txtStatus.update();
	}

	/**
	 * Der Teilnehmer wird vom Rennen gelöscht
	 */
	private void rennabmeldung() {
		String ret;
		String aktRennen = rennennamen[cmbOnlineRennen.getSelectionIndex()];
		if (isOnlRace()) {
			MTBSRaceServiceStub stub;
			try {
				stub = new MTBSRaceServiceStub();
				ConnectDB connect = new ConnectDB();
				connect.setDb(db);
				stub.connectDB(connect);

				DeleteTeilnahme delteil = new DeleteTeilnahme();
				delteil.setDb(db);
				delteil.setSNR(txtSNR.getText());
				//String aktRennen = rennennamen[cmbOnlineRennen.getSelectionIndex()];
				delteil.setRennen(aktRennen);
				DeleteTeilnahmeResponse delteilres = stub.deleteTeilnahme(delteil);
				Mlog.info("DeleteTeilnahme: "+delteilres.get_return()); 
				Statusmeldung(Messages.getString("OnlineRennen.abmeld2")); 
			} catch (Exception e1) {
				Mlog.ex(e1);
			}
		} else
			if (isLanRace()) {
				Vector<String> vret = clientSend("DeleteTeilnahme", Global.serverAdr, mACAdr, aktRennen);
				ret = (String) vret.firstElement();
				if (ret.startsWith("ERR")) { 	// Serverfehler 
					Mlog.error("Serverfehler bei Abmeldung: " + ret); 						
				} else {
					Mlog.info("DeleteTeilnahme: " + ret); 
					Statusmeldung(Messages.getString("OnlineRennen.abmeld2")); 		
				}
			}
	}

	/**
	 * Aufruf des RennAnmeldung Webservices
	 */
	public void rennanmeldung(String racename) {
		String aktRennen = racename;
		if (racename == null)
			aktRennen = rennennamen[cmbOnlineRennen.getSelectionIndex()];
		if (isOnlRace()) {
			MTBSRaceServiceStub stub;
			try {
				stub = new MTBSRaceServiceStub();
				ConnectDB connect = new ConnectDB();
				connect.setDb(db);
				stub.connectDB(connect);

				InsertTeilnahme insteil = new InsertTeilnahme();
				insteil.setDb(db);
				insteil.setSNR(txtSNR.getText());
				insteil.setRennen(aktRennen);
				insteil.setVnr(Global.getVersionCodeNr()+"");
				InsertTeilnahmeResponse insteilres = stub.insertTeilnahme(insteil);
				String ret = insteilres.get_return();
				if (ret.equalsIgnoreCase("WEBSV-Fehler! User ist nicht vorhanden!")) { 
					Statusmeldung(Messages.getString("OnlineRennen.nichtrgistriert")); 
					DoRegistrierung();
				} else {
					if (ret.startsWith("WEBSV-")) { // anderer Webservicefehler 
						Mlog.error(Messages.getString("OnlineRennen.webservicefehldp")+ret);  
						Statusmeldung(Messages.getString("OnlineRennen.webservicefehldp")+ret);  
					} else {
						Statusmeldung(Messages.getString("OnlineRennen.anmeld2"));  
						Global.rennenUserID = new Integer(ret);
					}
				}
			} catch (Exception e) {
				Mlog.ex(e);
			}
		} else {
			if (isLanRace()) {
				Vector<String> vret = clientSend("InsertTeilnahme", Global.serverAdr, mACAdr, aktRennen);
				String ret = (String) vret.firstElement();
				if (ret.equalsIgnoreCase("ERROR! User ist nicht vorhanden!")) { 
					Statusmeldung(Messages.getString("OnlineRennen.nichtrgistriert")); 
					DoRegistrierung();
				} else {
					if (ret.startsWith("ERR")) { // anderer Serverfehler 
						Mlog.error(Messages.getString("OnlineRennen.serverfehldp")+ret);  
						Statusmeldung(Messages.getString("OnlineRennen.serverfehldp")+ret);  
					} else {
						Statusmeldung(Messages.getString("OnlineRennen.anmeld2"));  
						Global.rennenUserID = new Integer(ret);
					}
				}				
			}
		}
		Rsmain.newkonfig.createXMLFileSettings(Global.standardsettingsdatei, Rsmain.biker);		// USER_ID merken in Settings
	}

	/**
	 * Öffnet die SQLite Datenbank und gibt Connection zurück.
	 * @param jdbc
	 * @param url zur DB
	 * @return Connection (klassenweit definiert)
	 */
	private Connection openDB(String jdbc, String url) {
        try {
			Class.forName(jdbc);
	        cn = DriverManager.getConnection( url, "", "" );	
		} catch (Exception e) {
			Mlog.ex(e);
		}
        return cn;
	}
	
	/**
	 * Einlesen der virtuellen Gegner für die eingestellte Tour.
	 */
	private void leseVirtGeg(String styp) {
		try
	    {
	        cn = openDB("org.sqlite.JDBC", "jdbc:sqlite:mtbs.sqlite");
	        Statement st = cn.createStatement();
	        String sql = "select name, ergebnis.platz, ergebnis.zeit, teil_id, ergebnis.leistung_schnitt, ergebnis.puls_schnitt, ergebnis.rpm_schnitt, trainer, ergebnis.zpstrecke " +
	        				"from rennen " +
	        				"join teilnahme on teil_renn_id = renn_id " +
	        				"join user on user_id = teil_user_id " +
	        				"join ergebnis on erge_teil_id = teil_id " +
	        				"where tour = '" + Global.strTourvideo+"' " +
	        				"and typ = '" + styp + "' " +
	        				"order by ergebnis.zpstrecke desc, ergebnis.zeit, name";
	        ResultSet  rs = st.executeQuery(sql);
	        tblVirtGeg.removeAll();

	        while (rs.next())
	        {
	        	TableItem item = new TableItem(tblVirtGeg, SWT.NONE);
	        	item.setChecked(true);
	        	item.setText(0, rs.getString("name"));
	        	item.setText(1, rs.getString("zeit"));
	        	item.setText(2, zfk0.format(new Double(rs.getString("leistung_schnitt"))));
	        	item.setText(3, zfk0.format(new Double(rs.getString("puls_schnitt"))));
	        	item.setText(4, rs.getString("teil_id"));
	        	item.setText(5, zfk0.format(new Double(rs.getString("rpm_schnitt"))));
	        	item.setText(7, rs.getString("trainer"));
	        	String zps = rs.getString("zpstrecke");

	        	item.setText(6, zfk0.format(new Double(zps)));
	        }
	        for (int i=0; i<header.length; i++) {
	            tblVirtGeg.getColumn(i).pack ();
	          }     
	        TableColumn tc = tblVirtGeg.getColumn(4);	// TEIL_ID unsichtbar machen
	        tc.setWidth(0);
	        tc.setResizable(false);	 
	        tc = tblVirtGeg.getColumn(6);	// und ZP unsichtbar machen
	        tc.setWidth(0);
	        tc.setResizable(false);	 
	        rs.close();
	    } catch (SQLException e) {
	    	setVirtGeg(false);
	    	chkVirtGeg.setSelection(false);
			Messages.errormessage(Messages.getString("OnlineRennen.fehlersqlite"));
	    } catch (Exception e) {
			Mlog.ex(e);
	    }
	}
	
	/**
	 * Einlesen der Koordinaten der ausgewählten virtuellen Gegner
	 */
	private  void loadVirtGeg(boolean zPRace) {
		SimpleDateFormat ztfmt = new SimpleDateFormat("H:mm:ss");
		OnlineGegner ogerg = null;
		int j = 0;
		if (zPRace)
			setVirtZP(false);
		else
			setVirtGeg(false);
		teilIds = new ArrayList<Integer>();
		posliste = new TreeMap<String, OnlineGegner>();
		ergliste = new ArrayList<OnlineGegner>();
		for (int i=0; i<tblVirtGeg.getItemCount(); i++) {
			if (tblVirtGeg.getItem(i).getChecked()) {
				teilIds.add(new Integer(tblVirtGeg.getItem(i).getText(4)));
				try {
					ogerg = new OnlineGegner("", tblVirtGeg.getItem(i).getText(0), ztfmt.parse(tblVirtGeg.getItem(i).getText(1)), 0L, "", 0, 0.0, new Double(tblVirtGeg.getItem(i).getText(3)).doubleValue(), new Double(tblVirtGeg.getItem(i).getText(5)).doubleValue(), new Double(tblVirtGeg.getItem(i).getText(2)).doubleValue(), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, new Double(tblVirtGeg.getItem(i).getText(6)).doubleValue(),0,0);
				} catch (Exception e) {
					Mlog.ex(e);
				}
				ergliste.add(ogerg);
//				Mlog.debug("table checked:"+tblVirtGeg.getItem(i).getText(0) + " - Teil-Id: " + teilIds.get(j));
				if (zPRace)
					setVirtZP(true);
				else
					setVirtGeg(true);
				
				// hier wird nun die Tabelle ACTPOS ausgelesen und in Liste geschrieben
				Statement st;
				try {
					st = cn.createStatement();
					String sql = "select name, zeitpunkt, gps_punkt, wind, gang, energie, puls, rpm, leistung, geschwindigkeit, strecke, steigung, sekunden, " +
					"hoehe, latitude, longitude, zpstrecke, rennen.minleistung, rennen.maxleistung, tfnormal, dynamik, leistungsfaktor, pulsmin, pulsmax, typ, schikane " +
					"from ACTPOS " +
					"join TEILNAHME ON teil_id = actp_teil_id " +
					"join USER ON user_id = teil_user_id " +
					"join RENNEN ON renn_id = teil_renn_id " +
					"where actp_teil_id = " + tblVirtGeg.getItem(i).getText(4) + " " +
					"order by actp_id";

					ResultSet  rs = st.executeQuery(sql);
					// Vorgaben für das virtuelle Training setzen (nur aus dem ersten ermittelten Rennen!):
					//--------------------------------------------
					if (j == 0) {
						double minleistung = new Double(rs.getString("minleistung")).doubleValue();
						Rsmain.biker.setMinleistung(minleistung);
						double maxleistung = new Double(rs.getString("maxleistung")).doubleValue();
						if (maxleistung > 0.0)
							Rsmain.biker.setMaxleistung(maxleistung);
						else
							Rsmain.biker.setMaxleistung(400);	// Defaultwert wenn nicht vorgegeben

						String tfn = rs.getString("tfnormal");
						double normTF = 0;
						if (!(tfn == null))
							normTF = new Double(tfn).doubleValue();
						Rsmain.biker.setDynRPMNormal(normTF);	// Normaltrittfrequenz 
						Rsmain.biker.setDynRPMWiege(0);
						Rsmain.newkonfig.setDynamik(rs.getString("dynamik").contentEquals("1")); 
						Rsmain.newkonfig.setaveraging(true);	// Höhendaten immer glätten
						Rsmain.biker.setCwa(0.35);
						Rsmain.biker.setK2(0.01);
						double lf = new Double(rs.getString("leistungsfaktor")).doubleValue();
						Rsmain.biker.setLfakt(lf);			// Leistungsfaktor
						//Rsmain.drpmmin = 30.0;				// neu: Mindestdrehzahl bei Rennen

						String pmin = rs.getString("pulsmin");
						double minpuls = 0;
						if (!(pmin == null))
							minpuls = new Double(pmin).doubleValue();
						
						String pmax = rs.getString("pulsmax");
						double maxpuls = 0;
						if (!(pmax == null))
							maxpuls = new Double(pmax).doubleValue();
						if (minpuls > 0.0) {
							Rsmain.biker.setPulsgruen(minpuls);
							Rsmain.biker.setPulsgelb(maxpuls+1);
						}
						if (rs.getString("typ").matches("(.*)puls(.*)"))
							setZprace(true);
						else
							setZprace(false);

						String sTyp = "\n" + Messages.getString("OnlineRennen.info1") + Rsmain.biker.getName() + Messages.getString("OnlineRennen.info2") + "\n" +
						Messages.getString("Auswertung.leistung") + ": " + zfk0.format(minleistung) + " - " + zfk0.format(maxleistung) + " W\n" +
						Messages.getString("Auswertung.puls") + ": " + zfk0.format(minpuls) + " - " + zfk0.format(maxpuls) + " bpm\n" +
						Messages.getString("OnlineRennen.dynamik") + ": " + Messages.einaus(Rsmain.newkonfig.isDynamik()) + "\n" +
						Messages.getString("Onlinerennen.LF") + ": " + zfk0.format(lf * 100) + "\n" + 
						Messages.getString("Onlinerennen.nTF") + ": " + zfk0.format(normTF) + "\n" +
						Messages.getString("OnlineRennen.info3");
						Mlog.info(sTyp);
						Messages.infomessage(shlRsWettkampf, sTyp);
					}


					while (rs.next())
					{
						gegner = new OnlineGegner("", rs.getString("name"), new Date(new Integer(rs.getString("sekunden"))*1000), new Long(rs.getString("gps_punkt")).longValue(), rs.getString("wind"), new Integer(rs.getString("gang")).intValue(), 
								new Double(rs.getString("energie")).doubleValue(), new Double(rs.getString("puls")).doubleValue(), new Double(rs.getString("rpm")).doubleValue(), 
								new Double(rs.getString("leistung")).doubleValue(), new Double(rs.getString("geschwindigkeit")).doubleValue(), new Double(rs.getString("strecke")).doubleValue(), 
								new Double(rs.getString("steigung")).doubleValue(), new Double(rs.getString("hoehe")).doubleValue(), new Double(rs.getString("latitude")).doubleValue(), 
								new Double(rs.getString("longitude")).doubleValue(), new Double(rs.getString("zpstrecke")).doubleValue(),0,0);
						posliste.put(tblVirtGeg.getItem(i).getText(4)+rs.getString("sekunden"), gegner);	// Key: TEIL_ID||Sekunden
					}
					rs.close();
				} catch (Exception e) {
					Mlog.ex(e);
				}
			j++;
			}
		}
		if (isVirtZP())
			setZprace(true);
	}
	
	/**
	 * Ermittelt aus der TreeMap posliste die Positionen der aktuellen Gegner und stellt die Rangfolge (gegliste) zusammen.
	 * @param sek			Anz. Sekunden seit Start
	 * @param name			Name
	 * @param puls			Puls
	 * @param rpm			Kurbelumdrehungen
	 * @param leistung		Leistung
	 * @param strecke		Strecke
	 * @param zpstrecke		Zielpulsstrecke
	 */
	public void calcVirtGegPos(long sek, String name, double puls, double rpm, double leistung, double strecke, double zpstrecke) {
		gegliste = new ArrayList<OnlineGegner>();
		OnlineGegner ich = new OnlineGegner("", name, null, 0, null, 0, 0.0, puls, rpm, leistung, 0.0, strecke, 0.0, 0.0, 0.0, 0.0, zpstrecke,0,0);
		if (!isZprace())
			ich.setZpstrecke(ich.getStrecke());

		gegliste.add(ich);
		
		for (int i=0; i<teilIds.size(); i++) {	// über alle TEIL_IDs...
			long j = sek;
			while (posliste.get(teilIds.get(i)+new Long(j).toString()) == null && j > 0) {  // rückwärts den letzten GPS-Punkt ermitteln
				j--;
			}
			OnlineGegner og = posliste.get(teilIds.get(i)+new Long(j).toString());
			if (og != null) {	// letzter GPS-Punkt des Gegners
				if (!isZprace())
					og.setZpstrecke(og.getStrecke());
				gegliste.add(og);
			}
		}
		try {
			Collections.sort(gegliste);
		} catch (NullPointerException e) {
			Mlog.debug("NPE: calcVirtGegPos - bei erstem Punkt: ok!");
		}
	}
}