package org.shmidusic;

import org.shmidusic.sheet_music.staff.Staff;
import org.shmidusic.sheet_music.SheetMusic;
import org.shmidusic.sheet_music.SheetMusicComponent;
import org.shmidusic.stuff.OverridingDefaultClasses.Scroll;

import java.awt.*;

import javax.swing.*;


final public class MainPanel extends JPanel {

	public static int MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe? // eliminate it nahuj maybe?

	final public JPanel northPanel;

	public SheetMusicComponent sheetContainer = new SheetMusicComponent(new SheetMusic(), this);
	public Scroll sheetScroll = new Scroll(sheetContainer);

	final public PianoLayoutPanel pianoLayoutPanel;

	public MainPanel() {
		super();

		this.setLayout(new BorderLayout());

		this.add(northPanel = new JPanel(new BorderLayout()), BorderLayout.NORTH);

		this.add(sheetScroll, BorderLayout.CENTER);
		sheetScroll.getVerticalScrollBar().setUnitIncrement(Staff.SISDISPLACE);

		northPanel.add(pianoLayoutPanel = new PianoLayoutPanel(this), BorderLayout.CENTER);
	}

	public void replaceSheetMusic(SheetMusic sheetMusic)
	{
		this.sheetContainer = new SheetMusicComponent(sheetMusic, this);
		this.remove(sheetScroll);
		this.add(sheetScroll = new Scroll(sheetContainer), BorderLayout.CENTER);
		sheetScroll.getVerticalScrollBar().setUnitIncrement(Staff.SISDISPLACE);
		this.revalidate();
	}
}

