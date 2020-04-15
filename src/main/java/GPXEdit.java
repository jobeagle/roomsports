import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
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
 * GPXEdit.java: Dialog, zum anpassen und löschen von GPS-Punkten.
 * Es kann die Höhe angepasst und eine zeitliche Verschiebung eingetragen
 * werden. Bei zeitlicher Verschiebung werden die nachfolgenden Punkte
 * ebenfalls entsprechend verschoben. 
 *****************************************************************************
 */
public class GPXEdit extends RsErwDialog {
	private Label lblGPSPunkt;
	private Label lblGPSZeit;
	private Label lblGPSZeitdiff;
	private Label lblGPSNeueHoehe;
	private Label lblGPSHoehe;
	private Label lblKartenZoom;
	private Text  txtGPSPunkt;
	private Text  txtGPSZeit;
	private Text  txtGPSHoehe;
	private Text  txtGPSZeitdiff;
	private Text  txtGPSNeueHoehe;
	private Button butGPSVorwaerts;
	private Button butGPSRueckwaerts;
	private Button butGPSWeitRueckwaerts;
	private Button butGPSWeitVorwaerts;
	private Button butGPSGoPos;
	private Button butGPSAnfang;
	private Button butGPSEnde;
	private Button chkZeigePunkte;
	private Button butSpeichern;
	public  Table  tblPosListe;
	private CCombo cmbZoom;
	private TrkPt  trkpt; 
	private String[] header = {Messages.getString("Gpxedit.indexli"), Messages.getString("Gpxedit.zeitli"), 
			Messages.getString("Gpxedit.sekli"), Messages.getString("Gpxedit.weggesli"), Messages.getString("Gpxedit.wegli"), 
			Messages.getString("Gpxedit.steigli"), Messages.getString("Gpxedit.hoeheli")}; 
	private String[] zoomfakts = {"10", "11", "12", "13", "14", "15", "16", "17", "18"};
	private int    index = 0;
	private int    weitstep = 10;
	private int    saveindex = 1;
	DecimalFormat  zfk1 = new DecimalFormat("#0.0");
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
	private OSMViewer.PointD osm_koord;
	
	/**
	 * Konstruktor des GPXEdit-Dialogs
	 */
	public GPXEdit() {
		super();					// Aufruf des RsErwDialog-Konstruktors
		this.shl.setText(Messages.getString("Gpxedit.gpxedit"));
		lblGPSPunkt = new Label(cmp, SWT.NONE);
		lblGPSPunkt.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblGPSPunkt.setText(Messages.getString("Gpxedit.gpspunkt")); 
		lblGPSPunkt.setBounds(10, 10, 90, 20);	
		Global.setFontSizeLabel(lblGPSPunkt);
		lblGPSZeit = new Label(cmp, SWT.NONE);
		lblGPSZeit.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblGPSZeit.setText(Messages.getString("Gpxedit.zeit")); 
		lblGPSZeit.setBounds(10, 40, 90, 20);	
		Global.setFontSizeLabel(lblGPSZeit);
		lblGPSHoehe = new Label(cmp, SWT.NONE);
		lblGPSHoehe.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblGPSHoehe.setText(Messages.getString("Gpxedit.hoehe")); 
		lblGPSHoehe.setBounds(10, 70, 90, 20);	
		Global.setFontSizeLabel(lblGPSHoehe);
		lblGPSZeitdiff = new Label(cmp, SWT.NONE);
		lblGPSZeitdiff.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblGPSZeitdiff.setText(Messages.getString("Gpxedit.zeitdiff")); 
		lblGPSZeitdiff.setBounds(180, 40, 120, 20);	
		Global.setFontSizeLabel(lblGPSZeitdiff);
		lblGPSNeueHoehe = new Label(cmp, SWT.NONE);
		lblGPSNeueHoehe.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblGPSNeueHoehe.setText(Messages.getString("Gpxedit.neuehoehe")); 
		lblGPSNeueHoehe.setBounds(180, 70, 120, 20);	
		Global.setFontSizeLabel(lblGPSNeueHoehe);
		lblKartenZoom = new Label(cmp, SWT.NONE);
		lblKartenZoom.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblKartenZoom.setText(Messages.getString("Gpxedit.zoom")); 
		lblKartenZoom.setBounds(10, 100, 90, 20);	
		Global.setFontSizeLabel(lblKartenZoom);
		
		txtGPSPunkt = new Text(cmp, SWT.BORDER);
		txtGPSPunkt.setToolTipText(Messages.getString("Gpxedit.editgpspunkt"));
		txtGPSPunkt.setBounds(100, 10, 60, 20);
		txtGPSPunkt.setOrientation(SWT.RIGHT_TO_LEFT);
		txtGPSPunkt.addVerifyListener(Global.VLZahlen);
		Global.setFontSizeText(txtGPSPunkt);
		txtGPSPunkt.addTraverseListener(new TraverseListener() {
					public void keyTraversed(TraverseEvent e) {
						if (e.detail == SWT.TRAVERSE_RETURN) {
							int indtmp = new Integer(txtGPSPunkt.getText());
							if (indtmp <= VerwaltungGPX.track.size() && indtmp >= 0) {
								index = indtmp;
								goPos(index);
							}								
						}
			        }
			    });
		txtGPSZeit = new Text(cmp, SWT.BORDER);
		txtGPSZeit.setToolTipText(Messages.getString("Gpxedit.gpszeit"));
		txtGPSZeit.setBounds(100, 40, 60, 20);
		txtGPSZeit.setOrientation(SWT.RIGHT_TO_LEFT);
		txtGPSZeit.setEditable(false);
		Global.setFontSizeText(txtGPSZeit);

		txtGPSZeitdiff = new Text(cmp, SWT.BORDER);
		txtGPSZeitdiff.setToolTipText(Messages.getString("Gpxedit.zeitdiff_tp"));
		txtGPSZeitdiff.setBounds(300, 40, 40, 20);
		txtGPSZeitdiff.setOrientation(SWT.RIGHT);
		txtGPSZeitdiff.addVerifyListener(Global.VLZahlenUndMinus);
		Global.setFontSizeText(txtGPSZeitdiff);
		txtGPSZeitdiff.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					int neueZeitDiff = new Integer(txtGPSZeitdiff.getText());
					if (neueZeitDiff != 0) {
						// Mlog.debug("für Zeitpunkte ab Punkt: "+txtGPSPunkt.getText()+" Zeitdiff: "+neueZeitDiff);
						if (!VerwaltungGPX.reCalcZeitenAbIndex(index, neueZeitDiff))
							Messages.errormessage(Messages.getString("Gpxedit.zeitdifferr"));
						tblPosListe.removeAll();
						fillPosListe();
						goPos(index);
					}
				}
			}
		});
		
		txtGPSHoehe = new Text(cmp, SWT.BORDER);
		txtGPSHoehe.setToolTipText(Messages.getString("Gpxedit.hoehe_tp"));
		txtGPSHoehe.setBounds(100, 70, 60, 20);
		txtGPSHoehe.setOrientation(SWT.RIGHT_TO_LEFT);
		txtGPSHoehe.setEditable(false);
		Global.setFontSizeText(txtGPSHoehe);

		txtGPSNeueHoehe = new Text(cmp, SWT.BORDER);
		txtGPSNeueHoehe.setToolTipText(Messages.getString("Gpxedit.neuehoehe_tp"));
		txtGPSNeueHoehe.setBounds(300, 70, 40, 20);
		txtGPSNeueHoehe.setOrientation(SWT.RIGHT);
		txtGPSNeueHoehe.addVerifyListener(Global.VLZahlenPunktMinus);
		Global.setFontSizeText(txtGPSNeueHoehe);
		txtGPSNeueHoehe.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					double neueHoehe = new Double(txtGPSNeueHoehe.getText());
					TrkPt trkpt = VerwaltungGPX.track.get(index);
					trkpt.setHoehe(neueHoehe);
					trkpt.setSteigung_proz(VerwaltungGPX.calcSteigung(trkpt, VerwaltungGPX.track.get(index-1).getHoehe(), VerwaltungGPX.track.get(index-1).getSteigung_proz(), false));
					trkpt.setSteigungAv_proz(VerwaltungGPX.calcSteigung(trkpt, VerwaltungGPX.track.get(index-1).getHoehe(), VerwaltungGPX.track.get(index-1).getSteigung_proz(), true));
					if (index < VerwaltungGPX.track.size() - 1) {
						trkpt = VerwaltungGPX.track.get(index + 1);
						trkpt.setSteigung_proz(VerwaltungGPX.calcSteigung(trkpt, VerwaltungGPX.track.get(index).getHoehe(), VerwaltungGPX.track.get(index).getSteigung_proz(), false));
						trkpt.setSteigungAv_proz(VerwaltungGPX.calcSteigung(trkpt, VerwaltungGPX.track.get(index).getHoehe(), VerwaltungGPX.track.get(index).getSteigung_proz(), true));						
					}
					tblPosListe.removeAll();
					fillPosListe();
					goPos(index);
			    }
			}
		});
		
		butGPSRueckwaerts = new Button(cmp, SWT.NONE);
		butGPSRueckwaerts.setImage(new Image(Display.getCurrent(), "pfeil1links.png")); 
		butGPSRueckwaerts.setToolTipText(Messages.getString("Gpxedit.gpsback")); 
		butGPSRueckwaerts.setBounds(250, 10, 20, 20);
		Global.setFontSizeButton(butGPSRueckwaerts);
		butGPSRueckwaerts.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (index > 0)
					tblPosListe.deselect(index);
					goPos(--index);
			}
		});
		butGPSVorwaerts = new Button(cmp, SWT.NONE);
		butGPSVorwaerts.setImage(new Image(Display.getCurrent(), "pfeil1rechts.png")); 
		butGPSVorwaerts.setToolTipText(Messages.getString("Gpxedit.gpsvor")); 
		butGPSVorwaerts.setBounds(300, 10, 20, 20);
		Global.setFontSizeButton(butGPSVorwaerts);
		butGPSVorwaerts.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (index < VerwaltungGPX.track.size())
					tblPosListe.deselect(index);
					goPos(++index);
			}
		});
		butGPSWeitRueckwaerts = new Button(cmp, SWT.NONE);
		butGPSWeitRueckwaerts.setImage(new Image(Display.getCurrent(), "pfeil2links.png")); 
		butGPSWeitRueckwaerts.setToolTipText(Messages.getString("Gpxedit.gpsweitback")); 
		butGPSWeitRueckwaerts.setBounds(225, 10, 20, 20);
		Global.setFontSizeButton(butGPSWeitRueckwaerts);
		butGPSWeitRueckwaerts.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (index > weitstep) {
					tblPosListe.deselect(index);
					index -= weitstep;
					goPos(index);
				}
			}
		});
		butGPSWeitVorwaerts = new Button(cmp, SWT.NONE);
		butGPSWeitVorwaerts.setImage(new Image(Display.getCurrent(), "pfeil2rechts.png")); 
		butGPSWeitVorwaerts.setToolTipText(Messages.getString("Gpxedit.gpsweitvor")); 
		butGPSWeitVorwaerts.setBounds(325, 10, 20, 20);
		Global.setFontSizeButton(butGPSWeitVorwaerts);
		butGPSWeitVorwaerts.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (index < VerwaltungGPX.track.size() - weitstep) {
					tblPosListe.deselect(index);
					index += weitstep;
					goPos(index);
				}
			}
		});
		butGPSGoPos = new Button(cmp, SWT.NONE);
		butGPSGoPos.setImage(new Image(Display.getCurrent(), "pfeiloben.png")); 
		butGPSGoPos.setToolTipText(Messages.getString("Gpxedit.gpspos")); 
		butGPSGoPos.setBounds(275, 10, 20, 20);
		Global.setFontSizeButton(butGPSGoPos);
		butGPSGoPos.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tblPosListe.deselect(index);
				index = new Integer(txtGPSPunkt.getText());
				goPos(index);
			}
		});
		butGPSAnfang = new Button(cmp, SWT.NONE);
		butGPSAnfang.setImage(new Image(Display.getCurrent(), "goanfang.png")); 
		butGPSAnfang.setToolTipText(Messages.getString("Gpxedit.gpsbeginn")); 
		butGPSAnfang.setBounds(200, 10, 20, 20);
		Global.setFontSizeButton(butGPSAnfang);
		butGPSAnfang.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tblPosListe.deselect(index);
				index = 0;
				goPos(index);
			}
		});
		butGPSEnde = new Button(cmp, SWT.NONE);
		butGPSEnde.setImage(new Image(Display.getCurrent(), "goende.png")); 
		butGPSEnde.setToolTipText(Messages.getString("Gpxedit.gpsende")); 
		butGPSEnde.setBounds(350, 10, 20, 20);
		Global.setFontSizeButton(butGPSEnde);
		butGPSEnde.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tblPosListe.deselect(index);
				index = VerwaltungGPX.track.size() - 1;
				goPos(index);
			}
		});
		
		chkZeigePunkte = new Button(cmp, SWT.CHECK);
		chkZeigePunkte.setText("Kartenpunkte anzeigen"); 
		chkZeigePunkte.setToolTipText(Messages.getString("Gpxedit.gpszeigepunkte")); 
		chkZeigePunkte.setBounds(180, 100, 140, 20);
		Global.setFontSizeButton(chkZeigePunkte);
		chkZeigePunkte.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Rsmain.osmv.setZeigePunkte(chkZeigePunkte.getSelection());
				Rsmain.osmv.redraw();
			}
		});

		tblPosListe = new Table(cmp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK);
		tblPosListe.setBounds(10, 150, 450, 400);
		tblPosListe.setHeaderVisible(true);
		tblPosListe.setLinesVisible(true);
		Global.setFontSizeTable(tblPosListe);
		tblPosListe.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (tblPosListe.getSelectionIndex() >= 0)
					index = tblPosListe.getSelectionIndex();
				goPos(index);
			}
		});
		tblPosListe.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					for (int i = 0; i < tblPosListe.getItemCount(); i++) {
						if (tblPosListe.getItem(i).getChecked()) {
							VerwaltungGPX.track.remove(i);
						}
					}
					VerwaltungGPX.reCalcTrackNachDel();
					tblPosListe.removeAll();
					fillPosListe();
					goPos(index);
				}
			}
		});
		cmbZoom = new CCombo(cmp, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		cmbZoom.setItems(zoomfakts);
		cmbZoom.setToolTipText(Messages.getString("Gpxedit.gpszoom_tp"));  
		cmbZoom.clearSelection();
		cmbZoom.setBounds(100, 100, 60, 20);
		cmbZoom.select(8);               // Default: höchste Zoomstufe
		Global.setFontSizeCCombo(cmbZoom);
		cmbZoom.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(SelectionEvent e) {
				Rsmain.osmv.setZoom(new Integer(cmbZoom.getText()));
				Rsmain.osmv.setCenterPosition(Rsmain.osmv.computePosition(osm_koord));
		    	Rsmain.osmv.redraw();
		    }
		});

		butSpeichern = new Button(cmp, SWT.NONE);
		butSpeichern.setText("Speichern");  
		butSpeichern.setBounds(weite-197, hoehe-65, 60, 25);
		Global.setFontSizeButton(butSpeichern);
		butSpeichern.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String tourdatei = Global.strTourvideo.substring(0, Global.strTourvideo.indexOf('.')) + "_" + saveindex++;
				// Mlog.debug("GPX Datei speichern: "+tourdatei);
				VerwaltungGPX.createGPXAusTrack(tourdatei);
			}
		});

    	osm_koord = new OSMViewer.PointD(Rsmain.rs_lon, Rsmain.rs_lat);		// OSM Koordinaten erzeugen

    	fillPosListe();
		
		goPos(index);
	}

	/**
	 * Füllt die Tabelle der GPS-Positionen
	 */
	private void fillPosListe() {	
		// Header füllen
		for (int i = 0; i < header.length; i++) {
			TableColumn column = new TableColumn(tblPosListe, SWT.BOLD | SWT.RIGHT);
			column.setText(header[i]);
			tblPosListe.getColumn(i).pack();
		}
		for (int i = 0; i < VerwaltungGPX.track.size(); i++) {
			TableItem item = new TableItem(tblPosListe, SWT.RIGHT);
        	trkpt = VerwaltungGPX.track.get(i);
        	item.setText(0, trkpt.getIndex()-1 + "");
        	item.setText(1, sdf.format(trkpt.getZeitpunkt()));
        	item.setText(2, trkpt.getAktsek() + "");
        	item.setText(3, zfk1.format(trkpt.getAbstand_m()));
        	item.setText(4, zfk1.format(trkpt.getAbstvorg_m()));
        	item.setText(5, zfk1.format(trkpt.getSteigung_proz()));
        	item.setText(6, trkpt.getHoehe() + "");
        }
	}
	
	/**
	 * lade GPS-Punkt aus Klasse LoadGPXFile und positioniere auf Video und Karte
	 * @param posindex  Index des GPS-Punktes
	 */
	private void goPos(int posindex) {
		//Mlog.debug("GoPos <posindex>"+posindex);
		txtGPSPunkt.setText(""+posindex);
		trkpt = VerwaltungGPX.track.get(posindex);
		txtGPSZeit.setText(""+trkpt.getAnzsek());
		txtGPSHoehe.setText(""+trkpt.getHoehe());
		tblPosListe.setSelection(posindex);

		osm_koord.x = trkpt.getLongitude();
		osm_koord.y = trkpt.getLatitude();

		if (Rsmain.aktstatus == Rsmain.Status.laeuft || Rsmain.aktstatus == Rsmain.Status.angehalten) {
			Rsmain.libvlc.libvlc_media_player_set_time(Rsmain.mediaplayer, (long) (trkpt.getAnzsek() * 1000));
			Rsmain.libvlc.libvlc_media_player_set_pause(Rsmain.mediaplayer, 1);

			Rsmain.osmv.setZoom(new Integer(cmbZoom.getText()));
			Rsmain.osmv.setCenterPosition(Rsmain.osmv.computePosition(osm_koord));
	    	Rsmain.osmv.redraw();
		} else {
			Messages.infomessage(Messages.getString("Gpxedit.gpsstarttour"));	
			doAbbruch();
		}
		Rsmain.toolbar.changeImgStartbutton("play.png");
		Rsmain.pausetaste = false;
	}
	
	@Override
	void doOk() {
		String tourdatei = Global.strTourvideo.substring(0, Global.strTourvideo.indexOf('.')) + "_" + saveindex++;
		VerwaltungGPX.createGPXAusTrack(tourdatei);
		setErgebnisOK(true);
		shl.setVisible(false);
		Rsmain.setImDialog(false);
	}

	@Override
	void doAbbruch() {
		setErgebnisOK(false);
		shl.setVisible(false);
		Rsmain.setImDialog(false);
	}
	
	@Override
	public void on(int weite, int hoehe) {
		butSpeichern.setBounds(weite-197, hoehe-65, 60, 25);
		super.on(weite, hoehe);
	}
}
