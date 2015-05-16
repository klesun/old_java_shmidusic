
package Storyspace.Music.Staff;
import Stuff.Midi.DeviceEbun;
import Model.AbstractHandler;
import Model.ActionFactory;
import Model.Combo;
import Storyspace.Music.Staff.Accord.Accord;
import Storyspace.Music.Staff.Accord.Nota.Nota;
import Stuff.Musica.PlayMusThread;

import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.JPanel;

public class StaffHandler extends AbstractHandler {
	// TODO: may be no need for this queue, cause we whatever can pass needed Nota to undo with paramsForUndo
	private LinkedList<Accord> deletedAccordQueue = new LinkedList<>();

	public StaffHandler(Staff context) {
		super(context);
	}

	public Staff getContext() {
		return Staff.class.cast(super.getContext());
	}

	@Override
	protected void initActionMap() {

		KeyEvent k = new KeyEvent(new JPanel(),0,0,0,0,'h'); // just for constants
		int ctrl = k.CTRL_MASK;
		Staff s = this.getContext();

		addCombo(ctrl, k.VK_P).setDo(s::triggerPlayer);
		addCombo(ctrl, k.VK_D).setDo(DeviceEbun::changeOutDevice);
		addCombos(ctrl, Arrays.asList(k.VK_0, k.VK_9)).stream().forEach(factory -> { factory.setDo(s::changeMode); });
		addCombo(ctrl, k.VK_RIGHT).setDo(s::moveFocusUsingCombo).setUndoChangeSign();

		addCombo(ctrl, k.VK_UP).setDo(s::moveFocusRow).setUndoChangeSign();
		addCombo(ctrl, k.VK_DOWN).setDo(s::moveFocusRow).setUndoChangeSign();

		for (Integer i: Arrays.asList(k.VK_LEFT, k.VK_RIGHT)) {
			addCombo(0, i).setDo((event) -> {
				PlayMusThread.shutTheFuckUp();
				s.moveFocusUsingCombo(event);
			}).setUndoChangeSign();
		}

		addCombo(0, k.VK_HOME).setDo2((event) -> {
			PlayMusThread.shutTheFuckUp();
			Integer lastIndex = s.getFocusedIndex();
			s.setFocusedIndex(-1);
			return new HashMap<String, Object>() {{
				put("lastIndex", lastIndex);
			}};
		}).setUndo((combo, paramsForUndo) -> {
			s.setFocusedIndex((Integer) paramsForUndo.get("lastIndex"));
		});

		addCombo(0, k.VK_END).setDo2((event) -> {
			PlayMusThread.shutTheFuckUp();
			Integer lastIndex = s.getFocusedIndex();
			s.setFocusedIndex(s.getAccordList().size() - 1);
			return new HashMap<String, Object>() {{
				put("lastIndex", lastIndex);
			}};
		}).setUndo((combo, paramsForUndo) -> {
			s.setFocusedIndex((Integer) paramsForUndo.get("lastIndex"));
		});
		addCombo(0, k.VK_DELETE).setDo((event) -> {
			Accord accord = s.getFocusedAccord();
			if (accord != null) {
				deletedAccordQueue.add(accord);
				s.getAccordList().remove(s.focusedIndex--);
				return true;
			} else {
				handleKey(new Combo(0, k.VK_RIGHT));
				return false;
			}
		}).setUndo((combo) -> {
			s.add(deletedAccordQueue.pollLast());
			s.moveFocus(1);
		});

		addCombo(0, k.VK_ESCAPE).setDo((event) -> {
			getContext().getConfig().getDialog().showMenuDialog();
		});

		for (Integer i: Combo.getAsciTuneMap().keySet()) {
			addCombo(11, i).setDo((combo) -> { // 11 - alt+shif+ctrl

				// TODO: move stuff like constants and mode into the handler

				long timestamp = System.currentTimeMillis();

				if (s.mode == Staff.aMode.passive) { return false; }
				else {
					Accord newAccord = new Accord(s);
					new Nota(newAccord, combo.asciiToTune()).setKeydownTimestamp(timestamp);
					s.add(newAccord);
					handleKey(new Combo(0, k.VK_RIGHT));
					return true;
				}
			});
		}
	}

	private List<ActionFactory> addCombos(int keyMods, List<Integer> keyCodes) {
		List factories = new ArrayList<>();
		for (int keyCode: keyCodes) {
			factories.add(new ActionFactory(new Combo(keyMods, keyCode)).addTo(this.actionMap));
		}
		return factories;
	}
}
