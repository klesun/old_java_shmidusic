
package org.shmidusic.sheet_music.staff.chord;

import org.klesun_model.*;
import org.shmidusic.sheet_music.staff.chord.nota.Nota;
import org.shmidusic.sheet_music.staff.chord.nota.NotaHandler;
import org.shmidusic.sheet_music.staff.chord.nota.NoteComponent;
import org.shmidusic.stuff.OverridingDefaultClasses.TruMap;
import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.stuff.graphics.Settings;

import java.util.*;
import java.util.function.*;

public class ChordHandler extends AbstractHandler {

	final public static int ACCORD_EPSILON = Nota.getTimeMilliseconds(new Fraction(1, 16), 120); // 0.125 sec

	public ChordHandler(ChordComponent context) {
		super(context);
	}

	@Override
	public ChordComponent getContext() {
		return (ChordComponent)super.getContext();
	}

	private static TruMap<Combo, ContextAction<ChordComponent>> actionMap = new TruMap<>();
	static {
		for (Map.Entry<Combo, ContextAction<NoteComponent>> entry: NotaHandler.getClassActionMap().entrySet()) {
			actionMap.p(entry.getKey(), mkAction(c -> c.childStream().forEach(entry.getValue()::redo))
				.setCaption("Notas: " + entry.getValue().getCaption()));
		}

		actionMap.p(new Combo(ctrl, k.VK_PERIOD), mkAction(a -> a.triggerIsDiminendo()).setCaption("Diminendo On/Off"))
			.p(new Combo(0, k.VK_UP), mkFailableAction(a -> a.moveFocus(-1)).setCaption("Up"))
			.p(new Combo(0, k.VK_DOWN), mkFailableAction(a -> a.moveFocus(1)).setCaption("Down"))
			.p(new Combo(0, k.VK_DELETE), mkAction(a -> a.getParentComponent().removeChord(a.chord)).setCaption("Delete"))
		;

		for (Combo combo: Combo.getNumberComboList(0)) {
			actionMap.p(combo, mkAction(a -> a.setFocusedIndex(combo.getPressedNumber()))
					.setOmitMenuBar(true)
			);
		}

		// MIDI-key press
		for (Map.Entry<Combo, Integer> entry: Combo.getComboTuneMap().entrySet()) {
			ContextAction<ChordComponent> action = new ContextAction<>();
			actionMap.p(entry.getKey(), action
					.setRedo(a -> System.currentTimeMillis() - a.chord.getEarliestKeydown() < ACCORD_EPSILON
						? new Explain(a.addNewNota(entry.getValue(), Settings.inst().getDefaultChannel()))
						: new Explain(false, "too slow. to collect nota-s into single chord, they have to be pressed in " + ACCORD_EPSILON + " milliseconds"))
					.setOmitMenuBar(true)
			);
		}
	}

	@Override
	public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() {
		return actionMap;
	}

	private static ContextAction<ChordComponent> mkAction(Consumer<ChordComponent> lambda) {
		ContextAction<ChordComponent> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<ChordComponent> mkFailableAction(Function<ChordComponent, Explain> lambda) {
		ContextAction<ChordComponent> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	@Override
	public Boolean mousePressedFinal(ComboMouse combo)
	{
		getContext().getParentComponent().setFocus(getContext());
		return true;
	}
}
