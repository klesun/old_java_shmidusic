
package BlockSpacePkg.StaffPkg.Accord;

import Model.*;
import BlockSpacePkg.StaffPkg.Accord.Nota.Nota;
import BlockSpacePkg.StaffPkg.Accord.Nota.NotaHandler;
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
	public LinkedHashMap<Combo, ContextAction> getStaticActionMap() {
		return new LinkedHashMap<>(makeStaticActionMap());
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
					? new Explain(accord.addNewNota(entry.getValue(), accord.getSettings().getDefaultChannel()))
					: new Explain("too slow. to collect Nota-s into single accord, they have to be pressed in " + ACCORD_EPSILON + " milliseconds"))
				.setOmitMenuBar(true)
			);
		}

		return actionMap;
	}

	private static ContextAction<Accord> mkAction(Consumer<Accord> lambda) {
		ContextAction<Accord> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<Accord> mkFailableAction(Function<Accord, Explain> lambda) {
		ContextAction<Accord> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}
