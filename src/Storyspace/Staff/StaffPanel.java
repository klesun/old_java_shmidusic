package Storyspace.Staff;

import Gui.Settings;
import Model.*;
import Stuff.Midi.DumpReceiver;
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

	public MajesticWindow parentWindow = null; // deprecated

	private StoryspaceScroll storyspaceScroll = null;
	public AbstractHandler handler = null;
	private Helper modelHelper = new Helper(this);
	private Helper h = modelHelper;
	private Staff staff = null;

	private StaffPanel storyspaceRepresentative = this;

	private JSONObject staffJson;
	private Boolean loadJsonOnFocus = false;

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
			public void focusGained(FocusEvent e) {
				if (loadJsonOnFocus) {
					staff.reconstructFromJson(staffJson);
					loadJsonOnFocus = false;
				}
				staff.getConfig().syncSyntChannels();
				DumpReceiver.eventHandler = (StaffHandler)staff.getHandler();
			}
			public void focusLost(FocusEvent e) { DumpReceiver.eventHandler = null; }
		});

		this.setFocusable(true);
		this.requestFocus();

		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		if (!loadJsonOnFocus) {
			getStaff().drawOn(g, 0, 0);
		}
	}

	public int getFocusedSystemY() {
		return Staff.SISDISPLACE * dy() * (getStaff().getFocusedIndex() / getStaff().getAccordInRowCount());
	}
	
	public void checkCam() {
		int width = getScrollPane().getWidth();
		JScrollBar vertical = getScrollPane().getVerticalScrollBar();
		if (vertical.getValue() + getScrollPane().getHeight() < getFocusedSystemY() + Staff.SISDISPLACE * dy() ||
			vertical.getValue() > getFocusedSystemY()) {
			vertical.setValue(getFocusedSystemY());
		}
		this.setPreferredSize(new Dimension(width - 25, getStaff().getHeightIf(width)));	//	Needed for the scrollBar bars to appear
		this.revalidate();	//	Needed to recalc the scrollBar bars

		this.repaint();
	}
	public Scroll getScrollPane() { return Scroll.class.cast(getParent().getParent()); } // -_-
	@Override
	public StoryspaceScroll getStoryspaceScroll() { return storyspaceRepresentative.storyspaceScroll; }

	// IModel implementation

	@Override
	public Staff getFocusedChild() { return getStaff(); }
	@Override
	public StoryspaceScroll getModelParent() {
		return getScrollPane() instanceof StoryspaceScroll
			? StoryspaceScroll.class.cast(getScrollPane())
			: null;
	}
	@Override
	public AbstractHandler getHandler() { return this.handler; }
	public Helper getModelHelper() { return modelHelper; }

	@Override
	public void getJsonRepresentation(JSONObject dict) {
		dict.put("staff", loadJsonOnFocus ? staffJson : getStaff().getJsonRepresentation());
	}
	@Override
	public StaffPanel reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.staffJson = jsObject.getJSONObject("staff");
		this.loadJsonOnFocus = true;
		return this;
	}

	// getters/setters

	public StaffPanel setStaff(Staff staff) { this.staff = staff; return this; }
	public Staff getStaff() { return this.staff; }

	// maybe put it into AbstractModel?
	private static int dy() { return Settings.getStepHeight(); }

	// Until here

	// event handles

	public void page(Combo combo) {
		JScrollBar vertical = getScrollPane().getVerticalScrollBar();
		vertical.setValue(h.limit(vertical.getValue() + combo.getSign() * Staff.SISDISPLACE * this.dy(), 0, vertical.getMaximum()));
		repaint();
	}

	// this method is bad and you should feel bad
	public void switchFullscreen() {
		parentWindow.isFullscreen = !parentWindow.isFullscreen;
		if (parentWindow.isFullscreen) {
			parentWindow.switchTo(MajesticWindow.cardEnum.CARDS_FULLSCREEN);

			StaffPanel fullscreenPanel = parentWindow.fullscreenStaffPanel;
			fullscreenPanel.storyspaceRepresentative = this;
			fullscreenPanel.setStaff(this.getStaff());
			fullscreenPanel.requestFocus();
			Settings.inst().scale(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_EQUALS));
			parentWindow.setTitle(getStoryspaceScroll().getTitle());
		} else {
			parentWindow.switchTo(MajesticWindow.cardEnum.CARDS_STORYSPACE);

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
				addCombo(0, k.VK_PAGE_DOWN).setDo(getContext()::page);
				addCombo(0, k.VK_PAGE_UP).setDo(getContext()::page);
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

