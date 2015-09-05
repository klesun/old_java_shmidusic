package org.shmidusic;

import org.shmidusic.sheet_music.staff.Staff;
import org.shmidusic.sheet_music.SheetMusic;
import org.shmidusic.sheet_music.SheetMusicComponent;
import org.shmidusic.sheet_music.staff.StaffComponent;
import org.shmidusic.sheet_music.staff.StaffHandler;
import org.shmidusic.stuff.midi.DumpReceiver;
import org.shmidusic.stuff.OverridingDefaultClasses.Scroll;

import java.awt.*;

import javax.swing.*;


final public class MainPanel extends JPanel {

	public static int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?

	final public JPanel northPanel;

	public SheetMusicComponent sheetContainer = new SheetMusicComponent(new SheetMusic(), this);
	public Scroll sheetScroll = new Scroll(sheetContainer);

	final private PianoLayoutPanel pianoLayoutPanel;
	final private JLabel statusField = new JLabel("status text");

	public MainPanel() {
		super();

		this.setLayout(new BorderLayout());

		this.add(northPanel = new JPanel(new BorderLayout()), BorderLayout.NORTH);

		this.add(sheetScroll, BorderLayout.CENTER);
		sheetScroll.getVerticalScrollBar().setUnitIncrement(Staff.SISDISPLACE);
		// removing stupid built-ins
		InputMap im = sheetScroll.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(KeyStroke.getKeyStroke("UP"), "none");
		im.put(KeyStroke.getKeyStroke("DOWN"), "none");
		im.put(KeyStroke.getKeyStroke("PAGE_UP"), "none");
		im.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "none");

		northPanel.add(pianoLayoutPanel = new PianoLayoutPanel(this), BorderLayout.CENTER);

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
		// removing stupid built-ins
		InputMap im = sheetScroll.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(KeyStroke.getKeyStroke("UP"), "none");
		im.put(KeyStroke.getKeyStroke("DOWN"), "none");
		im.put(KeyStroke.getKeyStroke("PAGE_UP"), "none");
		im.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "none");

		this.revalidate();
        this.repaint();
	}

	public void chordChanged() {
		pianoLayoutPanel.paintImmediately(pianoLayoutPanel.getVisibleRect()); // simple repaint() would sometimes lag for a half second, what was not likable
		// this may also start lagging, but only if you unfocus window
		statusField.setText(getStatusText());
		statusField.paintImmediately(statusField.getVisibleRect());
		sheetContainer.checkCam();
	}

	private String getStatusText()
	{
		String result = "" +
			"" +
			"" +
			"" +
			"";

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

