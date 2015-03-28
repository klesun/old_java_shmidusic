
package Model.StaffConfig;

import Musica.PlayMusThread;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public class StaffConfigHandler {
	
	private StaffConfig context = null;
	private static LinkedHashMap<List<Integer>, Consumer<KeyEvent>> handleEvent = null; // although it is static, it is not suppoused to be called outside the instance
	
	public StaffConfigHandler(StaffConfig context) {
		this.context = context;
		this.init();
	}


	private void init() {
		if (handleEvent == null) {
			handleEvent = new LinkedHashMap<>();

			this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_DOWN), (event) -> {
				getContext().chooseNextParam();
			});
			this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_UP), (event) -> {
				// this.sheet.getFocusedStaff().getPhantom().choosePrevParam();
			});

			this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_ADD), (event) -> {
				getContext().changeValue(1);
			});
			this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_SUBTRACT), (event) -> {
				getContext().changeValue(-1);
			});
			this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_BACK_SPACE), (event) -> {
				getContext().backspace();
			});

			Consumer<KeyEvent> writeNumber = (e) -> {
				getContext().tryToWrite(e.getKeyChar());
			};
			for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; ++i) { this.handleEvent.put(Arrays.asList(0, i), writeNumber); };
			for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; ++i) { this.handleEvent.put(Arrays.asList(0, i), writeNumber); };
		}
	}

	public Boolean handleKey(KeyEvent e) {
		List<Integer> key = Arrays.asList(e.getModifiers(), e.getKeyCode());
		if (handleEvent.containsKey(key)) {
			Consumer<KeyEvent> handle = handleEvent.get(key);
			handle.accept(e);
			getContext().getParentStaff().parentSheetMusic.parentWindow.keyHandler.requestNewSurface();
			return true;
		}
		return false;
	}

	public StaffConfig getContext() {
		return this.context;
	}
}