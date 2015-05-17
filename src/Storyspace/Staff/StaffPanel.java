package Storyspace.Staff;

import Gui.Settings;
import Model.AbstractHandler;
import Model.ComboMouse;
import Stuff.Midi.DumpReceiver;
import Model.AbstractModel;
import Model.Combo;
import Main.MajesticWindow;
import Storyspace.IStoryspacePanel;
import Storyspace.StoryspaceScroll;
import Storyspace.Storyspace;
import Stuff.OverridingDefaultClasses.Scroll;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;

import javax.swing.*;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;


final public class StaffPanel extends JPanel implements IStoryspacePanel {

	public static int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?
	public static int MARGIN_H = 1; // TODO: move it into Constants class maybe?
	final public static int SISDISPLACE = 40;

	private StoryspaceScroll storyspaceScroll = null;
	public MajesticWindow parentWindow = null; // deprecated
	public AbstractHandler handler = null;
	private Staff staff = null;

	private StaffPanel storyspaceRepresentative = this;

	public StaffPanel(Storyspace parentStoryspace) {
		this.parentWindow = parentStoryspace.getWindow();
		thisMiddle();
		storyspaceScroll = parentStoryspace.addModelChild(this);
	}

	public StaffPanel(MajesticWindow window) {
		this.parentWindow = window;
		thisMiddle();
	}

	private void thisMiddle() {
		this.staff = new Staff(this);

		this.addKeyListener(handler = makeHandler());
		this.addMouseListener(handler);
		this.addMouseMotionListener(handler);

		addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) { DumpReceiver.eventHandler = (StaffHandler)staff.getHandler(); }
			public void focusLost(FocusEvent e) { DumpReceiver.eventHandler = null; }
		});

		this.setFocusable(true);
		this.requestFocus();

		repaint();
	}

	public int getTotalRowCount() { return getStaff().getAccordRowList().size(); }
	
	@Override
	public void paintComponent(Graphics g) { getStaff().drawOn(g, 0, 0); }

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

	public Scroll getScrollPane() { return Scroll.class.cast(getParent().getParent()); } // -_-
	@Override
	public StoryspaceScroll getStoryspaceScroll() { return storyspaceRepresentative.storyspaceScroll; }

	// IModel implementation

	@Override
	public AbstractModel getFocusedChild() { return getStaff(); }
	@Override
	public StoryspaceScroll getModelParent() {
		return getScrollPane() instanceof StoryspaceScroll
			? StoryspaceScroll.class.cast(getScrollPane())
			: null;
	}
	@Override
	public AbstractHandler getHandler() { return this.handler; }

	@Override
	public void getJsonRepresentation(JSONObject dict) { 	dict.put("staff", getStaff().getJsonRepresentation()); }
	@Override
	public StaffPanel reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.staff = new Staff(this).reconstructFromJson(jsObject.getJSONObject("staff"));
		return this;
	}

	// getters/setters

	public StaffPanel setStaff(Staff staff) { this.staff = staff; return this; }
	public Staff getStaff() { return this.staff; }

	// maybe put it into AbstractModel?
	private static int dx() { return Settings.getStepWidth(); }
	private static int dy() { return Settings.getStepHeight(); }

	// Until here

	// TODO: move to Staff
	public static int getMarginX() {
		return Math.round(MARGIN_H * dx());
	}
	public static int getMarginY() {
		return Math.round(MARGIN_V * dy());
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

	// this method is bad and you should feel bad
	public void switchFullscreen(Combo combo) {
		parentWindow.isFullscreen = !parentWindow.isFullscreen;
		if (parentWindow.isFullscreen) {
			parentWindow.fullscreenStaffPanel.storyspaceRepresentative = this;
			parentWindow.fullscreenStaffPanel.setStaff(this.getStaff());
			((CardLayout)parentWindow.cards.getLayout()).show(parentWindow.cards, parentWindow.CARDS_FULLSCREEN);
			parentWindow.fullscreenStaffPanel.requestFocus();
			Settings.inst().scale(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_EQUALS));
		} else {
			((CardLayout) parentWindow.cards.getLayout()).show(parentWindow.cards, MajesticWindow.CARDS_STORYSPACE);
			Settings.inst().scale(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_MINUS));
			storyspaceRepresentative.requestFocus();
		}
		this.validate();
		this.repaint();
	}

	// private methods

	private AbstractHandler makeHandler() {
		return new AbstractHandler(this) {
			protected void initActionMap() {
				addCombo(ctrl, k.VK_F).setDo(getContext()::switchFullscreen);
				addCombo(ctrl, k.VK_PAGE_DOWN).setDo(getContext()::page);
				addCombo(ctrl, k.VK_PAGE_UP).setDo(getContext()::page);
			}
			public Boolean mousePressedFinal(ComboMouse mouse) {
				if (mouse.leftButton) {
					getContext().requestFocus();
					return true;
				} else { return false; }
			}
			public StaffPanel getContext() { return StaffPanel.class.cast(super.getContext()); }
		};
	}
}

