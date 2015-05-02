package Model.Panels;

import Gui.Settings;
import Midi.DumpReceiver;
import Model.AbstractModel;
import Model.Combo;
import Model.IHandlerContext;
import Model.Staff.Staff;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.*;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;


final public class SheetPanel extends JPanel implements IHandlerContext {
//	public JScrollPane scrollBar;

	public int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?
	public static int MARGIN_H = 1; // TODO: move it into Constants class maybe?
	final public static int SISDISPLACE = 40;

	public Window parentWindow = null;
	public BlockHandler handler = null;
	private Staff staff = null;

	public SheetPanel(Window parent) {
		this.parentWindow = parent;
		this.staff = new Staff(this);

		handler = new BlockHandler(this);
		this.addKeyListener(handler);

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				DumpReceiver.eventHandler = handler;
				getScrollPane().setBorder(BorderFactory.createLineBorder(Color.GRAY, 3));
			}

			@Override
			public void focusLost(FocusEvent e) {
				DumpReceiver.eventHandler = null;
				getScrollPane().setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
			}
		});

		this.setFocusable(true);
		this.requestFocus();

		repaint();
	}

	public int getTotalRowCount() { return getStaff().getAccordRowList().size(); }
	
	@Override
	public void paintComponent(Graphics g) {
		int highestLineY = this.getMarginY();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		int gPos = this.getMarginX() + 3 * this.dx();

		getStaff().drawOn(g, gPos - dx(), highestLineY);
	}

	public int getFocusedSystemY() {
		return SISDISPLACE * dy() * (getStaff().getFocusedIndex() / getStaff().getAccordInRowCount());
	}
	
	public void checkCam() {
		JScrollBar vertical = getScrollPane().getVerticalScrollBar();
		if (vertical.getValue() + parentWindow.getHeight() < getFocusedSystemY() + SISDISPLACE * dy() ||
			vertical.getValue() > getFocusedSystemY()) {
			vertical.setValue(getFocusedSystemY());
		}
		this.setPreferredSize(new Dimension(10, this.getTotalRowCount() * SISDISPLACE * this.dy()));	//	Needed for the scrollBar bars to appear
		this.revalidate();	//	Needed to recalc the scrollBar bars

		this.repaint();
	}

	public JScrollPane getScrollPane() {
		return (JScrollPane)getParent().getParent(); // -_-
	}
	
	public void page(Combo combo) {
		JScrollBar vertical = getScrollPane().getVerticalScrollBar();
		int pos = vertical.getValue() + combo.getSign() * SISDISPLACE * this.dy();
		if (pos<0) pos = 0;
		if (pos>vertical.getMaximum()) pos = vertical.getMaximum();
		vertical.setValue(pos);
		repaint();
	}

	// getters/setters

	public SheetPanel setStaff(Staff staff) { this.staff = staff; return this; }
	public Staff getStaff() { return this.staff; }

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

	@Override
	public AbstractModel getFocusedChild() { return getStaff(); }
	@Override
	public IHandlerContext getModelParent() { return null; }
}

