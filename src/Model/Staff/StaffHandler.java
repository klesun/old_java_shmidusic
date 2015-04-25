
package Model.Staff;
import Midi.DeviceEbun;
import Midi.MidiCommon;
import Model.AbstractHandler;
import Model.ActionFactory;
import Model.Combo;
import Model.Staff.Accord.Accord;
import Model.Staff.Accord.Nota.Nota;
import Musica.PlayMusThread;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StaffHandler extends AbstractHandler {
	// TODO: may be no need for this queue, cause we whatever can pass needed Nota to undo with paramsForUndo
	private LinkedList<Accord> deletedAccordQueue = new LinkedList<>();

	public StaffHandler(Staff context) {
		super(context);
	}

	public Staff getContext() {
		return (Staff)super.getContext();
	}

	@Override
	protected void init() {

		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_P)).addTo(actionMap).setDo((event) -> {
			if (PlayMusThread.stop) { // no need to handle ctr-z for this, cause it just generates another actions, that can be handled. Ah smart, aint i?
				PlayMusThread.shutTheFuckUp();
				PlayMusThread.stop = false;
				(new PlayMusThread(this)).start();
			} else {
				PlayMusThread.stopMusic();
			}
		});
		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_D)).addTo(actionMap).setDo((event) -> {
			MidiCommon.listDevicesAndExit(false, true, false);
			DeviceEbun.changeOutDevice();
		});
		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_0)).addTo(actionMap).setDo((event) -> {
			getContext().changeMode();
		});
		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_RIGHT)).addTo(actionMap).setDo(getContext()::moveFocusUsingCombo).setUndoChangeSign();
		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_UP)).addTo(actionMap).setDo(getContext()::moveFocusRow).setUndoChangeSign();
		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_DOWN)).addTo(actionMap).setDo(getContext()::moveFocusRow).setUndoChangeSign();

		Consumer<Combo> handleMuteChannel = (e) -> {
			int cod = e.getKeyCode();
			if (cod >= '0' && cod <= '9') {
				getContext().changeChannelFlag(cod - '0');
			}
		};
		for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; ++i) {
			new ActionFactory(new Combo(KeyEvent.ALT_MASK, i)).addTo(actionMap).setDo(handleMuteChannel).biDirectional(); }

		for (Integer i: Arrays.asList(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT)) {
			new ActionFactory(new Combo(0, i)).addTo(actionMap).setDo((event) -> {
				PlayMusThread.shutTheFuckUp();
				getContext().moveFocusUsingCombo(event);
			}).setUndoChangeSign();
		}

		new ActionFactory(new Combo(0, KeyEvent.VK_HOME)).addTo(actionMap).setDo2((event) -> {
			PlayMusThread.shutTheFuckUp();
			Integer lastIndex = getContext().getFocusedIndex();
			getContext().setFocusedIndex(-1);
			return new HashMap<String, Object>(){{ put("lastIndex", lastIndex); }};
		}).setUndo((combo, paramsForUndo) -> {
			getContext().setFocusedIndex((Integer)paramsForUndo.get("lastIndex"));
		});

		new ActionFactory(new Combo(0, KeyEvent.VK_END)).addTo(actionMap).setDo2((event) -> {
			PlayMusThread.shutTheFuckUp();
			Integer lastIndex = getContext().getFocusedIndex();
			getContext().setFocusedIndex(getContext().getAccordList().size() - 1);
			return new HashMap<String, Object>() {{
				put("lastIndex", lastIndex);
			}};
		}).setUndo((combo, paramsForUndo) -> {
			getContext().setFocusedIndex((Integer) paramsForUndo.get("lastIndex"));
		});
		new ActionFactory(new Combo(0, KeyEvent.VK_DELETE)).addTo(actionMap).setDo((event) -> {
			Accord accord = getContext().getFocusedAccord();
			if (accord != null) {
				deletedAccordQueue.add(accord);
				getContext().getAccordList().remove(getContext().focusedIndex--);
				return true;
			} else {
				return false;
			}
		}).setUndo((combo) -> {
			getContext().add(deletedAccordQueue.pollLast());
			getContext().moveFocus(1);
		});

		new ActionFactory(new Combo(0, KeyEvent.VK_ESCAPE)).addTo(actionMap).setDo((event) -> {
			this.showMenuDialog();
		});

		for (Integer i: Combo.getAsciTuneMap().keySet()) {
			new ActionFactory(new Combo(11, i)).addTo(actionMap).setDo((combo) -> { // 11 - alt+shif+ctrl

				// TODO: move stuff like constants and mode into the handler

				long timestamp = System.currentTimeMillis();

				if (getContext().mode == Staff.aMode.passive || getContext().mode == Staff.aMode.playin) {
					// Показать, какую ноту ты нажимаешь
					return false;
				} else {
					Accord newAccord = new Accord(getContext());
					new Nota(newAccord, combo.asciiToTune()).setKeydownTimestamp(timestamp);
					getContext().add(newAccord);
					handleKey(new Combo(0, KeyEvent.VK_RIGHT));
					return true;
				}
			});
		}
	}

	public void showMenuDialog() {

		// TODO: prevent typing more than 100%
		
		JTextField[] channelInstrumentInputList = new JTextField[10];
		JTextField[] channelVolumeInputList = new JTextField[10];
		
		JPanel huJPanel = new JPanel();
		JPanel channelGridPanel = new JPanel(new GridLayout(10, 3, 20, 20));
		channelGridPanel.setPreferredSize(new Dimension(150, 400));
		huJPanel.add(channelGridPanel);

		for (int i = 0; i < 10; ++i) {
			channelGridPanel.add(new JLabel("      " + i));
			channelInstrumentInputList[i] = new JTextField(getContext().getConfig().getInstrumentArray()[i] + "");
			channelGridPanel.add(channelInstrumentInputList[i]); channelInstrumentInputList[i].setForeground(Nota.getColorByChannel(i));

			channelVolumeInputList[i] = new JTextField(getContext().getConfig().getVolumeArray()[i] + "");
			channelGridPanel.add(channelVolumeInputList[i]); channelVolumeInputList[i].setForeground(Nota.getColorByChannel(i));
		}
		
		int option = JOptionPane.showConfirmDialog(null, huJPanel, "Enter instruments for channels", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			for (int i = 0; i < 10; ++i) {
				getContext().getConfig().getInstrumentArray()[i] = Integer.parseInt(channelInstrumentInputList[i].getText());
				getContext().getConfig().getVolumeArray()[i] = Integer.parseInt(channelVolumeInputList[i].getText());
			};
			getContext().getConfig().syncSyntChannels();
		}
	}
}
