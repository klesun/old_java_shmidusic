package Model.Panels;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import Gui.Settings;
import Model.*;
import Tools.FileProcessor;

public class BlockHandler implements KeyListener {

	JFileChooser chooserSave = new JFileChooser("/home/klesun/yuzefa_git/a_opuses_json/new");
	JFileChooser chooserExport = new JFileChooser();

	ArrayList<JFileChooser> fileChooserList = new ArrayList<>();

	Window window;
	SheetPanel context = null;

	protected Map<Combo, ActionFactory> actionMap = new LinkedHashMap<>();

	public BlockHandler(SheetPanel context) {
		this.context = context;
		this.window = context.parentWindow;

		chooserSave.setFileFilter(new FileNameExtensionFilter("Json Midi-music data","json"));
		chooserExport.setFileFilter(new FileNameExtensionFilter("PNG image", "png"));
		fileChooserList.add(chooserSave);
		fileChooserList.add(chooserExport);

		this.initActionMap();
	}

	private void initActionMap() {

		KeyEvent k = new KeyEvent(new JPanel(),0,0,0,0,'h'); // just for constants
		int ctrl = KeyEvent.CTRL_MASK;

		addCombo(ctrl, k.VK_E).setDo(makeSaveFileDialog(FileProcessor::savePNG, "png"));
		addCombo(ctrl, k.VK_S).setDo(makeSaveFileDialog(FileProcessor::saveJsonFile, "json"));

		addCombo(ctrl, k.VK_O).setDo(combo -> {
			int i = okcancel("Are your sure? Unsaved data will be lost."); // 2 - cancel, 0 - ok очевидно же
			if (i == 0) {
				int sVal = chooserSave.showOpenDialog(window);
				if (sVal == JFileChooser.APPROVE_OPTION) {
					if (chooserSave.getSelectedFile().getAbsolutePath().endsWith(".json")) {
						FileProcessor.openJsonFile(chooserSave.getSelectedFile(), this.context.getStaff());
					}
				}
			}
		});

		addCombo(ctrl, k.VK_EQUALS).setDo(Settings.inst()::scaleUp);
		addCombo(ctrl, k.VK_MINUS).setDo(Settings.inst()::scaleDown);
		addCombo(ctrl, k.VK_F).setDo(window::switchFullscreen);

		addCombo(ctrl, k.VK_PAGE_DOWN).setDo(context::page);
		addCombo(ctrl, k.VK_PAGE_UP).setDo(context::page);
	}

	public void handleMidiEvent(Integer tune, int forca, int timestamp) {
		if (forca > 0) {
			this.handleKey(new Combo(11, Combo.tuneToAscii(tune))); // (11 -ctrl+shift+alt)+someKey
		} else {
			// keyup event
		}
	}

	final private Consumer<Combo> makeSaveFileDialog(BiConsumer<File, SheetPanel> lambda, String ext) {
		JFileChooser c2 = fileChooserList.stream().reduce(null, (a, b)
			-> a != null && a.getFileFilter().accept(new File("huj." + ext)) ? a : b);
		return combo -> {
			int rVal = c2.showSaveDialog(window);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				File fn = c2.getSelectedFile();
				if (!fn.getAbsolutePath().endsWith("." + ext)) { fn = new File(fn + "." + ext); }
				lambda.accept(fn, this.context);
			}
		};
	}

	final private ActionFactory addCombo(int keyMods, int keyCode) {
		return new ActionFactory(new Combo(keyMods, keyCode)).addTo(this.actionMap);
	}

	final public Boolean handleKey(Combo combo) {
		Boolean result = false;
		if (context.getStaff() != null &&
			context.getStaff().gettHandler().handleKey(combo)) {
			result = true;
		} else {
			if (actionMap.containsKey(combo)) {
				Model.Action action = actionMap.get(combo).createAction();
				result = action.doDo();
			}
		}
		context.checkCam();
		return result;
	}

	final private static int okcancel(String theMessage) {
		int result = JOptionPane.showConfirmDialog((Component) null,
			theMessage, "alert", JOptionPane.OK_CANCEL_OPTION);
		return result;
	}

	@Override
	public void keyPressed(KeyEvent e) { this.handleKey(new Combo(e)); }
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}
