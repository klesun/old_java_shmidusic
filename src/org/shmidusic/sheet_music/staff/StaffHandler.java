
package org.shmidusic.sheet_music.staff;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.klesun_model.*;
import org.shmidusic.sheet_music.staff.chord.note.Note;
import org.shmidusic.sheet_music.staff.staff_config.StaffConfig;
import org.shmidusic.stuff.midi.DeviceEbun;
import org.shmidusic.sheet_music.staff.chord.Chord;
import org.shmidusic.stuff.OverridingDefaultClasses.TruMap;
import org.shmidusic.stuff.graphics.Settings;
import org.shmidusic.stuff.tools.Logger;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;
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
			.p(new Combo(0, k.VK_HOME), mkAction(p -> { p.cancelSelection(); p.setFocus(-1); }).setCaption("To Start").setPostfix(navigation))
			.p(new Combo(0, k.VK_END), mkAction(p -> { p.cancelSelection();; p.setFocus(p.staff.getChordList().size() - 1); }).setCaption("To End").setPostfix(navigation))
			.p(new Combo(ctrl, k.VK_LEFT), mkAction(p -> p.moveFocusTact(-1)).setCaption("Left Tact").setPostfix("Navigation"))
			.p(new Combo(ctrl, k.VK_RIGHT), mkAction(p -> p.moveFocusTact(1)).setCaption("Right Tact").setPostfix("Navigation"))
			.p(new Combo(k.SHIFT_MASK, k.VK_LEFT), mkAction(s -> s.moveSelectionEnd(-1)).setCaption("Setlect Left"))
			.p(new Combo(k.SHIFT_MASK, k.VK_RIGHT), mkAction(s -> s.moveSelectionEnd(1)).setCaption("Setlect Left"))
			.p(new Combo(0, k.VK_LEFT), mkFailableAction(s -> s.moveFocusWithPlayback(-1)).setCaption("Left").setPostfix(navigation))
			.p(new Combo(0, k.VK_RIGHT), mkFailableAction(s -> s.moveFocusWithPlayback(1)).setCaption("Right").setPostfix(navigation))
			.p(new Combo(0, k.VK_UP), mkFailableAction(s -> s.moveFocusRow(-1)).setCaption("Up").setPostfix(navigation))
			.p(new Combo(0, k.VK_DOWN), mkFailableAction(s -> s.moveFocusRow(1)).setCaption("Down").setPostfix(navigation))

			// TODO: move it to StaffConfig
			.p(new Combo(ctrl, k.VK_D), mkFailableAction(s -> DeviceEbun.changeOutDevice(s.staff.getConfig()))
				.setCaption("Change Playback Device"))
			.p(new Combo(ctrl, k.VK_0), mkAction(s -> s.staff.mode = Staff.aMode.passive).setCaption("Disable Input From MIDI Device"))
			.p(new Combo(ctrl, k.VK_9), mkAction(s -> s.staff.mode = Staff.aMode.insert).setCaption("Enable Input From MIDI Device"))

			.p(new Combo(ctrl, k.VK_C), mkAction(StaffHandler::copySelectedChords).setCaption("Copy Selected"))
			.p(new Combo(ctrl, k.VK_V), mkFailableAction(StaffHandler::pasteChords).setCaption("Copy Selected"))

			/** @legacy */
//			.p(new Combo(ctrl, k.VK_W), mkAction(StaffHandler::updateDeprecatedPauses).setCaption("Convert Deprecated Pauses"))
		;

		// MIDI-key press
		for (Map.Entry<Combo, Integer> entry: Combo.getComboTuneMap().entrySet()) {
			ContextAction<StaffComponent> action = new ContextAction<>();
			actionMap.p(entry.getKey(), action
				.setRedo(s -> {
					if (s.staff.mode != Staff.aMode.passive) {
						s.addNewChordWithPlayback().addNewNote(entry.getValue(), Settings.inst().getDefaultChannel());
						return new Explain(true);
					} else {
						return new Explain("Cant do, passive mode is on!");
					}
				}).setOmitMenuBar(true));
		}
	}

	public void handleMidiEvent(Integer tune, int forca, int timestamp)
	{
		if (forca > 0) {
			// BEWARE: we get sometimes double messages when my synt has "LAYER/AUTO HARMONIZE" button on. That is button, that makes one key press sound with two instruments
			this.handleKey(new Combo(Combo.getAsciiTuneMods(), Combo.tuneToAscii(tune))); // (11 -ctrl+shift+alt)+someKey
		} else {
			// keyup event
		}
	}

	private static void copySelectedChords(StaffComponent comp)
	{
		JSONArray childListJs = new JSONArray(comp.getSelectedChords().stream().map(IModel::getJsonRepresentation).toArray());
		StringSelection selection = new StringSelection(childListJs.toString(2));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
	}

	private static Explain pasteChords(StaffComponent comp)
	{
		return getTextFromClipboard()
			.ifSuccess(StaffHandler::constructChordList)
			.whenSuccess(chordList -> { chordList.forEach(comp::addChord); comp.repaint(); });
	}

	private static Explain<String> getTextFromClipboard()
	{
		try {
			String text = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
			return new Explain<>(text);
		} catch (IOException exc) {
			return new Explain<>(false, "OS Clipboard error: " + exc.getMessage());
		} catch (UnsupportedFlavorException exc) {
			Logger.fatal(exc, "Ololololo!");
			return new Explain<>(false, "Ololololo");
		}
	}

	private static Explain<List<Chord>> constructChordList(String chordListJs)
	{
		return parseJsonArray(chordListJs).ifSuccess(jsArr ->
		{
			List<Chord> result = new ArrayList<>();
			try {
				for (int i = 0; i < jsArr.length(); ++i) {
					result.add((Chord)new Chord().reconstructFromJson(jsArr.getJSONObject(i)));
				}
				return new Explain<>(result);
			} catch (JSONException exc) {
				return new Explain<>(false, "Json in your clipboard is not a chord list!");
			}
		});
	}

	private static Explain<JSONArray> parseJsonArray(String jsString)
	{
		try {
			JSONArray jsonParse = new JSONArray(jsString);
			return new Explain(jsonParse);
		} catch (JSONException exc) {
			return new Explain(false, "Failed to parse json from your clipboard - [" + exc.getMessage() + "]");
		}
	}

	@Override
	public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() { return actionMap; }

	private static ContextAction<StaffComponent> mkAction(Consumer<StaffComponent> lambda) {
		ContextAction<StaffComponent> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<StaffComponent> mkFailableAction(Function<StaffComponent, Explain> lambda) {
		ContextAction<StaffComponent> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}
