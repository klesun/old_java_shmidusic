
package Storyspace.Staff.Accord;

import Model.*;
import Storyspace.Staff.Accord.Nota.Nota;
import Storyspace.Staff.Accord.Nota.NotaHandler;
import Stuff.OverridingDefaultClasses.TruMap;
import org.apache.commons.math3.fraction.Fraction;

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

	@Override
	public LinkedHashMap<Combo, ContextAction> getStaticActionMap() {
		LinkedHashMap<Combo, ContextAction> huj = new LinkedHashMap<>();
		huj.putAll(makeStaticActionMap()); // no, java is retarded after all
		return huj;
	}

	public static LinkedHashMap<Combo, ContextAction<Accord>> makeStaticActionMap() {

		TruMap<Combo, ContextAction<Accord>> actionMap = new TruMap<>();
		for (Map.Entry<Combo, ContextAction<Nota>> entry: NotaHandler.makeStaticActionMap().entrySet()) {
			actionMap.p(entry.getKey(), mkAction(accord -> accord.getNotaList().forEach(entry.getValue()::redo))
					.setCaption("Notas: " + entry.getValue().getCaption()));
		}

		// overwrites putDot() action of child Nota-s
		actionMap.p(new Combo(ctrl, k.VK_PERIOD), mkAction(Accord::triggerIsDiminendo).setCaption("Diminendo On/Off"))
			.p(new Combo(0, k.VK_UP), mkFailableAction(a -> a.moveFocus(-1)).setCaption("Up"))
			.p(new Combo(0, k.VK_DOWN), mkFailableAction(a -> a.moveFocus(1)).setCaption("Down"))
			.p(new Combo(0, k.VK_DELETE), mkAction(accord -> accord.getParentStaff().remove(accord)).setCaption("Delete"))
			;

		for (Combo combo: Combo.getNumberComboList(0)) {
			actionMap.p(combo, mkAction(a -> a.setFocusedIndex(combo.getPressedNumber()))
					.setOmitMenuBar(true)
			);
		}

		// MIDI-key press
		for (Map.Entry<Combo, Integer> entry: Combo.getComboTuneMap().entrySet()) {
			ContextAction<Accord> action = new ContextAction<>();
			actionMap.p(entry.getKey(), action
				.setRedo(accord -> System.currentTimeMillis() - accord.getEarliestKeydown() < ACCORD_EPSILON
					? new ActionResult(accord.addNewNota(entry.getValue(), accord.getSettings().getDefaultChannel()))
					: new ActionResult("too slow. to collect Nota-s into single accord, they have to be pressed in " + ACCORD_EPSILON + " milliseconds"))
				.setOmitMenuBar(true)
			);
		}

		return actionMap;
	}

	// stupid java, stupid lambdas don't see stupid generics! that's why i was forced to create separate method to do it in one line
	private static ContextAction<Accord> mkAction(Consumer<Accord> lambda) {
		ContextAction<Accord> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<Accord> mkFailableAction(Function<Accord, ActionResult> lambda) {
		ContextAction<Accord> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}
