
package Storyspace.Music.Staff.Accord;

import Model.AbstractHandler;
import Model.Combo;
import Storyspace.Music.Staff.Accord.Nota.Nota;
import Storyspace.Music.Staff.Staff;
import Stuff.Musica.PlayMusThread;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.*;

public class AccordHandler extends AbstractHandler {
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

		// TODO: nota appending should be AccordHandler event!!! (now it is done with code in Staff::addPressed())

		// TODO: actionMap should not be accessed by successors, use some method instead... eventually

		// TODO: does not work
		// character-key press
		Consumer<Combo> handlePressChar = (e) -> getContext().setSlog(getContext().getSlog().concat("" + e.getKeyChar()));
		Consumer<Combo> dehandlePressChar = (e) -> getContext().setSlog(getContext().getSlog().substring(0, getContext().getSlog().length() - 1));
		for (int i: Combo.getCharacterKeycodeList()) {
			addCombo(0, i).setDo(handlePressChar).setUndo(dehandlePressChar);
			addCombo(KeyEvent.SHIFT_MASK, i).setDo(handlePressChar).setUndo(dehandlePressChar);
		}

		// TODO: move logic that action applies to all children, like dispatching event to them

		addCombo(ctrl, k.VK_3).setDo((event) -> { getContext().getNotaList().forEach(Nota::triggerTupletDenominator); }).biDirectional();
		addCombo(ctrl, k.VK_H).setDo((event) -> { getContext().getNotaList().forEach(Nota::triggerIsMuted); }).biDirectional();
		addCombo(k.SHIFT_MASK, k.VK_3).setDo((event) -> { getContext().getNotaList().forEach(Nota::triggerIsSharp); }).biDirectional();
		for (Integer i: Arrays.asList(k.VK_OPEN_BRACKET, k.VK_CLOSE_BRACKET)) {
			addCombo(0, i).setDo((event) -> {
				for (Nota nota : getContext().getNotaList()) { nota.changeLength(event); }
			}).setUndoChangeSign();
		}

		addCombo(0, k.VK_DELETE).setDo((event) -> {
			Nota nota = getContext().getFocusedNota();
			if (nota != null) {
				deletedNotaQueue.add(nota);
				getContext().deleteFocused();
				return true;
			} else {
				return false;
			}
		}).setUndo((event) -> {
			getContext().add(deletedNotaQueue.pollLast());
			getContext().setFocusedIndex(getContext().getFocusedIndex() + 1);
		});

		for (Integer i: Arrays.asList(k.VK_DOWN, k.VK_UP)) { addCombo(0, i).setDo(getContext()::moveFocus).setUndoChangeSign(); }

		for (Integer i: Combo.getNumberKeyList()) { addCombo(0, i)
			.setDo(combo -> { getContext().setFocusedIndex(combo.getPressedNumber());})
			.setUndo(combo -> { getContext().setFocusedIndex(-1); });
		}

		addCombo(0, k.VK_BACK_SPACE).setDo2((combo) -> {
			String slog = getContext().getSlog();
			if (slog.length() < 1) {
				return null;
			} else {
				char erasedChar = slog.charAt(slog.length() - 1);
				getContext().setSlog(slog.substring(0, slog.length() - 1));
				return new HashMap<String, Object>(){{ put("erasedChar", erasedChar + ""); }};
			}
		}).setUndo((combo, paramsForUndo) -> {
			getContext().setSlog(getContext().getSlog() + paramsForUndo.get("erasedChar"));
		});

		addCombo(0, k.VK_ENTER).setDo((event) -> {
			PlayMusThread.shutTheFuckUp();
			PlayMusThread.playAccord(getContext());
		});

		// MIDI-key press
		for (Integer i: Combo.getAsciTuneMap().keySet()) {
			addCombo(11, i).setDo((combo) -> { // 11 - alt+shif+ctrl

				// important TODO: UX WANNA ctrl-z, bleaaatj!

				// TODO: move stuff like constants and mode into the handler
				long timestamp = System.currentTimeMillis();

				if (getContext().getParentStaff().mode == Staff.aMode.passive) { return false; }

				if (timestamp - getContext().getEarliestKeydown() < Staff.ACCORD_EPSILON) {
					new Nota(getContext(), combo.asciiToTune()).setKeydownTimestamp(timestamp);
					return true;
				} else {
					return false;
				}
			});
		}
	}
}
