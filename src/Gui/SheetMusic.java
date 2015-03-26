package Gui;

import Model.Staff;
import Model.Accord.Accord;
import Model.Accord.Nota.Nota;
import Model.StaffConfig.StaffConfig;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;


final public class SheetMusic extends JPanel {
	JScrollPane scroll;
	
	final public static int NORMAL_HEIGHT = 40;
	final public static int NORMAL_WIDTH = 25;

	public int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?
	public int MARGIN_H = 1; // TODO: move it into Constants class maybe?
	int maxy = 0;
	int SISDISPLACE = 40;
	
	public static BufferedImage[] vseKartinki = new BufferedImage[7]; // TODO: поменять этот уродский массив на ключ-значение // Гузно у тебя уродское
	public static BufferedImage[] vseKartinki0 = new BufferedImage[7];

	int taktCount = 1;
	int curAccord = -2;

	private Boolean surfaceChanged = true;
	private BufferedImage surface = null;
	
	Staff stan;
	ArrayList<Staff> staffList = new ArrayList();
	public Window parentWindow = null;
		
	public SheetMusic(Window parent) {
		this.parentWindow = parent;

	    System.out.println(getClass().getResource("../").getPath());
	    try {	vseKartinki[0] = ImageIO.read(new File("imgs/vio_sized.png"));
				vseKartinki[1] = ImageIO.read(new File("imgs/bass_sized.png"));
				vseKartinki[2] = ImageIO.read(new File("imgs/flat_sized.png"));
				vseKartinki[6] = ImageIO.read(new File("imgs/sharp_sized.png")); // -_-
				vseKartinki[3] = ImageIO.read(new File("imgs/MyPointer.png"));
				vseKartinki[4] = ImageIO.read(new File("imgs/volume.png"));
				vseKartinki[5] = ImageIO.read(new File("imgs/instrument.png"));
	    } catch (IOException e) { e.printStackTrace(); System.out.println("Темнишь что-то со своей картинкой..."); }
	    for (int i = 0; i < vseKartinki.length; ++i ) {
	        vseKartinki0[i] = vseKartinki[i];
	        vseKartinki[i] = changeSize(i);
	    }

		this.addNewStaff();
		this.stan = this.getFocusedStaff();
	}

	public int getTotalRowCount() {
		// TODO: only when one Staff
		return this.getStaffList().get(0).getAccordRowList().size();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		for (Staff stave: this.getStaffList()) {
			int highestLineY = this.getMarginY();

			g.setColor(Color.WHITE);
			g.fillRect(0,0,this.getWidth(),this.getHeight());
			g.setColor(Color.BLUE);

			int gPos = this.getMarginX() + 4 * this.getStepWidth() -2 * this.getStepWidth();

			taktCount = 1;
			int curCislic = 0;
			int curZnamen = 1;

			drawPhantom(stave.getPhantom(), g, gPos, highestLineY);
			gPos += 2* this.getStepWidth();

			int i = 0;
			for (List<Accord> row: stave.getAccordRowList()) {
				// Переходим на новую октаву
				g.drawImage(vseKartinki[1], this.getStepWidth(), 11 * this.getStepHeight() + highestLineY +2 + i * SISDISPLACE * getStepHeight(), this);
				g.drawImage(vseKartinki[0], this.getStepWidth(), highestLineY + i * SISDISPLACE * getStepHeight() -3 * this.getStepHeight(), this);
				for (int j = 0; j < 11; ++j){
					if (j == 5) continue;
					g.drawLine(this.getMarginX(), highestLineY + i * SISDISPLACE * getStepHeight() + j* this.getStepHeight() *2, this.getWidth() - this.getMarginX()*2, highestLineY + i * SISDISPLACE * getStepHeight() + j* this.getStepHeight() *2);
				}

				int j = 0;
				for (Accord accord: row) {
					if (getFocusedStaff().getFocusedAccord() == accord) { g.drawImage(this.getPointerImage(), gPos + j * (2 * this.getStepWidth()) + getStepWidth(), highestLineY + i * SISDISPLACE * getStepHeight() - this.getStepHeight() *14, this ); }

					if (accord.getNotaList().size() > 0) {

						curCislic += accord.getShortest().getNumerator();
						curCislic = drawTaktLineIfNeeded(curCislic, curZnamen, g, gPos + j * (2 * this.getStepWidth()), highestLineY + i * SISDISPLACE * getStepHeight());
						g.drawImage(accord.getImage(), gPos + j * (2 * this.getStepWidth()), highestLineY + i * SISDISPLACE * getStepHeight() - 12 * getStepHeight(), this);
					}
					++j;
				}
				++i;
			}
			
			// TODO: maybe move it to checkCam() and call checkCam before repaint() ???
			this.setPreferredSize(new Dimension(this.getWidth() - 20, this.getTotalRowCount() * SISDISPLACE * this.getStepHeight()));	//	Needed for the scroll bars to appear

		}
		this.surfaceChanged = false;
		this.revalidate();	//	Needed to recalc the scroll bars
	}
	
	public void changeScale(int n) {
		Settings.inst().changeScale(n);
		refresh();
	}
	
	public void refresh() {
	    for (int i = 0; i < vseKartinki.length; ++i ) { // >_<
	        vseKartinki[i] = changeSize(i);
	    }
	
	    Nota.refreshSizes(this);
	    maxy = 0;
		Nota.bufInit(this);
	    repaint();
	}
	
	private BufferedImage changeSize(int idx) { // TODO
	    int w0 = vseKartinki0[idx].getWidth();
	    int h0 = vseKartinki0[idx].getHeight();
	    int w1 = w0*this.getNotaWidth()/NORMAL_WIDTH;
	    int h1 = h0*this.getNotaHeight()/NORMAL_HEIGHT;
	
	    BufferedImage tmp = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
	    Graphics g = tmp.createGraphics();
	    Image scaledImage = vseKartinki0[idx].getScaledInstance(w1, h1, Image.SCALE_SMOOTH);
	    g.drawImage(scaledImage, 0, 0, w1, h1, null);
	    g.dispose();
	    return tmp;
	}
	
	private void drawPhantom(StaffConfig phantomka, Graphics g, int xIndent, int yIndent) {
		int dX = this.getNotaWidth()/5, dY = this.getNotaHeight()*2;
		g.drawImage(phantomka.getImage(), xIndent - dX, yIndent - dY, this);
		int deltaY = 0, deltaX = 0;
		switch (phantomka.changeMe) {
			case numerator:	deltaY += 9 * this.getStepHeight(); break;
			case tempo: deltaY -= 1 * this.getStepHeight(); break;
			case instrument: deltaY += 4 * this.getStepHeight(); deltaX += this.getStepWidth() / 4; break;
			case volume: deltaY += 24 * this.getStepHeight(); break;
			default: break;
		}
		if (phantomka.parentStaff.getFocusedAccord() == null) {
			g.drawImage(this.getPointerImage(), xIndent - 7*this.getNotaWidth()/25 + deltaX, yIndent - this.getStepHeight() * 14 + deltaY, this);	
		}
	}

	public int getFocusedSystemY() {
		return SISDISPLACE * this.getStepHeight() * (getFocusedStaff().getFocusedIndex() / (getStepInOneSystemCount() / 2 - 2) -1);
	}
	
	public void checkCam() {
		JScrollBar vertical = scroll.getVerticalScrollBar();
		vertical.setValue(getFocusedSystemY());
	}
	
	public void page(int pageCount) {
		JScrollBar vertical = scroll.getVerticalScrollBar();
		int pos = vertical.getValue()+pageCount*SISDISPLACE* this.getStepHeight();
		if (pos<0) pos = 0;
		if (pos>vertical.getMaximum()) pos = vertical.getMaximum();
		vertical.setValue(pos);
		repaint();
	}
	
	private int drawTaktLineIfNeeded(int curCislic, int curZnamen, Graphics g, int xIndent, int yIndent) {
		if (curCislic >= stan.cislic) {
	    	curCislic %= stan.cislic;
	        g.setColor(Color.BLACK);
	        if (curCislic > 0) {
	            g.setColor(Color.BLUE);
	        }
	        g.drawLine(xIndent + this.getStepWidth() * 2, yIndent - this.getStepHeight() * 5, xIndent + this.getStepWidth() * 2, yIndent + this.getStepHeight() * 20);
	        g.setColor(Color.decode("0x00A13E"));
	        g.drawString(taktCount + "", xIndent + this.getStepWidth(), yIndent - this.getStepHeight() * 9);
	
	        ++taktCount;
		}
		return curCislic;
	}

	public SheetMusic requestNewSurface() {
		this.surfaceChanged = true;
		this.parentWindow.keyHandler.requestNewSurface();
		return this;
	}

	// getters/setters

	public ArrayList<Staff> getStaffList() {
		ArrayList staveList = (ArrayList)this.staffList.clone();
		staveList.add(this.stan);
		return staveList;
	}

	public Staff getFocusedStaff() {
		// TODO: do something
		return this.getStaffList().get(0);
	}

	public SheetMusic addNewStaff() {
		this.staffList.add(new Staff(this));
		return this;
	}

	public BufferedImage getPointerImage() {
		return this.vseKartinki[3];
	}

	public BufferedImage getFlatImage() {
		return this.vseKartinki[2];
	}

	public BufferedImage getSharpImage() {
		return this.vseKartinki[6];
	}

	public int getStepInOneSystemCount() {
		return (int)Math.floor(this.getWidth() / this.getStepWidth() - 2 * this.MARGIN_H);
	}

	// TODO: use from Settings

	public int getNotaWidth() {
		return Settings.inst().getNotaWidth();
	}

	public int getNotaHeight() {
		return Settings.inst().getNotaHeight();
	}

	public int getStepWidth() {
		return Settings.inst().getStepWidth();
	}

	public int getStepHeight() {
		return Settings.inst().getStepHeight();
	}

	// Until here

	public int getMarginX() {
		return (int)Math.round(MARGIN_H * this.getStepWidth());
	}

	public int getMarginY() {
		return (int)Math.round(MARGIN_V * this.getStepHeight());
	}
}

