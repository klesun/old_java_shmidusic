
package Storyspace.Staff.Accord;

import Gui.Settings;
import Model.*;
import Storyspace.Staff.Accord.Nota.Nota;
import Storyspace.Staff.Accord.Nota.NotaHandler;
import Storyspace.Staff.Staff;
import Storyspace.Staff.StaffHandler;
import Stuff.Musica.PlayMusThread;
import Stuff.OverridingDefaultClasses.TruHashMap;
import org.apache.commons.math3.fraction.Fraction;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.*;

public class AccordHandler extends AbstractHandler {

	final public static int ACCORD_EPSILON = Nota.getTimeMilliseconds(new Fraction(1, 16), 240); // 0.0625 sec

	private LinkedList<Nota> deletedNotaQueue = new LinkedList<>();

	public AccordHandler(Accord context) {
		super(context);
	}

	@Override
	public Accord getContext() {
		return (Accord)super.getContext();
	}

	@Override
	protected void initActionMap() {
		for (Map.Entry<Combo, ContextAction<Accord>> entry: makeStaticActionMap().entrySet()) {
			new ActionFactory(entry.getKey()).addTo(this.actionMap).setDo(() -> entry.getValue().redo(getContext()).isSuccess());
		}
	}

	public static Map<Combo, ContextAction<Accord>> makeStaticActionMap() {

		TruHashMap<Combo, ContextAction<Accord>> actionMap = new TruHashMap<>();
		for (Map.Entry<Combo, ContextAction<Nota>> entry: NotaHandler.makeStaticActionMap().entrySet()) {
			actionMap.p(entry.getKey(), mkAction(accord -> accord.getNotaList().forEach(entry.getValue()::redo)));
		}

		// overwrites putDot() action of child Nota-s
		actionMap.p(new Combo(ctrl, k.VK_PERIOD), mkAction(Accord::triggerIsDiminendo))
			.p(new Combo(0, k.VK_UP), mkAction(a -> a.moveFocus(-1)))
			.p(new Combo(0, k.VK_DOWN), mkAction(a -> a.moveFocus(1)))
			.p(new Combo(0, k.VK_DELETE), mkAction(accord -> accord.getParentStaff().remove(accord)))
			;

		for (Combo combo: Combo.getNumberComboList(0)) {
			actionMap.p(combo, mkAction(a -> a.setFocusedIndex(combo.getPressedNumber())));
		}

		// MIDI-key press
		for (Map.Entry<Combo, Integer> entry: Combo.getComboTuneMap().entrySet()) {
			ContextAction<Accord> action = new ContextAction<>();
			actionMap.p(entry.getKey(), action.setRedo(accord -> System.currentTimeMillis() - accord.getEarliestKeydown() < ACCORD_EPSILON
					? new ActionResult(accord.addNewNota(entry.getValue(), Settings.inst().getDefaultChannel()))
					: new ActionResult("too slow. to collect Nota-s into single accord, they have to be pressed in " + ACCORD_EPSILON + " milliseconds")));
		}

		return actionMap;
	}

	// stupid java, stupid lambdas don't see stupid generics! that's why i was forced to create separate method to do it in one line
	private static ContextAction<Accord> mkAction(Consumer<Accord> lambda) {
		ContextAction<Accord> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}
