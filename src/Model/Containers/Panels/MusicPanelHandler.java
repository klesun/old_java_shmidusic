package Model.Containers.Panels;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import Gui.Settings;
import Model.*;
import Tools.FileProcessor;

public class MusicPanelHandler extends AbstractHandler {

	public MusicPanelHandler(MusicPanel context) { super(context); }

	@Override
	protected void initActionMap() {

		KeyEvent k = new KeyEvent(new JPanel(),0,0,0,0,'h'); // just for constants
		int ctrl = KeyEvent.CTRL_MASK;

		addCombo(ctrl, k.VK_E).setDo(makeSaveFileDialog(FileProcessor::savePNG, "png"));
		addCombo(ctrl, k.VK_S).setDo(makeSaveFileDialog(FileProcessor::saveJsonFile, "json"));

		addCombo(ctrl, k.VK_O).setDo(combo -> {
			JFileChooser chooser = new JFileChooser("/home/klesun/yuzefa_git/a_opuses_json/new");
			chooser.setFileFilter(new FileNameExtensionFilter("Json Midi-music data", "json"));
			int i = okcancel("Are your sure? Unsaved data will be lost."); // 2 - cancel, 0 - ok очевидно же
			if (i == 0) {
				int sVal = chooser.showOpenDialog(getContext().parentWindow);
				if (sVal == JFileChooser.APPROVE_OPTION) {
					if (chooser.getSelectedFile().getAbsolutePath().endsWith(".json")) {
						FileProcessor.openJsonFile(chooser.getSelectedFile(), getContext().getStaff());
					}
				}
			}
		});

		addCombo(ctrl, k.VK_EQUALS).setDo(Settings.inst()::scale);
		addCombo(ctrl, k.VK_MINUS).setDo(Settings.inst()::scale);
		addCombo(ctrl, k.VK_F).setDo(getContext()::switchFullscreen);

		addCombo(ctrl, k.VK_PAGE_DOWN).setDo(getContext()::page);
		addCombo(ctrl, k.VK_PAGE_UP).setDo(getContext()::page);
	}

	public void handleMidiEvent(Integer tune, int forca, int timestamp) {
		if (forca > 0) {
			this.handleKey(new Combo(11, Combo.tuneToAscii(tune))); // (11 -ctrl+shift+alt)+someKey
		} else {
			// keyup event
		}
	}

	@Override
	public Boolean mousePressedFinal(ComboMouse mouse) {
		if (mouse.leftButton) {
			getContext().requestFocus();
			return true;
		} else { return false; }
	}

	@Override
	public MusicPanel getContext() { return MusicPanel.class.cast(super.getContext()); }

	// private methods

	final private Consumer<Combo> makeSaveFileDialog(BiConsumer<File, MusicPanel> lambda, String ext) {
		JFileChooser c2 = new JFileChooser("/home/klesun/yuzefa_git/a_opuses_json/new");
		c2.setFileFilter(new FileNameExtensionFilter(ext + "-file", ext));

		return combo -> {
			int rVal = c2.showSaveDialog(getContext().parentWindow);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				File fn = c2.getSelectedFile();
				if (!fn.getAbsolutePath().endsWith("." + ext)) { fn = new File(fn + "." + ext); }
				// TODO: prompt on overwrite
				lambda.accept(fn, getContext());
			}
		};
	}

	final private static int okcancel(String theMessage) {
		int result = JOptionPane.showConfirmDialog((Component) null,
			theMessage, "alert", JOptionPane.OK_CANCEL_OPTION);
		return result;
	}
}
