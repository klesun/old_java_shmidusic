
package org.sheet_midusic.staff;
import org.klesun_model.AbstractHandler;
import org.klesun_model.Combo;
import org.klesun_model.ContextAction;
import org.klesun_model.Explain;
import org.sheet_midusic.staff.staff_config.StaffConfig;
import org.sheet_midusic.staff.staff_panel.StaffComponent;
import org.sheet_midusic.stuff.Midi.DeviceEbun;
import org.sheet_midusic.staff.chord.Chord;
import org.sheet_midusic.staff.chord.nota.Nota;
import org.sheet_midusic.stuff.OverridingDefaultClasses.TruMap;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class StaffHandler extends AbstractHandler {

	public StaffHandler(StaffComponent context) { super(context); }
	public StaffComponent getContext() {
		return (StaffComponent)super.getContext();
	}

	private static TruMap<Combo, ContextAction<StaffComponent>> actionMap = new TruMap<>();
	static {
		JFileChooser pngChooser = new JFileChooser();
		pngChooser.setFileFilter(new FileNameExtensionFilter("PNG images", "png"));

		String navigation = "navigation";

		actionMap
//			.p(new Combo(ctrl, k.VK_M), mkFailableAction(FileProcessor::openJMusic).setCaption("Open JMusic project"))
//			.p(new Combo(ctrl, k.VK_K), mkFailableAction(FileProcessor::openJMusic2).setCaption("Open JMusic project (with rounding)"))

			.p(new Combo(0, k.VK_ESCAPE), mkAction(p -> p.staff.getConfig().getDialog().showMenuDialog(StaffConfig::syncSyntChannels))
				.setCaption("Settings").setPostfix(navigation))

			// Navigation
			.p(new Combo(0, k.VK_HOME), mkAction(p -> p.staff.setFocusedIndex(-1)).setCaption("To Start").setPostfix(navigation))
			.p(new Combo(0, k.VK_END), mkAction(p -> p.staff.setFocusedIndex(p.staff.getChordList().size() - 1)).setCaption("To End").setPostfix(navigation))
			.p(new Combo(ctrl, k.VK_LEFT), mkAction(p -> p.staff.moveFocusTact(-1)).setCaption("Left Tact").setPostfix("Navigation"))
			.p(new Combo(ctrl, k.VK_RIGHT), mkAction(p -> p.staff.moveFocusTact(1)).setCaption("Right Tact").setPostfix("Navigation"))
			.p(new Combo(0, k.VK_LEFT), mkFailableAction(s -> s.staff.moveFocusWithPlayback(-1)).setCaption("Left").setPostfix(navigation))
			.p(new Combo(0, k.VK_RIGHT), mkFailableAction(s -> s.staff.moveFocusWithPlayback(1)).setCaption("Right").setPostfix(navigation))
			.p(new Combo(0, k.VK_UP), mkFailableAction(s -> s.staff.moveFocusRow(-1, s.getWidth())).setCaption("Up").setPostfix(navigation))
			.p(new Combo(0, k.VK_DOWN), mkFailableAction(s -> s.staff.moveFocusRow(1, s.getWidth())).setCaption("Down").setPostfix(navigation))

			// TODO: move it to StaffConfig
			.p(new Combo(ctrl, k.VK_D), mkFailableAction(s -> DeviceEbun.changeOutDevice(s.staff.getConfig()))
				.setCaption("Change Playback Device"))
			.p(new Combo(ctrl, k.VK_0), mkAction(s -> s.staff.mode = Staff.aMode.passive).setCaption("Disable Input From MIDI Device"))
			.p(new Combo(ctrl, k.VK_9), mkAction(s -> s.staff.mode = Staff.aMode.insert).setCaption("Enable Input From MIDI Device"))

			/** @legacy */
//			.p(new Combo(ctrl, k.VK_W), mkAction(StaffHandler::updateDeprecatedPauses).setCaption("Convert Deprecated Pauses"))
		;

		// MIDI-key press
		for (Map.Entry<Combo, Integer> entry: Combo.getComboTuneMap().entrySet()) {
			ContextAction<StaffComponent> action = new ContextAction<>();
			actionMap.p(entry.getKey(), action
				.setRedo(s -> {
					if (s.staff.mode != Staff.aMode.passive) {
						s.addNewChordWithPlayback().addNewNota(entry.getValue(), s.getSettings().getDefaultChannel());
						return new Explain(true);
					} else {
						return new Explain("Cant do, passive mode is on!");
					}
				}).setOmitMenuBar(true));
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
		for (Chord chord : staff.getChordList()) {
			Nota oldPause = chord.findByTuneAndChannel(36, 0);
			if (oldPause != null) {
				chord.remove(oldPause);
				chord.addNewNota(0, 0).setLength(oldPause.length.get()).isTriplet.set(oldPause.isTriplet.get());
			}
		}
	}

	private static ContextAction<StaffComponent> mkAction(Consumer<StaffComponent> lambda) {
		ContextAction<StaffComponent> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<StaffComponent> mkFailableAction(Function<StaffComponent, Explain> lambda) {
		ContextAction<StaffComponent> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}
