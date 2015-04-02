
package Model.Staff.StaffConfig;

import Model.AbstractHandler;
import Model.Action;
import Model.Combo;

import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class StaffConfigHandler extends AbstractHandler {
	
	public StaffConfigHandler(StaffConfig context) {
		super(context);
	}

	@Override
	public StaffConfig getContext() {
		return (StaffConfig)super.getContext();
	}

	@Override
	final protected void init() {
		if (actionMap == null) {
			actionMap = new LinkedHashMap<>();

			actionMap.put(new Combo(0, KeyEvent.VK_DOWN), new Action().setDo((event) -> {
				getContext().chooseNextParam();
			}));
			actionMap.put(new Combo(0, KeyEvent.VK_UP), new Action().setDo((event) -> {
				// this.sheet.getFocusedStaff().getPhantom().choosePrevParam();
			}));

			actionMap.put(new Combo(0, KeyEvent.VK_ADD), new Action().setDo((event) -> {
				getContext().changeValue(1);
			}));
			actionMap.put(new Combo(0, KeyEvent.VK_SUBTRACT), new Action().setDo((event) -> {
				getContext().changeValue(-1);
			}));
			actionMap.put(new Combo(0, KeyEvent.VK_BACK_SPACE), new Action().setDo((event) -> {
				getContext().backspace();
			}));

			Consumer<Combo> writeNumber = (e) -> {
				getContext().tryToWrite(e.getKeyChar());
			};
			for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; ++i) { actionMap.put(new Combo(0, i), new Action().setDo(writeNumber)); };
			for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; ++i) { actionMap.put(new Combo(0, i), new Action().setDo(writeNumber)); };
		}
	}
}