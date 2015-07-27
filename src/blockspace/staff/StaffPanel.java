package blockspace.staff;

import blockspace.staff.accord.Accord;
import gui.Constants;
import gui.ImageStorage;
import gui.Settings;
import model.*;
import blockspace.BlockSpace;
import stuff.Midi.DumpReceiver;
import blockspace.IBlockSpacePanel;
import blockspace.Block;
import stuff.OverridingDefaultClasses.Scroll;
import stuff.OverridingDefaultClasses.TruMap;
import org.json.JSONException;
import org.json.JSONObject;
import stuff.tools.jmusic_integration.INota;

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.IntStream;


final public class StaffPanel extends JPanel implements IBlockSpacePanel {

	public static int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?
	public static int MARGIN_H = 1; // TODO: move it into Constants class maybe?

	final private Block parentBlock;
	final private AbstractHandler handler;
	final private Helper modelHelper = new Helper(this);
	final private Staff staff;

	private JSONObject staffJson;
	private Boolean loadJsonOnFocus = false;
	private Boolean simpleRepaint = false;
	private Boolean surfaceCompletelyChanged = false;

	/** @debug - return private when done */
	final public RealStaffPanel staffContainer;
	final private Scroll staffScroll;
	final private PianoLayoutPanel pianoLayoutPanel;

	public StaffPanel(BlockSpace parentBlockSpace) {
		super();
		this.staff = new Staff(this);
		this.parentBlock = parentBlockSpace.addModelChild(this);

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
				DumpReceiver.eventHandler = staff.getHandler();
			}

			public void focusLost(FocusEvent e) {
				DumpReceiver.eventHandler = null;
			}
		});

		this.setLayout(new BorderLayout());
		this.setFocusable(true);
		this.requestFocus();

		staffContainer = new RealStaffPanel(staff);
		staffScroll = new Scroll(staffContainer);
		this.add(staffScroll, BorderLayout.CENTER);

		this.add(pianoLayoutPanel = new PianoLayoutPanel(staff), BorderLayout.PAGE_END);
	}

	private void iThinkItInterruptsPreviousPaintingThreadsSoTheyDidntSlowCurrent() {

		// i don't know, who is stupider: linuxes, awt or me, but need it cuz it forces to repaint JUST the time it's requested to repaint
		// on windows issues does not occur
		// i aproximetely know, what's happening, it's calling something like revalidate(), but i amnt not sure
		getParentBlock().getModelParent().getWindow().terminal.append("\n");
		// TODO: discover, whats this append() calling that makes repainting instant and use here instead of the hack
	}

	@Override
	public void paintComponent(Graphics g) {
		if (!loadJsonOnFocus) {

			super.paintComponent(g);

			// maybe need to move it to RealStaffPanel::paintComponent()
			iThinkItInterruptsPreviousPaintingThreadsSoTheyDidntSlowCurrent();
		}
	}

	public void surfaceCompletelyChanged() {
		this.surfaceCompletelyChanged = true;
		this.checkCam();
	}

	public int getFocusedSystemY() {
		return Staff.SISDISPLACE * dy() * (getStaff().getFocusedIndex() / getStaff().getAccordInRowCount());
	}
	
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
	public Block getParentBlock() { return parentBlock; }

	// IModel implementation

	@Override
	public Staff getFocusedChild() { return getStaff(); }
	@Override
	public Block getModelParent() {
		return getParentBlock();
	}
	@Override
	public AbstractHandler getHandler() { return this.handler; }
	public Helper getModelHelper() { return modelHelper; }

	public JSONObject getJsonRepresentation() {
		return new JSONObject()
				.put("staff", loadJsonOnFocus ? staffJson : getStaff().getJsonRepresentation());
	}

	@Override
	public StaffPanel reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.staffJson = jsObject.getJSONObject("staff");
		this.loadJsonOnFocus = true;
		return this;
	}

	// getters/setters

	public Staff getStaff() { return this.staff; }
	public Settings getSettings() {
		Block scroll = getParentBlock();
		BlockSpace blockSpace = scroll.getModelParent();
		return blockSpace.getSettings();
	}

	// maybe put it into AbstractModel?
	private int dy() { return getSettings().getStepHeight(); }

	// Until here

	// private methods

	private AbstractHandler makeHandler() {
		return new AbstractHandler(this) {
			public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() {
				return new LinkedHashMap<>(makeStaticActionMap());
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

	private static LinkedHashMap<Combo, ContextAction<StaffPanel>> makeStaticActionMap() {
		return new TruMap<>();
	}

	private static ContextAction<StaffPanel> mkAction(Consumer<StaffPanel> lambda) {
		ContextAction<StaffPanel> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	public class RealStaffPanel extends JPanel
	{
		final private Staff staff;

		public RealStaffPanel(Staff staff)
		{
			this.staff = staff;
			this.setBackground(Color.WHITE);
		}

		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			staff.drawOn(g, true);
		}

//		context.getVerticalScrollBar().addMouseListener(new MouseAdapter() {
//			public void mouseEntered(MouseEvent e) {
//				context.getVerticalScrollBar().setCursor(Cursor.getDefaultCursor());
//			}
//		});

//		.p(new Combo(0, KeyEvent.VK_PAGE_DOWN), mkAction(b -> b.page(1)).setCaption("Scroll Up"))
//		.p(new Combo(0, KeyEvent.VK_PAGE_UP), mkAction(b -> b.page(-1)).setCaption("Scroll Up"));

//		public void page(int sign) {
//			JScrollBar vertical = getVerticalScrollBar();
//			vertical.setValue(limit(vertical.getValue() + sign * Staff.SISDISPLACE * getModelParent().getSettings().getStepHeight(), 0, vertical.getMaximum()));
//			repaint();
//		}

		// TODO: move to Staff scroll
		// removing stupid built-ins
//		InputMap im = context.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//		im.put(KeyStroke.getKeyStroke("UP"), "none");
//		im.put(KeyStroke.getKeyStroke("DOWN"), "none");
//		im.put(KeyStroke.getKeyStroke("PAGE_UP"), "none");
//		im.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "none");
	}
}

