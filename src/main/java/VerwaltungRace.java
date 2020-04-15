import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
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
 * VerwaltungRace.java: 
 * Verwaltung der LAN-Rennen. Es werden tabellarische Anzeigen für die
 * Rennen und User verwendet. Außerdem können Rennen und User angelegt und gelöscht
 * werden und die Ergebnisse zu den Rennen werden angezeigt. Die Speicherung
 * erfolgt in der SQLite-DB
 *****************************************************************************
 */

public class VerwaltungRace extends RsErwDialog {
	private Text txtBezeichnung = null;
	private Text txtStartzeit = null;
	private CCombo cmbTyp = null;
	private Text txtTour = null;
	private CCombo cmbDynamik = null;
	private Combo cmbStatus = null;
	private Text txtLeistungMin = null;
	private Text txtLeistungMax = null;
	private Text txtPulsMin = null;
	private Text txtPulsMax = null;
	private Text txtLeistungsfaktor = null;
	private Text txtErgebnis = null;
	private CCombo cmbSchikane = null;
	private Button butKopie = null;
	private Button butWieder = null;
	private Button butNeu = null;
	private Button butLoeschen = null;
	private Button butSpeichern = null;
	private Button butAbgelLoeschen = null;
	private Button butRennenAbschluss = null;
	
	private String[] rennenHeader = {"ID", "Bezeichnung", "Startzeit", "Typ", "Tour", "Dyn.", "P min", "P max", "Status", "Puls min", 
			"Puls max", "LF", "Schikane", "angemeldet", "TF norm." , "TF min.", "Cw", "K2", "Bikegewicht"};
	private String[] rennenTyp = {"Normal", "Zielpuls"};
	private String[] rennenEinAus = {Messages.getString("Onlinerennen.aus"), Messages.getString("Onlinerennen.ein") };
	private String[] rennenStatus = {Messages.getString("Onlinerennen.status0"), Messages.getString("Onlinerennen.status1"), Messages.getString("Onlinerennen.status2") };
	public  String rennenName = null;
	
	private DecimalFormat zfk0 = new DecimalFormat("#");
    private SimpleDateFormat tfmtDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat tfmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private TimeZone tz = TimeZone.getTimeZone("CET");
    private TimeZone tzDB = TimeZone.getTimeZone("UTC");  	// SQLite DB speichert UTC-Zeit  
    private boolean changed = false;						// Flag für Änderungen im Dialog
    private boolean userchg = true;							// User verursacht Änderungen (nicht Programm)
    private int cntdownSek = 30;							// Countdownzeit in Sekunden für Autostart
    
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
	 * @return the userchg
	 */
	public boolean isUserchg() {
		return userchg;
	}

	/**
	 * @param userchg the userchg to set
	 */
	public void setUserchg(boolean userchg) {
		this.userchg = userchg;
	}

	private class Rennen {
		int	renn_id;
		String bezeichnung;
		java.util.Date startzeit;
		int teilnehmer;
		String typ;
		String tour;
		int dynamik;
		int minleistung;
		int maxleistung;
		int status;
		int pulsmin;
		int pulsmax;
		int schikane;
		double lfProz;		// wird hier gleich in % gespeichert (in der DB als Faktor!)
		int tfnormal;
		int tfmin;
		double konstant_cw;
		double konstant_k2;
		double gewicht_bike;
		
		/**
		 * @return the renn_id
		 */
		public int getRenn_id() {
			return renn_id;
		}
		/**
		 * @param renn_id the renn_id to set
		 */
		public void setRenn_id(int renn_id) {
			this.renn_id = renn_id;
		}
		/**
		 * @return the bezeichnung
		 */
		public String getBezeichnung() {
			return bezeichnung;
		}
		/**
		 * @param bezeichnung the bezeichnung to set
		 */
		public void setBezeichnung(String bezeichnung) {
			this.bezeichnung = bezeichnung;
		}
		/**
		 * @return the startzeit
		 */
		public Date getStartzeit() {
			return startzeit;
		}
		/**
		 * @param startzeit the startzeit to set
		 */
		public void setStartzeit(Date startzeit) {
			this.startzeit = startzeit;
		}
		/**
		 * @return the teilnehmer
		 */
		public int getTeilnehmer() {
			return teilnehmer;
		}
		/**
		 * @param teilnehmer the teilnehmer to set
		 */
		public void setTeilnehmer(int teilnehmer) {
			this.teilnehmer = teilnehmer;
		}
		/**
		 * @return the typ
		 */
		public String getTyp() {
			return typ;
		}
		/**
		 * @param typ the typ to set
		 */
		public void setTyp(String typ) {
			this.typ = typ;
		}
		
		/**
		 * @return the tour
		 */
		public String getTour() {
			return tour;
		}
		/**
		 * @param tour the tour to set
		 */
		public void setTour(String tour) {
			this.tour = tour;
		}
		/**
		 * @return the dynamik
		 */
		public int getDynamik() {
			return dynamik;
		}
		/**
		 * @param dynamik the dynamik to set
		 */
		public void setDynamik(int dynamik) {
			this.dynamik = dynamik;
		}
		/**
		 * @return the minleistung
		 */
		public int getMinleistung() {
			return minleistung;
		}
		/**
		 * @param minleistung the minleistung to set
		 */
		public void setMinleistung(int minleistung) {
			this.minleistung = minleistung;
		}
		/**
		 * @return the maxleistung
		 */
		public int getMaxleistung() {
			return maxleistung;
		}
		/**
		 * @param maxleistung the maxleistung to set
		 */
		public void setMaxleistung(int maxleistung) {
			this.maxleistung = maxleistung;
		}
		/**
		 * @return the status
		 */
		public int getStatus() {
			return status;
		}
		/**
		 * @param status the status to set
		 */
		public void setStatus(int status) {
			this.status = status;
		}
		/**
		 * @return the pulsmin
		 */
		public int getPulsmin() {
			return pulsmin;
		}
		/**
		 * @param pulsmin the pulsmin to set
		 */
		public void setPulsmin(int pulsmin) {
			this.pulsmin = pulsmin;
		}
		/**
		 * @return the pulsmax
		 */
		public int getPulsmax() {
			return pulsmax;
		}
		/**
		 * @param pulsmax the pulsmax to set
		 */
		public void setPulsmax(int pulsmax) {
			this.pulsmax = pulsmax;
		}
		/**
		 * @return the schikane
		 */
		public int getSchikane() {
			return schikane;
		}
		/**
		 * @param schikane the schikane to set
		 */
		public void setSchikane(int schikane) {
			this.schikane = schikane;
		}
		/**
		 * @return the leistungsfaktor
		 */
		public double getLfProz() {
			return lfProz;
		}
		/**
		 * @param leistungsfaktor the leistungsfaktor to set
		 */
		public void setlfProz(double leistungsfaktor) {
			this.lfProz = leistungsfaktor;
		}
		/**
		 * @return the tfnormal
		 */
		public int getTfnormal() {
			return tfnormal;
		}
		/**
		 * @param tfnormal the tfnormal to set
		 */
		public void setTfnormal(int tfnormal) {
			this.tfnormal = tfnormal;
		}
		/**
		 * @return the tfmin
		 */
		public int getTfmin() {
			return tfmin;
		}
		/**
		 * @param tfmin the tfmin to set
		 */
		public void setTfmin(int tfmin) {
			this.tfmin = tfmin;
		}
		/**
		 * @return the konstant_cw
		 */
		public double getKonstant_cw() {
			return konstant_cw;
		}
		/**
		 * @param konstant_cw the konstant_cw to set
		 */
		public void setKonstant_cw(double konstant_cw) {
			this.konstant_cw = konstant_cw;
		}
		/**
		 * @return the konstant_k2
		 */
		public double getKonstant_k2() {
			return konstant_k2;
		}
		/**
		 * @param konstant_k2 the konstant_k2 to set
		 */
		public void setKonstant_k2(double konstant_k2) {
			this.konstant_k2 = konstant_k2;
		}
		/**
		 * @return the gewicht_bike
		 */
		public double getGewicht_bike() {
			return gewicht_bike;
		}
		/**
		 * @param gewicht_bike the gewicht_bike to set
		 */
		public void setGewicht_bike(double gewicht_bike) {
			this.gewicht_bike = gewicht_bike;
		}

		/**
		 * Setzen der Variablen der Klasse Rennen
		 * @param renn_id      Id des Rennens
		 * @param bezeichnung  Bezeichnung
		 * @param startzeit    Startzeit
		 * @param teilnehmer   Teilnehmer
		 * @param typ          Typ
		 * @param tour         Tour
		 * @param dynamik      Dynamik
		 * @param minleistung  MinLeistung
		 * @param maxleistung  Maxleistung
		 * @param status       Status
		 * @param pulsmin      min. Puls
		 * @param pulsmax      max. Puls
		 * @param schikane     Schikane
		 * @param leistungsfaktor in Prozent
		 * @param tfnormal     Trittfrequenznormalwert (entspr. nominaler videogeschwindigkeit)
		 * @param tfmin        Trittfrequenzminimun
		 * @param konstant_cw  Konstante CW
		 * @param konstant_k2  Konstante K2
		 * @param gewicht_bike Gewicht des Bikes
		 */
		private void setRennenWerte(int renn_id, String bezeichnung, Date startzeit, int teilnehmer, String typ, String tour, int dynamik, int minleistung, int maxleistung,
				int status, int pulsmin, int pulsmax, int schikane, double leistungsfaktor, int tfnormal, int tfmin, double konstant_cw,
				double konstant_k2, double gewicht_bike) {
			setRenn_id(renn_id); setBezeichnung(bezeichnung); setStartzeit(startzeit); setTeilnehmer(teilnehmer);
			setTyp(typ); setTour(tour); setDynamik(dynamik); setMinleistung(minleistung); setMaxleistung(maxleistung);
			setStatus(status); setPulsmin(pulsmin); setPulsmax(pulsmax); setSchikane(schikane); setlfProz(leistungsfaktor);
			setTfnormal(tfnormal); setTfmin(tfmin); setKonstant_cw(konstant_cw); setKonstant_k2(konstant_k2); setGewicht_bike(gewicht_bike);
		}			
	}
	
	private Rennen mRennen = new Rennen();		// Rennenobjekt
	private ArrayList<Rennen> rennenliste = new ArrayList<Rennen>();

	private Table tblRennen = null;
	
	public VerwaltungRace() {
		super();					// Aufruf des RsErwDialog-Konstruktors

		tfmtDB.setTimeZone(tzDB);
		tfmt.setTimeZone(tz);
		shl.setText(Messages.getString("OnlineRennen.verwaltung"));
		butAbbruch.setEnabled(false);
		
		Label lblBezeichnung = new Label(cmp, SWT.NONE);
		lblBezeichnung.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblBezeichnung.setBounds(10, 10, 100, 15);
		Global.setFontSizeLabel(lblBezeichnung);
		lblBezeichnung.setText(Messages.getString("OnlineRennen.lblBezeichnung.text")); 

		Label lblStartzeit = new Label(cmp, SWT.NONE);
		lblStartzeit.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblStartzeit.setBounds(10, 32, 100, 15);
		Global.setFontSizeLabel(lblStartzeit);
		lblStartzeit.setText(Messages.getString("OnlineRennen.lblStartzeit.text")); 

		Label lblTyp = new Label(cmp, SWT.NONE);
		lblTyp.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblTyp.setBounds(10, 54, 100, 15);
		Global.setFontSizeLabel(lblTyp);
		lblTyp.setText(Messages.getString("OnlineRennen.lblTyp.text")); 

		Label lblTour = new Label(cmp, SWT.NONE);
		lblTour.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblTour.setBounds(10, 76, 100, 15);
		Global.setFontSizeLabel(lblTour);
		lblTour.setText(Messages.getString("Auswertung.tour")); 

		Label lblDynamik = new Label(cmp, SWT.NONE);
		lblDynamik.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblDynamik.setBounds(10, 98, 100, 15);
		Global.setFontSizeLabel(lblDynamik);
		lblDynamik.setText(Messages.getString("Onlinerennen.lblDynamik")); 

		Label lblLeistungMin = new Label(cmp, SWT.NONE);
		lblLeistungMin.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblLeistungMin.setBounds(270, 10, 100, 15);
		Global.setFontSizeLabel(lblLeistungMin);
		lblLeistungMin.setText(Messages.getString("Onlinerennen.lblleistungmin")); 

		Label lblLeistungMax = new Label(cmp, SWT.NONE);
		lblLeistungMax.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblLeistungMax.setBounds(270, 32, 100, 15);
		Global.setFontSizeLabel(lblLeistungMax);
		lblLeistungMax.setText(Messages.getString("Onlinerennen.lblleistungmax")); 

		Label lblPulsMin = new Label(cmp, SWT.NONE);
		lblPulsMin.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblPulsMin.setBounds(270, 54, 100, 15);
		Global.setFontSizeLabel(lblPulsMin);
		lblPulsMin.setText(Messages.getString("Onlinerennen.lblpulsmin")); 

		Label lblPulsMax = new Label(cmp, SWT.NONE);
		lblPulsMax.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblPulsMax.setBounds(270, 76, 100, 15);
		Global.setFontSizeLabel(lblPulsMax);
		lblPulsMax.setText(Messages.getString("Onlinerennen.lblpulsmax")); 

		Label lblLeistungsfaktor = new Label(cmp, SWT.NONE);
		lblLeistungsfaktor.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblLeistungsfaktor.setBounds(270, 98, 100, 15);
		Global.setFontSizeLabel(lblLeistungsfaktor);
		lblLeistungsfaktor.setText(Messages.getString("Onlinerennen.lblleistungsfaktor")); 

		Label lblSchikane = new Label(cmp, SWT.NONE);
		lblSchikane.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblSchikane.setBounds(270, 120, 100, 15);
		Global.setFontSizeLabel(lblSchikane);
		lblSchikane.setText(Messages.getString("OnlineRennen.lblSchikane")); 

		txtBezeichnung = new Text(cmp, SWT.BORDER);
		txtBezeichnung.setToolTipText(Messages.getString("OnlineRennen.txtBezeichnung.text"));
		txtBezeichnung.setBounds(110, 10, 150, 20);
		Global.setFontSizeText(txtBezeichnung);
		txtBezeichnung.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (isUserchg())
					setChanged(true);
	        }
	    });

		txtStartzeit = new Text(cmp, SWT.BORDER);
		txtStartzeit.setToolTipText(Messages.getString("OnlineRennen.txtStartzeit.text"));
		txtStartzeit.setBounds(110, 32, 150, 20);
		Global.setFontSizeText(txtStartzeit);
		txtStartzeit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (isUserchg())
					setChanged(true);
	        }
	    });

		cmbTyp = new CCombo(cmp, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		cmbTyp.setItems(rennenTyp);
		cmbTyp.setToolTipText(Messages.getString("Onlinerennen.cmbtyptp")); 
		cmbTyp.setBounds(110, 54, 150, 20);
		Global.setFontSizeCCombo(cmbTyp);
		cmbTyp.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	        	//int ind = cmbTyp.getSelectionIndex();
				if (isUserchg())
					setChanged(true);
	        }
	    });

		// TODO Tourauswahl zus. auch über Filesuche-Button!
		txtTour = new Text(cmp, SWT.BORDER);
		txtTour.setToolTipText(Messages.getString("Onlinerennen.txttourtp"));
		txtTour.setBounds(110, 76, 150, 20);
		Global.setFontSizeText(txtTour);
		txtTour.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (isUserchg())
					setChanged(true);
	        }
	    });

		cmbDynamik = new CCombo(cmp, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		cmbDynamik.setItems(rennenEinAus);
		cmbDynamik.setToolTipText(Messages.getString("konfig.dynamik_modus")); 
		cmbDynamik.setBounds(110, 98, 150, 20);
		Global.setFontSizeCCombo(cmbDynamik);
		cmbDynamik.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	        	//int ind = cmbDynamik.getSelectionIndex();
				if (isUserchg())
					setChanged(true);
	        }
	    });
		
		cmbStatus = new Combo(cmp, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		cmbStatus.setItems(rennenStatus);
		cmbStatus.setToolTipText(Messages.getString("Onlinerennen.cmbstatustp")); 
		cmbStatus.setBounds(110, 120, 150, 20);
		cmbStatus.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	        	//int ind = cmbStatus.getSelectionIndex();
				if (isUserchg())
					setChanged(true);
	        }
	    });
		cmbStatus.setVisible(false);	// das trägt sonst nur zur Verwirrung bei!

		txtLeistungMin = new Text(cmp, SWT.BORDER);
		txtLeistungMin.setToolTipText(Messages.getString("Onlinerennen.txtLeistungMinTp"));
		txtLeistungMin.setBounds(370, 10, 60, 20);
		txtLeistungMin.addVerifyListener(Global.VLZahlen);
		Global.setFontSizeText(txtLeistungMin);
		txtLeistungMin.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (isUserchg())
					setChanged(true);
	        }
	    });

		txtLeistungMax = new Text(cmp, SWT.BORDER);
		txtLeistungMax.setToolTipText(Messages.getString("Onlinerennen.txtLeistungMaxTp"));
		txtLeistungMax.setBounds(370, 32, 60, 20);
		txtLeistungMax.addVerifyListener(Global.VLZahlen);
		Global.setFontSizeText(txtLeistungMax);
		txtLeistungMax.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (isUserchg())
					setChanged(true);
	        }
	    });
		
		txtPulsMin = new Text(cmp, SWT.BORDER);
		txtPulsMin.setToolTipText(Messages.getString("Onlinerennen.txtPulsMinTp"));
		txtPulsMin.setBounds(370, 54, 60, 20);
		txtPulsMin.addVerifyListener(Global.VLZahlen);
		Global.setFontSizeText(txtPulsMin);
		txtPulsMin.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (isUserchg())
					setChanged(true);
	        }
	    });

		txtPulsMax = new Text(cmp, SWT.BORDER);
		txtPulsMax.setToolTipText(Messages.getString("Onlinerennen.txtPulsMaxTp"));
		txtPulsMax.setBounds(370, 76, 60, 20);
		txtPulsMax.addVerifyListener(Global.VLZahlen);
		Global.setFontSizeText(txtPulsMax);
		txtPulsMax.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (isUserchg())
					setChanged(true);
	        }
	    });

		txtLeistungsfaktor = new Text(cmp, SWT.BORDER);
		txtLeistungsfaktor.setToolTipText(Messages.getString("Onlinerennen.txtleistungsfaktortp"));
		txtLeistungsfaktor.setBounds(370, 98, 60, 20);
		txtLeistungsfaktor.addVerifyListener(Global.VLZahlen);
		Global.setFontSizeText(txtLeistungsfaktor);
		txtLeistungsfaktor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (isUserchg())
					setChanged(true);
	        }
	    });

		cmbSchikane = new CCombo(cmp, SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbSchikane.setItems(rennenEinAus);
		cmbSchikane.setToolTipText(Messages.getString("Onlinerennen.cmbschikanetp")); 
		cmbSchikane.setBounds(370, 120, 60, 20);
		Global.setFontSizeCCombo(cmbSchikane);
		cmbSchikane.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	        	//int ind = cmbSchikane.getSelectionIndex();
				if (isUserchg())
					setChanged(true);
	        }
	    });

		txtErgebnis = new Text(cmp, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		txtErgebnis.setBounds(440, 10, 350, 133);
		txtErgebnis.setEditable(false);
        FontData fd = new FontData("Courier",8,SWT.NORMAL); 
        txtErgebnis.setFont(new Font(display, fd));

		butKopie = new Button(cmp, SWT.NONE);
		butKopie.setToolTipText(Messages.getString("OnlineRennen.butKopieTP"));
		butKopie.setText(Messages.getString("OnlineRennen.butKopie")); 
		butKopie.setBounds(10, 155, 60, 25);
		Global.setFontSizeButton(butKopie);
		butKopie.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				kopiereRennen();
				leseRennen();
			}
		});

		butNeu = new Button(cmp, SWT.NONE);
		butNeu.setToolTipText(Messages.getString("OnlineRennen.butNeuTP"));
		butNeu.setText(Messages.getString("OnlineRennen.butNeu")); 
		butNeu.setBounds(70, 155, 60, 25);
		Global.setFontSizeButton(butNeu);
		butNeu.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				neuRennen();
				leseRennen();
			}
		});
		
		butWieder = new Button(cmp, SWT.NONE);
		butWieder.setToolTipText(Messages.getString("OnlineRennen.butWiederTP"));
		butWieder.setText(Messages.getString("OnlineRennen.butWieder")); 
		butWieder.setBounds(130, 155, 120, 25);
		Global.setFontSizeButton(butWieder);
		butWieder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				wiederholeRennen();
				leseRennen();
			}
		});
		
		butSpeichern = new Button(cmp, SWT.NONE);
		butSpeichern.setToolTipText(Messages.getString("OnlineRennen.butAendernTP"));
		butSpeichern.setText(Messages.getString("OnlineRennen.butAendern")); 
		butSpeichern.setBounds(250, 155, 60, 25);
		Global.setFontSizeButton(butSpeichern);
		butSpeichern.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				aktualisiereRennenobjekt();
				saveRennen();
				leseRennen();
			}
		});
		butSpeichern.setFocus();
		
		butLoeschen = new Button(cmp, SWT.NONE);
		butLoeschen.setToolTipText(Messages.getString("OnlineRennen.butLoeschenTP"));
		butLoeschen.setText(Messages.getString("OnlineRennen.butLoeschen")); 
		butLoeschen.setBounds(310, 155, 60, 25);
		Global.setFontSizeButton(butLoeschen);
		butLoeschen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = tblRennen.getSelection();
				//Mlog.debug("Anzahl selektiert für löschen: "+items.length);
				if (items.length > 0) {
					for (int i=0; i<items.length; i++) {
						TableItem item = items[i];
						int rennId = new Integer(item.getText(0));
						loescheRennen(rennId);
					}
					leseRennen();
				}
			}
		});
		
		butAbgelLoeschen = new Button(cmp, SWT.NONE);
		butAbgelLoeschen.setToolTipText(Messages.getString("OnlineRennen.butAbgelLoeschenTP")); 
		butAbgelLoeschen.setText(Messages.getString("OnlineRennen.butAbgelLoeschen"));
		butAbgelLoeschen.setBounds(370, 155, 120, 25);
		Global.setFontSizeButton(butAbgelLoeschen);
		butAbgelLoeschen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				loescheAbgelRennen();
				leseRennen();
			}
		});

		butRennenAbschluss = new Button(cmp, SWT.NONE);
		butRennenAbschluss.setToolTipText(Messages.getString("OnlineRennen.butRennenAbschlussTP"));
		butRennenAbschluss.setText(Messages.getString("OnlineRennen.butRennenAbschluss"));
		butRennenAbschluss.setBounds(490, 155, 120, 25);
		Global.setFontSizeButton(butRennenAbschluss);
		butRennenAbschluss.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = tblRennen.getSelection();
				//Mlog.debug("Anzahl selektiert für abschliessen: "+items.length);
				if (items.length > 0) {
					for (int i=0; i<items.length; i++) {
						TableItem item = items[i];
						int rennId = new Integer(item.getText(0));
						String typ = item.getText(3);
						Rsmain.server.raengeErmitteln(0, rennId, typ.equals("Zielpuls"), true);
					}
					leseRennen();
				}
			}
		});

		tblRennen = new Table(cmp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI );
		tblRennen.setBounds(10, 180, 780, 252);
		tblRennen.setHeaderVisible(true);
		tblRennen.setLinesVisible(true);
		Global.setFontSizeTable(tblRennen);
		tblRennen.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	        	if (isChanged()) {
	        		setChanged(false);
	        		if (Messages.entscheidungmessage(shl, Messages.getString("OnlineRennen.savechg"))) {
	    				aktualisiereRennenobjekt();
	    				saveRennen();
	    				leseRennen();
	        		}
	        	}
	        	TableItem[] items = tblRennen.getSelection();
	        	//Mlog.debug("Anzahl selektiert: "+items.length);
	        	if (items.length > 0) {
					try {
	        		TableItem item = items[0];
					String date = item.getText(2);
					java.util.Date startzeit;
					startzeit = tfmt.parse(date);
					int rennId = new Integer(item.getText(0));
		        	mRennen.setRennenWerte(rennId,item.getText(1), startzeit, 
		        			new Integer(item.getText(13)), item.getText(3), item.getText(4), new Integer(item.getText(5)), 
		        			new Integer(item.getText(6)), new Integer(item.getText(7)), new Integer(item.getText(8)), 
		        			new Integer(item.getText(9)), new Integer(item.getText(10)), new Integer(item.getText(12)), 
		        			new Double(item.getText(11)), new Integer(item.getText(14)), new Integer(item.getText(15)), 
		        			new Double(item.getText(16)), new Double(item.getText(17)), new Double(item.getText(18)));	        		
		    	    aktualisiereEingabefelder();
					showErgebnis(rennId);
					} catch (ParseException e1) {
						Mlog.ex(e1);						
					}
	        	}
	        }
	    });

		for (int i = 0; i < rennenHeader.length; i++) {
			TableColumn column = new TableColumn(tblRennen, SWT.NONE);
			column.setText(rennenHeader[i]);
		}
		leseRennen();
	}

	/**
	 * für den Autostart werden alle abgelaufenen Rennen gelöscht und ein neues Rennen mit Start in 30 Sek. (cntdownSek) eingetragen.
	 * Das macht aber nur der Server, im anderen Fall wird als Name des Rennens null zurückgegeben!
	 * @param isServer		bin ich der Server?
	 * @return Name des aktuellen Rennens
	 */
	public String doAutostart(boolean isServer) {
		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator('.');
		DecimalFormat zfk1 = new DecimalFormat("0.0", dfs); 
		DecimalFormat zfk2 = new DecimalFormat("0.00", dfs); 

		if (isServer) {
			java.util.Date startzeit = addSeconds(new Date(), cntdownSek);
			rennenName = "Race" + startzeit.getTime();
			loescheAlleRennen();

			try
			{
				Rsmain.server.reConnectDB();
				Statement st = Server.con.createStatement();
				
				int dynamik = (Rsmain.newkonfig.isDynamik()) ? 1 : 0;
				double minLeistung = Rsmain.biker.getMinleistung();
				double maxLeistung = Rsmain.biker.getMaxleistung();
				double lf = Rsmain.biker.getLfakt();
				double dynRPMNormal = Rsmain.biker.getDynRPMNormal();
				double cwa = Rsmain.biker.getCwa();
				double k2 = Rsmain.biker.getK2();
				double gewichtBike = Rsmain.biker.getBikegewicht();
				
				String sql = "INSERT INTO RENNEN (BEZEICHNUNG,TOUR,TEILNEHMER,STARTZEIT,TYP,DYNAMIK,STATUS,MINLEISTUNG,MAXLEISTUNG," +
						"PULSMIN,PULSMAX,LEISTUNGSFAKTOR,SCHIKANE,TEILNEHMERLIMIT,TFNORMAL,TFMIN,"+
						"KONSTANT_CW,KONSTANT_K2,GEWICHT_BIKE) VALUES ('" + rennenName + "', " +
						"'" + Global.strTourvideo + "',0," +
						"'" + tfmtDB.format(startzeit) + "', " +
						"'Normal', " +		// akt. kein Zielpulsrennen!
						dynamik + ", 0, " +
						zfk0.format(minLeistung) + ", " +
						zfk0.format(maxLeistung) + ", " +
						0 + ", " +
						0 + ", " +
						zfk1.format(lf) + ", " +
						0 + ", 10, " + 
						zfk0.format(dynRPMNormal) + ", " +
						35 + ", " +
						zfk2.format(cwa) + ", " +
						zfk2.format(k2) + ", " +
						zfk1.format(gewichtBike) + ")";
				//Mlog.debug("sql = " + sql);
				st.executeUpdate(sql);
				st.close();			
			} catch (SQLException e) {
				Messages.errormessage(Messages.getString("OnlineRennen.fehlersqlite")+": "+e.getMessage());
				return null;
			} catch (Exception e) {
				Mlog.ex(e);
				return null;
			}
		} 
		
		return rennenName;
	}
	
	/**
	 * Die Eingabefelder werden aus dem Rennen-Objekt gefüllt
	 */
	private void aktualisiereEingabefelder() {
		setUserchg(false);
		txtBezeichnung.setText(mRennen.getBezeichnung());
		txtStartzeit.setText(tfmt.format(mRennen.getStartzeit()));
		cmbTyp.setText(mRennen.getTyp());
		txtTour.setText(mRennen.getTour());
		cmbDynamik.setText(rennenEinAus[mRennen.getDynamik()]);
		cmbStatus.setText(rennenStatus[mRennen.getStatus()]);
		txtLeistungMin.setText(mRennen.getMinleistung()+"");
		txtLeistungMax.setText(mRennen.getMaxleistung()+"");
		txtPulsMin.setText(mRennen.getPulsmin()+"");
		txtPulsMax.setText(mRennen.getPulsmax()+"");
		txtLeistungsfaktor.setText(zfk0.format(mRennen.getLfProz())+"");
		cmbSchikane.setText(rennenEinAus[mRennen.getSchikane()]);
		setUserchg(true);
	}

	/**
	 * Das Rennen-Objekt wird aus den Eingabefeldern aktualisiert
	 */
	private void aktualisiereRennenobjekt() {
		mRennen.setBezeichnung(txtBezeichnung.getText());
		try {
			mRennen.setStartzeit(tfmt.parse(txtStartzeit.getText()));
		} catch (ParseException e) {
			Mlog.error(Messages.getString("OnlineRennen.fehlerdatum")+": "+e.getMessage());
			Messages.errormessage(Messages.getString("OnlineRennen.fehlerdatum")+": "+e.getMessage());
		}
		mRennen.setTyp(cmbTyp.getText());
		mRennen.setTour(txtTour.getText());
		mRennen.setDynamik(cmbDynamik.getSelectionIndex());
		mRennen.setStatus(cmbStatus.getSelectionIndex());
		mRennen.setMinleistung(new Integer(txtLeistungMin.getText()));
		mRennen.setMaxleistung(new Integer(txtLeistungMax.getText()));
		mRennen.setPulsmin(new Integer(txtPulsMin.getText()));
		mRennen.setPulsmax(new Integer(txtPulsMax.getText()));
		mRennen.setlfProz(new Double(txtLeistungsfaktor.getText()));
		mRennen.setSchikane(cmbSchikane.getSelectionIndex());
	}

	/**
	 * Einlesen aller Rennen aus Datenbank. Dabei wird die Tabelle mit den Renninfos gefüllt.
	 */
	private void leseRennen() {
		Mlog.debug("leseRennen...");
		try
	    {
			if (Rsmain.server == null)
				Rsmain.server = new Server();
			Rsmain.server.reConnectDB();
	        Statement st = Server.con.createStatement();
	        String sql = "select renn_id, bezeichnung, startzeit, teilnehmer, typ, tour, dynamik, minleistung, maxleistung, status, pulsmin, pulsmax, " +
	        				"schikane, leistungsfaktor, tfnormal, tfmin, konstant_cw, konstant_k2, gewicht_bike " +
	        				"from rennen " +
	        				"order by startzeit desc";
	        ResultSet  rs = st.executeQuery(sql);
	        tblRennen.removeAll();

	        while (rs.next())
	        {
				String date = rs.getString("STARTZEIT");
				java.util.Date startzeit = tfmtDB.parse(date);
	        	mRennen.setRennenWerte(new Integer(rs.getString("renn_id")), rs.getString("bezeichnung"), startzeit, 
	        			new Integer(rs.getString("teilnehmer")), rs.getString("TYP"), rs.getString("TOUR"), new Integer(rs.getString("DYNAMIK")), 
	        			new Integer(rs.getString("MINLEISTUNG")), new Integer(rs.getString("MAXLEISTUNG")), new Integer(rs.getString("STATUS")), 
	        			new Integer(rs.getString("PULSMIN")), new Integer( rs.getString("PULSMAX")), new Integer(rs.getString("SCHIKANE")), 
	        			new Double(rs.getString("LEISTUNGSFAKTOR"))*100, new Integer(rs.getString("TFNORMAL")), new Integer(rs.getString("TFMIN")), 
	        			new Double(rs.getString("KONSTANT_CW")), new Double(rs.getString("KONSTANT_K2")), new Double(rs.getString("GEWICHT_BIKE")));

	        	rennenliste.add(mRennen);

	    		//"Bezeichnung", "Startzeit", "Typ", "Tour", "Dynamik", "Leistung min", "Leistung max", "Status", "Puls min", "Puls max", "Leistungsfaktor", "Schikane", "angemeldet" };
	        	TableItem item = new TableItem(tblRennen, SWT.NONE);
	        	item.setText(0, mRennen.getRenn_id()+"");
	        	item.setText(1, mRennen.getBezeichnung());
	        	item.setText(2, tfmt.format(mRennen.getStartzeit()));
	        	item.setText(3, mRennen.getTyp());
	        	item.setText(4, mRennen.getTour());
	        	item.setText(5, mRennen.getDynamik()+"");
	        	item.setText(6, mRennen.getMinleistung()+"");
	        	item.setText(7, mRennen.getMaxleistung()+"");
	        	item.setText(8, mRennen.getStatus()+"");
	        	item.setText(9, mRennen.getPulsmin()+"");
	        	item.setText(10, mRennen.getPulsmax()+"");
	        	item.setText(11, zfk0.format(mRennen.getLfProz())+"");
	        	item.setText(12, mRennen.getSchikane()+"");
	        	item.setText(13, mRennen.getTeilnehmer()+"");
	        	item.setText(14, mRennen.getTfnormal()+"");
	        	item.setText(15, mRennen.getTfmin()+"");
	        	item.setText(16, mRennen.getKonstant_cw()+"");
	        	item.setText(17, mRennen.getKonstant_k2()+"");
	        	item.setText(18, mRennen.getGewicht_bike()+"");
	        }
	        for (int i=0; i<rennenHeader.length; i++) {
	            tblRennen.getColumn(i).pack ();
	          }     
	        TableColumn tc = tblRennen.getColumn(0);	// ID unsichtbar machen
	        tc.setWidth(0);
	        tc.setResizable(false);	 
	        tc = tblRennen.getColumn(14);	// und TF norm unsichtbar machen
	        tc.setWidth(0);
	        tc.setResizable(false);	 
	        tc = tblRennen.getColumn(15);	// und TF min unsichtbar machen
	        tc.setWidth(0);
	        tc.setResizable(false);	 
	        tc = tblRennen.getColumn(16);	// und Cw unsichtbar machen
	        tc.setWidth(0);
	        tc.setResizable(false);	 
	        tc = tblRennen.getColumn(17);	// und K2 unsichtbar machen
	        tc.setWidth(0);
	        tc.setResizable(false);	 
	        tc = tblRennen.getColumn(18);	// und Gewicht Bike unsichtbar machen
	        tc.setWidth(0);
	        tc.setResizable(false);	 
	        rs.close();
			st.close();	
	    } catch (SQLException e) {
			Messages.errormessage(Messages.getString("OnlineRennen.fehlersqlite"));
	    } catch (Exception e) {
			Mlog.ex(e);
	    }

	    tblRennen.setSelection(0);
    	TableItem[] items = tblRennen.getSelection();
    	if (items.length == 0)
    		return;
		TableItem item = items[0];
		String date = item.getText(2);
		java.util.Date startzeit = null;
		try {
			startzeit = tfmt.parse(date);
		} catch (ParseException e) {
			Mlog.ex(e);
		}

    	mRennen.setRennenWerte(new Integer(item.getText(0)),item.getText(1), startzeit, 
    			new Integer(item.getText(13)), item.getText(3), item.getText(4), new Integer(item.getText(5)), 
    			new Integer(item.getText(6)), new Integer(item.getText(7)), new Integer(item.getText(8)), 
    			new Integer(item.getText(9)), new Integer(item.getText(10)), new Integer(item.getText(12)), 
    			new Double(item.getText(11)), new Integer(item.getText(14)), new Integer(item.getText(15)), 
    			new Double(item.getText(16)), new Double(item.getText(17)), new Double(item.getText(18)));	        		
	    aktualisiereEingabefelder();
		setChanged(false);
	}

	/**
	 * Anzeige der Ergebnisliste im Ergebnisfeld rechts oeben.
	 * @param rennId  ID des Rennens
	 */
	private void showErgebnis(int rennId) {
		//String tmp;
		try
	    {
	    	Rsmain.server.reConnectDB();
	        Statement st = Server.con.createStatement();
	        String sql = "select platz, zeit, energie, rpm_schnitt, puls_schnitt, leistung_schnitt, zpstrecke, name, ort, geburtsjahr " +
	        				"from ergebnis " +
	        				"join teilnahme on teil_id = erge_teil_id " +
	        				"join user on user_id = teil_user_id " +
	        				"where erge_renn_id = " + rennId + " " +
	        				"order by platz";
	        //Mlog.debug("sql = " + sql);
	        ResultSet  rs = st.executeQuery(sql);

	        txtErgebnis.setText("Pos     Zeit      Name Puls  RPM Watt\n");	// TODO bei ZP-Rennen statt Zeit die ZP-Strecke anzeigen
	        while (rs.next())
	        {
				String platz = rs.getString("PLATZ");
				String zeit = rs.getString("ZEIT");
				String name = rs.getString("NAME");
				double puls = rs.getDouble("PULS_SCHNITT");
				double rpm = rs.getDouble("RPM_SCHNITT");
				double leistung = rs.getDouble("LEISTUNG_SCHNITT");
				txtErgebnis.append(String.format("%2s.%9s%10s%5.0f%5.0f%5.0f\n",platz, zeit, name, puls, rpm, leistung));  
	        }
	        rs.close();
			st.close();				        
	    } catch (SQLException e) {
			Messages.errormessage(Messages.getString("OnlineRennen.fehlersqlite"));
	    } catch (Exception e) {
			Mlog.ex(e);
	    }
	}

	/**
	 * Hier wird das (geänderte) Rennen angespeichert (UPDATE).
	 */
	private void saveRennen() {
		aktualisiereRennenobjekt();
		try
		{
			Rsmain.server.reConnectDB();
			Statement st = Server.con.createStatement();
			String sql = "UPDATE RENNEN SET BEZEICHNUNG = '" + mRennen.getBezeichnung() + "', " +
						 "STARTZEIT = '" + tfmtDB.format(mRennen.getStartzeit()) + "', " +
						 "TYP = '" + mRennen.getTyp() + "', " +
						 "TOUR = '" + mRennen.getTour() + "', " +
						 "DYNAMIK = " + mRennen.getDynamik() + ", " +
						 "STATUS = " + mRennen.getStatus() + ", " +
						 "MINLEISTUNG = " + mRennen.getMinleistung() + ", " +
						 "MAXLEISTUNG = " + mRennen.getMaxleistung() + ", " +
						 "PULSMIN = " + mRennen.getPulsmin() + ", " +
						 "PULSMAX = " + mRennen.getPulsmax() + ", " +
						 "LEISTUNGSFAKTOR = " + mRennen.getLfProz()/100.0 + ", " +
						 "SCHIKANE = " + mRennen.getSchikane() + " " +
						 "WHERE RENN_ID = "+mRennen.getRenn_id();
			//Mlog.debug("sql = " + sql);
			st.executeUpdate(sql);
			st.close();			
		} catch (SQLException e) {
			Messages.errormessage(Messages.getString("OnlineRennen.fehlersqlite")+": "+e.getMessage());
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}
	
	/**
	 * Hier wird ein neues Rennen angelegt (INSERT).
	 */
	private void kopiereRennen() {
		//java.util.Date startzeit = new Date();
		java.util.Date startzeit = addMinutes(new Date(), 10);
		aktualisiereRennenobjekt();

		try
		{
			Rsmain.server.reConnectDB();
			Statement st = Server.con.createStatement();
			String sql = "INSERT INTO RENNEN (BEZEICHNUNG,TOUR,TEILNEHMER,STARTZEIT,TYP,DYNAMIK,STATUS,MINLEISTUNG,MAXLEISTUNG," +
						"PULSMIN,PULSMAX,LEISTUNGSFAKTOR,SCHIKANE,TEILNEHMERLIMIT,TFNORMAL,TFMIN,"+
						"KONSTANT_CW,KONSTANT_K2,GEWICHT_BIKE) VALUES ('" + concatNr(mRennen.getBezeichnung()) + "', " +
						 "'" + mRennen.getTour() + "',0," +
						 "'" + tfmtDB.format(startzeit) + "', " +
						 "'" + mRennen.getTyp() + "', " +
						 mRennen.getDynamik() + ", 0, " +
						 mRennen.getMinleistung() + ", " +
						 mRennen.getMaxleistung() + ", " +
						 mRennen.getPulsmin() + ", " +
						 mRennen.getPulsmax() + ", " +
						 mRennen.getLfProz()/100.0 + ", " +
						 mRennen.getSchikane() + ", 10, " + 
						 mRennen.getTfnormal() + ", " +
						 mRennen.getTfmin() + ", " +
						 mRennen.getKonstant_cw() + ", " +
						 mRennen.getKonstant_k2() + ", " +
						 mRennen.getGewicht_bike() + ")";
			//Mlog.debug("sql = " + sql);
			st.executeUpdate(sql);
			st.close();			
		} catch (SQLException e) {
			Messages.errormessage(Messages.getString("OnlineRennen.fehlersqlite")+": "+e.getMessage());
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * Hier wird ein neues Rennen angelegt (INSERT).
	 */
	private void neuRennen() {
		//java.util.Date startzeit = new Date();
		java.util.Date startzeit = addMinutes(new Date(), 10);

		//aktualisiereRennenobjekt();
		try
		{
			Rsmain.server.reConnectDB();
			Statement st = Server.con.createStatement();
			String sql = "INSERT INTO RENNEN (BEZEICHNUNG,TOUR,TEILNEHMER,STARTZEIT,TYP,DYNAMIK,STATUS,MINLEISTUNG,MAXLEISTUNG," +
						"PULSMIN,PULSMAX,LEISTUNGSFAKTOR,SCHIKANE,TEILNEHMERLIMIT,TFNORMAL,TFMIN,"+
						"KONSTANT_CW,KONSTANT_K2,GEWICHT_BIKE) VALUES ('<>','" + Global.strTourvideo + "',0," +
						 "'" + tfmtDB.format(startzeit) + "', " +
						 "'" + rennenTyp[0] + "', " +
						 "0, 0, 120, 400, 0, 0,1.0, 0, 10, 90, 35, " +
						 "0.35, 0.01, 13)";
			//Mlog.debug("sql = " + sql);
			st.executeUpdate(sql);
			st.close();			
		} catch (SQLException e) {
			Messages.errormessage(Messages.getString("OnlineRennen.fehlersqlite")+": "+e.getMessage());
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * Hier wird das Rennen wiederholt. Dabei werden vorher alle Positionen gelöscht
	 * und ebenso alle Ergebnisse.
	 */
	private void wiederholeRennen() {
		java.util.Date startzeit = addMinutes(new Date(), 10);

		aktualisiereRennenobjekt();
		try
		{
			Rsmain.server.reConnectDB();
			Statement st = Server.con.createStatement();
			int rennId = mRennen.getRenn_id();
			// zuerst Positionsdaten des Rennens löschen:
			String sql = "DELETE FROM ACTPOS WHERE ACTP_TEIL_ID IN (SELECT TEIL_ID FROM TEILNAHME WHERE TEIL_RENN_ID = " + rennId + ")";
			//Mlog.debug("sql = " + sql);
			st.executeUpdate(sql);
			// jetzt Ergebnisse löschen:
			sql = "DELETE FROM ERGEBNIS WHERE ERGE_RENN_ID = " + rennId;
			st.executeUpdate(sql);
			// jetzt Teilnahmen löschen:
			sql = "DELETE FROM TEILNAHME WHERE TEIL_RENN_ID = " + rennId;
			st.executeUpdate(sql);
			// jetzt das Rennen updaten
			sql = "UPDATE RENNEN set STARTZEIT = '" + tfmtDB.format(startzeit) + "', STATUS = 0 WHERE RENN_ID = " + rennId;
			//Mlog.debug("sql = " + sql);
			st.executeUpdate(sql);
			st.close();	
			showErgebnis(rennId);
			
		} catch (SQLException e) {
			Messages.errormessage(Messages.getString("OnlineRennen.fehlersqlite")+": "+e.getMessage());
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * Hier wird ein Rennen gelöscht (DELETE).
	 * Zuerst werden die Positionsdaten, dann die Ergebnisse, dann
	 * die Teilnahmen und zuletzt das Rennen gelöscht.
	 * @param rennId	RENN_ID des zu löschenden Rennens
	 */
	private void loescheRennen(int rennId) {
		try
		{
			Rsmain.server.reConnectDB();
			Statement st = Server.con.createStatement();
			// zuerst Positionsdaten des Rennens löschen:
			String sql = "DELETE FROM ACTPOS WHERE ACTP_TEIL_ID IN (SELECT TEIL_ID FROM TEILNAHME WHERE TEIL_RENN_ID = " + rennId + ")";
			//Mlog.debug("sql = " + sql);
			st.executeUpdate(sql);
			// nun Positionsdaten aller abgelaufenen Rennen löschen:
			sql = "DELETE FROM ACTPOS WHERE ACTP_TEIL_ID IN (SELECT TEIL_ID FROM TEILNAHME WHERE TEIL_RENN_ID IN (SELECT RENN_ID FROM RENNEN WHERE STATUS = 2))";
			st.executeUpdate(sql);
			// jetzt Ergebnisse löschen:
			sql = "DELETE FROM ERGEBNIS WHERE ERGE_RENN_ID = " + rennId;
			st.executeUpdate(sql);
			// jetzt Teilnahmen löschen:
			sql = "DELETE FROM TEILNAHME WHERE TEIL_RENN_ID = " + rennId;
			st.executeUpdate(sql);
			// jetzt Rennen löschen:
			sql = "DELETE FROM RENNEN WHERE RENN_ID = " + rennId;
			st.executeUpdate(sql);
			Mlog.info("LAN-Rennen (RENN_ID: " + rennId + " gelöscht.");
			st.close();			
		} catch (SQLException e) {
			Messages.errormessage(Messages.getString("OnlineRennen.fehlersqlite")+": "+e.getMessage());
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * Hier werden alle abgelaufene Rennen (einschl. Ergebnisse) gelöscht (DELETE).
	 * Zuerst werden die Positionsdaten, dann die Ergebnisse, dann
	 * die Teilnahmen und zuletzt die Rennen gelöscht.
	 */
	private void loescheAbgelRennen() {
		try
		{
			Rsmain.server.reConnectDB();
			Statement st = Server.con.createStatement();
			// Zuerst Positionsdaten aller abgelaufenen Rennen löschen:
			String sql = "DELETE FROM ACTPOS WHERE ACTP_TEIL_ID IN (SELECT TEIL_ID FROM TEILNAHME WHERE TEIL_RENN_ID IN (SELECT RENN_ID FROM RENNEN WHERE STATUS = 2))";
			st.executeUpdate(sql);
			// jetzt Ergebnisse löschen:
			sql = "DELETE FROM ERGEBNIS WHERE ERGE_RENN_ID IN (SELECT RENN_ID FROM RENNEN WHERE STATUS = 2)";
			st.executeUpdate(sql);
			// jetzt Teilnahmen löschen:
			sql = "DELETE FROM TEILNAHME WHERE TEIL_RENN_ID IN (SELECT RENN_ID FROM RENNEN WHERE STATUS = 2)";
			st.executeUpdate(sql);
			// jetzt Rennen löschen:
			sql = "DELETE FROM RENNEN WHERE STATUS = 2";
			st.executeUpdate(sql);
			Mlog.info("Alle abgelaufenen LAN-Rennen wurden inkl. Ergebnissen gelöscht.");
			st.close();			
		} catch (SQLException e) {
			Messages.errormessage(Messages.getString("OnlineRennen.fehlersqlite")+": "+e.getMessage());
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * Hier werden alle Rennen (einschl. Ergebnisse) gelöscht (DELETE).
	 * Zuerst werden die Positionsdaten, dann die Ergebnisse, dann
	 * die Teilnahmen und zuletzt die Rennen gelöscht.
	 */
	private void loescheAlleRennen() {
		try
		{
			Rsmain.server.reConnectDB();
			Statement st = Server.con.createStatement();
			// Zuerst Positionsdaten aller abgelaufenen Rennen löschen:
			String sql = "DELETE FROM ACTPOS WHERE ACTP_TEIL_ID IN (SELECT TEIL_ID FROM TEILNAHME WHERE TEIL_RENN_ID IN (SELECT RENN_ID FROM RENNEN))";
			st.executeUpdate(sql);
			// jetzt Ergebnisse löschen:
			sql = "DELETE FROM ERGEBNIS WHERE ERGE_RENN_ID IN (SELECT RENN_ID FROM RENNEN)";
			st.executeUpdate(sql);
			// jetzt Teilnahmen löschen:
			sql = "DELETE FROM TEILNAHME WHERE TEIL_RENN_ID IN (SELECT RENN_ID FROM RENNEN)";
			st.executeUpdate(sql);
			// jetzt Rennen löschen:
			sql = "DELETE FROM RENNEN";
			st.executeUpdate(sql);
			Mlog.info("Alle LAN-Rennen wurden inkl. Ergebnissen gelöscht.");
			st.close();			
		} catch (SQLException e) {
			Messages.errormessage(Messages.getString("OnlineRennen.fehlersqlite")+": "+e.getMessage());
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	/**
	 * addiert Minuten zum aktuellen Zeitpunkt.
	 * @param 	zeitpunkt   akt. Zeitpunkt
	 * @param 	minuten     Anzahl der Minuten, die addiert werden sollen
	 * @return	Zeit als Date
	 */
	private Date addMinutes(Date zeitpunkt, int minuten) {
		Calendar aktTime = Calendar.getInstance();
		
		aktTime.add(Calendar.MINUTE, minuten);
		return (aktTime.getTime());
	}

	/**
	 * addiert Sekunden zum aktuellen Zeitpunkt.
	 * @param 	zeitpunkt  akt. Zeitpunkt
	 * @param 	sekunden   Anzahl der Sekunden, die addiert werden sollen
	 * @return	Zeit als Date
	 */
	private Date addSeconds(Date zeitpunkt, int sekunden) {
		Calendar aktTime = Calendar.getInstance();
		
		aktTime.add(Calendar.SECOND, sekunden);
		return (aktTime.getTime());
	}
	
	/**
	 * An den übergebenen String wird hinten ein Index ergänzt.
	 * Verwendet bei der Bezeichnung des Rennens da dort ein eindeutigber Index auf der DB besteht.
	 * @param bez	Übergabestring
	 * @return		Übergabestring mit Index
	 */
	private String concatNr(String bez) {
		String ret;
		int len = bez.length();
		if (len < 1)
			return ("1");
		
		if (bez.substring(len-1, len).matches("\\d")) { // Zahl am Ende?
			ret = bez.substring(0, len - 1);
			int szahl = new Integer(bez.substring(len - 1, len));
			ret += ++szahl;
		} else {
			ret = bez;
			ret += "0";
		}
		return (ret);
	}
	
	@Override
	void doOk() {
    	if (isChanged()) {
    		if (Messages.entscheidungmessage(shl, Messages.getString("OnlineRennen.savechg"))) {
				aktualisiereRennenobjekt();
				saveRennen();
				leseRennen();
    		}
    	}
		setErgebnisOK(true);
		shl.setVisible(false);		
	}

	@Override
	void doAbbruch() {
		setErgebnisOK(false);
		shl.setVisible(false);
	}
}
