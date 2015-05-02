
package Model.Staff.StaffConfig;

import Model.AbstractHandler;
import Model.ActionFactory;
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
		if (actionMap == null) { // TODO: does not work... and should be in config dialog. And gay

			new ActionFactory(new Combo(0, KeyEvent.VK_DOWN)).addTo(actionMap).setDo((event) -> {
				getContext().chooseNextParam();
			});
			new ActionFactory(new Combo(0, KeyEvent.VK_UP)).addTo(actionMap).setDo((event) -> {
				// this.sheet.getStaff().getConfig().choosePrevParam();
			});

			new ActionFactory(new Combo(0, KeyEvent.VK_ADD)).addTo(actionMap).setDo((event) -> {
				getContext().changeValue(1);
			});
			new ActionFactory(new Combo(0, KeyEvent.VK_SUBTRACT)).addTo(actionMap).setDo((event) -> {
				getContext().changeValue(-1);
			});
			new ActionFactory(new Combo(0, KeyEvent.VK_BACK_SPACE)).addTo(actionMap).setDo((event) -> {
				getContext().backspace();
			});

			Consumer<Combo> writeNumber = (e) -> {
				getContext().tryToWrite(e.getKeyChar());
			};
			for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; ++i) { new ActionFactory(new Combo(0, i)).addTo(actionMap).setDo(writeNumber); };
			for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; ++i) { new ActionFactory(new Combo(0, i)).addTo(actionMap).setDo(writeNumber); };
		}
	}
}