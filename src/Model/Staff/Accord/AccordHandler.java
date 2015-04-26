
package Model.Staff.Accord;

import Model.AbstractHandler;
import Model.ActionFactory;
import Model.Combo;
import Model.Staff.Accord.Nota.Nota;
import Model.Staff.Staff;
import Musica.PlayMusThread;

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
	protected void init() {

		// TODO: nota appending should be AccordHandler event!!! (now it is done with code in Staff::addPressed())

		// TODO: actionMap should not be accessed by successors, use some method instead... eventually

		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_3)).addTo(actionMap).setDo((event) -> {
			for (Nota n: getContext().getNotaList()) { n.setTupletDenominator(n.getTupletDenominator() == 3 ? 1 : 3); }
		}).biDirectional();

		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_H)).addTo(actionMap).setDo((event) -> {
			for (Nota n: getContext().getNotaList()) { n.setIsMuted(!n.getIsMuted()); }
		}).biDirectional();

		new ActionFactory(new Combo(KeyEvent.SHIFT_MASK, KeyEvent.VK_3)).addTo(actionMap).setDo((event) -> {
			for (Nota nota: getContext().getNotaList()) { nota.triggerIsSharp(); }
		}).biDirectional();

		for (Integer i: Arrays.asList(KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_CLOSE_BRACKET)) {
			new ActionFactory(new Combo(KeyEvent.CTRL_MASK, i)).addTo(actionMap).setDo((event) -> {
				for (Nota nota : getContext().getNotaList()) { nota.changeLength(event); }
			}).setUndoChangeSign();
		}
		new ActionFactory(new Combo(0, KeyEvent.VK_DELETE)).addTo(actionMap).setDo((event) -> {
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

		for (Integer i: Arrays.asList(KeyEvent.VK_DOWN, KeyEvent.VK_UP)) {
			new ActionFactory(new Combo(0, i)).addTo(actionMap).setDo(getContext()::moveFocus).setUndoChangeSign();
		}

		for (Integer i: Combo.getNumberKeyList()) {
			new ActionFactory(new Combo(0, i)).addTo(actionMap).setDo((e) -> {
				getContext().setFocusedIndex(e.getPressedNumber());
			}).setUndo((event) -> {
				getContext().setFocusedIndex(-1);
			});
		}

		new ActionFactory(new Combo(0, KeyEvent.VK_BACK_SPACE)).addTo(actionMap).setDo2((event) -> {
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

		new ActionFactory(new Combo(0, KeyEvent.VK_ENTER)).addTo(actionMap).setDo((event) -> {
			PlayMusThread.shutTheFuckUp();
			PlayMusThread.playAccord(getContext());
		});

		// character-key press
		Consumer<Combo> handlePressChar = (e) -> getContext().setSlog(getContext().getSlog().concat("" + e.getKeyChar()));
		Consumer<Combo> dehandlePressChar = (e) -> getContext().setSlog(getContext().getSlog().substring(0, getContext().getSlog().length() - 1));
		for (int i: Combo.getCharacterKeycodeList()) {
			new ActionFactory(new Combo(0, i)).addTo(actionMap).setDo(handlePressChar).setUndo(dehandlePressChar);
			new ActionFactory(new Combo(KeyEvent.SHIFT_MASK, i)).addTo(actionMap).setDo(handlePressChar).setUndo(dehandlePressChar);
		}

		// MIDI-key press
		for (Integer i: Combo.getAsciTuneMap().keySet()) {
			new ActionFactory(new Combo(11, i)).addTo(actionMap).setDo((combo) -> { // 11 - alt+shif+ctrl

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
