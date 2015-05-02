package Gui;

import Midi.DumpReceiver;
import Model.Combo;
import Model.Staff.Staff;
import test.ResizableScrollPane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.*;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;


final public class SheetPanel extends JPanel {
	JScrollPane scrollBar;

	public int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?
	public static int MARGIN_H = 1; // TODO: move it into Constants class maybe?
	final public static int SISDISPLACE = 40;

	public Window parentWindow = null;
	public BlockHandler handler = null;

	public int focusedIndex = -1;
		
	public SheetPanel(Window parent) {
		this.parentWindow = parent;
		this.scrollBar = new ResizableScrollPane(this);
		this.scrollBar.getVerticalScrollBar().setUnitIncrement(16); // 16 вероятно как-то связано с размером картинки ноты

		handler = new BlockHandler(this);
		this.addKeyListener(handler);

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				DumpReceiver.eventHandler = handler;
				scrollBar.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3));
			}
			@Override
			public void focusLost(FocusEvent e) {
				DumpReceiver.eventHandler = null;
				scrollBar.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
			}
		});

		this.setFocusable(true);

		repaint();
	}

	public int getTotalRowCount() {
		// TODO: only when one Staff
		return getFocusedStaff() != null
				? getFocusedStaff().getAccordRowList().size()
				: 0;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		int highestLineY = this.getMarginY();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		int gPos = this.getMarginX() + 3 * this.dx();

		if (getFocusedStaff() != null) { getFocusedStaff().drawOn(g, gPos - dx(), highestLineY); }
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
	
	public void page(Combo combo) {
		JScrollBar vertical = scrollBar.getVerticalScrollBar();
		int pos = vertical.getValue() + combo.getSign() * SISDISPLACE * this.dy();
		if (pos<0) pos = 0;
		if (pos>vertical.getMaximum()) pos = vertical.getMaximum();
		vertical.setValue(pos);
		repaint();
	}

	// getters/setters

	public Staff getFocusedStaff() {
		// TODO: do something
		return this.focusedIndex > -1
				? parentWindow.staffList.get(this.focusedIndex)
				: null;
	}

	public SheetPanel setFocusedIndex(int index) {
		this.focusedIndex = index;
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

	public static int getMarginX() {
		return Math.round(MARGIN_H * Settings.getStepWidth());
	}
	public int getMarginY() {
		return Math.round(MARGIN_V * this.dy());
	}

}

