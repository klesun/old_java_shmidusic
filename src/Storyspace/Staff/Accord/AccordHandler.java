
package Storyspace.Staff.Accord;

import Gui.Settings;
import Model.AbstractHandler;
import Model.ActionFactory;
import Model.Combo;
import Model.ContextAction;
import Storyspace.Staff.Accord.Nota.Nota;
import Storyspace.Staff.Accord.Nota.NotaHandler;
import Storyspace.Staff.Staff;
import Storyspace.Staff.StaffHandler;
import Stuff.Musica.PlayMusThread;
import Stuff.OverridingDefaultClasses.TruHashMap;

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

		for (Map.Entry<Combo, ContextAction<Accord>> entry: makeStaticActionMap().entrySet()) {
			new ActionFactory(entry.getKey()).addTo(this.actionMap).setDo(() -> entry.getValue().redo(getContext()));
		}

		// TODO: nota appending should be AccordHandler event!!! (now it is done with code in Staff::addPressed())

		// TODO: does not work
		// character-key press
//		Consumer<Combo> handlePressChar = (e) -> getContext().setSlog(getContext().getSlog().concat("" + e.getKeyChar()));
//		Consumer<Combo> dehandlePressChar = (e) -> getContext().setSlog(getContext().getSlog().substring(0, getContext().getSlog().length() - 1));
//		for (int i: Combo.getCharacterKeycodeList()) {
//			addCombo(0, i).setDo(handlePressChar).setUndo(dehandlePressChar);
//			addCombo(KeyEvent.SHIFT_MASK, i).setDo(handlePressChar).setUndo(dehandlePressChar);
//		}

		// TODO: move logic that action applies to all children, like dispatching event to them

//		addCombo(ctrl, k.VK_3).setDo((event) -> { getContext().getNotaList().forEach(Nota::triggerTupletDenominator); }).biDirectional();
//		addCombo(ctrl, k.VK_H).setDo((event) -> { getContext().getNotaList().forEach(Nota::triggerIsMuted); }).biDirectional();
//		addCombo(k.SHIFT_MASK, k.VK_3).setDo((event) -> { getContext().getNotaList().forEach(Nota::triggerIsSharp); }).biDirectional();
//		for (Integer i: Arrays.asList(k.VK_OPEN_BRACKET, k.VK_CLOSE_BRACKET)) {
//			addCombo(0, i).setDo((event) -> {
//				for (Nota nota : getContext().getNotaList()) { nota.changeLength(event); }
//			}).setUndoChangeSign();
//			// TODO: NO! IT'S NOT undoChangeSign!!! what if some notas where already at the edge and some not?! you stupid full of shit!
//		}
//
//		addCombo(0, k.VK_DELETE).setDo((event) -> {
//			Nota nota = getContext().getFocusedNota();
//			if (nota != null) {
//				deletedNotaQueue.add(nota);
//				getContext().deleteFocused();
//				return true;
//			} else {
//				return false;
//			}
//		}).setUndo((event) -> {
//			getContext().getNotaList().add(deletedNotaQueue.pollLast());
//			getContext().setFocusedIndex(getContext().getFocusedIndex() + 1);
//		});
//
//		addCombo(ctrl, k.VK_PERIOD).setDo(getContext()::triggerIsDiminendo).biDirectional();
//
////		for (Integer i: Arrays.asList(k.VK_DOWN, k.VK_UP)) { addCombo(0, i).setDo(getContext()::moveFocus).setUndoChangeSign(); }
//
//		for (Integer i: Combo.getNumberKeyList()) { addCombo(0, i)
//			.setDo(combo -> { getContext().setFocusedIndex(combo.getPressedNumber());})
//			.setUndo(combo -> { getContext().setFocusedIndex(-1); });
//		}
//
////		addCombo(0, k.VK_BACK_SPACE).setDo2((combo) -> {
////			String slog = getContext().getSlog();
////			if (slog.length() < 1) {
////				return null;
////			} else {
////				char erasedChar = slog.charAt(slog.length() - 1);
////				getContext().setSlog(slog.substring(0, slog.length() - 1));
////				return new HashMap<String, Object>(){{ put("erasedChar", erasedChar + ""); }};
////			}
////		}).setUndo((combo, paramsForUndo) -> {
////			getContext().setSlog(getContext().getSlog() + paramsForUndo.get("erasedChar"));
////		});
//
//		addCombo(0, k.VK_ENTER).setDo((event) -> {
//			PlayMusThread.shutTheFuckUp();
//			PlayMusThread.playAccord(getContext());
//		});
//
//		// MIDI-key press
//		for (Integer i: Combo.getAsciTuneMap().keySet()) {
//			addCombo(Combo.getAsciiTuneMods(), i).setOmitMenuBar(true).setDo((combo) -> {
//
//				if (getContext().getParentStaff().mode == Staff.aMode.passive) { return false; }
//
//				if (System.currentTimeMillis() - getContext().getEarliestKeydown() < Staff.ACCORD_EPSILON) {
//					getContext().addNewNota(combo.asciiToTune(), getStaffHandler().getDefaultChannel());
//					return true;
//				} else {
//					return false;
//				}
//			});
//		}
	}

	public static Map<Combo, ContextAction<Accord>> makeStaticActionMap() {

		TruHashMap<Combo, ContextAction<Accord>> actionMap = new TruHashMap<>();
		for (Map.Entry<Combo, ContextAction<Nota>> entry: NotaHandler.makeStaticActionMap().entrySet()) {
			actionMap.p(entry.getKey(), mkAction(accord -> accord.getNotaList().forEach(entry.getValue()::redo)));
		}

		// overwrites putDot() action of child Nota-s
		actionMap.p(new Combo(ctrl, k.VK_PERIOD), mkAction(Accord::triggerIsDiminendo))
			.p(new Combo(0, k.VK_UP), mkAction(a -> a.moveFocus(-1)))
			.p(new Combo(0, k.VK_DOWN), mkAction(a -> a.moveFocus(1)));

		for (Combo combo: Combo.getNumberComboList(0)) {
			actionMap.p(combo, mkAction(a -> a.setFocusedIndex(combo.getPressedNumber())));
		}

		// MIDI-key press
		for (Map.Entry<Combo, Integer> entry: Combo.getComboTuneMap().entrySet()) {
			ContextAction<Accord> action = new ContextAction<>();
			actionMap.p(entry.getKey(), action.setRedo(accord -> {
				if (System.currentTimeMillis() - accord.getEarliestKeydown() < Staff.ACCORD_EPSILON) {
					accord.addNewNota(entry.getValue(), Settings.inst().getDefaultChannel());
					return true;
				} else {
					return false;
				}
			}));
		}

		return actionMap;
	}

	// stupid java, stupid lambdas don't see stupid generics! that's why i was forced to create separate method to do it in one line
	private static ContextAction<Accord> mkAction(Consumer<Accord> lambda) {
		ContextAction<Accord> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}
