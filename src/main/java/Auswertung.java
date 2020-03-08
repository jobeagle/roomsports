import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jstrava.JStravaV3;
import jstrava.UploadStatus;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.wb.swt.SWTResourceManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
 * Auswertung.java: Auswertungsdialog (nach Trainingsende)
 *****************************************************************************
 *
 * Das Auswertewindow zeigt nach Trainingsende eine Auswertung anhand des CSV-Protokolls.
 * Es ersetzt die vorherige protokoll-Klasse, die anschliessend entfällt.
 * Diese Klasse sorgt ferner für die Datenprotokollierung einer Tour als CSV-Datei. 
 * Beim Start wird die Datei geöffnet und die Überschrift geschrieben.
 * Bei jedem neuen Zeitpunkt (aktuell eine Sekunde) wird eine neue Zeile geschrieben. 
 *
 */
public class Auswertung {
	private Shell shell = null;
	private Composite cmpAuswertung = null;
	private Composite cmpToolbar = null;
	private FileWriter fw;
	private PrintWriter pw;
	private Date datum; 		 
	private SimpleDateFormat datumformat = new SimpleDateFormat("E', den' dd.MM.yyyy", Locale.GERMANY); 
	private SimpleDateFormat datumformatkurz = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY); 
	private boolean initialisiert = false;
	public  boolean iblEnde = false;
	
	private String protDatei = null;
	private String TCXDatei = null;
	private String CSVDatei = null;
	private long index = 0;
	public  List<Psatz> pliste;
	private Psatz ps;
	private StyledText styledTextLabels;
	private StyledText styledTextWerte1;
	private StyledText styledTextWerte2;
	private StyledText styledTextWerte3;
	private Label lblTrainingsauswertung;
	private Label lblBelohnung;
	//private JFreeChart chartWerte;
    private XYPlot plot;
    private XYItemRenderer rendererLeistung = null;
    private XYItemRenderer rendererPuls = null;
    private XYItemRenderer rendererRPM = null;
    private XYItemRenderer rendererSteigung = null;
    private XYItemRenderer rendererHoehe = null;
    private NumberAxis axisLeistung = null;
    private NumberAxis axisPuls = null;
    private NumberAxis axisRPM = null;
    private NumberAxis axisSteigung = null;
    private NumberAxis axisHoehe = null;
    private ChartComposite frame;
	private XYSeriesCollection dsLeistung = new XYSeriesCollection();
	private XYSeriesCollection dsRPM = new XYSeriesCollection(); 
	private XYSeriesCollection dsPuls = new XYSeriesCollection(); 
	private XYSeriesCollection dsSteigung = new XYSeriesCollection(); 
	private XYSeriesCollection dsHoehe = new XYSeriesCollection(); 
	private XYSeries profilDataLeistung = new XYSeries(Messages.getString("Auswertung.leistung"));      
	private XYSeries profilDataPuls = new XYSeries(Messages.getString("Auswertung.puls"));   
	private XYSeries profilDataRPM = new XYSeries(Messages.getString("Auswertung.rpm"));     
	private XYSeries profilDataSteigung = new XYSeries(Messages.getString("Auswertung.steigung"));    
	private XYSeries profilDataHoehe = new XYSeries(Messages.getString("Auswertung.hoehe")); 

	private Button butok = null;
	private Button butSave = null;
	private Button butReset = null;
	private Button butRefresh = null;
	private Button butStrava = null;

	private Text txtErgebnis = null;
	
	// Zwischen-Ergebniswerte:
	private long   lanzahl = 0;
	private double dpulssum = 0;
	private double dmaxpuls = 0;
	private double dminpuls = 200;
	private double drpmsum = 0;
	private double dmaxrpm = 0;
	private double dminrpm = 200;
	private double dpsum = 0;
	private double dmaxp = 0;
	private double dminp = 500;
	private double dsteigungsum = 0;
	private double dmaxsteigung = -100;
	private double dminsteigung = 100;
	private double dvsum = 0;
	private double dmaxv = 0;
	private double dminv = 200;
	private long   lsek = 0;				// Tausendstel Sekunden!
	private long   lrtsek = 0;
	private long   lzdiff = 0;
	private double dm = 0;
	private double dhm = 0;
	private double dkcal = 0;
//	private double dfw = 0;
	private double dpulsav = 0;
	private double drpmav = 0;
	private double dpav = 0;
	private double dvav = 0;
	private double dsteigungav = 0;
	private Date   letztezeit = new Date();

	private String strCol1 = new String();
	private String strCol2 = new String();
	private String strCol3 = new String();
	private String strCol4 = new String();

	public  SimpleDateFormat tfmt;
	public  SimpleDateFormat tfmttcx;
//	private DecimalFormat zfk2 = new DecimalFormat("0.00"); 
	private DecimalFormat zfk1 = new DecimalFormat("###0.0"); 
	private DecimalFormat zfk0 = new DecimalFormat("######"); 

    private long   gesTrainingszeit;	// Gesamtwerte
    private double gesStrecke;
    private double gesHM;
    private double gesKCal;
//    private double gesFitnesswert;
    private double gesPuls;
    private double gesRPM;
    private double gesLeistung;
    private long   gesAnzahl;
    
    public  boolean bSave = false;		// wurde gespeichert?
    private long cZeitSchranke = 300000;	// nur wenn länger als 5 Min. trainiert wird, wird beim verlassen der Konfiguration nachgefragt ob speichern.
	private OnlineGegner onlgeg;
	private String outstr = null;
	
	private String stravaURL = "https://www.strava.com/oauth/authorize?client_id=13826&response_type=code&redirect_uri=http://strava.mtbsimulator.de&scope=write&approval_prompt=force";
	
	/**
	 * @return the protDatei
	 */
	public String getprotDatei() {
		return protDatei;
	}

	/**
	 * @param protDat the protDatei to set
	 */
	public void setprotDatei(String protDat) {
		protDatei = protDat;
	}

	/**
	 * @return the iblEnde
	 */
	public boolean isIblEnde() {
		return iblEnde;
	}

	/**
	 * @param iblEnde the iblEnde to set
	 */
	public void setIblEnde(boolean iblEnde) {
		this.iblEnde = iblEnde;
	}

	/**
	 * @return the initialisiert
	 */
	public boolean isInitialisiert() {
		return initialisiert;
	}

	/**
	 * @param initialisiert the initialisiert to set
	 */
	public void setInitialisiert(boolean initialisiert) {
		this.initialisiert = initialisiert;
	}

	/**
	 * Einstiegspunkt: Darstellung der Auswertung.
	 * @param txtLabels   Labels
	 * @param txtWerte1   Werte 1
	 * @param txtWerte2   Werte 2
	 * @param txtWerte3   Werte 3
	 * @wbp.parser.entryPoint
	 */
	public void createAuswertung(String txtLabels, String txtWerte1, String txtWerte2, String txtWerte3) {
		if (shell != null) {	// Shell schon vorhanden?
			shell.setVisible(true);
			shell.setFocus();
		} else {
			shell = new Shell(Rsmain.sShell.getDisplay(), SWT.TITLE|SWT.BORDER);	    
			shell.setSize(760, 720);
			shell.setBounds(Rsmain.aktcr.width/2+Rsmain.aktcr.x-380, Rsmain.aktcr.height/2+Rsmain.aktcr.y-360, 760, 720);
			shell.setLayout(null);
			shell.setText(Global.progname + Messages.getString("Auswertung.minusauswertung")); 

			shell.addListener (SWT.Dispose, new Listener () {
				@Override
				public void handleEvent(Event arg0) {
					shell.setVisible(false);
				}       	
			});

			cmpAuswertung = new Composite(shell, SWT.NONE);
			cmpAuswertung.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cmpAuswertung.setBounds(0, 40, shell.getClientArea().width, 680);

			cmpToolbar = new Composite(shell, SWT.NONE);
			cmpToolbar.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			cmpToolbar.setBounds(0, 0, shell.getClientArea().width, 40);

			styledTextLabels = new StyledText(cmpAuswertung, SWT.NONE);
			styledTextLabels.setDoubleClickEnabled(false);
			styledTextLabels.setEditable(false);
			styledTextLabels.setBounds(10, 30, 100, 260);
			styledTextLabels.setText(txtLabels);
			Global.setFontSizeStyledText(styledTextLabels);

			styledTextWerte1 = new StyledText(cmpAuswertung, SWT.NONE);
			styledTextWerte1.setDoubleClickEnabled(false);
			styledTextWerte1.setEditable(false);
			styledTextWerte1.setBounds(110, 30, 90, 260);
			Global.setFontSizeStyledText(styledTextWerte1);

			styledTextWerte2 = new StyledText(cmpAuswertung, SWT.NONE);
			styledTextWerte2.setDoubleClickEnabled(false);
			styledTextWerte2.setEditable(false);
			styledTextWerte2.setBounds(200, 30, 70, 260);
			Global.setFontSizeStyledText(styledTextWerte2);

			styledTextWerte3 = new StyledText(cmpAuswertung, SWT.NONE);
			styledTextWerte3.setDoubleClickEnabled(false);
			styledTextWerte3.setEditable(false);
			styledTextWerte3.setBounds(280, 30, 70, 260);
			Global.setFontSizeStyledText(styledTextWerte3);

			lblTrainingsauswertung = new Label(cmpAuswertung, SWT.NONE);
			lblTrainingsauswertung.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblTrainingsauswertung.setBounds(10, 0, 744, 15);
			Global.setFontSizeLabel(lblTrainingsauswertung);

			lblBelohnung = new Label(cmpAuswertung, SWT.NONE);
			lblBelohnung.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblBelohnung.setBounds(10, 14, 400, 15);
			Global.setFontSizeLabel(lblBelohnung);

			JFreeChart chartWerte = ChartFactory.createXYLineChart("", "", "", dsLeistung, PlotOrientation.VERTICAL, true, false, false);  
			frame = new ChartComposite(cmpAuswertung, SWT.NONE, chartWerte, true);

			plot = (XYPlot) chartWerte.getPlot();
			plot.setBackgroundPaint(Color.lightGray);

			// customise the range axis...
			axisLeistung = (NumberAxis) plot.getRangeAxis(0);			
			axisLeistung.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			axisLeistung.setAutoRangeIncludesZero(false);

			axisPuls = new NumberAxis("");		 
			axisRPM = new NumberAxis("");	
			axisSteigung = new NumberAxis("");
			axisHoehe = new NumberAxis("");

			axisLeistung.setTickLabelPaint(Color.gray);
			plot.setRangeAxis(1, axisPuls);
			plot.setRangeAxis(2, axisRPM);
			plot.setRangeAxis(3, axisSteigung);
			plot.setRangeAxis(4, axisHoehe);
			plot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_LEFT);
			plot.setRangeAxisLocation(4, AxisLocation.BOTTOM_OR_LEFT);
			plot.setRangeGridlinePaint(Color.darkGray);
			plot.setDomainGridlinePaint(Color.darkGray);
			plot.setDataset(1, dsPuls);
			plot.mapDatasetToRangeAxis(1, 1);
			plot.setDataset(2, dsRPM);
			plot.mapDatasetToRangeAxis(2, 2);
			plot.setDataset(3, dsSteigung);
			plot.mapDatasetToRangeAxis(3, 3);
			plot.setDataset(4, dsHoehe);
			plot.mapDatasetToRangeAxis(4, 4);
			rendererLeistung = new XYLineAndShapeRenderer(true, false);
			plot.setRenderer(0, rendererLeistung);
			rendererPuls = new XYLineAndShapeRenderer(true, false);
			plot.setRenderer(1, rendererPuls);
			rendererRPM = new XYLineAndShapeRenderer(true, false);
			plot.setRenderer(2, rendererRPM);
			rendererSteigung = new XYLineAndShapeRenderer(true, false);
			plot.setRenderer(3, rendererSteigung);
			rendererHoehe = new XYLineAndShapeRenderer(true, false);
			plot.setRenderer(4, rendererHoehe);

			LegendTitle legend = chartWerte.getLegend();
			legend.setBackgroundPaint(Color.lightGray);

			frame.pack();
			frame.setBounds(0, 320, cmpAuswertung.getSize().x, 300);

			final Button chkLeistung = new Button(cmpAuswertung, SWT.CHECK);
			chkLeistung.setBounds(300, 300, 80, 16);
			chkLeistung.setText(Messages.getString("Auswertung.leistung")); 
			chkLeistung.setSelection(true);
			chkLeistung.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeButton(chkLeistung);
			chkLeistung.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkLeistung.getSelection()) {
						rendererLeistung.setSeriesVisible(0, true);
						axisLeistung.setVisible(true);
					}	
					else {
						rendererLeistung.setSeriesVisible(0, false);
						axisLeistung.setVisible(false);
					}
				}
			});

			final Button chkPuls = new Button(cmpAuswertung, SWT.CHECK);
			chkPuls.setSelection(true);
			chkPuls.setBounds(380, 300, 80, 16);
			chkPuls.setText(Messages.getString("Auswertung.puls")); 
			chkPuls.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeButton(chkPuls);
			chkPuls.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkPuls.getSelection()) {
						rendererPuls.setSeriesVisible(0, true);
						axisPuls.setVisible(true);
					}
					else {
						rendererPuls.setSeriesVisible(0, false);
						axisPuls.setVisible(false);
					}
				}
			});

			final Button chkRPM = new Button(cmpAuswertung, SWT.CHECK);
			chkRPM.setSelection(true);
			chkRPM.setBounds(460, 300, 80, 16);
			chkRPM.setText(Messages.getString("Auswertung.rpm")); 
			chkRPM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeButton(chkRPM);
			chkRPM.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkRPM.getSelection()) {
						rendererRPM.setSeriesVisible(0, true);
						axisRPM.setVisible(true);
					}
					else {
						rendererRPM.setSeriesVisible(0, false);
						axisRPM.setVisible(false);
					}
				}
			});

			final Button chkSteigung = new Button(cmpAuswertung, SWT.CHECK);
			chkSteigung.setSelection(true);
			chkSteigung.setBounds(540, 300, 80, 16);
			chkSteigung.setText(Messages.getString("Auswertung.steigung")); 
			chkSteigung.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeButton(chkSteigung);
			chkSteigung.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkSteigung.getSelection()) {
						rendererSteigung.setSeriesVisible(0, true);
						axisSteigung.setVisible(true);
					}
					else {
						rendererSteigung.setSeriesVisible(0, false);
						axisSteigung.setVisible(false);
					}
				}
			});

			final Button chkHoehe = new Button(cmpAuswertung, SWT.CHECK);
			chkHoehe.setSelection(true);
			chkHoehe.setBounds(620, 300, 80, 16);
			chkHoehe.setText(Messages.getString("Auswertung.hoehe")); 
			chkHoehe.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			Global.setFontSizeButton(chkHoehe);
			chkHoehe.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (chkHoehe.getSelection()) {
						rendererHoehe.setSeriesVisible(0, true);
						axisHoehe.setVisible(true);
					}
					else {
						rendererHoehe.setSeriesVisible(0, false);
						axisHoehe.setVisible(false);
					}
				}
			});

			// Buttons
			butReset = new Button(cmpAuswertung, SWT.NONE);
			butReset.setText("Reset");  
			butReset.setToolTipText(Messages.getString("Auswertung.loeschen"));  
			butReset.setBounds(33, 620, 70, 25);
			Global.setFontSizeButton(butReset);
			butReset.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (Messages.entscheidungmessage(shell, Messages.getString("Auswertung.alleloeschen"))) {
						gesTrainingszeit = 0; 
						gesStrecke = 0;
						gesHM = 0; 
						gesKCal = 0; 
						gesAnzahl = 0;
//						gesFitnesswert = 0;
						gesPuls = 0;
						gesRPM = 0;
						gesLeistung = 0;
						formatAuswertung();		
						styledTextWerte1.setText(strCol2);
						styledTextWerte3.setText(strCol4);
						setInitialisiert(true);
						saveGesamtTrainingsdaten(false);
						Messages.infomessage(shell, Messages.getString("Auswertung.wurdengeloescht"));
					}
				}
			});

			butSave = new Button(cmpAuswertung, SWT.NONE);
			butSave.setText(Messages.getString("Auswertung.butspeichern"));  
			butSave.setToolTipText(Messages.getString("Auswertung.speichern"));  
			butSave.setBounds(620, 620, 80, 25);
			butSave.setFocus();
			Global.setFontSizeButton(butSave);
			butSave.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					saveGesamtTrainingsdaten(true);
					formatAuswertung();		
					styledTextWerte1.setText(strCol2);
					styledTextWerte3.setText(strCol4);
					butSave.setEnabled(false);
				}
			});

			butok = new Button(cmpAuswertung, SWT.NONE);
			butok.setText(Messages.getString("Auswertung._ok_"));  
			butok.setToolTipText(Messages.getString("Auswertung.auswschliess"));  
			butok.setBounds(700, 620, 50, 25);
			Global.setFontSizeButton(butok);

			// aus der Toolbar herausgenommen, da breiter
			butStrava = new Button(cmpToolbar, SWT.NONE);
			butStrava.setToolTipText(Messages.getString("Auswertung.strava_tp")); 
			butStrava.setImage(new Image(Display.getCurrent(), "strava.png"));
			butStrava.setBounds(113, 0, 180, 40);
			butStrava.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {				
					sendeTraining2Strava(protDatei, Global.stravaKey);
				}
			});

			txtErgebnis = new Text(cmpAuswertung, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
			txtErgebnis.setBounds(360, 50, 380, 235);
			txtErgebnis.setEditable(false);
	        FontData fd = new FontData("Courier",8,SWT.NORMAL); 
	        txtErgebnis.setFont(new Font(Display.getCurrent(), fd));

			Label lblZiel = new Label(cmpAuswertung, SWT.NONE);
			lblZiel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblZiel.setBounds(360, 30, 100, 15);
			lblZiel.setText(Messages.getString("Auswertung.lblZiel"));
			Global.setFontSizeLabel(lblZiel);
			
			butRefresh = new Button(cmpAuswertung, SWT.NONE);
			butRefresh.setText(Messages.getString("Auswertung.butrefresh"));  
			butRefresh.setToolTipText(Messages.getString("Auswertung.butrefreshTP"));  
			butRefresh.setBounds(641, 28, 100, 20);
			Global.setFontSizeButton(butRefresh);
			if (Rsmain.wettkampf.isAktiv())
				butRefresh.setEnabled(true);
			else
				butRefresh.setEnabled(false);
			butRefresh.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
						if (Rsmain.wettkampf.isLanRace()) {
							if (Rsmain.wettkampf.ergliste != null) {
								if (Rsmain.wettkampf.ergliste.size() > 0) {	// nur wenn Ergebnisse da sind
									Rsmain.wettkampf.getRaceErgebnis("0");
									fillResultTable();
								}
							}
						} else
							fillResultTable();									// bei Onlinetraining immer ausführen							
				}
			});

			butok.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (!bSave && lrtsek > cZeitSchranke) {
						if (Messages.entscheidungmessage(shell, Messages.getString("Auswertung.oknosave"))) {
							saveGesamtTrainingsdaten(true);
							bSave = true;
						} 
						shell.setVisible(false);
					} else {
						shell.setVisible(false);
					}
				}
			});
		}
		if (Rsmain.wettkampf.isVirtGeg() || Rsmain.wettkampf.isVirtZP() || Rsmain.wettkampf.isLanRace() || Rsmain.wettkampf.isAktiv()) {
			fillResultTable();
		}
		styledTextWerte1.setText(txtWerte1);
		styledTextWerte2.setText(txtWerte2);
		styledTextWerte3.setText(txtWerte3);
		lblTrainingsauswertung.setText(Messages.getString("Auswertung.trainingsdatei")+protDatei); 
		lblBelohnung.setText(Rsmain.belText.replace("\n", " ")); 
		Toolbar();
		shell.open();
	}

	/**
	 * Schreibt die Ergebnisse aus onlinerennen.ergliste in die Ergebnistabelle (Text: txtErgebnis) für das virtuelle Rennen.
	 */
	private void fillResultTable() {
		tfmt = new SimpleDateFormat();
		tfmt.applyPattern("H:mm:ss");  
		int i = 0;
		Mlog.info("Ergebnis des Rennens:");
		txtErgebnis.setText("");
		if (Rsmain.wettkampf.isVirtGeg() || Rsmain.wettkampf.isVirtZP()) {	// bei virtuellen Gegnern, die eigenen Daten adden
			OnlineGegner ich = new OnlineGegner("", Rsmain.biker.getName(), new Date(Rsmain.trainingsmillisek), 0, null, 0, 0.0, 
					dpulsav, drpmav, dpav, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Rsmain.dzpstrecke, 0, 0);
			Rsmain.wettkampf.ergliste.add(ich);
		}
		if (Rsmain.wettkampf.isVirtGeg() || Rsmain.wettkampf.isVirtZP() || Rsmain.wettkampf.isLanRace()) {	
			Collections.sort(Rsmain.wettkampf.ergliste);
			outstr = "Pos         Name    Zeit    km  HR RPM Watt";
			txtErgebnis.setText(outstr+"\n");	
			Mlog.info(outstr);

			for (Iterator<?> iterator = Rsmain.wettkampf.ergliste.iterator(); iterator.hasNext();) {
				i++;
				onlgeg = (OnlineGegner) iterator.next();
				String name = onlgeg.getNachname();
				if (Rsmain.wettkampf.isZprace())
					outstr = String.format("%2d.%13.13s%8s%6.2f%4d%4d%5d",i, name, tfmt.format(onlgeg.getZeitpunkt()), 
							onlgeg.getZpstrecke()/1000, (int) onlgeg.getPuls(), (int) onlgeg.getRpm(), 
							(int) onlgeg.getLeistung());  
				else	
					outstr = String.format("%2d.%13.13s%8s%6.2f%4d%4d%5d",i, name, tfmt.format(onlgeg.getZeitpunkt()), 
							onlgeg.getStrecke()/1000, (int) onlgeg.getPuls(), (int) onlgeg.getRpm(), 
							(int) onlgeg.getLeistung());  
				Mlog.info(outstr);
				txtErgebnis.append(outstr+"\n");
			}			
		} else {							// bei Onlinetraining die aktuelle Rangliste abfragen
			Rsmain.wettkampf.receiveData();
			outstr = "Pos         Name    km  HR RPM Watt";
			txtErgebnis.setText(outstr+"\n");	
			Mlog.info(outstr);

			for (Iterator<?> iterator = Rsmain.wettkampf.gegliste.iterator(); iterator.hasNext();) {
				i++;
				onlgeg = (OnlineGegner) iterator.next();
				String name = onlgeg.getNachname();
				if (Rsmain.wettkampf.isZprace())
					outstr = String.format("%2d.%13.13s%6.2f%4d%4d%5d",i, name, 
							onlgeg.getZpstrecke()/1000, (int) onlgeg.getPuls(), (int) onlgeg.getRpm(), 
							(int) onlgeg.getLeistung());  
				else	
					outstr = String.format("%2d.%13.13s%6.2f%4d%4d%5d",i, name, 
							onlgeg.getStrecke()/1000, (int) onlgeg.getPuls(), (int) onlgeg.getRpm(), 
							(int) onlgeg.getLeistung());  
				Mlog.info(outstr);
				txtErgebnis.append(outstr+"\n");
			}
		}
	}

	
	/**
	 * Hier wird die Konfigurations-Toolbar dargestellt.
	 */
	private void Toolbar() {
		ToolBar toolBar = new ToolBar(cmpToolbar, SWT.FLAT | SWT.RIGHT);
		final ToolBarManager manager = new ToolBarManager(toolBar);

		final Action actionOpen =
			new Action(
					Messages.getString("Auswertung.toureinles"), 
					ImageDescriptor.createFromImage(
							new Image(Display.getCurrent(), "open.png"))) { 
			public void run() {
				readCSV(null);
			}
		};

		final Action actionCSV =
			new Action(
					Messages.getString("Auswertung.csvspeichern"), 
					ImageDescriptor.createFromImage(
							new Image(Display.getCurrent(), "spreadsheet.png"))) { 
			public void run() {
				FileInputStream inp;
				FileOutputStream out;
				
		    	FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setFilterNames (new String [] {Messages.getString("Auswertung.csvdateien")});    
				dialog.setFilterExtensions (new String [] {"*.csv"});  
				dialog.setFilterPath(Global.strPfad);   
				CSVDatei = dialog.open();

				if (CSVDatei != null && protDatei != null) {
					File datei = new File(protDatei);
					if (datei.exists()) {
						try {
							inp = new FileInputStream(protDatei);
							out = new FileOutputStream(CSVDatei);
							IOUtils.copy(inp, out);
							inp.close();
							out.close();
						} catch (FileNotFoundException e) {
							Mlog.ex(e);
						} catch (IOException e) {
							Mlog.ex(e);
						}
					}						
				}
			}
		};

		final Action actionTcx =
			new Action(
					Messages.getString("Auswertung.tcxecportieren"), 
					ImageDescriptor.createFromImage(
							new Image(Display.getCurrent(), "garmin.png"))) { 
			public void run() {
				writeTCXFile(FilenameUtils.removeExtension(protDatei)+".tcx", false);
			}
		};

		manager.add(actionOpen);
		manager.add(actionCSV);
		manager.add(actionTcx);
		manager.update(true);
	}

	/**
	 * Es wird ein Browserfenster geöffnet zur Anmeldung an Strava. Ist die Anmeldung
	 * erfolgreich, bzw. ist der User bereits angemeldet, dann wird ein Zugrifftoken
	 * angezeigt.
	 * Verwendet wird als Redirect-URL strava.mtbsimulator.de (dort beantwortet ein Servlet die Strava Anfrage)
	 */
	private void openStravaBrowser() {
		// Browserfenster öffnen
		try {
    		if (Platform.isWindows())
    			Runtime.getRuntime().exec( "rundll32 url.dll,FileProtocolHandler " + stravaURL ); 
    		else if (Platform.isMac())
    			Runtime.getRuntime().exec( new String[] { "open" , "-a", "Safari", stravaURL }) ;	    			
		} catch (IOException e) {
			Mlog.ex(e);
		}		
	}


	/**
	 * Aus dem dateinamen wird der Titel für den Strava-Upload gebildet.
	 * Verfahren: Es wird zuerst die Dateiextension abgeschnitten und vorne "RS - " angehängt.
	 * Dann wird überprüft, ob er überhaupt zu lang ist (maxlen).
	 * Dann wird am Jahr (alle MTBS-Touren haben ein Jahr mit 20 drin) abgeschnitten, falls kein 20 enthalten, dann an
	 * der Position maxlen-3 und "..." angehängt.
	 * @param dateiname  kompletter Dateiname (Upload-Datei)
	 * @return strava Titel
	 */
	public String setStravaTitel(String dateiname) {
		int ind;
		int maxlen = 35;
		boolean schnitt = false;
		
		String tname = FilenameUtils.removeExtension(dateiname);

		tname = "RS - " + tname;
		
		if (tname.length() < maxlen)				// dann so lassen
			return tname;
		
		ind = tname.indexOf("20");				// ggf. bei "20.." = Jahr abschneiden
		if (ind == -1) {
			ind = maxlen-3;
			schnitt = true;
		}
		
		if (ind < tname.length()) {
			tname = tname.substring(0, ind);
			if (schnitt)
				tname = tname + "...";
		}
		
		return tname;
	}
	
	/**
	 * Das aktuelle Training wird zu Strava hochgeladen
	 * Wird ein Key übergeben, dann wird dieser zum Upload verwendet. Falls nicht, wird ein Browserfenster zur Registrierung geöffnet.
	 * Der übermittelte Token muss über die Zwischenablage in die Konfiguration übernommen werden.
	 * Auf strava.mtbsimulator.de liegt ein Servlet, (StravaService.getToken) das die Autorisierung bei Strava unterstützt.
	 * @param protDatei		Protokolldatei (CSV-Datei)
	 * @param stravaKey		Strava-Zugriffstoken (aus Global), wenn leer, dann wird Token angezeigt im Browser
	 * @return Id der Aktivität (0, wenn nicht erfolgreich)
	 */
    public long sendeTraining2Strava(String protDatei, String stravaKey) {
    	int cnt = 0;
    	long newactid = 0;
    	int maxtries = 10;	// der Upload darf maximal 10 Sek. dauern
    	String id = "";
    	String trainingName = "";
    	Shell info = null;
    	String infotext = null;
    	
    	TimeZone.setDefault(null);
    	SimpleDateFormat tfmtstrava = new SimpleDateFormat();
        tfmtstrava.applyPattern("dd.MM.yyyy, HH:mm");    
    	
    	if (stravaKey.isEmpty()) {
    		Messages.infomessage(shell, Messages.getString("Auswertung.stravaautorisierung"));
    		openStravaBrowser();
    		return 0;
    	}
        JStravaV3 strava= new JStravaV3(stravaKey);
        if (pliste.isEmpty()) {
        	Messages.errormessage(shell, Messages.getString("Auswertung.stravakeintraining"));
        	return 0;
        }
        	
    	File csvdatei = new File(protDatei);
		File uploaddatei = new File(FilenameUtils.removeExtension(protDatei)+".tcx");    		
		if (csvdatei.exists()) {
    		if (!uploaddatei.exists()) {
    			// dann wird jetzt die csv-Datei nach tcx gewandelt
    			writeTCXFile(uploaddatei.getAbsolutePath(), true);
    		}
    	} else {
        	Messages.errormessage(shell, Messages.getString("Auswertung.csvnichtda"));
        	return 0;
    	}
		
		infotext = "Strava-Upload: "+uploaddatei.getName();
    	Mlog.debug(infotext);
		info = Messages.infomessage_on(infotext,450,40,10);
    	
    	UploadStatus uploadstat;
		try {
			uploadstat = strava.uploadActivity("tcx", uploaddatei);
	    	id = uploadstat.getId();
	    	//Mlog.debug("Id: " + id);
	    	Mlog.debug("Stravaupload-Error: " + uploadstat.getError());
	    	while (uploadstat.getStatus().contains("processed") && cnt++ < maxtries) {
	    		Global.sleep(1000);
	    		uploadstat = strava.checkUploadStatus(new Long(id));
	    	}
	    	Mlog.debug("cnt: " + cnt);
	    	if (cnt >= maxtries) {
	    		Messages.infomessage_off(info);
	    		Messages.errormessage(shell, Messages.getString("Auswertung.stravanotok")+uploaddatei.getAbsolutePath());
	    		return 0;
	    	}
	    	newactid = uploadstat.getActivity_id();
	    	Mlog.debug("Activity-Id: " + newactid);
		} catch (Exception e1) {
			if (e1.toString().contains("401")) {
				Mlog.debug("Strava Autorisierungsfehler 1: "+e1.toString());
	    		openStravaBrowser();
			} else
				Mlog.ex(e1);
    		Messages.infomessage_off(info);
			return 0;
		} 
		Messages.infomessage_off(info);
		Global.sleep(1000);
		
		infotext = "Strava-Update: "+uploaddatei.getName();
		info = Messages.infomessage_on(infotext,450,40,10);
        try {
			HashMap<String, String> optionalParameters= new HashMap<String, String>();
			String description = "RoomSports Training: "+uploaddatei.getName()+", eingefügt am "+tfmtstrava.format(new Date().getTime())+" Uhr";
			trainingName = setStravaTitel(uploaddatei.getName());
			String acttype = "VirtualRide";
			optionalParameters.put("type", acttype);
			optionalParameters.put("workout_type", "12");		// workout
// sonst keine Kartenansicht!			optionalParameters.put("trainer", "true");		
			optionalParameters.put("description",description);
			optionalParameters.put("name",trainingName);
			strava.updateActivity(newactid,optionalParameters);
		} catch (Exception e2) {
			if (e2.toString().contains("401")) {				
				Mlog.debug("Strava Autorisierungsfehler 2: "+e2.toString());
	    		openStravaBrowser();
			} else 
				if (e2.toString().contains("JsonSyntaxException")) {
					Mlog.debug("JStravaV3 activity-id Überlauf: neue Version unter https://github.com/dustedrob/JStrava verfügbar?");
				} else
					Mlog.ex(e2);
		} 
		Messages.infomessage_off(info);

		if (newactid != 0) {
        	Messages.infomessage(shell, Messages.getString("Auswertung.stravaok") + trainingName);
        }
        return newactid;
	}

	/**
     * Hier wird mittels Dateidialog die CSV-Datei ausgewählt und eingelesen.
     * @param csvDatei 		Datei, die eingelesen werden soll, wenn null dann wird per FileDialog ausgewählt
     */
    public void readCSV(String csvDatei) {
		BufferedReader reader = null;
		String zeile;
		String[] split = null;
		int n = 0;
		Date zeitpunkt = null;
		Date tag = null;
		
		if (csvDatei == null) {
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			dialog.setFilterNames (new String [] {Messages.getString("Auswertung.csvdateien")});    
			dialog.setFilterExtensions (new String [] {"*.csv"}); //Windows wild cards    
			dialog.setFilterPath (Global.strPfad);   
			protDatei = dialog.open();
		} else
			protDatei = csvDatei;

		if (protDatei != null) {
			// CSV-Datei einlesen und pliste füllen
			profilDataLeistung.clear();
			profilDataPuls.clear();
			profilDataRPM.clear();
			profilDataSteigung.clear();
			profilDataHoehe.clear();
			dsLeistung.removeAllSeries();
			dsPuls.removeAllSeries();
			dsRPM.removeAllSeries();
			dsHoehe.removeAllSeries();
			dsSteigung.removeAllSeries();
			
			Mlog.debug("CSV-Datei: "+protDatei+" wird eingelesen");  
			pliste = new ArrayList<Psatz>();
			initkumWerte();
			try {
				reader = new BufferedReader(new FileReader(protDatei));
				while ((zeile = reader.readLine()) != null) {
					if (n == 0) {
						String sDatum = zeile.substring(zeile.lastIndexOf(", den ")+6);
						tag = datumformatkurz.parse(sDatum);
					}
					if (n > 2) {
						zeile = zeile.replace(',', '.');
						split = zeile.split(";");
						zeitpunkt = new Date(tag.getTime() + tfmt.parse(split[0]).getTime());  
						//Mlog.debug("Zeitpunkt:" + zeitpunkt);
						//ps = new psatz(datum, punkt, wind, gang, work, puls, rpm, leistung, geschw, strecke, steigung, hoehe, lat, lon, hm);
						ps = new Psatz(n, zeitpunkt, new Long(split[1]).longValue(), 
								new Double(split[4]).doubleValue(), new Double(split[5]).doubleValue(), new Double(split[6]).doubleValue(), 
								new Double(split[7]).doubleValue(), new Double(split[8]).doubleValue(), new Double(split[9]).doubleValue(), 
								new Double(split[10]).doubleValue(), new Double(split[11]).doubleValue(), new Double(split[12]).doubleValue(), 
								new Double(split[13]).doubleValue());
						pliste.add(ps);
						profilDataLeistung.add(ps.strecke/1000, ps.leistung);   
						//Mlog.debug("debug: Strecke: "+ps.strecke/1000 + " - Leistung: " +ps.leistung);
						profilDataPuls.add(ps.strecke/1000, ps.puls);   
						profilDataRPM.add(ps.strecke/1000, ps.rpm);   
						profilDataHoehe.add(ps.strecke/1000, ps.hoehe);   
						profilDataSteigung.add(ps.strecke/1000, ps.steigung);   
					}
					n++;
				}
			} catch (IOException ioe) {
				Mlog.ex(ioe);
			} catch (ParseException pe) {
				Mlog.ex(pe);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ioe) {
						Mlog.ex(ioe);
					}
					reader = null;
				}
			}	
		} 
		dsLeistung.addSeries(profilDataLeistung);
		dsPuls.addSeries(profilDataPuls);
		dsRPM.addSeries(profilDataRPM);
		dsHoehe.addSeries(profilDataHoehe);
		dsSteigung.addSeries(profilDataSteigung);

		calcAuswertung();
		formatAuswertung();
		styledTextWerte1.setText(strCol2);
		styledTextWerte2.setText(strCol3);
		styledTextWerte3.setText(strCol4);
		lblTrainingsauswertung.setText(Messages.getString("Auswertung.trainingsdatei")+protDatei); 
		lrtsek = 0;
	}


    /**
     * Initialisierung der kumulierten Werte (Summen, Zeiten etc.)
     */
    private void initkumWerte() {
    	lanzahl = 0;
    	dpulssum = 0;
    	dmaxpuls = 0;
    	dminpuls = 200;
    	drpmsum = 0;
    	dmaxrpm = 0;
    	dminrpm = 200;
    	dpsum = 0;
    	dmaxp = 0;
    	dminp = 500;
    	dsteigungsum = 0;
    	dmaxsteigung = -100;
    	dminsteigung = 100;
    	dvsum = 0;
    	dmaxv = 0;
    	dminv = 200;
    	lsek = 0;
    	lzdiff = 0;
    	dm = 0;
    	dhm = 0;
    	dkcal = 0;
//    	dfw = 0;
    	dpulsav = 0;
    	drpmav = 0;
    	dpav = 0;
    	dvav = 0;
    	dsteigungav = 0;
    }
    
	/**
	 * Initialisiert die Protokollliste und das schreiben in die Protokolldatei.
	 * Es dient der Tourname und der übergebene Name des Bikers als Dateiname (mit einem Zeitstempel versehen) 
	 * 
	 * @param konfig    akt. Konfiguration zur Ausgabe im Header
	 * @param biker		der Biker der zu diesem Zeitpunkt ausgewählt ist
	 * @param tour		Tourname (kommt in die Überschrift)
	 */
	public void init(Konfiguration konfig, Fahrer biker, String tour) {
		if (isInitialisiert())
			return;

		index = 0;

		profilDataLeistung.clear();
		profilDataPuls.clear();
		profilDataRPM.clear();
		profilDataSteigung.clear();
		profilDataHoehe.clear();
		dsLeistung.removeAllSeries();
		dsPuls.removeAllSeries();
		dsRPM.removeAllSeries();
		dsHoehe.removeAllSeries();
		dsSteigung.removeAllSeries();
		
		//if (initialisiert)
		//	return;

		pliste = new ArrayList<Psatz>();
			
		initkumWerte();

		try {
			Calendar cal = Calendar.getInstance(); 	
			SimpleDateFormat dateidatumformat = new SimpleDateFormat("yyyyMMdd-HHmmss"); 
			datumformat.setTimeZone(TimeZone.getTimeZone(Global.zeitzone));
			dateidatumformat.setTimeZone(TimeZone.getTimeZone(Global.zeitzone)); 
			protDatei = new String(Global.strPfad+tour+biker.getName()+dateidatumformat.format(cal.getTime())+".csv");   
			fw = new FileWriter(protDatei);
			pw = new PrintWriter(fw, true);
			// Überschriften speichern:
			pw.println(Messages.getString("Auswertung.tour")+tour+Messages.getString("Auswertung.minusbiker")+biker.getName()+Messages.getString("Auswertung.minusnorm")+zfk1.format(biker.getDynRPMNormal())+Messages.getString("Auswertung.wiege")+zfk1.format(biker.getDynRPMWiege())+Messages.getString("protokoll.gewicht")+zfk1.format(biker.getFahrergewicht()+biker.getBikegewicht())+Messages.getString("Auswertung.minuspmax")+zfk1.format(biker.getMaxleistung())+Messages.getString("Auswertung.minuspmin")+zfk1.format(biker.getMinleistung())+Messages.getString("protokoll.HP_GLATT")+konfig.isaveraging()+Messages.getString("protokoll.Date")+datumformat.format(cal.getTime()));
			pw.println(Messages.getString("protokoll.UEBERSCHRIFT")); 
		} catch (FileNotFoundException e) {
			Mlog.ex(e);
		} catch (IOException e) {
			Mlog.ex(e);
		}
		setInitialisiert(true);
	}

	/**
	 * Schreibt eine Zeile mit Trainingsdaten in die Protokollliste und wenn eingeschaltet in die Protokolldatei. 
	 * Es werden die übergebenen Parameter in die Zeile geschrieben. Zusätzlich vorne der Zeitpunkt.
	 *
	 * @param inDateischreiben  in Datei schreiben (true/false)
	 * @param punkt		akt. Punkt
	 * @param wind		akt. Wind
	 * @param gang		akt. Gang
	 * @param work		akt. kCal
	 * @param puls		akt. Puls
	 * @param rpm		akt. Pedalumdrehungen
	 * @param leistung	akt. Leistung
	 * @param geschw	akt. Geschwindigkeit
	 * @param strecke	akt. gefahrene Strecke insgesamt
	 * @param steigung	akt. Steigung
	 * @param hoehe		aktuelle Höhe
	 * @param lat     	Latitude (Breitengrad)
     * @param lon		Longitude (Längengrad)

	 */
	public void schreibedaten(
			boolean inDateischreiben,
			long punkt, 
			String wind, 
			int gang, 
			double work, 
			double puls, 
			double rpm, 
			double leistung, 
			double geschw, 
			double strecke, 
			double steigung, 
			double hoehe,
			double lat,
			double lon) {
		datum = new Date();
		SimpleDateFormat zeitformat = new SimpleDateFormat("HH:mm:ss"); 
		zeitformat.setTimeZone(TimeZone.getTimeZone(Global.zeitzone));
		DecimalFormat zfk0 = new DecimalFormat("#"); 
		DecimalFormat zfk1 = new DecimalFormat("#.#"); 
		DecimalFormat zfk2 = new DecimalFormat("#.##"); 
		DecimalFormat zfk8 = new DecimalFormat("#.########"); 
		
		ps = new Psatz(++index, datum, punkt, work, puls, rpm, leistung, geschw, strecke, steigung, hoehe, lat, lon);
		pliste.add(ps);
		profilDataLeistung.add(ps.strecke/1000, ps.leistung);    
		profilDataPuls.add(ps.strecke/1000, ps.puls);    
		profilDataRPM.add(ps.strecke/1000, ps.rpm);    
		profilDataHoehe.add(ps.strecke/1000, ps.hoehe);    
		profilDataSteigung.add(ps.strecke/1000, ps.steigung);    
		
		if (inDateischreiben)
			pw.println(zeitformat.format(datum)+";"+punkt+";"+wind+";"+gang+";"+zfk1.format(work)+";"+zfk1.format(puls)+";"+zfk1.format(rpm)+";"+zfk0.format(leistung)+";"+zfk1.format(geschw)+";"+zfk1.format(strecke)+";"+zfk0.format(steigung)+";"+zfk2.format(hoehe)+";"+zfk8.format(lat)+";"+zfk8.format(lon)); 
	}

	/**
	 * berechnet die kumulierten Ausgabewerte aus der Liste (pliste).
	 */
	private void calcAuswertung() {
		double letztehoehe = 0.0;

		if (pliste == null)
			return;
		initkumWerte();
		for (Iterator<Psatz> iterator = pliste.iterator(); iterator.hasNext();) {
			ps = (Psatz) iterator.next();
			if (ps.rpm >= Rsmain.drpmmin) {
				lanzahl += 1;
				
				if (ps.punkt == 1 || lanzahl == 1) {  // erster Punkt?
					letztehoehe = ps.hoehe;
					letztezeit = ps.zeitpunkt;
				}
				else {
					dhm += (ps.hoehe > letztehoehe) ? (ps.hoehe - letztehoehe) : 0;
					lzdiff = ps.zeitpunkt.getTime() - letztezeit.getTime();
					//Mlog.debug("Punkt:"+ps.punkt+" / Index:"+ps.index+" / Zeitpunkt:"+ps.zeitpunkt+" / LetzteZeit:"+letztezeit+" / Diff:"+lzdiff+" / dhm:"+dhm);
					if (lzdiff <= 4000) // sonst ist es Pausenzeit!
						lsek += lzdiff;
				}	
				dpulssum += ps.puls;
				drpmsum += ps.rpm;
				dpsum += ps.leistung;
				dsteigungsum += ps.steigung;
				dvsum += ps.geschw;
				//dm += ps.geschw / 3.6;  // das ist sehr ungenau!
				dm = ps.strecke;
				dkcal = ps.work;
				dmaxpuls = (ps.puls > dmaxpuls) ? ps.puls : dmaxpuls;
				dminpuls = (ps.puls < dminpuls) ? ps.puls : dminpuls;
				dmaxrpm = (ps.rpm > dmaxrpm) ? ps.rpm : dmaxrpm;
				dminrpm = (ps.rpm < dminrpm) ? ps.rpm : dminrpm;
				dmaxp = (ps.leistung > dmaxp) ? ps.leistung : dmaxp;
				dminp = (ps.leistung < dminp) ? ps.leistung : dminp;
				dmaxv = (ps.geschw > dmaxv) ? ps.geschw : dmaxv;
				dminv = (ps.geschw < dminv) ? ps.geschw : dminv;
				dmaxsteigung = (ps.steigung > dmaxsteigung) ? ps.steigung : dmaxsteigung;
				dminsteigung = (ps.steigung < dminsteigung) ? ps.steigung : dminsteigung;
				letztezeit = ps.zeitpunkt;
				letztehoehe = ps.hoehe;
			}
		}

//		dfw = (dpsum / lanzahl) / (dpulssum / lanzahl);
		dpulsav = dpulssum / lanzahl;
		drpmav = drpmsum / lanzahl;
		dpav = dpsum / lanzahl;
		dvav = dvsum / lanzahl;
		dsteigungav = dsteigungsum / lanzahl;
		lrtsek = lsek;
	}

	/**
	 * formatiert die Ausgabestrings für die (obere) Auswertung.
	 */
	private void formatAuswertung() {
		strCol1 = 
		Messages.getString("protokoll.trainingszeit")+"\n"+  
		Messages.getString("protokoll.strecke_km")+"\n"+  
		Messages.getString("protokoll.hoehenmeter")+"\n"+  
		Messages.getString("protokoll.kcal")+"\n"+ 
		"-------------------\n"+  
		Messages.getString("protokoll.puls")+"\n"+   
		Messages.getString("protokoll.rpm")+"\n"+  
		Messages.getString("protokoll.leistung")+"\n"+  
		Messages.getString("protokoll.geschwindigkeit")+"\n"+  
		Messages.getString("protokoll.steigung")+"\n"+
		"-------------------\n"+  		
		Messages.getString("protokoll.trainingszeit")+"\n"+  
		Messages.getString("protokoll.strecke_km")+"\n"+  
		Messages.getString("protokoll.hoehenmeter")+"\n"+  
		Messages.getString("protokoll.kcal")+"\n"  
		;	  

		
		strCol2 = 
		//tfmt.format(lsek * 1000)+"\n"+ 
		tfmt.format(lsek)+"\n"+ 
		zfk1.format(dm / 1000)+"\n"+ 
		zfk0.format(dhm)+"\n"+ 
		zfk0.format(dkcal)+"\n"+ 
		Messages.getString("Auswertung.durchschnitt_dp")+  
		zfk1.format(dpulsav)+"\n"+  
		zfk1.format(drpmav)+"\n"+ 
		zfk0.format(dpav)+"\n"+ 
		zfk1.format(dvav)+"\n"+ 
		zfk1.format(dsteigungav)+"\n"+
		Messages.getString("Auswertung.gesamtwertedp")+"\n"+
		//tfmt.format(gesTrainingszeit)+"\n"+  
		String.format("%02d:",TimeUnit.MILLISECONDS.toDays(gesTrainingszeit))+tfmt.format(gesTrainingszeit)+"\n"+
		zfk0.format(gesStrecke / 1000)+"\n"+ 
		zfk0.format(gesHM)+"\n"+ 
		zfk0.format(gesKCal)+"\n"; 
		;	 

		strCol3 = 
		"\n\n\n\n"+ 
		Messages.getString("Auswertung.maximal_dp")+  
		zfk1.format(dmaxpuls)+"\n"+  
		zfk1.format(dmaxrpm)+"\n"+ 
		zfk0.format(dmaxp)+"\n"+ 
		zfk1.format(dmaxv)+"\n"+ 
		zfk1.format(dmaxsteigung)+"\n"+
		"------------\n"+
		"\n"+
		Messages.getString("protokoll.puls")+"\n"+   
		Messages.getString("protokoll.rpm")+"\n"+  
		Messages.getString("protokoll.leistung")+"\n";

		long div = gesAnzahl == 0 ? 1 : gesAnzahl;
		strCol4 = 
		"\n\n\n\n"+ 
		Messages.getString("Auswertung.minimal_dp")+  
		zfk1.format(dminpuls)+"\n"+  
		zfk1.format(dminrpm)+"\n"+ 
		zfk0.format(dminp)+"\n"+ 
		zfk1.format(dminv)+"\n"+ 
		zfk1.format(dminsteigung)+"\n"+	 
		"------------\n"+		
		"\n"+
		zfk1.format(gesPuls/div)+"\n"+
		zfk1.format(gesRPM/div)+"\n"+
		zfk1.format(gesLeistung/div)+"\n"
		;

	}

	/**
	 * XML-Datei mittels DOM-Objekt erzeugen.
	 * Wird hier verwendet zum schreiben der TCX-Datei für den Upload z. B. ins Garmin Trainingcenter.
	 * @param tcxDateiname	wenn autosave = true, dann steht hier der Dateiname der zu erstellenden TCX-Datei
	 * @param autosave wenn autosave = true, dann wird direkt die Datei geschrieben, bei false kommt die Dateiauswahl
	 */
	public void writeTCXFile(String tcxDateiname, boolean autosave){
		Document dom;  
		// hole factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// zfk1pkt formatiert mit Punkt als Dezimaltrenner fürs TCX-Format
		DecimalFormat zfk1pkt = new DecimalFormat("###0.0"); 
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		zfk1pkt.setDecimalFormatSymbols(dfs);
		Psatz psLast = null; 

		try {			
			Date startzeit = pliste.get(0).zeitpunkt;
			// mittels factory instance des document builder erzeugen
			DocumentBuilder db = dbf.newDocumentBuilder();

			// DOM Representation des XML file erzeugen 
			dom = db.newDocument();
			DOMSource source = new DOMSource(dom);

			Element eletcdb = dom.createElementNS("http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2", "TrainingCenterDatabase");   
			eletcdb.setAttribute("xmlns", "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2");  
			eletcdb.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd");   
			dom.appendChild(eletcdb);
			// ------------------
			Element eleActivities = dom.createElement("Activities"); 
			eletcdb.appendChild(eleActivities);
			Element eleActivity = dom.createElement("Activity"); 
			eleActivity.setAttribute("Sport", "Biking");  
			eleActivities.appendChild(eleActivity);
			Element eleId = dom.createElement("Id");  
			eleId.setTextContent(tfmttcx.format(startzeit)+"T"+tfmt.format(startzeit)+"Z");  
			eleActivity.appendChild(eleId);
			Element elelap = dom.createElement("Lap"); 
			elelap.setAttribute("StartTime", tfmttcx.format(startzeit)+"T"+tfmt.format(startzeit)+"Z");   
			eleActivity.appendChild(elelap);
			Element eletottime = dom.createElement("TotalTimeSeconds");  
			//eletottime.setTextContent(zfk0.format(lsek));
			eletottime.setTextContent(zfk0.format(lsek / 1000));
			elelap.appendChild(eletottime);
			Element eledist = dom.createElement("DistanceMeters");  
			eledist.setTextContent(zfk1pkt.format(dm));
			elelap.appendChild(eledist);
			Element elespeedmax = dom.createElement("MaximumSpeed");  
			elespeedmax.setTextContent(zfk1pkt.format(dmaxv / 3.6));
			elelap.appendChild(elespeedmax);
			Element elecalo = dom.createElement("Calories");  
			elecalo.setTextContent(zfk0.format(dkcal));
			elelap.appendChild(elecalo);
			Element elehravg = dom.createElement("AverageHeartRateBpm");  
			Element eleval2 = dom.createElement("Value"); 				 
			eleval2.setTextContent(zfk0.format(dpulsav)+""); 
			elehravg.appendChild(eleval2);
			elelap.appendChild(elehravg);
			Element elehrmax = dom.createElement("MaximumHeartRateBpm");  
			Element eleval1 = dom.createElement("Value"); 				 
			eleval1.setTextContent(zfk0.format(dmaxpuls));
			elehrmax.appendChild(eleval1);
			elelap.appendChild(elehrmax);
			Element eleintens = dom.createElement("Intensity");  
			eleintens.setTextContent("Active"); 
			elelap.appendChild(eleintens);
			Element elecadavg = dom.createElement("Cadence");  
			elecadavg.setTextContent(zfk0.format(drpmav));
			elelap.appendChild(elecadavg);
			Element eletrigger = dom.createElement("TriggerMethod");  
			eletrigger.setTextContent("Manual"); 
			elelap.appendChild(eletrigger);
			Element eletrack = dom.createElement("Track");  
			elelap.appendChild(eletrack);

			for (Iterator<Psatz> iterator = pliste.iterator(); iterator.hasNext();) {
				ps = (Psatz) iterator.next();
				if (psLast != null) {
					if (ps.lat != psLast.lat || ps.lon != psLast.lon) {
						Element eletrackpoint = dom.createElement("Trackpoint");  
						Element eletime = dom.createElement("Time"); 				 
						eletime.setTextContent(tfmttcx.format(psLast.zeitpunkt)+"T"+tfmt.format(psLast.zeitpunkt)+"Z");  
						eletrackpoint.appendChild(eletime);
						Element elepos = dom.createElement("Position"); 				 
						Element elelat = dom.createElement("LatitudeDegrees"); 				 
						elelat.setTextContent(psLast.lat+""); 
						elepos.appendChild(elelat);
						Element elelon = dom.createElement("LongitudeDegrees"); 				 
						elelon.setTextContent(psLast.lon+""); 
						elepos.appendChild(elelon);				
						eletrackpoint.appendChild(elepos);
						Element elealt = dom.createElement("AltitudeMeters"); 				 
						elealt.setTextContent(psLast.hoehe+""); 
						eletrackpoint.appendChild(elealt);
						Element eledistm = dom.createElement("DistanceMeters");  
						eledistm.setTextContent(psLast.strecke+"");  
						//eledistm.setTextContent("0");  				// 0 ausgeben, da sonst zu hohe Geschwindigkeiten ausgegeben werden. 
						eletrackpoint.appendChild(eledistm);
						Element elehr = dom.createElement("HeartRateBpm");  
						Element eleval = dom.createElement("Value"); 				 
						eleval.setTextContent(zfk0.format(psLast.puls));
						elehr.appendChild(eleval);
						eletrackpoint.appendChild(elehr);
						Element elecad = dom.createElement("Cadence"); 				 
						elecad.setTextContent(zfk0.format(psLast.rpm));
						eletrackpoint.appendChild(elecad);
						Element elesense = dom.createElement("SensorState"); 				 
						elesense.setTextContent("Present"); 
						eletrackpoint.appendChild(elesense);
						Element eleext = dom.createElement("Extensions"); 				 
						Element eletpx = dom.createElement("TPX"); 				 
						eletpx.setAttribute("xmlns", "http://www.garmin.com/xmlschemas/ActivityExtension/v2");  
						Element elewatts = dom.createElement("Watts"); 				 
						elewatts.setTextContent(zfk0.format(psLast.leistung));
						eletpx.appendChild(elewatts);
						eleext.appendChild(eletpx);
						eletrackpoint.appendChild(eleext);

						eletrack.appendChild(eletrackpoint);
					}
				}
				psLast = ps;
			}

			Element eleext1 = dom.createElement("Extensions"); 				 
			Element elelx = dom.createElement("LX"); 				 
			elelx.setAttribute("xmlns", "http://www.garmin.com/xmlschemas/ActivityExtension/v2");  
			Element elewattsavg = dom.createElement("AvgWatts"); 				 
			elewattsavg.setTextContent(zfk0.format(dpav));
			elelx.appendChild(elewattsavg);
			eleext1.appendChild(elelx);
			elelap.appendChild(eleext1);
			// Creator
			Element elecreator = dom.createElement("Creator"); 				 
			elecreator.setAttribute("xsi:type", "Device_t");  
			Element elename = dom.createElement("Name"); 				 
			elename.setTextContent("RoomSports"); 
			//elename.setTextContent("EDGE705");
			elecreator.appendChild(elename);
			Element eleUnitId = dom.createElement("UnitId"); 				 
			eleUnitId.setTextContent("3393600080"); 
			elecreator.appendChild(eleUnitId);
			Element eleProdId = dom.createElement("ProductID"); 				 
			//eleProdId.setTextContent("625");
			eleProdId.setTextContent("0"); 
			elecreator.appendChild(eleProdId);
			Element eleVersion = dom.createElement("Version"); 				 
			Element eleVersionMaj = dom.createElement("VersionMajor"); 				 
			eleVersionMaj.setTextContent("0"); 
			eleVersion.appendChild(eleVersionMaj);
			Element eleVersionMin = dom.createElement("VersionMinor"); 				 
			eleVersionMin.setTextContent("0"); 
			eleVersion.appendChild(eleVersionMin);
			Element eleBuildMaj = dom.createElement("BuildMajor"); 				 
			eleBuildMaj.setTextContent("0"); 
			eleVersion.appendChild(eleBuildMaj);
			Element eleBuildMin = dom.createElement("BuildMinor"); 				 
			eleBuildMin.setTextContent("0"); 
			eleVersion.appendChild(eleBuildMin);
			elecreator.appendChild(eleVersion);
			eleActivity.appendChild(elecreator);
			eletcdb.appendChild(eleActivities);
			// Author
			Element eleauthor = dom.createElement("Author"); 				 
			eleauthor.setAttribute("xsi:type", "Application_t");  
			Element elename1 = dom.createElement("Name"); 				 
			elename1.setTextContent("RoomSports"); 
			//elename1.setTextContent("EDGE705");
			eleauthor.appendChild(elename1);
			Element eleBuild = dom.createElement("Build"); 				 
			Element eleVersion1 = dom.createElement("Version"); 				 
			Element eleVersionMaj1 = dom.createElement("VersionMajor"); 				 
			eleVersionMaj1.setTextContent("0"); 
			eleVersion1.appendChild(eleVersionMaj1);
			Element eleVersionMin1 = dom.createElement("VersionMinor"); 				 
			eleVersionMin1.setTextContent("0"); 
			eleVersion1.appendChild(eleVersionMin1);
			Element eleBuildMaj1 = dom.createElement("BuildMajor"); 				 
			eleBuildMaj1.setTextContent("0"); 
			eleVersion1.appendChild(eleBuildMaj1);
			Element eleBuildMin1 = dom.createElement("BuildMinor"); 				 
			eleBuildMin1.setTextContent("0"); 
			eleVersion1.appendChild(eleBuildMin1);
			eleBuild.appendChild(eleVersion1);
			Element eleType = dom.createElement("Type"); 				 
			eleType.setTextContent("Release"); 
			eleBuild.appendChild(eleType);
			eleauthor.appendChild(eleBuild);
			Element eleLangID = dom.createElement("LangID"); 				 
			eleLangID.setTextContent("NA"); 
			eleauthor.appendChild(eleLangID);
			Element elePart = dom.createElement("PartNumber"); 				 
			elePart.setTextContent("0"); 
			eleauthor.appendChild(elePart);

			eletcdb.appendChild(eleauthor);

			if (!autosave) {
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setFilterNames (new String [] {Messages.getString("Auswertung.tcxdateien")});    
				dialog.setFilterExtensions (new String [] {"*.tcx"});  
				dialog.setFilterPath (Global.strPfad);   
				TCXDatei = dialog.open();
			} else {
				TCXDatei = tcxDateiname;
			}
			Mlog.debug("TCXDatei: "+TCXDatei);
			if (TCXDatei != null) {
				StreamResult result = new StreamResult(TCXDatei); 
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.transform(source, result);
			}
		} catch (Exception e) {
			Mlog.ex(e);
		}
	}

	
	/**
	 * Auswertung der Trainingsdaten. Die auszuwertenden Daten stehen in der Protokollliste "pliste".
	 */
	public void doAuswertung(){
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));  
        tfmt = new SimpleDateFormat();
        tfmt.applyPattern("HH:mm:ss");  
        tfmttcx = new SimpleDateFormat();
        tfmttcx.applyPattern("yyyy-MM-dd");    
        
		dsLeistung.removeAllSeries();
		dsPuls.removeAllSeries();
		dsRPM.removeAllSeries();
		dsHoehe.removeAllSeries();
		dsSteigung.removeAllSeries();		
		
		if (isInitialisiert()) {
			dsLeistung.addSeries(profilDataLeistung);
			dsPuls.addSeries(profilDataPuls);
			dsRPM.addSeries(profilDataRPM);
			dsHoehe.addSeries(profilDataHoehe);
			dsSteigung.addSeries(profilDataSteigung);
			calcAuswertung();
		}

		if (Rsmain.wettkampf.isLanRace()) {
			Rsmain.wettkampf.getRaceErgebnis("1");
		}
		getGesamtTrainingsdaten();
		formatAuswertung();		
 		createAuswertung(strCol1, strCol2, strCol3, strCol4); 
		butSave.setEnabled(true); 		
	}

	/**
	 * holt die aktuellen Gesamttrainingsdaten aus dem Bikerprofil.
	 */
	private void getGesamtTrainingsdaten() {
		gesTrainingszeit = Rsmain.biker.getGesTrainingszeit();
		gesStrecke = Rsmain.biker.getGesStrecke();
		gesHM = Rsmain.biker.getGesHM();
		gesKCal = Rsmain.biker.getGesKCal();
		gesAnzahl = Rsmain.biker.getGesAnzahl();
//		gesFitnesswert = Rsmain.biker.getGesFitnesswert();
		gesPuls = Rsmain.biker.getGesPuls();
		gesRPM = Rsmain.biker.getGesRPM();
		gesLeistung = Rsmain.biker.getGesLeistung();		
	}
	
	/**
	 * Berechnet die neuen Gesamtwerte und 
	 * speichert die neuen Gesamttrainingsdaten im Biker-Profil ab.
	 * 
	 * @param getValues  Gesamtwerte ermitteln?
	 */
	public void saveGesamtTrainingsdaten(boolean getValues) {
		if (!isInitialisiert())
			return;

		if (getValues) {
			calcAuswertung();
			gesTrainingszeit = Rsmain.biker.getGesTrainingszeit() + lsek;
			gesStrecke = Rsmain.biker.getGesStrecke() + dm;
			gesHM = Rsmain.biker.getGesHM() + dhm;
			gesKCal = Rsmain.biker.getGesKCal() + dkcal;
			gesAnzahl = Rsmain.biker.getGesAnzahl() + 1;
//			gesFitnesswert = Rsmain.biker.getGesFitnesswert() + dfw;
			gesPuls = Rsmain.biker.getGesPuls() + dpulsav;
			gesRPM = Rsmain.biker.getGesRPM() + drpmav;
			gesLeistung = Rsmain.biker.getGesLeistung() + dpav;
		}

		Rsmain.biker.setGesTrainingszeit(gesTrainingszeit);
		Rsmain.biker.setGesStrecke(gesStrecke);
		Rsmain.biker.setGesHM(gesHM);
		Rsmain.biker.setGesKCal(gesKCal);
		Rsmain.biker.setGesAnzahl(gesAnzahl);
//		Rsmain.biker.setGesFitnesswert(gesFitnesswert);
		Rsmain.biker.setGesPuls(gesPuls);
		Rsmain.biker.setGesRPM(gesRPM);
		Rsmain.biker.setGesLeistung(gesLeistung);
		Rsmain.newkonfig.saveProfil(Rsmain.biker, Rsmain.thisTrainer);	
		bSave = true;
	}
}
