
package Model;
import Midi.DeviceEbun;
import Midi.MidiCommon;
import Model.Accord.AccordHandler;
import Model.StaffConfig.StaffConfigHandler;
import Musica.PlayMusThread;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public class StaffHandler {
	
	private Staff context = null;
	private static LinkedHashMap<List<Integer>, Consumer<KeyEvent>> handleEvent = null; // although it is static, it is not suppoused to be called outside the instance
	
	public StaffHandler(Staff context) {
		this.context = context;
		this.init();
	}

	private void init() {
		if (handleEvent == null) {
			handleEvent = new LinkedHashMap<>();
			
			// It may not work when we have multiple staffs, i don't know how java lambdas work
			this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_P), (event) -> {
				if (DeviceEbun.stop) {
					DeviceEbun.stop = false;
					(new PlayMusThread(this)).start();
				} else {
					DeviceEbun.stopMusic();
				}
			});
			this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_D), (event) -> {
				MidiCommon.listDevicesAndExit(false, true, false);
				DeviceEbun.changeOutDevice();
			});
			this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_0), (event) -> {
				getContext().changeMode(); // i broken java lol. I can call instance methods from static.
			});
			this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_Z), (event) -> {
//				getContext().undo(); // TODO: do ctrl-Z for child - if success - break, else do ctrl-z for parent
			});
			this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_Y), (event) -> {
//				getContext().redo();
			});
			this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_RIGHT), (event) -> {
				getContext().moveFocus(1);
				// stan.drawPanel.checkCam();
			});

			Consumer<KeyEvent> handleMuteChannel = (e) -> { 
				int cod = e.getKeyCode();
				if (cod >= '0' && cod <= '9') {
					getContext().changeChannelFlag(cod - '0');
				}
			};
			for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; ++i) { this.handleEvent.put(Arrays.asList(KeyEvent.ALT_MASK, i), handleMuteChannel); }

			this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_RIGHT), (event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().moveFocus(1);
				// stan.drawPanel.checkCam();
			});
			this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_LEFT), (event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().moveFocus(-1);
				// stan.drawPanel.checkCam();
			});
			this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_UP), (event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().moveFocus(-getContext().getNotaInRowCount());
				getContext().parentSheetMusic.checkCam(); // O_o move it into requestNewSurface maybe?
			});
			this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_DOWN), (event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().moveFocus(+getContext().getNotaInRowCount());
				getContext().parentSheetMusic.checkCam(); // O_o move it into requestNewSurface maybe?
			});
			this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_HOME), (event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().setFocusedIndex(-1);
				getContext().parentSheetMusic.checkCam();
			});
			this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_END), (event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().setFocusedIndex(getContext().getAccordList().size() - 1);
				getContext().parentSheetMusic.checkCam();
			});
			this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_ENTER), (event) -> { // TODO: maybe it would better fit into accord handler?
				PlayMusThread.shutTheFuckUp();
				PlayMusThread.playAccord(getContext().getFocusedAccord());
			});
			this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_DELETE), (event) -> {
				if (getContext().getFocusedAccord() != null) {
					getContext().getAccordList().remove(getContext().focusedIndex--);
				} else {
					getContext().moveFocus(1);
				}
			});
			this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_MINUS), (event) -> {
				if (event.getKeyCode() == '-') {
					getContext().moveFocus(1);
				}
			});
		}
	}

	public Boolean handleKey(KeyEvent e) {

		if (getContext().getFocusedAccord() != null) {
			new AccordHandler(getContext().getFocusedAccord()).handleKey(e);
		} else {
			new StaffConfigHandler(getContext().getPhantom()).handleKey(e);
		}

		List<Integer> key = Arrays.asList(e.getModifiers(), e.getKeyCode());
		if (handleEvent.containsKey(key)) {
			Consumer<KeyEvent> handle = handleEvent.get(key);
			handle.accept(e);
			getContext().parentSheetMusic.parentWindow.keyHandler.requestNewSurface();
			return true;
		}
		return false;
	}

	public Staff getContext() {
		return this.context;
	}
}
