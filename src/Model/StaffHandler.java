
package Model;
import Midi.DeviceEbun;
import Midi.MidiCommon;
import Musica.PlayMusThread;
import java.awt.event.KeyEvent;

public class StaffHandler {
	
	private Staff context = null;
	
	public StaffHandler(Staff context) {
		this.context = context;
	}

	public void handleKey(KeyEvent e) {
		if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && ((e.getModifiers() & KeyEvent.ALT_MASK) == 0)) {
			switch (e.getKeyCode()) {	
				case 'P':case 'p':case 'З':case 'з':
					System.out.println("Вы нажали ctrl+p!");
					if (DeviceEbun.stop) {
						DeviceEbun.stop = false;
						(new PlayMusThread(this)).start();
					} else {
						DeviceEbun.stopMusic();
					}
					break;
				case 'D':case 'd':case 'В':case 'в':
					System.out.println("Вы нажали ctrl+д это менять аутпут!");
					MidiCommon.listDevicesAndExit(false, true, false);
					DeviceEbun.changeOutDevice();
					break;
				default: break;
			}
		}
	}

	public Staff getContext() {
		return this.context;
	}
	
}
