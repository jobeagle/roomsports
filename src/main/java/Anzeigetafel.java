import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

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
 * Anzeigetafel.java: Klasse für die Darstellung der Wertedisplays
 *****************************************************************************
 */
public class Anzeigetafel {
	private Composite comp;
	private Composite farbrahm;
	private Label lblheader;
	private Label lblwert;
	private Label lblwert1;
	private Label lblwert2;
	private Label lblwert3;
	private Label lblwert4;
	private Label lbllabel1;
	private Label lbllabel2;
	private Label lbllabel3;
	private Label lbllabel4;
    private static Display display;  

	/**
	 * @return the lblheader
	 */
	public Label getLblheader() {
		return lblheader;
	}

	/**
	 * @param lblheader the lblheader to set
	 */
	public void setLblheader(Label lblheader) {
		this.lblheader = lblheader;
	}

	/**
	 * @return the lblwert
	 */
	public Label getLblwert() {
		return lblwert;
	}

	/**
	 * @param lblwert the lblwert to set
	 */
	public void setLblwert(Label lblwert) {
		this.lblwert = lblwert;
	}

	/**
	 * @return the lblwert1
	 */
	public Label getLblwert1() {
		return lblwert1;
	}

	/**
	 * @param lblwert1 the lblwert1 to set
	 */
	public void setLblwert1(Label lblwert1) {
		this.lblwert1 = lblwert1;
	}

	/**
	 * @return the lblwert2
	 */
	public Label getLblwert2() {
		return lblwert2;
	}

	/**
	 * @param lblwert2 the lblwert2 to set
	 */
	public void setLblwert2(Label lblwert2) {
		this.lblwert2 = lblwert2;
	}

	/**
	 * @return the lblwert3
	 */
	public Label getLblwert3() {
		return lblwert3;
	}

	/**
	 * @param lblwert3 the lblwert3 to set
	 */
	public void setLblwert3(Label lblwert3) {
		this.lblwert3 = lblwert3;
	}

	/**
	 * @return the lblwert4
	 */
	public Label getLblwert4() {
		return lblwert4;
	}

	/**
	 * @param lblwert4 the lblwert4 to set
	 */
	public void setLblwert4(Label lblwert4) {
		this.lblwert4 = lblwert4;
	}

	/**
	 * @return the lbllabel1
	 */
	public Label getLbllabel1() {
		return lbllabel1;
	}

	/**
	 * @param lbllabel1 the lbllabel1 to set
	 */
	public void setLbllabel1(Label lbllabel1) {
		this.lbllabel1 = lbllabel1;
	}

	/**
	 * @return the lbllabel2
	 */
	public Label getLbllabel2() {
		return lbllabel2;
	}

	/**
	 * @param lbllabel2 the lbllabel2 to set
	 */
	public void setLbllabel2(Label lbllabel2) {
		this.lbllabel2 = lbllabel2;
	}

	/**
	 * @return the lbllabel3
	 */
	public Label getLbllabel3() {
		return lbllabel3;
	}

	/**
	 * @param lbllabel3 the lbllabel3 to set
	 */
	public void setLbllabel3(Label lbllabel3) {
		this.lbllabel3 = lbllabel3;
	}

	/**
	 * @return the lbllabel4
	 */
	public Label getLbllabel4() {
		return lbllabel4;
	}

	/**
	 * @param lbllabel4 the lbllabel4 to set
	 */
	public void setLbllabel4(Label lbllabel4) {
		this.lbllabel4 = lbllabel4;
	}

	/**
	 * Initialisierung der Komponenten des Anzeigedisplays
	 * @param pcomp		Composite auf dem das Display angezeigt werden soll
	 * @param pheader	Überschrift (Fett)
	 * @param plabel1	1. Label (links oben)
	 * @param plabel2	2. Label (rechts oben)
	 * @param plabel3	3. Label (links unten)
	 * @param plabel4	4. Label (rechts unten)
	 * @param pcolor	Farbe des Displays
	 */
	public void InitAnzeigetafel(Composite pcomp, String pheader, String plabel1, String plabel2, String plabel3, String plabel4, 
			                     Color pcolor) {
        FontData wertFont = new FontData("Arial",(int) (32/Global.resolutionFactor),SWT.BOLD); 
        FontData wertKlFont = new FontData("Arial",(int) (14/Global.resolutionFactor),SWT.BOLD); 
        FontData headerFont = new FontData("Arial",(int) (10/Global.resolutionFactor),SWT.BOLD); 
        FontData labelFont = new FontData("Arial",(int) (8/Global.resolutionFactor),SWT.NORMAL); 
        display = Display.getDefault();
		
		comp = pcomp;
		comp.setBackground(pcolor);
		
		farbrahm = new Composite(comp, SWT.NONE);
		farbrahm.setBounds(0, 20, 156, 48);
		farbrahm.setBackground(pcolor);
		
		lblheader = new Label(comp, SWT.BORDER|SWT.CENTER);
		lblheader.setFont(new Font(display, headerFont));
		lblheader.setText(pheader);
		lblheader.setBounds(0, 0, 154, 19);
		lblheader.setBackground(pcolor);
		
		lblwert = new Label(farbrahm, SWT.BORDER|SWT.CENTER);		
		lblwert.setFont(new Font(display, wertFont));
		lblwert.setBounds(0, 0, 154, 48);
		lblwert.setBackground(pcolor);

		// Labels
		lbllabel1 = new Label(comp, SWT.NONE);
		lbllabel1.setFont(new Font(display, labelFont));
		lbllabel1.setText(plabel1);		
		lbllabel1.setBounds(0, 72, 28, 16);
		lbllabel1.setBackground(pcolor);

		lbllabel2 = new Label(comp, SWT.NONE);
		lbllabel2.setFont(new Font(display, labelFont));
		lbllabel2.setText(plabel2);		
		lbllabel2.setBounds(81, 72, 28, 16);
		lbllabel2.setBackground(pcolor);

		lbllabel3 = new Label(comp, SWT.NONE);
		lbllabel3.setFont(new Font(display, labelFont));
		lbllabel3.setText(plabel3);		
		lbllabel3.setBounds(0, 92, 28, 16);
		lbllabel3.setBackground(pcolor);

		lbllabel4 = new Label(comp, SWT.NONE);
		lbllabel4.setFont(new Font(display, labelFont));
		lbllabel4.setText(plabel4);		
		lbllabel4.setBounds(81, 92, 28, 16);
		lbllabel4.setBackground(pcolor);

		// Werte
		lblwert1 = new Label(comp, SWT.NONE);  // SWT.BORDER
		lblwert1.setFont(new Font(display, wertKlFont));
		lblwert1.setAlignment(SWT.RIGHT);
		lblwert1.setBounds(30, 68, 50, 22);
		lblwert1.setBackground(pcolor);

		lblwert2 = new Label(comp, SWT.NONE);
		lblwert2.setFont(new Font(display, wertKlFont));
		lblwert2.setAlignment(SWT.RIGHT);
		lblwert2.setBounds(106, 68, 45, 22);
		lblwert2.setBackground(pcolor);
		
		lblwert3 = new Label(comp, SWT.NONE);
		lblwert3.setFont(new Font(display, wertKlFont));
		lblwert3.setAlignment(SWT.RIGHT);
		lblwert3.setBounds(30, 88, 50, 22);
		lblwert3.setBackground(pcolor);
		
		lblwert4 = new Label(comp, SWT.NONE);
		lblwert4.setFont(new Font(display, wertKlFont));
		lblwert4.setAlignment(SWT.RIGHT);
		lblwert4.setBounds(106, 88, 45, 22);
		lblwert4.setBackground(pcolor);
	}

	/**
	 * Wert auf Anzeigetafel ausgeben.
	 * Die Anzeigetafel ist wie folgt aufgeteilt:
	 * 			0
	 * 		1		2
	 * 		3		4
	 * 0 ist der Hauptwert und wird groß und mit Farbe angezeigt.
	 * @param pind		Index des Werts (0=Groß ..4)
	 * @param pwert		Wert der angezeigt werden soll
	 * @param farbe		Farbindex als int
	 */
	public void show(int pind, String pwert, int farbe) {
		switch (pind) {
		case 0:		// Hauptwert 
			lblwert.setText(pwert);
			lblwert.setBackground(farbrahm.getBackground());
			if (farbe != 0)
				farbrahm.setBackground(display.getSystemColor(farbe));
			else
				farbrahm.setBackground(Rsmain.getRahmenfarbe());
				
			farbrahm.pack();

			break;
			
		case 1: 
			lblwert1.setText(pwert);
			break;
			
		case 2: 
			lblwert2.setText(pwert);
			break;
			
		case 3: 
			lblwert3.setText(pwert);
			break;
			
		case 4: 
			lblwert4.setText(pwert);
			break;
			
		}
	}

	/**
	 * Wert von Anzeigetafel lesen
	 * @param pind		Index des Werts (0=Groß ..4)
	 * @return angezeigter Wert
	 */
	public String read(int pind) {
		switch (pind) {
		case 0:		// Hauptwert 
			return (lblwert.getText());
			
		case 1: 
			return (lblwert1.getText());
			
		case 2: 
			return (lblwert2.getText());
			
		case 3: 
			return (lblwert3.getText());
			
		case 4: 
			return (lblwert4.getText());

		default:
			return ("0");
			
		}
	}
	
	/**
	 * Enabled-Flag eines Werts auf der Anzeigetafel setzen
	 * @param pind  Index des Wertes (0..4)
	 * @param pflag enable/disable
	 */
	public void setEnabled(int pind, boolean pflag) {
		switch (pind) {
		case 0:		// Hauptwert 
			lblwert.setEnabled(pflag);
			break;
			
		case 1: 
			lblwert1.setEnabled(pflag);
			break;
			
		case 2: 
			lblwert2.setEnabled(pflag);
			break;
			
		case 3: 
			lblwert3.setEnabled(pflag);
			break;
			
		case 4: 
			lblwert4.setEnabled(pflag);
			break;
		}
		
	}
	
	/**
	 * Enabled-Flag eines Elements von der Anzeigetafel lesen.
	 * @param pind Index des Wertes (0..4)
	 * @return true oder false
	 */
	public boolean getEnabled(int pind) {
		switch (pind) {
		case 0:		// Hauptwert 
			return (lblwert.getEnabled());
			
		case 1: 
			return (lblwert1.getEnabled());
			
		case 2: 
			return (lblwert2.getEnabled());
			
		case 3: 
			return (lblwert3.getEnabled());
			
		case 4: 
			return (lblwert4.getEnabled());

		default:
			return (false);
			
		}		
	}
	
	/**
	 * Setzt die Farbe hinter dem Wert
	 * @param pcolor Farbe
	 */
	public void setcolor(Color pcolor) {
		farbrahm.setBackground(pcolor);
		farbrahm.pack();
	}

	/**
	 * Label auf Anzeigetafel ändern
	 * @param pind		Index des Labels (entspr. Wert: 1..4)
	 * @param plabel	Label das geändert werden soll
	 */
	public void Label(int pind, String plabel) {		
		switch (pind) {
		case 1: 
			lbllabel1.setText(plabel);
			break;
			
		case 2: 
			lbllabel2.setText(plabel);
			break;
			
		case 3: 
			lbllabel3.setText(plabel);
			break;
			
		case 4: 
			lbllabel4.setText(plabel);
			break;			
		}
	}
	
	/**
	 * Farbe der Anzeigetafel ändern.
	 * @param pcomp  Composite
	 * @param pcolor neue Farbe
	 */
	public void SetColorAnzeigetafel(Composite pcomp, Color pcolor) {
		comp = pcomp;
		comp.setBackground(pcolor);

		farbrahm.setBackground(pcolor);
		lblheader.setBackground(pcolor);
		lblwert.setBackground(pcolor);
		lbllabel1.setBackground(pcolor);
		lbllabel2.setBackground(pcolor);
		lbllabel3.setBackground(pcolor);
		lbllabel4.setBackground(pcolor);
		lblwert1.setBackground(pcolor);
		lblwert2.setBackground(pcolor);
		lblwert3.setBackground(pcolor);
		lblwert4.setBackground(pcolor);
	}
}
