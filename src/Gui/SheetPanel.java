package Gui;

import Model.IModel;
import Model.Staff.Staff;
import Model.Staff.Accord.Nota.Nota;

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


final public class SheetPanel extends JPanel implements IModel {
	JScrollPane scroll;
	
	final public static int NORMAL_HEIGHT = 40;
	final public static int NORMAL_WIDTH = 25;

	public int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?
	public int MARGIN_H = 1; // TODO: move it into Constants class maybe?
	int maxy = 0;
	final public static int SISDISPLACE = 40;
	
	public static BufferedImage[] vseKartinki = new BufferedImage[7]; // TODO: поменять этот уродский массив на ключ-значение // Гузно у тебя уродское
	public static BufferedImage[] vseKartinki0 = new BufferedImage[7];

	int curAccord = -2;

	private Boolean surfaceChanged = true;
	private BufferedImage surface = null;
	
	ArrayList<Staff> staffList = new ArrayList();
	public Window parentWindow = null;
		
	public SheetPanel(Window parent) {
		this.parentWindow = parent;

	    System.out.println(getClass().getResource("../").getPath());
	    try {	vseKartinki[0] = ImageIO.read(new File("../imgs/vio_sized.png"));
				vseKartinki[1] = ImageIO.read(new File("../imgs/bass_sized.png"));
				vseKartinki[2] = ImageIO.read(new File("../imgs/flat_sized.png"));
				vseKartinki[6] = ImageIO.read(new File("../imgs/sharp_sized.png")); // -_-
				vseKartinki[3] = ImageIO.read(new File("../imgs/MyPointer.png"));
				vseKartinki[4] = ImageIO.read(new File("../imgs/volume.png"));
				vseKartinki[5] = ImageIO.read(new File("../imgs/instrument.png"));
	    } catch (IOException e) { e.printStackTrace(); System.out.println("Темнишь что-то со своей картинкой..."); }
	    for (int i = 0; i < vseKartinki.length; ++i ) {
	        vseKartinki0[i] = vseKartinki[i];
	        vseKartinki[i] = changeSize(i);
	    }

		this.addNewStaff();
	}

	public int getTotalRowCount() {
		// TODO: only when one Staff
		return this.getStaffList().get(0).getAccordRowList().size();
	}
	
	@Override
	public void paintComponent(Graphics g) {

		int highestLineY = this.getMarginY();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,this.getWidth(),this.getHeight());
		int gPos = this.getMarginX() + 3 * this.dx();
		
		for (Staff stave: this.getStaffList()) {
			stave.drawOn(g, gPos - dx(), highestLineY);
		}

		// TODO: maybe move it to checkCam() and call checkCam before repaint() ???
		this.setPreferredSize(new Dimension(this.getWidth() - 20, this.getTotalRowCount() * SISDISPLACE * this.dy()));	//	Needed for the scroll bars to appear
		this.revalidate();	//	Needed to recalc the scroll bars
	}
	
	public void changeScale(int n) {
		Settings.inst().changeScale(n);
		refreshImageSizes();
	}
	
	// TODO: store (and refresh) images in Settings maybe
	public void refreshImageSizes() {
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

	public int getFocusedSystemY() {
		return SISDISPLACE * this.dy() * (getFocusedStaff().getFocusedIndex() / (getStepInOneSystemCount() / 2 - 2) -1);
	}
	
	public void checkCam() {
		JScrollBar vertical = scroll.getVerticalScrollBar();
		vertical.setValue(getFocusedSystemY());
	}
	
	public void page(int pageCount) {
		JScrollBar vertical = scroll.getVerticalScrollBar();
		int pos = vertical.getValue()+pageCount*SISDISPLACE* this.dy();
		if (pos<0) pos = 0;
		if (pos>vertical.getMaximum()) pos = vertical.getMaximum();
		vertical.setValue(pos);
		repaint();
	}

	// getters/setters

	public ArrayList<Staff> getStaffList() {
		return this.staffList;
	}

	public Staff getFocusedStaff() {
		// TODO: do something
		return this.getStaffList().get(0);
	}

	public SheetPanel addNewStaff() {
		this.staffList.add(new Staff(this));
		return this;
	}

	public BufferedImage getFlatImage() {
		return vseKartinki[2];
	}

	public BufferedImage getSharpImage() {
		return vseKartinki[6];
	}

	public int getStepInOneSystemCount() {
		return (int)Math.floor(this.getWidth() / this.dx() - 2 * this.MARGIN_H);
	}

	// TODO: use from Settings

	public int getNotaWidth() {
		return Settings.getNotaWidth();
	}

	public int getNotaHeight() {
		return Settings.getNotaHeight();
	}

	public int dx() {
		return Settings.getStepWidth();
	}

	public int dy() {
		return Settings.getStepHeight();
	}

	// Until here

	public int getMarginX() {
		return Math.round(MARGIN_H * this.dx());
	}

	public int getMarginY() {
		return Math.round(MARGIN_V * this.dy());
	}
}

