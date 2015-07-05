
package BlockSpacePkg.StaffPkg;
import Model.*;
import BlockSpacePkg.StaffPkg.StaffConfig.StaffConfig;
import Stuff.Midi.DeviceEbun;
import BlockSpacePkg.StaffPkg.Accord.Accord;
import BlockSpacePkg.StaffPkg.Accord.Nota.Nota;
import Stuff.OverridingDefaultClasses.TruMap;
import Stuff.Tools.FileProcessor;

import java.io.File;
import java.util.*;
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
	}

	@Override
	public LinkedHashMap<Combo, ContextAction> getStaticActionMap() {
		LinkedHashMap<Combo, ContextAction> huj = new LinkedHashMap<>();
		huj.putAll(makeStaticActionMap()); // no, java is retarded after all
		return huj;
	}

	public static LinkedHashMap<Combo, ContextAction<Staff>> makeStaticActionMap() {

		TruMap<Combo, ContextAction<Staff>> actionMap = new TruMap<>();

		// TODO: make folder of project default path
		JFileChooser jsonChooser = new JFileChooser("/home/klesun/yuzefa_git/a_opuses_json/");
		jsonChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith(".midi.json") || f.isDirectory();
			}

			public String getDescription() {
				return "Json Midi-music data";
			}
		});

		JFileChooser pngChooser = new JFileChooser();
		pngChooser.setFileFilter(new FileNameExtensionFilter("PNG images", "png"));

		actionMap
			.p(new Combo(ctrl, k.VK_P), mkAction(Staff::triggerPlayback).setCaption("Play/Stop"))

			// TODO: maybe move these two into Scroll
			.p(new Combo(ctrl, k.VK_S), mkFailableAction(FileProcessor::saveMusicPanel).setCaption("Save StaffPkg"))
			.p(new Combo(ctrl, k.VK_O), mkFailableAction(FileProcessor::openStaff).setCaption("Open StaffPkg"))

			.p(new Combo(0, k.VK_ESCAPE), mkAction(s -> s.getConfig().getDialog().showMenuDialog(StaffConfig::syncSyntChannels))
				.setCaption("Settings"))
			.p(new Combo(0, k.VK_HOME), mkAction(s -> s.setFocusedIndex(-1)).setCaption("To Start"))
			.p(new Combo(0, k.VK_END), mkAction(s -> s.setFocusedIndex(s.getAccordList().size() - 1)).setCaption("To End"))
			.p(new Combo(0, k.VK_LEFT), mkFailableAction(s -> s.moveFocusWithPlayback(-1)).setCaption("Left"))
			.p(new Combo(0, k.VK_RIGHT), mkFailableAction(s -> s.moveFocusWithPlayback(1)).setCaption("Right"))
			.p(new Combo(0, k.VK_UP), mkFailableAction(s -> s.moveFocusRow(-1)).setCaption("Up"))
			.p(new Combo(0, k.VK_DOWN), mkFailableAction(s -> s.moveFocusRow(1)).setCaption("Down"))
			.p(new Combo(ctrl, k.VK_D), mkFailableAction(s -> DeviceEbun.changeOutDevice(s.getConfig()))
				.setCaption("Change Playback Device"))
			.p(new Combo(ctrl, k.VK_0), mkAction(s -> s.mode = Staff.aMode.passive).setCaption("Disable Input From MIDI Device"))
			.p(new Combo(ctrl, k.VK_9), mkAction(s -> s.mode = Staff.aMode.insert).setCaption("Enable Input From MIDI Device"))
			.p(new Combo(ctrl, k.VK_E), mkFailableAction(FileProcessor::savePNG).setCaption("Export png"))
			/** @legacy */
			.p(new Combo(ctrl, k.VK_W), mkAction(StaffHandler::updateDeprecatedPauses).setCaption("Convert Deprecated Pauses"))
			;

		// MIDI-key press
		for (Map.Entry<Combo, Integer> entry: Combo.getComboTuneMap().entrySet()) {
			ContextAction<Staff> action = new ContextAction<>();
			actionMap.p(entry.getKey(), action
					.setRedo(s -> { s.addNewAccord().addNewNota(entry.getValue(), s.getSettings().getDefaultChannel()); })
					.setOmitMenuBar(true));
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

	private static ContextAction<Staff> mkAction(Consumer<Staff> lambda) {
		ContextAction<Staff> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<Staff> mkFailableAction(Function<Staff, ActionResult> lambda) {
		ContextAction<Staff> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}
