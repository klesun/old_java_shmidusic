package Model.Containers.Panels;

import Gui.Settings;
import Midi.DumpReceiver;
import Model.AbstractModel;
import Model.Combo;
import Model.Containers.MajesticWindow;
import Model.Containers.ResizableScroll;
import Model.Containers.Storyspace;
import Model.IModel;
import Model.Staff.Staff;
import OverridingDefaultClasses.Scroll;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;

import javax.swing.*;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;


final public class MusicPanel extends JPanel implements IModel {

	public int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?
	public static int MARGIN_H = 1; // TODO: move it into Constants class maybe?
	final public static int SISDISPLACE = 40;

	private Storyspace parentStoryspace = null;
	public MajesticWindow parentWindow = null; // deprecated
	public MusicPanelHandler handler = null;
	private Staff staff = null;

	public MusicPanel(Storyspace parentStoryspace) {
		this.parentWindow = parentStoryspace.getWindow();
		this.staff = new Staff(this);

		this.addKeyListener(handler = new MusicPanelHandler(this));
		this.addMouseListener(handler);
		this.addMouseMotionListener(handler);

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) { DumpReceiver.eventHandler = handler; }
			@Override
			public void focusLost(FocusEvent e) { DumpReceiver.eventHandler = null; }
		});

		this.setFocusable(true);
		this.requestFocus();

		(this.parentStoryspace = parentStoryspace).addModelChild(this);

		repaint();
	}

	public int getTotalRowCount() { return getStaff().getAccordRowList().size(); }
	
	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		getStaff().drawOn(g, this.getMarginX(), this.getMarginY());
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

	public Scroll getScrollPane() {
		return Scroll.class.cast(getParent().getParent()); // -_-
	}

	@Deprecated
	public MusicPanel hideGracefully() {
		// funny thing, we get fake empty Scroll because fullscreen MusicPanel
		// is MusicPanel too and is placed to Storyspace as well
		this.getScrollPane().setVisible(false);
		return this;
	}

	// IModel implementation

	@Override
	public AbstractModel getFocusedChild() { return getStaff(); }
	@Override
	public ResizableScroll getModelParent() {
		return getScrollPane() instanceof ResizableScroll
			? ResizableScroll.class.cast(getScrollPane())
			: null;
	}
	@Override
	public MusicPanelHandler getHandler() { return this.handler; }

	@Override
	public JSONObject getJsonRepresentation() {
		JSONObject dict = new JSONObject();
		dict.put("className", getClass().getSimpleName());
		dict.put("staff", getStaff());
		return dict;
	}
	@Override
	public MusicPanel reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.staff = new Staff(this).reconstructFromJson(jsObject.getJSONObject("staff"));
		return this;
	}

	// getters/setters

	public MusicPanel setStaff(Staff staff) { this.staff = staff; return this; }
	public Staff getStaff() { return this.staff; }

	// maybe put it into AbstractModel?
	private int dy() { return Settings.getStepHeight(); }

	// Until here

	public static int getMarginX() {
		return Math.round(MARGIN_H * Settings.getStepWidth());
	}
	public int getMarginY() {
		return Math.round(MARGIN_V * this.dy());
	}

	// event handles

	public void page(Combo combo) {
		JScrollBar vertical = getScrollPane().getVerticalScrollBar();
		int pos = vertical.getValue() + combo.getSign() * SISDISPLACE * this.dy();
		if (pos<0) pos = 0;
		if (pos>vertical.getMaximum()) pos = vertical.getMaximum();
		vertical.setValue(pos);
		repaint();
	}

	public void switchFullscreen(Combo combo) {
		parentWindow.isFullscreen = !parentWindow.isFullscreen;
		if (parentWindow.isFullscreen) {
			parentWindow.musicPanel.setStaff(this.getStaff());
			((CardLayout)parentWindow.cards.getLayout()).show(parentWindow.cards, parentWindow.CARDS_FULLSCREEN);
			parentWindow.musicPanel.requestFocus();
			Settings.inst().scale(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_EQUALS));
		} else {
			((CardLayout)parentWindow.cards.getLayout()).show(parentWindow.cards, MajesticWindow.CARDS_STORYSPACE);
			Settings.inst().scale(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_MINUS));
		}
		this.validate();
		this.repaint();
	}
}

