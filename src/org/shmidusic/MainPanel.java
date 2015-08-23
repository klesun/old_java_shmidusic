package org.shmidusic;

import org.shmidusic.sheet_music.staff.Staff;
import org.shmidusic.sheet_music.SheetMusic;
import org.shmidusic.sheet_music.SheetMusicComponent;
import org.shmidusic.sheet_music.staff.StaffComponent;
import org.shmidusic.sheet_music.staff.StaffHandler;
import org.shmidusic.stuff.Midi.DumpReceiver;
import org.shmidusic.stuff.OverridingDefaultClasses.Scroll;
import org.shmidusic.stuff.tools.Fp;

import java.awt.*;
import java.text.DecimalFormat;

import javax.swing.*;


final public class MainPanel extends JPanel {

	public static int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?

	final public JPanel northPanel;

	public SheetMusicComponent sheetContainer = new SheetMusicComponent(new SheetMusic(), this);
	public Scroll sheetScroll = new Scroll(sheetContainer);

	final private PianoLayoutPanel pianoLayoutPanel;
	final private JTextField statusField = new JTextField(null, "status text", 20);

	public MainPanel() {
		super();

		this.setLayout(new BorderLayout());

		this.add(northPanel = new JPanel(new BorderLayout()), BorderLayout.NORTH);

		this.add(sheetScroll, BorderLayout.CENTER);
		sheetScroll.getVerticalScrollBar().setUnitIncrement(Staff.SISDISPLACE);

		northPanel.add(pianoLayoutPanel = new PianoLayoutPanel(this), BorderLayout.CENTER);

		statusField.setEditable(false);
		northPanel.add(statusField, BorderLayout.EAST);
	}

	public void replaceSheetMusic(SheetMusic sheetMusic)
	{
		this.sheetContainer = new SheetMusicComponent(sheetMusic, this);
		this.remove(sheetScroll);
		this.add(sheetScroll = new Scroll(sheetContainer), BorderLayout.CENTER);
		sheetScroll.getVerticalScrollBar().setUnitIncrement(Staff.SISDISPLACE);
		sheetContainer.requestFocus();
		DumpReceiver.eventHandler = (StaffHandler) sheetContainer.getFocusedChild().getHandler();

		this.revalidate();
	}

	public void chordChanged() {
		pianoLayoutPanel.repaint();
		statusField.setText(getStatusText());
		sheetContainer.checkCam();
	}

	private String getStatusText()
	{
		String result = "status | ";

		StaffComponent staffComp = sheetContainer.getFocusedChild();
		if (staffComp.getFocusedChild() != null) {
			double seconds = staffComp.getFocusedChild().determineStartTimestamp();
			result += "time: " + seconds + " seconds";
		} else {
			result += "chord not focused";
		}

		return result;
	}
}

