
package Storyspace.Staff;
import Gui.Settings;
import Storyspace.Staff.StaffConfig.StaffConfig;
import Stuff.Midi.DeviceEbun;
import Model.AbstractHandler;
import Model.ActionFactory;
import Model.Combo;
import Storyspace.Staff.Accord.Accord;
import Storyspace.Staff.Accord.Nota.Nota;
import Stuff.Musica.PlayMusThread;
import Stuff.Tools.FileProcessor;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class StaffHandler extends AbstractHandler {
	// TODO: may be no need for this queue, cause we whatever can pass needed Nota to undo with paramsForUndo
	private LinkedList<Accord> deletedAccordQueue = new LinkedList<>();

	public StaffHandler(Staff context) { super(context); }
	public Staff getContext() {
		return Staff.class.cast(super.getContext());
	}

	private int defaultChannel = 0; // default channel for new Nota-s

	public void handleMidiEvent(Integer tune, int forca, int timestamp) {
		if (forca > 0) {
			// BEWARE: we get sometimes double messages when my synt has "LAYER/AUTO HARMONIZE" button on. That is button, that makes one key press sound with two instruments
			this.handleKey(new Combo(Combo.getAsciiTuneMods(), Combo.tuneToAscii(tune))); // (11 -ctrl+shift+alt)+someKey
		} else {
			// keyup event
		}
	}

	@Override
	protected void initActionMap() {

		JFileChooser jsonChooser = new JFileChooser("/home/klesun/yuzefa_git/a_opuses_json/");
		jsonChooser.setFileFilter(new FileNameExtensionFilter("Json Midi-music data", "json"));

		JFileChooser pngChooser = new JFileChooser();
		pngChooser.setFileFilter(new FileNameExtensionFilter("PNG images", "png"));


		KeyEvent k = new KeyEvent(new JPanel(),0,0,0,0,'h'); // just for constants
		int ctrl = k.CTRL_MASK;
		Staff s = this.getContext();

		addCombo(ctrl, k.VK_P).setDo(s::triggerPlayer);
		addCombo(ctrl, k.VK_D).setDo(DeviceEbun::changeOutDevice);
		addCombos(ctrl, Arrays.asList(k.VK_0, k.VK_9)).stream().forEach(factory -> {
			factory.setDo(s::changeMode);
		});
		addCombo(ctrl, k.VK_RIGHT).setDo(s::moveFocusUsingCombo).setUndoChangeSign();

		addCombo(ctrl, k.VK_UP).setDo(s::moveFocusRow).setUndoChangeSign();
		addCombo(ctrl, k.VK_DOWN).setDo(s::moveFocusRow).setUndoChangeSign();

		// teared from StaffPanel
		addCombo(ctrl, k.VK_EQUALS).setDo(Settings.inst()::scale);
		addCombo(ctrl, k.VK_MINUS).setDo(Settings.inst()::scale);
		addCombo(ctrl, k.VK_E).setDo(makeSaveFileDialog(FileProcessor::savePNG, pngChooser, "png"));
		// TODO: save should use same chooser as open one day
		addCombo(ctrl, k.VK_S).setDo(makeSaveFileDialog(FileProcessor::saveMusicPanel, jsonChooser, "json"));
		addCombo(ctrl, k.VK_O).setDo(combo -> {
			int sVal = jsonChooser.showOpenDialog(getContext().getParentSheet());
			if (sVal == JFileChooser.APPROVE_OPTION) {
				FileProcessor.openStaff(jsonChooser.getSelectedFile(), getContext());
			}
		});


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
			return new HashMap<String, Object>() {{
				put("lastIndex", lastIndex);
			}};
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
				handleKey(new Combo(0, k.VK_RIGHT));
				return false;
			}
		}).setUndo((combo) -> {
			s.add(deletedAccordQueue.pollLast());
			s.moveFocus(1);
		});

		addCombo(0, k.VK_ESCAPE).setDo((event) -> {
			getContext().getConfig().getDialog().showMenuDialog(StaffConfig::syncSyntChannels);
		});

		for (Integer i: Combo.getAsciTuneMap().keySet()) {
			addCombo(Combo.getAsciiTuneMods(), i).setDo((combo) -> { // 11 - alt+shif+ctrl

				// TODO: lets have a mode, that would change on "Insert" button instead of ctrl-shift-alt combination, please. thanx

				if (s.mode == Staff.aMode.passive) { return false; }
				else {
					s.addNewAccord().addNewNota(combo.asciiToTune(), getDefaultChannel());
					handleKey(new Combo(0, k.VK_RIGHT));
					return true;
				}
			});
		}
	}

	public void setDefaultChannel(int value) { this.defaultChannel = value; }
	public int getDefaultChannel() { return this.defaultChannel; }

	// private methods

	final private Consumer<Combo> makeSaveFileDialog(BiConsumer<File, Staff> lambda, JFileChooser chooser, String ext) {
		return combo -> {
			int rVal = chooser.showSaveDialog(getContext().getParentSheet());
			if (rVal == JFileChooser.APPROVE_OPTION) {
				File fn = chooser.getSelectedFile();
				if (!chooser.getFileFilter().accept(fn)) { fn = new File(fn + "." + ext); }
				// TODO: prompt on overwrite
				lambda.accept(fn, getContext());
			}
		};
	}

	private List<ActionFactory> addCombos(int keyMods, List<Integer> keyCodes) {
		List factories = new ArrayList<>();
		for (int keyCode: keyCodes) {
			factories.add(new ActionFactory(new Combo(keyMods, keyCode)).addTo(this.actionMap));
		}
		return factories;
	}
}
