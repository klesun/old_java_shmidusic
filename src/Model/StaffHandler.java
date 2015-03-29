
package Model;
import Midi.DeviceEbun;
import Midi.MidiCommon;
import Model.Accord.AccordHandler;
import Model.Accord.Nota.Nota;
import Model.StaffConfig.StaffConfig;
import Model.StaffConfig.StaffConfigHandler;
import Musica.PlayMusThread;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
			handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_P), (event) -> {
				if (DeviceEbun.stop) {
					PlayMusThread.shutTheFuckUp();
					DeviceEbun.stop = false;
					(new PlayMusThread(this)).start();
				} else {
					DeviceEbun.stopMusic();
				}
			});
			handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_D), (event) -> {
				MidiCommon.listDevicesAndExit(false, true, false);
				DeviceEbun.changeOutDevice();
			});
			handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_0), (event) -> {
				getContext().changeMode(); // i broken java lol. I can call instance methods from static.
			});
			handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_Z), (event) -> {
//				getContext().undo(); // TODO: do ctrl-Z for child - if success - break, else do ctrl-z for parent
			});
			handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_Y), (event) -> {
//				getContext().redo();
			});
			handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_RIGHT), (event) -> {
				getContext().moveFocus(1);
				// stan.drawPanel.checkCam();
			});
			handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_UP), (event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().moveFocus(-getContext().getNotaInRowCount());
				getContext().parentSheetMusic.checkCam(); // O_o move it into requestNewSurface maybe?
			});
			handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_DOWN), (event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().moveFocus(+getContext().getNotaInRowCount());
				getContext().parentSheetMusic.checkCam(); // O_o move it into requestNewSurface maybe?
			});

			Consumer<KeyEvent> handleMuteChannel = (e) -> { 
				int cod = e.getKeyCode();
				if (cod >= '0' && cod <= '9') {
					getContext().changeChannelFlag(cod - '0');
				}
			};
			for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; ++i) { handleEvent.put(Arrays.asList(KeyEvent.ALT_MASK, i), handleMuteChannel); }

			handleEvent.put(Arrays.asList(0, KeyEvent.VK_RIGHT), (event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().moveFocus(1);
				// stan.drawPanel.checkCam();
			});
			handleEvent.put(Arrays.asList(0, KeyEvent.VK_LEFT), (event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().moveFocus(-1);
				// stan.drawPanel.checkCam();
			});
			handleEvent.put(Arrays.asList(0, KeyEvent.VK_HOME), (event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().setFocusedIndex(-1);
				getContext().parentSheetMusic.checkCam();
			});
			handleEvent.put(Arrays.asList(0, KeyEvent.VK_END), (event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().setFocusedIndex(getContext().getAccordList().size() - 1);
				getContext().parentSheetMusic.checkCam();
			});
			handleEvent.put(Arrays.asList(0, KeyEvent.VK_ENTER), (event) -> { // TODO: maybe it would better fit into accord handler?
				PlayMusThread.shutTheFuckUp();
				PlayMusThread.playAccord(getContext().getFocusedAccord());
			});
			handleEvent.put(Arrays.asList(0, KeyEvent.VK_DELETE), (event) -> {
				if (getContext().getFocusedAccord() != null) {
					getContext().getAccordList().remove(getContext().focusedIndex--);
				} else {
					getContext().moveFocus(1);
				}
			});
			handleEvent.put(Arrays.asList(0, KeyEvent.VK_MINUS), (event) -> {
				if (event.getKeyCode() == '-') {
					getContext().moveFocus(1);
				}
			});
			handleEvent.put(Arrays.asList(0, KeyEvent.VK_ESCAPE), (event) -> {
				this.showMenuDialog();
			});
		}
	}

	public void showMenuDialog() {
		
		JTextField[] channelInstrumentInputList = new JTextField[10];
		JTextField[] channelVolumeInputList = new JTextField[10];
		
		JPanel huJPanel = new JPanel();
		JPanel channelGridPanel = new JPanel(new GridLayout(10, 3, 20, 20));
		channelGridPanel.setPreferredSize(new Dimension(150, 400));
		huJPanel.add(channelGridPanel);

		for (int i = 0; i < 10; ++i) {
			channelGridPanel.add(new JLabel("      " + i));
			channelInstrumentInputList[i] = new JTextField(getContext().getPhantom().getInstrumentArray()[i] + "");
			channelGridPanel.add(channelInstrumentInputList[i]); channelInstrumentInputList[i].setForeground(Nota.getColorByChannel(i));

			channelVolumeInputList[i] = new JTextField(getContext().getPhantom().getVolumeArray()[i] + "");
			channelGridPanel.add(channelVolumeInputList[i]); channelVolumeInputList[i].setForeground(Nota.getColorByChannel(i));
		}
		
		int option = JOptionPane.showConfirmDialog(null, huJPanel, "Enter instruments for channels", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			for (int i = 0; i < 10; ++i) { 
				getContext().getPhantom().getInstrumentArray()[i] = Integer.parseInt(channelInstrumentInputList[i].getText()); 
				getContext().getPhantom().getVolumeArray()[i] = Integer.parseInt(channelVolumeInputList[i].getText()); 
			};
			getContext().getPhantom().syncSyntChannels();
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
