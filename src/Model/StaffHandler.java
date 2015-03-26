
package Model;
import Midi.DeviceEbun;
import Midi.MidiCommon;
import Model.Accord.AccordHandler;
import Model.StaffConfig.StaffConfigHandler;
import Musica.PlayMusThread;
import java.awt.event.KeyEvent;

public class StaffHandler {
	
	private Staff context = null;
	
	public StaffHandler(Staff context) {
		this.context = context;
	}

	public void handleKey(KeyEvent e) {

		if (getContext().getFocusedAccord() != null) {
			new AccordHandler(getContext().getFocusedAccord()).handleKey(e);
		} else {
			new StaffConfigHandler(getContext().getPhantom()).handleKey(e);
		}

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
				case '0':
					System.out.println("Вы нажали 0!");
					getContext().changeMode();
					break;
				default: break;
			}
		} else if (((e.getModifiers() & KeyEvent.CTRL_MASK) == 0)
				&& ((e.getModifiers() & KeyEvent.ALT_MASK) != 0)) {
			int cod = e.getKeyCode();
			if (cod >= '0' && cod <= '9') {
				getContext().changeChannelFlag(cod - '0');
				getContext().requestNewSurface();
			}
		}
		switch (e.getKeyCode()) {
			case KeyEvent.VK_RIGHT:
					PlayMusThread.shutTheFuckUp();
					getContext().moveFocus(1);
					// stan.drawPanel.checkCam();
					break;
			case KeyEvent.VK_LEFT:
				PlayMusThread.shutTheFuckUp();
				getContext().moveFocus(-1);
				// stan.drawPanel.checkCam();
				break;
			case KeyEvent.VK_UP:
				PlayMusThread.shutTheFuckUp();
				getContext().setFocusedIndex(getContext().getFocusedIndex() - getContext().getNotaInRowCount());
				getContext().requestNewSurface();
				getContext().parentSheetMusic.checkCam(); // O_o move it into requestNewSurface maybe?
				break;
			case KeyEvent.VK_DOWN:
				PlayMusThread.shutTheFuckUp();
				getContext().setFocusedIndex(getContext().getFocusedIndex() + getContext().getNotaInRowCount());
				getContext().requestNewSurface();
				getContext().parentSheetMusic.checkCam(); // same here
				break;
			case KeyEvent.VK_HOME:
				PlayMusThread.shutTheFuckUp();
				getContext().setFocusedIndex(-1);
				getContext().parentSheetMusic.checkCam();
				break;
			case KeyEvent.VK_END:
				PlayMusThread.shutTheFuckUp();
				getContext().setFocusedIndex(getContext().getAccordList().size() - 1);
				getContext().parentSheetMusic.checkCam();
				break;
			case KeyEvent.VK_ENTER: // TODO: maybe it would better fit into accord handler?
				PlayMusThread.shutTheFuckUp();
				PlayMusThread.playAccord(getContext().getFocusedAccord());
				break;
			case KeyEvent.VK_DELETE:
				System.out.println("Вы нажали Delete!");
				getContext().delNotu();
				getContext().requestNewSurface();
				break;
			default:
				if (e.getKeyCode() == '-') {
					getContext().moveFocus(1);
				}
				break;
		}
	}

	public Staff getContext() {
		return this.context;
	}
}
