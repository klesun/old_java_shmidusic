
package Model.Staff.Accord;

import Model.AbstractHandler;
import Model.Action;
import Model.Combo;
import Model.Staff.Accord.Nota.Nota;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.*;

public class AccordHandler extends AbstractHandler {

	public AccordHandler(Accord context) {
		super(context);
	}
	private LinkedList<Nota> deletedNotaQueue = new LinkedList<>();

	@Override
	public Accord getContext() {
		return (Accord)super.getContext();
	}

	@Override
	protected void init() {

		// TODO: nota appending should be AccordHandler event!!! (now it is done with code in Staff::addPressed())

		// TODO: Handle event should not be accessed by successors, use some method instead... eventually

		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_3), new Action().setDo((event) -> {
			for (Nota n: getContext().getNotaList()) { n.setTupletDenominator(n.getTupletDenominator() == 3 ? 1 : 3); }
		}).biDirectional());

		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_H), new Action().setDo((event) -> {
			for (Nota n: getContext().getNotaList()) { n.setIsMuted(!n.getIsMuted()); }
		}).biDirectional());

		actionMap.put(new Combo(KeyEvent.SHIFT_MASK, KeyEvent.VK_3), new Action().setDo((event) -> {
			for (Nota nota: getContext().getNotaList()) { nota.triggerIsSharp(); }
		}).biDirectional());

		actionMap.put(new Combo(0, KeyEvent.VK_ADD), new Action().setDo((event) -> {
			for (Nota nota : getContext().getNotaList()) { nota.changeDur(event); }
		}).setUndoChangeSign());

		actionMap.put(new Combo(0, KeyEvent.VK_SUBTRACT), new Action().setDo((event) -> {
			for (Nota nota : getContext().getNotaList()) { nota.changeDur(event); }
		}).setUndoChangeSign());

		actionMap.put(new Combo(0, KeyEvent.VK_DELETE), new Action().setDo((event) -> {
			Nota nota = getContext().getFocusedNota();
			if (nota != null) {
				deletedNotaQueue.add(nota);
				getContext().deleteFocused();
				return true;
			} else {
				return false;
			}
		}).setUndo((event) -> {
			Nota deletedNota = deletedNotaQueue.pollLast();
			getContext().add(deletedNota);
		}));

		for (Integer i: Arrays.asList(KeyEvent.VK_DOWN, KeyEvent.VK_UP)) {
			actionMap.put(new Combo(0, i), new Action().setDo(getContext()::moveFocus).setUndoChangeSign());
		}

		for (Integer i: Combo.getNumberKeyList()) {
			this.actionMap.put(new Combo(0, i), new Action().setDo((e) -> {
				getContext().setFocusedIndex(e.getPressedNumber());
			}).setUndo((event) -> {
				getContext().setFocusedIndex(-1);
			}));
		}

		this.actionMap.put(new Combo(0, KeyEvent.VK_BACK_SPACE), new Action().setDo2((event) -> {
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
		}));

		Consumer<Combo> handlePressChar = (e) -> getContext().setSlog(getContext().getSlog().concat("" + e.getKeyChar()));
		Consumer<Combo> dehandlePressChar = (e) -> getContext().setSlog(getContext().getSlog().substring(0, getContext().getSlog().length() - 1));
		for (int i: Combo.getCharacterKeycodeList()) {
			this.actionMap.put(new Combo(0, i), new Action().setDo(handlePressChar).setUndo(dehandlePressChar)); }
		for (int i: Combo.getCharacterKeycodeList()) {
			this.actionMap.put(new Combo(KeyEvent.SHIFT_MASK, i), new Action().setDo(handlePressChar).setUndo(dehandlePressChar)); }

	}
}
