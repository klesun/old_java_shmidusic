
package blockspace.staff;
import model.*;
import blockspace.staff.StaffConfig.StaffConfig;
import stuff.Midi.DeviceEbun;
import blockspace.staff.accord.Accord;
import blockspace.staff.accord.nota.Nota;
import stuff.OverridingDefaultClasses.TruMap;
import stuff.tools.FileProcessor;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class StaffHandler extends AbstractHandler {

	public StaffHandler(Staff context) { super(context); }
	public Staff getContext() {
		return Staff.class.cast(super.getContext());
	}

	private static TruMap<Combo, ContextAction<Staff>> actionMap = new TruMap<>();
	static {
		JFileChooser pngChooser = new JFileChooser();
		pngChooser.setFileFilter(new FileNameExtensionFilter("PNG images", "png"));

		actionMap
			.p(new Combo(ctrl, k.VK_P), mkAction(Staff::triggerPlayback).setCaption("Play/Stop"))

				// TODO: maybe move these two into Scroll
			.p(new Combo(ctrl, k.VK_S), mkFailableAction(FileProcessor::saveMusicPanel).setCaption("Save"))
			.p(new Combo(ctrl, k.VK_O), mkFailableAction(FileProcessor::openStaff).setCaption("Open"))
			.p(new Combo(ctrl, k.VK_M), mkFailableAction(FileProcessor::openJMusic).setCaption("Open JMusic project"))
			.p(new Combo(ctrl, k.VK_K), mkFailableAction(FileProcessor::openJMusic2).setCaption("Open JMusic project (with rounding)"))

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
	}

	public void handleMidiEvent(Integer tune, int forca, int timestamp) {
		if (forca > 0) {
			// BEWARE: we get sometimes double messages when my synt has "LAYER/AUTO HARMONIZE" button on. That is button, that makes one key press sound with two instruments
			this.handleKey(new Combo(Combo.getAsciiTuneMods(), Combo.tuneToAscii(tune))); // (11 -ctrl+shift+alt)+someKey
		} else {
			// keyup event
		}
	}

	@Override
	public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() { return actionMap; }

	/** @legacy */
	private static void updateDeprecatedPauses(Staff staff) {
		for (Accord accord: staff.getAccordList()) {
			Nota oldPause = accord.findByTuneAndChannel(36, 0);
			if (oldPause != null) {
				accord.remove(oldPause);
				accord.addNewNota(0, 0).setLength(oldPause.length.get()).isTriplet.set(oldPause.isTriplet.get());
			}
		}
	}

	private static ContextAction<Staff> mkAction(Consumer<Staff> lambda) {
		ContextAction<Staff> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<Staff> mkFailableAction(Function<Staff, Explain> lambda) {
		ContextAction<Staff> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}
