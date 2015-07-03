
package Storyspace.Staff;
import Gui.Settings;
import Model.*;
import Storyspace.Staff.StaffConfig.StaffConfig;
import Stuff.Midi.DeviceEbun;
import Storyspace.Staff.Accord.Accord;
import Storyspace.Staff.Accord.Nota.Nota;
import Stuff.Midi.Playback;
import Stuff.Musica.PlayMusThread;
import Stuff.OverridingDefaultClasses.TruHashMap;
import Stuff.Tools.FileProcessor;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class StaffHandler extends AbstractHandler {
	// TODO: may be no need for this queue, cause we whatever can pass needed Nota to undo with paramsForUndo
	@Deprecated
	private LinkedList<Accord> deletedAccordQueue = new LinkedList<>();

	public StaffHandler(Staff context) { super(context); }
	public Staff getContext() {
		return Staff.class.cast(super.getContext());
	}

	@Deprecated
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

		for (Map.Entry<Combo, ContextAction<Staff>> entry: makeStaticActionMap().entrySet()) {
			new ActionFactory(entry.getKey()).addTo(this.actionMap).setDo(() -> entry.getValue().redo(getContext()).isSuccess());
		}

//		JFileChooser chooser = new JFileChooser("/home/klesun/yuzefa_git/a_opuses_json/");
//		jsonChooser.setFileFilter(new FileFilter() {
//			public boolean accept(File f) { 	return f.getAbsolutePath().endsWith(".midi.json") || f.isDirectory(); }
//			public String getDescription() { return "Json Midi-music data"; }
//		});
//
//		JFileChooser pngChooser = new JFileChooser();
//		pngChooser.setFileFilter(new FileNameExtensionFilter("PNG images", "png"));
//
//		Staff s = this.getContext();
//
//		addCombo(ctrl, k.VK_P).setDo(s::triggerPlayer);
//		addCombo(ctrl, k.VK_D).setDo(() -> {
//			DeviceEbun.changeOutDevice(getContext().getConfig());
//
//		});
//		addCombos(ctrl, Arrays.asList(k.VK_0, k.VK_9)).stream().forEach(factory -> factory.setDo(s::changeMode));
//		addCombo(ctrl, k.VK_RIGHT).setDo(s::moveFocusUsingCombo).setUndoChangeSign();
//
//		addCombo(ctrl, k.VK_UP).setDo(s::moveFocusRow).setUndoChangeSign();
//		addCombo(ctrl, k.VK_DOWN).setDo(s::moveFocusRow).setUndoChangeSign();
//
//		// teared from StaffPanel
//		addCombo(ctrl, k.VK_EQUALS).setDo(() -> Settings.inst().scale(+1));
//		addCombo(ctrl, k.VK_MINUS).setDo(() -> Settings.inst().scale(-1));
//
//		addCombo(ctrl, k.VK_E).setDo(makeSaveFileDialog(FileProcessor::savePNG, pngChooser, "png"));
//
//		addCombo(ctrl, k.VK_F).setDo(() -> {
//			return Settings.inst().scale(getContext().getParentSheet().getStoryspaceScroll().isFullscreen() ? -1 : 1);  // we don't want to interrupt parent's action, just do something before
//		});
//
//		// TODO: save should use same chooser as open one day
//		addCombo(ctrl, k.VK_S).setDo(makeSaveFileDialog(FileProcessor::saveMusicPanel, jsonChooser, "midi.json"));
//		addCombo(ctrl, k.VK_O).setDo(combo -> {
//			int sVal = jsonChooser.showOpenDialog(getContext().getParentSheet());
//			if (sVal == JFileChooser.APPROVE_OPTION) {
//				FileProcessor.openStaff(jsonChooser.getSelectedFile(), getContext());
//			}
//		});
//
//		addCombo(ctrl, k.VK_W).setDo(() -> updateDeprecatedPauses(s));
//
//
//		for (Integer i: Arrays.asList(k.VK_LEFT, k.VK_RIGHT)) {
//			addCombo(0, i).setDo((event) -> {
//				PlayMusThread.shutTheFuckUp();
//				s.moveFocusUsingCombo(event);
//			}).setUndoChangeSign();
//		}
//
//		addCombo(0, k.VK_HOME).setDo2((event) -> {
//			PlayMusThread.shutTheFuckUp();
//			Integer lastIndex = s.getFocusedIndex();
//			s.setFocusedIndex(-1);
//			return new HashMap<String, Object>() {{
//				put("lastIndex", lastIndex);
//			}};
//		}).setUndo((combo, paramsForUndo) -> {
//			s.setFocusedIndex((Integer) paramsForUndo.get("lastIndex"));
//		});
//
//		addCombo(0, k.VK_END).setDo2((event) -> {
//			PlayMusThread.shutTheFuckUp();
//			Integer lastIndex = s.getFocusedIndex();
//			s.setFocusedIndex(s.getAccordList().size() - 1);
//			return new HashMap<String, Object>() {{
//				put("lastIndex", lastIndex);
//			}};
//		}).setUndo((combo, paramsForUndo) -> {
//			s.setFocusedIndex((Integer) paramsForUndo.get("lastIndex"));
//		});
//
//		addCombo(0, k.VK_DELETE).setDo((event) -> {
//			Accord accord = s.getFocusedAccord();
//			if (accord != null) {
//				deletedAccordQueue.add(accord);
//				s.getAccordList().remove(s.focusedIndex--);
//				return true;
//			} else {
//				handleKey(new Combo(0, k.VK_RIGHT));
//				return false;
//			}
//		}).setUndo((combo) -> {
//			s.add(deletedAccordQueue.pollLast());
//			s.moveFocus(1);
//		});
//
//		addCombo(0, k.VK_ESCAPE).setDo((event) -> {
//			getContext().getConfig().getDialog().showMenuDialog(StaffConfig::syncSyntChannels);
//		});
//
//		for (Integer i: Combo.getAsciTuneMap().keySet()) {
//			addCombo(Combo.getAsciiTuneMods(), i).setOmitMenuBar(true).setDo((combo) -> { // 11 - alt+shif+ctrl
//
//				// TODO: lets have a mode, that would change on "Insert" button instead of ctrl-shift-alt combination, please. thanx
//
//				if (s.mode == Staff.aMode.passive) {
//					return false;
//				} else {
//					s.addNewAccord().addNewNota(combo.asciiToTune(), getDefaultChannel());
//					handleKey(new Combo(0, k.VK_RIGHT));
//					return true;
//				}
//			});
//		}
	}

	public static Map<Combo, ContextAction<Staff>> makeStaticActionMap() {

		TruHashMap<Combo, ContextAction<Staff>> actionMap = new TruHashMap<>();

		// TODO: make folder of project default path
		JFileChooser jsonChooser = new JFileChooser("/home/klesun/yuzefa_git/a_opuses_json/");
		jsonChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) { 	return f.getAbsolutePath().endsWith(".midi.json") || f.isDirectory(); }
			public String getDescription() { return "Json Midi-music data"; }
		});

		JFileChooser pngChooser = new JFileChooser();
		pngChooser.setFileFilter(new FileNameExtensionFilter("PNG images", "png"));

		actionMap
			.p(new Combo(ctrl, k.VK_P), mkAction(Staff::triggerPlayback))
			.p(new Combo(ctrl, k.VK_D), mkFailableAction(s -> DeviceEbun.changeOutDevice(s.getConfig())))
			.p(new Combo(ctrl, k.VK_MINUS), mkFailableAction(s -> Settings.inst().scale(-1)))
			.p(new Combo(ctrl, k.VK_EQUALS), mkFailableAction(s -> Settings.inst().scale(+1)))
			.p(new Combo(ctrl, k.VK_0), mkAction(s -> s.mode = Staff.aMode.passive))
			.p(new Combo(ctrl, k.VK_9), mkAction(s -> s.mode = Staff.aMode.insert))
			.p(new Combo(0, k.VK_ESCAPE), mkAction(s -> s.getConfig().getDialog().showMenuDialog(StaffConfig::syncSyntChannels)))
			.p(new Combo(0, k.VK_LEFT), mkFailableAction(s -> s.moveFocusWithPlayback(-1)))
			.p(new Combo(0, k.VK_RIGHT), mkFailableAction(s -> s.moveFocusWithPlayback(1)))
			.p(new Combo(0, k.VK_UP), mkFailableAction(s -> s.moveFocusRow(-1)))
			.p(new Combo(0, k.VK_DOWN), mkFailableAction(s -> s.moveFocusRow(1)))
			.p(new Combo(0, k.VK_HOME), mkAction(s -> s.setFocusedIndex(-1)))
			.p(new Combo(0, k.VK_END), mkAction(s -> s.setFocusedIndex(s.getAccordList().size() - 1)))
			.p(new Combo(ctrl, k.VK_UP), mkFailableAction(s -> s.moveFocusRow(-1)))
			.p(new Combo(ctrl, k.VK_DOWN), mkFailableAction(s -> s.moveFocusRow(1)))
			.p(new Combo(ctrl, k.VK_F), mkFailableAction(s -> Settings.inst().scale(s.getParentSheet().getStoryspaceScroll().isFullscreen() ? -1 : 1)))
			.p(new Combo(ctrl, k.VK_E), mkFailableAction(FileProcessor::savePNG))
			.p(new Combo(ctrl, k.VK_S), mkFailableAction(FileProcessor::saveMusicPanel))
			.p(new Combo(ctrl, k.VK_O), mkFailableAction(FileProcessor::openStaff))
			/** @legacy */
			.p(new Combo(ctrl, k.VK_W), mkAction(StaffHandler::updateDeprecatedPauses))
			;

		// MIDI-key press
		for (Map.Entry<Combo, Integer> entry: Combo.getComboTuneMap().entrySet()) {
			ContextAction<Staff> action = new ContextAction<>();
			actionMap.p(entry.getKey(), action.setRedo(s -> { s.addNewAccord().addNewNota(entry.getValue(), Settings.inst().getDefaultChannel()); }));
		}

		return actionMap;
	}

	/** @legacy */
	private static void updateDeprecatedPauses(Staff staff) {
		for (Accord accord: staff.getAccordList()) {
			Nota oldPause = accord.findByTuneAndChannel(36, 0);
			if (oldPause != null) {
				accord.remove(oldPause);
				accord.addNewNota(0, 0).setLength(oldPause.length.get()).setTupletDenominator(oldPause.getTupletDenominator());
			}
		}
	}

	// stupid java, stupid lambdas don't see stupid generics! that's why i was forced to create separate method to do it in one line
	private static ContextAction<Staff> mkAction(Consumer<Staff> lambda) {
		ContextAction<Staff> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<Staff> mkFailableAction(Function<Staff, ActionResult> lambda) {
		ContextAction<Staff> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}
