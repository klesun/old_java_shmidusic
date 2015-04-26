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
	JScrollPane scrollBar;

	public int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?
	public int MARGIN_H = 1; // TODO: move it into Constants class maybe?
	final public static int SISDISPLACE = 40;

	ArrayList<Staff> staffList = new ArrayList();
	public Window parentWindow = null;
		
	public SheetPanel(Window parent) {
		this.parentWindow = parent;
		this.addNewStaff();
		repaint();
	}

	public int getTotalRowCount() {
		// TODO: only when one Staff
		return this.getStaffList().get(0).getAccordRowList().size();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		int highestLineY = this.getMarginY();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		int gPos = this.getMarginX() + 3 * this.dx();
		
		for (Staff stave: this.getStaffList()) {
			stave.drawOn(g, gPos - dx(), highestLineY);
		}
	}

	public int getFocusedSystemY() {
		return SISDISPLACE * dy() * (getFocusedStaff().getFocusedIndex() / getFocusedStaff().getAccordInRowCount());
	}
	
	public void checkCam() {
		JScrollBar vertical = scrollBar.getVerticalScrollBar();
		if (vertical.getValue() + parentWindow.getHeight() < getFocusedSystemY() + SISDISPLACE * dy() ||
			vertical.getValue() > getFocusedSystemY()) {
			vertical.setValue(getFocusedSystemY());
		}
		this.setPreferredSize(new Dimension(this.getWidth() - 20, this.getTotalRowCount() * SISDISPLACE * this.dy()));	//	Needed for the scrollBar bars to appear
		this.revalidate();	//	Needed to recalc the scrollBar bars

		this.repaint();
	}
	
	public void page(int pageCount) {
		JScrollBar vertical = scrollBar.getVerticalScrollBar();
		int pos = vertical.getValue()+pageCount*SISDISPLACE* this.dy();
		if (pos<0) pos = 0;
		if (pos>vertical.getMaximum()) pos = vertical.getMaximum();
		vertical.setValue(pos);
		repaint();
	}

	// getters/setters

	public ArrayList<Staff> getStaffList() { return this.staffList; }
	public Staff getFocusedStaff() {
		// TODO: do something
		return this.getStaffList().get(0);
	}

	public SheetPanel addNewStaff() {
		this.staffList.add(new Staff(this));
		return this;
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

