package org.sheet_midusic.staff.staff_panel;

import com.sun.istack.internal.NotNull;
import org.sheet_midusic.stuff.main.Main;
import org.sheet_midusic.staff.Staff;
import org.sheet_midusic.stuff.graphics.Settings;
import org.klesun_model.*;
import org.sheet_midusic.stuff.OverridingDefaultClasses.Scroll;
import org.sheet_midusic.stuff.OverridingDefaultClasses.TruMap;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;

import javax.swing.*;

import java.util.LinkedHashMap;


final public class MainPanel extends JPanel implements IComponent, IModel {

	public static int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?
	public static int MARGIN_H = 1; // TODO: move it into Constants class maybe?

//	final private Block parentBlock;
	final private AbstractHandler handler;
	final private Helper modelHelper = new Helper(this);
//	final private Staff staff;

	private JSONObject staffJson;
	private Boolean loadJsonOnFocus = false;
	private Boolean simpleRepaint = false;
	private Boolean surfaceCompletelyChanged = false;

	/** @debug - return private when done */
	@NotNull public SheetMusicPanel staffContainer = new SheetMusicPanel(new SheetMusic(), this);
	@NotNull private Scroll staffScroll = new Scroll(staffContainer);
	final private PianoLayoutPanel pianoLayoutPanel;

	public MainPanel() {
		super();
//		this.staff = new Staff(this);
//		this.parentBlock = parentBlockSpace.addModelChild(this);

		this.addKeyListener(handler = makeHandler());
		this.addMouseListener(handler);
		this.addMouseMotionListener(handler);

		this.setLayout(new BorderLayout());
		this.setFocusable(true);
		this.requestFocus();

		this.add(staffScroll, BorderLayout.CENTER);
		this.add(pianoLayoutPanel = new PianoLayoutPanel(staffContainer), BorderLayout.PAGE_END);
	}

	public void replaceSheetMusicPanelWith(SheetMusicPanel newPanel)
	{
		this.staffContainer = newPanel;
		this.remove(staffScroll);
		this.add(staffScroll = new Scroll(staffContainer));
		this.revalidate();
	}

	private void iThinkItInterruptsPreviousPaintingThreadsSoTheyDidntSlowCurrent() {

		// i don't know, who is stupider: linuxes, awt or me, but need it cuz it forces to repaint JUST the time it's requested to repaint
		// on windows issues does not occur
		// i aproximetely know, what's happening, it's calling something like revalidate(), but i amnt not sure
		Main.window.terminal.append("\n");
		// TODO: discover, whats this append() calling that makes repainting instant and use here instead of the hack
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// maybe need to move it to SheetMusicPanel::paintComponent()
		iThinkItInterruptsPreviousPaintingThreadsSoTheyDidntSlowCurrent();
	}

	// TODO: move to StaffComponent
	public int getFocusedSystemY() {
		return Staff.SISDISPLACE * dy() * (getStaff().getFocusedIndex() / getStaff().getAccordInRowCount(staffContainer.getWidth()));
	}

	// TODO: move to StaffComponent
	public void checkCam() {
		simpleRepaint = !surfaceCompletelyChanged;
		surfaceCompletelyChanged = false;

		JScrollBar vertical = staffScroll.getVerticalScrollBar();
		if (vertical.getValue() + staffScroll.getHeight() < getFocusedSystemY() + Staff.SISDISPLACE * dy() ||
			vertical.getValue() > getFocusedSystemY()) {
			vertical.setValue(getFocusedSystemY());
			simpleRepaint = false;
		}

		repaint();
	}
//	public Block getParentBlock() { return parentBlock; }

	// IModel implementation

	@Override
	public SheetMusicPanel getFocusedChild() { return staffContainer; }
	@Override
	public IComponent getModelParent() {
		return null;
	}
	@Override
	public AbstractHandler getHandler() { return this.handler; }
	public Helper getModelHelper() { return modelHelper; }

	public JSONObject getJsonRepresentation() {
		return new JSONObject()
				.put("Staff", loadJsonOnFocus ? staffJson : getStaff().getJsonRepresentation());
	}

	@Override
	public MainPanel reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.staffJson = jsObject.getJSONObject("Staff");
		this.loadJsonOnFocus = true;
		return this;
	}

	// getters/setters

	public Staff getStaff() { return this.staffContainer.getFocusedChild().staff; }
	public Settings getSettings() {
//		Block scroll = getParentBlock();
//		BlockSpace blockSpace = scroll.getModelParent();
//		return blockSpace.getSettings();
		return Settings.inst();
	}

	// maybe put it into AbstractModel?
	private int dy() { return getSettings().getStepHeight(); }

	// Until here

	// private methods

	private AbstractHandler makeHandler() {
		return new AbstractHandler(this) {
			public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() {
				return new LinkedHashMap<>(new TruMap<>());
			}
		};
	}
}

