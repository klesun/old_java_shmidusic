package BlockSpacePkg.StaffPkg;

import Gui.Settings;
import Model.*;
import BlockSpacePkg.BlockSpace;
import Stuff.Midi.DumpReceiver;
import BlockSpacePkg.IBlockSpacePanel;
import BlockSpacePkg.Block;
import Stuff.OverridingDefaultClasses.TruMap;
import Stuff.Tools.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;


final public class StaffPanel extends JPanel implements IBlockSpacePanel {

	public static int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?
	public static int MARGIN_H = 1; // TODO: move it into Constants class maybe?

	final private Block scroll;
	final private AbstractHandler handler;
	final private Helper modelHelper = new Helper(this);
	final private Staff staff;

	private JSONObject staffJson;
	private Boolean loadJsonOnFocus = false;
	private Boolean simpleRepaint = false;
	private Boolean surfaceCompletelyChanged = false;

	public StaffPanel(BlockSpace parentBlockSpace) {
		this.staff = new Staff(this);
		this.scroll = parentBlockSpace.addModelChild(this);

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

		this.setFocusable(true);
		this.requestFocus();

		this.setBackground(Color.WHITE);
	}

	private void iThinkItInterruptsPreviousPaintingThreadsSoTheyDidntSlowCurrent() {

		// i don't know, who is stupider: linuxes, awt or me, but need it cuz it forces to repaint JUST the time it's requested to repaint
		// on windows issues does not occur
		// i aproximetely know, what's happening, it's calling something like revalidate(), but i amnt not sure
		getScroll().getModelParent().getWindow().terminal.append("\n");
		// TODO: discover, whats this append() calling that makes repainting instant and use here instead of the hack
	}

	@Override
	public void paintComponent(Graphics g) {
		if (!loadJsonOnFocus) {
			if (simpleRepaint) {

				iThinkItInterruptsPreviousPaintingThreadsSoTheyDidntSlowCurrent();

				simpleRepaint = false;
				getStaff().drawOn(g, false);

			} else {

				super.paintComponent(g);
				getStaff().drawOn(g, true);
			}
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

		JScrollBar vertical = getScroll().getVerticalScrollBar();
		if (vertical.getValue() + getScroll().getHeight() < getFocusedSystemY() + Staff.SISDISPLACE * dy() ||
			vertical.getValue() > getFocusedSystemY()) {
			vertical.setValue(getFocusedSystemY());
			simpleRepaint = false;
		}

		repaint();
	}
	public Block getScroll() { return scroll; }

	// IModel implementation

	@Override
	public Staff getFocusedChild() { return getStaff(); }
	@Override
	public Block getModelParent() {
		return getScroll();
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

	public Staff getStaff() { return this.staff; }
	public Settings getSettings() {
		Block scroll = getScroll();
		BlockSpace blockSpace = scroll.getModelParent();
		return blockSpace.getSettings();
	}

	// maybe put it into AbstractModel?
	private int dy() { return getSettings().getStepHeight(); }

	// Until here

	// event handles

	public void page(int sign) {
		JScrollBar vertical = getScroll().getVerticalScrollBar();
		vertical.setValue(limit(vertical.getValue() + sign * Staff.SISDISPLACE * this.dy(), 0, vertical.getMaximum()));
		repaint();
	}

	// private methods

	private AbstractHandler makeHandler() {
		return new AbstractHandler(this) {
			public LinkedHashMap<Combo, ContextAction> getStaticActionMap() {
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
		return new TruMap<>()
				.p(new Combo(0, KeyEvent.VK_PAGE_DOWN), mkAction(sp -> sp.page(1)))
				.p(new Combo(0, KeyEvent.VK_PAGE_UP), mkAction(sp -> sp.page(-1)));
	}

	private static ContextAction<StaffPanel> mkAction(Consumer<StaffPanel> lambda) {
		ContextAction<StaffPanel> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}

