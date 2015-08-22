package org.shmidusic.staff.staff_panel;

import org.shmidusic.stuff.main.Main;
import org.shmidusic.staff.Staff;
import org.shmidusic.stuff.graphics.Settings;
import org.klesun_model.*;
import org.shmidusic.stuff.OverridingDefaultClasses.Scroll;
import org.shmidusic.stuff.OverridingDefaultClasses.TruMap;
import org.json.JSONObject;

import java.awt.*;

import javax.swing.*;

import java.util.LinkedHashMap;


final public class MainPanel extends JPanel implements IComponent {

	public static int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?

//	final private Block parentBlock;
	final private AbstractHandler handler;
//	final private Staff staff;

	private JSONObject staffJson;
	private Boolean loadJsonOnFocus = false;
	private Boolean simpleRepaint = false;
	private Boolean surfaceCompletelyChanged = false;

	/** @debug - return private when done */
	public SheetMusicComponent staffContainer = new SheetMusicComponent(new SheetMusic(), this);
	public Scroll staffScroll = new Scroll(staffContainer);
	final public PianoLayoutPanel pianoLayoutPanel;

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
		staffScroll.getVerticalScrollBar().setUnitIncrement(Staff.SISDISPLACE);
		this.add(pianoLayoutPanel = new PianoLayoutPanel(this), BorderLayout.PAGE_END);
	}

	public void replaceSheetMusic(SheetMusic sheetMusic)
	{
		this.staffContainer = new SheetMusicComponent(sheetMusic, this);
		this.remove(staffScroll);
		this.add(staffScroll = new Scroll(staffContainer), BorderLayout.CENTER);
		staffScroll.getVerticalScrollBar().setUnitIncrement(Staff.SISDISPLACE);
		this.revalidate();
	}

	private void iThinkItInterruptsPreviousPaintingThreadsSoTheyDidntSlowCurrent()
	{
		// i don't know, who is stupider: linuxes, awt or me, but need it cuz it forces to repaint JUST the time it's requested to repaint
		// on windows issues does not occur
		// i aproximetely know, what's happening, it's calling something like revalidate(), but i amnt not sure
		Main.window.terminal.append("\n");
		// TODO: discover, whats this append() calling that makes repainting instant and use here instead of the hack
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// maybe need to move it to SheetMusicComponent::paintComponent()
		iThinkItInterruptsPreviousPaintingThreadsSoTheyDidntSlowCurrent();
	}

	// IModel implementation

	@Override
	public IModel getModel() {
		return new IModel() {
			public Helper getModelHelper() {
				return new Helper(this);
			}
		};
	}
	@Override
	public SheetMusicComponent getFocusedChild() { return staffContainer; }
	@Override
	public IComponent getModelParent() {
		return null;
	}
	@Override
	public AbstractHandler getHandler() { return this.handler; }

	// getters/setters

	public Staff getStaff() { return this.staffContainer.getFocusedChild().staff; }
	public Settings getSettings() {
//		Block scroll = getParentBlock();
//		BlockSpace blockSpace = scroll.getModelParent();
//		return blockSpace.getSettings();
		return Settings.inst();
	}

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

