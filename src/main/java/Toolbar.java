import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;

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
 * Toolbar.java: Beinhaltet die Toolbar von Rsmain
 *****************************************************************************
 * 
 * Darstellung und Verwaltung der Toolbar
 *
 */
public class Toolbar {
	public  Action actionStart;
	public  Action actionOpen;
	private Action actionKonfig;
	public  Action actionStop;
	public  Action actionRace;
	private Action actionSwitch;
	private Action actionAuswertung;
	private Action actionGanghoch;
	private Action actionGangrunter;
	private Action actionProfil;
	private Action actionGegenwind;
	private Action actionRueckenwind;
	private Action actionLFhoch;
	private Action actionLFrunter;
	private Action actionVor;
	private Action actionZurueck;
	private Action actionDoku;
	private Action actionAbout;
	private Action actionExit;
	public  MenuManager menuManager;

	/**
	 * Für die Onlinetrainings und Ligarennen werden einige Optionen abgeschaltet...
	 */
	public void disableItems4Race() {
		actionOpen.setEnabled(false);		
		actionKonfig.setEnabled(false);		
		actionRace.setEnabled(false);		
		actionProfil.setEnabled(false);		
		actionGegenwind.setEnabled(false);		
		actionRueckenwind.setEnabled(false);		
		actionVor.setEnabled(false);		
		actionZurueck.setEnabled(false);
		actionLFhoch.setEnabled(false);
		actionLFrunter.setEnabled(false);
		Rsmain.cmbfahrer.setEnabled(false);
		Rsmain.cmbwind.setEnabled(false);
		Rsmain.sldvideo.setEnabled(false);
		if (Rsmain.isIbl()) {
			actionGanghoch.setEnabled(false);
			actionGangrunter.setEnabled(false);
		}
	}
	
	/**
	 * Constructor der Toolbar
	 * @param comp		Composite
	 * @param rsmain	Rsmain Klasse
	 */
	public Toolbar(Composite comp, final Rsmain rsmain) {
		menuManager = new MenuManager();

		ToolBar toolBar = new ToolBar(comp, SWT.FLAT | SWT.RIGHT);
		final ToolBarManager manager = new ToolBarManager(toolBar);
		// Adds tool bar items using actions.
		actionOpen =
				new Action(
						Messages.getString("Toolbar.tour_einlesen"), 
						ImageDescriptor.createFromImage(
								new Image(Display.getCurrent(), "open.png"))) { 
			public void run() {
				rsmain.setTour(null);
			}
		};
		actionStart =
				new Action(
						Messages.getString("Toolbar.start_pause"), 
						ImageDescriptor.createFromImage(
								new Image(Display.getCurrent(), "play.png"))) { 
			public void run() {
				rsmain.doStart();
			}
		};
		actionStart.setEnabled(false);
		actionStop =
				new Action(
						Messages.getString("Toolbar.stop"), 
						ImageDescriptor.createFromImage(
								new Image(Display.getCurrent(), "stop.png"))) { 
			public void run() {
				rsmain.doStop(true, true);
			}
		};
		actionStop.setEnabled(false);
		actionKonfig =
				new Action(
						Messages.getString("Toolbar.konfiguration"), 
						ImageDescriptor.createFromImage(
								new Image(Display.getCurrent(), "konfig.gif"))) { 
			public void run() {
				if (Rsmain.Profildatei.indexOf(Global.strPfad) == -1)
					Rsmain.Profildatei = Global.strPfad+Rsmain.Profildatei;
				Rsmain.newkonfig.show(Rsmain.biker, Rsmain.thisTrainer);
				Rsmain.setImDialog(true);
			}
		};
		actionRace =
				new Action(
						Messages.getString("Toolbar.csv_onlinetrainng"), 
						ImageDescriptor.createFromImage(
								new Image(Display.getCurrent(), "csvrace.png"))) { 			
			public void run() {
				//Rsmain.initcsvrace();
				Rsmain.wettkampf.createOnlineRennen(null);
			}
		};
		actionSwitch =
				new Action(
						Messages.getString("Toolbar.ansicht_wechseln"),  
						ImageDescriptor.createFromImage(
								new Image(Display.getCurrent(), "viewrefresh.png"))) { 

			public void run() {
				rsmain.switchComposites();
			}
		};
		actionAuswertung =
				new Action(
						Messages.getString("Toolbar.auswertung"), 
						ImageDescriptor.createFromImage(
								new Image(Display.getCurrent(), "auswert1.png"))) { 
			public void run() {
				rsmain.auswertung.doAuswertung();
			}
		};
		actionGanghoch =
				new Action(
						Messages.getString("Toolbar.gang_hoch"), 
						null) {
			public void run() {
				rsmain.changeGanghoch();
			}
		};
		actionGangrunter =
				new Action(
						Messages.getString("Toolbar.gang_runter"), 
						null) {
			public void run() {
				rsmain.changeGangrunter();
			}
		};
		actionProfil =
				new Action(
						Messages.getString("Toolbar.bikerprofil"), 
						null) {
			public void run() {
				rsmain.changeBikerprofil();
			}
		};
		actionGegenwind =
				new Action(
						Messages.getString("Toolbar.gegenwind"), 
						null) {
			public void run() {
				rsmain.changeGegenwind();
			}
		};
		actionRueckenwind =
				new Action(
						Messages.getString("Toolbar.rueckenwind"), 
						null) {
			public void run() {
				rsmain.changeRueckenwind();
			}
		};
		actionLFhoch =
				new Action(
						Messages.getString("Toolbar.leistungsfakt_gr"), 
						null) {
			public void run() {
				rsmain.changeLFhoch(0.05);
			}
		};
		actionLFrunter =
				new Action(
						Messages.getString("Toolbar.leistungsfakt_kl"), 
						null) {
			public void run() {
				rsmain.changeLFrunter(0.05);
			}
		};
		actionVor =
				new Action(
						Messages.getString("Toolbar.sprung_vor"), 
						null) {
			public void run() {
				rsmain.sliderjump(Rsmain.sliderpageinc);
			}
		};
		actionZurueck =
				new Action(
						Messages.getString("Toolbar.sprung_rueck"), 
						null) {
			public void run() {
				rsmain.sliderjump(-Rsmain.sliderpageinc);
			}
		};

		actionDoku =
				new Action(
						Messages.getString("Toolbar.doku"), 
						null) {
			public void run() {
				try {
					Runtime.getRuntime().exec( "rundll32 url.dll,FileProtocolHandler " 
							+ Global.strProgramPfad+"\\RoomSports.pdf" ); 
				} catch (IOException e) {
					Mlog.ex(e);
				}
			}
		};

		actionAbout =
				new Action(
						"&About", 
						null) {
			public void run() {
				MessageDialog.openInformation(rsmain.getShell(), "About", 
								Global.version+"\n\n" + 
								Messages.getString("Toolbar.arbeitsverzeichnis")+"\n"+Global.strPfad+"\n" +			    		
								"Trainer : "+Global.ergoVersion+"\n\n" +
								Messages.getString("Toolbar.allerechte"));   
			}
		};
		actionExit =
				new Action(
						Messages.getString("Toolbar.beenden"), 
						null) {
			public void run() {
				rsmain.close();
			}
		};

		manager.add(actionStart); 
		manager.add(actionStop);
		manager.add(actionOpen);
		manager.add(actionAuswertung);
		manager.add(actionKonfig);
		//manager.add(actionVLC);
		manager.add(actionRace);
		manager.add(actionSwitch);

		manager.update(true);

		MenuManager fileMenuManager = new MenuManager(Messages.getString("Toolbar.datei")); 
		fileMenuManager.add(actionOpen);
		fileMenuManager.add(actionRace);
		fileMenuManager.add(new Separator());
		fileMenuManager.add(actionExit);

		MenuManager ansichtMenuManager = new MenuManager(Messages.getString("Toolbar.ansicht")); 
		ansichtMenuManager.add(actionSwitch);
		ansichtMenuManager.add(actionAuswertung);
		ansichtMenuManager.add(actionKonfig);
		//ansichtMenuManager.add(actionVLC);

		MenuManager steuerungMenuManager = new MenuManager(Messages.getString("Toolbar.steuerung")); 
		steuerungMenuManager.add(actionStart);		
		steuerungMenuManager.add(actionStop);		
		steuerungMenuManager.add(new Separator());
		steuerungMenuManager.add(actionProfil);		
		steuerungMenuManager.add(new Separator());
		steuerungMenuManager.add(actionGanghoch);		
		steuerungMenuManager.add(actionGangrunter);		
		steuerungMenuManager.add(new Separator());
		steuerungMenuManager.add(actionGegenwind);		
		steuerungMenuManager.add(actionRueckenwind);		
		steuerungMenuManager.add(new Separator());
		steuerungMenuManager.add(actionLFhoch);		
		steuerungMenuManager.add(actionLFrunter);		
		steuerungMenuManager.add(new Separator());
		steuerungMenuManager.add(actionVor);		
		steuerungMenuManager.add(actionZurueck);
		MenuManager hilfeMenuManager = new MenuManager(Messages.getString("Toolbar.hilfe")); 
		hilfeMenuManager.add(actionDoku);
		hilfeMenuManager.add(new Separator());
		hilfeMenuManager.add(actionAbout);

		menuManager.add(fileMenuManager);
		menuManager.add(ansichtMenuManager);
		menuManager.add(steuerungMenuManager);
		menuManager.add(hilfeMenuManager);

		menuManager.updateAll(true);
		comp.getShell().setMenuBar(menuManager.createMenuBar((Decorations)comp.getShell()));
	}

	/**
	 * Damit kann das Symbol von Start auf Pause umgestellt werden.
	 * @param strimg
	 */
	public void changeImgStartbutton(String strimg) {
		actionStart.setImageDescriptor(ImageDescriptor.createFromImage(new Image(Display.getCurrent(), strimg)));
	}

	@SuppressWarnings("unused")
	private void init() {
	}

}

