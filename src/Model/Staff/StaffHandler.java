
package Model.Staff;
import Midi.DeviceEbun;
import Midi.MidiCommon;
import Model.AbstractHandler;
import Model.Action;
import Model.Combo;
import Model.Staff.Accord.Nota.Nota;
import Musica.PlayMusThread;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StaffHandler extends AbstractHandler {
	
	public StaffHandler(Staff context) {
		super(context);
	}

	public Staff getContext() {
		return (Staff)super.getContext();
	}

	@Override
	protected void init() {
		actionMap = new LinkedHashMap<>();

		// It may not work when we have multiple staffs, i don't know how java lambdas work
		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_P), new Action().setDo((event) -> {
			if (DeviceEbun.stop) { // no need to handle ctr-z for this, cause it just generates another actions, that can be handled. Ah smart, aint i?
				PlayMusThread.shutTheFuckUp();
				DeviceEbun.stop = false;
				(new PlayMusThread(this)).start();
			} else {
				DeviceEbun.stopMusic();
			}
		}));
		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_D), new Action().setDo((event) -> {
			MidiCommon.listDevicesAndExit(false, true, false);
			DeviceEbun.changeOutDevice();
		}));
		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_0), new Action().setDo((event) -> {
			getContext().changeMode();
		}));
		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_Z), new Action().setDo((event) -> {
//				getContext().undo(); // TODO: do ctrl-Z for child - if success - break, else do ctrl-z for parent
		}));
		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_Y), new Action().setDo((event) -> {
//				getContext().redo();
		}));
		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_RIGHT), new Action().setDo((event) -> {
			getContext().moveFocus(1);
			// stan.drawPanel.checkCam();
		}));
		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_UP), new Action().setDo((event) -> {
			PlayMusThread.shutTheFuckUp();
			getContext().moveFocus(-getContext().getNotaInRowCount());
			getContext().getParentSheet().checkCam(); // O_o move it into requestNewSurface maybe?
		}));
		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_DOWN), new Action().setDo((event) -> {
			PlayMusThread.shutTheFuckUp();
			getContext().moveFocus(+getContext().getNotaInRowCount());
			getContext().getParentSheet().checkCam(); // O_o move it into requestNewSurface maybe?
		}));

		Consumer<Combo> handleMuteChannel = (e) -> {
			int cod = e.getKeyCode();
			if (cod >= '0' && cod <= '9') {
				getContext().changeChannelFlag(cod - '0');
			}
		};
		for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; ++i) { actionMap.put(new Combo(KeyEvent.ALT_MASK, i), new Action().setDo(handleMuteChannel).setUndo(handleMuteChannel)); }

		actionMap.put(new Combo(0, KeyEvent.VK_RIGHT), new Action().setDo((event) -> {
			PlayMusThread.shutTheFuckUp();
			getContext().moveFocus(1);
			// stan.drawPanel.checkCam();
		}));
		actionMap.put(new Combo(0, KeyEvent.VK_LEFT), new Action().setDo((event) -> {
			PlayMusThread.shutTheFuckUp();
			getContext().moveFocus(-1);
			// stan.drawPanel.checkCam();
		}));
		actionMap.put(new Combo(0, KeyEvent.VK_HOME), new Action().setDo((event) -> {
			PlayMusThread.shutTheFuckUp();
			getContext().setFocusedIndex(-1);
			getContext().getParentSheet().checkCam();
		}));
		actionMap.put(new Combo(0, KeyEvent.VK_END), new Action().setDo((event) -> {
			PlayMusThread.shutTheFuckUp();
			getContext().setFocusedIndex(getContext().getAccordList().size() - 1);
			getContext().getParentSheet().checkCam();
		}));
		actionMap.put(new Combo(0, KeyEvent.VK_ENTER), new Action().setDo((event) -> {
			PlayMusThread.shutTheFuckUp();
			PlayMusThread.playAccord(getContext().getFocusedAccord());
		}));
		actionMap.put(new Combo(0, KeyEvent.VK_DELETE), new Action().setDo((event) -> {
			if (getContext().getFocusedAccord() != null) {
				getContext().getAccordList().remove(getContext().focusedIndex--);
			} else {
				getContext().moveFocus(1);
			}
		}));
		actionMap.put(new Combo(0, KeyEvent.VK_MINUS), new Action().setDo((event) -> {
			if (event.getKeyCode() == '-') {
				getContext().moveFocus(1);
			}
		}));
		actionMap.put(new Combo(0, KeyEvent.VK_ESCAPE), new Action().setDo((event) -> {
			this.showMenuDialog();
		}));
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
}
