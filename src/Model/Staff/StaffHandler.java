
package Model.Staff;
import Gui.ImageStorage;
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
import java.util.*;
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

		KeyEvent k = new KeyEvent(getContext().getParentSheet(),0,0,0,0,'h'); // just for constants
		int ctrl = k.CTRL_MASK;
		Staff s = this.getContext();

		addCombo(ctrl, k.VK_P).setDo((event) -> {
			if (PlayMusThread.stop) { // no need to handle ctr-z for this, cause it just generates another actions, that can be handled. Ah smart, amn't i?
				PlayMusThread.shutTheFuckUp();
				PlayMusThread.stop = false;
				(new PlayMusThread(this)).start();
			} else {
				PlayMusThread.stopMusic();
			}
		});
		addCombo(ctrl, k.VK_D).setDo((event) -> {
			MidiCommon.listDevicesAndExit(false, true, false);
			DeviceEbun.changeOutDevice();
		});
		addCombos(ctrl, Arrays.asList(k.VK_0, k.VK_9)).stream().forEach(factory -> { factory.setDo(s::changeMode); });
		addCombo(ctrl, k.VK_RIGHT).setDo(s::moveFocusUsingCombo).setUndoChangeSign();
		addCombo(ctrl, k.VK_UP).setDo(s::moveFocusRow).setUndoChangeSign();
		addCombo(ctrl, k.VK_DOWN).setDo(s::moveFocusRow).setUndoChangeSign();

		Consumer<Combo> handleMuteChannel = (e) -> {
			int cod = e.getKeyCode();
			if (cod >= '0' && cod <= '9') {
				s.changeChannelFlag(cod - '0');
			}
		};
		for (int i = k.VK_0; i <= k.VK_9; ++i) {
			addCombo(k.ALT_MASK, i).setDo(handleMuteChannel).biDirectional(); }

		for (Integer i: Arrays.asList(k.VK_LEFT, k.VK_RIGHT)) {
			addCombo(0, i).setDo((event) -> {
				PlayMusThread.shutTheFuckUp();
				s.moveFocusUsingCombo(event);
			}).setUndoChangeSign();
		}

		addCombo(0, k.VK_HOME).setDo2((event) -> {
			PlayMusThread.shutTheFuckUp();
			Integer lastIndex = s.getFocusedIndex();
			s.setFocusedIndex(-1);
			return new HashMap<String, Object>(){{ put("lastIndex", lastIndex); }};
		}).setUndo((combo, paramsForUndo) -> {
			s.setFocusedIndex((Integer) paramsForUndo.get("lastIndex"));
		});

		addCombo(0, k.VK_END).setDo2((event) -> {
			PlayMusThread.shutTheFuckUp();
			Integer lastIndex = s.getFocusedIndex();
			s.setFocusedIndex(s.getAccordList().size() - 1);
			return new HashMap<String, Object>() {{
				put("lastIndex", lastIndex);
			}};
		}).setUndo((combo, paramsForUndo) -> {
			s.setFocusedIndex((Integer) paramsForUndo.get("lastIndex"));
		});
		addCombo(0, k.VK_DELETE).setDo((event) -> {
			Accord accord = s.getFocusedAccord();
			if (accord != null) {
				deletedAccordQueue.add(accord);
				s.getAccordList().remove(s.focusedIndex--);
				return true;
			} else {
				return false;
			}
		}).setUndo((combo) -> {
			s.add(deletedAccordQueue.pollLast());
			s.moveFocus(1);
		});

		addCombo(0, k.VK_ESCAPE).setDo((event) -> {
			this.showMenuDialog();
		});

		for (Integer i: Combo.getAsciTuneMap().keySet()) {
			addCombo(11, i).setDo((combo) -> { // 11 - alt+shif+ctrl

				// TODO: move stuff like constants and mode into the handler

				long timestamp = System.currentTimeMillis();

				if (s.mode == Staff.aMode.passive) { return false; }
				else {
					Accord newAccord = new Accord(s);
					new Nota(newAccord, combo.asciiToTune()).setKeydownTimestamp(timestamp);
					s.add(newAccord);
					handleKey(new Combo(0, k.VK_RIGHT));
					return true;
				}
			});
		}
	}

	private ActionFactory addCombo(int keyMods, int keyCode) {
		return new ActionFactory(new Combo(keyMods, keyCode)).addTo(this.actionMap);
	}

	private List<ActionFactory> addCombos(int keyMods, List<Integer> keyCodes) {
		List factories = new ArrayList<>();
		for (int keyCode: keyCodes) {
			factories.add(new ActionFactory(new Combo(keyMods, keyCode)).addTo(this.actionMap));
		}
		return factories;
	}

	// TODO: move this method's implementation into StaffConfig class maybe?
	public void showMenuDialog() {

		// TODO: use float instead of %
		
		JTextField[] channelInstrumentInputList = new JTextField[10];
		JTextField[] channelVolumeInputList = new JTextField[10];
		
		JPanel huJPanel = new JPanel();
		JPanel channelGridPanel = new JPanel(new GridLayout(10, 3, 20, 20));
		channelGridPanel.setPreferredSize(new Dimension(150, 400));
		huJPanel.add(channelGridPanel);

		for (int i = 0; i < 10; ++i) {
			channelGridPanel.add(new JLabel("      " + i));
			channelInstrumentInputList[i] = new JTextField(getContext().getConfig().getInstrumentArray()[i] + "");
			channelGridPanel.add(channelInstrumentInputList[i]); channelInstrumentInputList[i].setForeground(ImageStorage.getColorByChannel(i));

			channelVolumeInputList[i] = new JTextField(getContext().getConfig().getVolumeArray()[i] + "");
			channelGridPanel.add(channelVolumeInputList[i]); channelVolumeInputList[i].setForeground(ImageStorage.getColorByChannel(i));
		}
		
		int option = JOptionPane.showConfirmDialog(null, huJPanel, "Enter instruments for channels", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			for (int i = 0; i < 10; ++i) {
				int instrument = Integer.parseInt(channelInstrumentInputList[i].getText());
				instrument = instrument > 127 ? 127 : instrument < 0 ? 0 : instrument;
				int volume = Integer.parseInt(channelVolumeInputList[i].getText());
				volume = volume > 100 ? 100 : volume < 0 ? 0 : volume;

				getContext().getConfig().getInstrumentArray()[i] = instrument;
				getContext().getConfig().getVolumeArray()[i] = volume;
			};
			getContext().getConfig().syncSyntChannels();
		}
	}
}
