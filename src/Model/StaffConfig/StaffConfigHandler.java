
package Model.StaffConfig;

import Musica.PlayMusThread;
import java.awt.event.KeyEvent;

public class StaffConfigHandler {
	
	private StaffConfig context = null;
	
	public StaffConfigHandler(StaffConfig context) {
		this.context = context;
	}
	public void handleKey(KeyEvent e) {

		if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && ((e.getModifiers() & KeyEvent.ALT_MASK) == 0)) {
			switch (e.getKeyCode()) {	
				case KeyEvent.VK_DOWN:
					getContext().chooseNextParam();
					getContext().requestNewSurface();
					break;
				case KeyEvent.VK_UP:
					// this.sheet.getFocusedStaff().getPhantom().choosePrevParam();
					// sheet.repaint();
					break;
				default: break;
			}
		}
		switch (e.getKeyCode()) {
			case KeyEvent.VK_ADD:
				getContext().changeValue(1);
				getContext().requestNewSurface();
				break;
			case KeyEvent.VK_SUBTRACT:
				getContext().changeValue(-1);
				getContext().requestNewSurface();
				break;
			case KeyEvent.VK_BACK_SPACE:
				getContext().backspace();
				getContext().requestNewSurface();
				break;
			case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
			case KeyEvent.VK_NUMPAD0:case KeyEvent.VK_NUMPAD1:case KeyEvent.VK_NUMPAD2:case KeyEvent.VK_NUMPAD3:case KeyEvent.VK_NUMPAD4:
			case KeyEvent.VK_NUMPAD5:case KeyEvent.VK_NUMPAD6:case KeyEvent.VK_NUMPAD7:case KeyEvent.VK_NUMPAD8:case KeyEvent.VK_NUMPAD9:
				getContext().tryToWrite(e.getKeyChar());
			default: break;
		}
	}

	public StaffConfig getContext() {
		return this.context;
	}
}