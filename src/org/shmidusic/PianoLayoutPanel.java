package org.shmidusic;


import org.klesun_model.Combo;
import org.shmidusic.sheet_music.staff.Staff;
import org.shmidusic.sheet_music.staff.chord.Chord;
import org.shmidusic.stuff.graphics.Constants;
import org.shmidusic.stuff.graphics.ImageStorage;
import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.stuff.tools.Fp;
import org.shmidusic.stuff.tools.INote;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class PianoLayoutPanel extends JPanel
{
	final private static int TUNE_COUNT = 72; // 6 octaves
	final public static int FIRST_TUNE = 24;

	final private static int IVORY_WIDTH = 10;
	final private static int EBONY_WIDTH = IVORY_WIDTH * 3/5;

	final private static int IVORY_LENGTH = Constants.NORMAL_NOTE_HEIGHT * 3/5;
	final private static int EBONY_LENGTH = IVORY_LENGTH * 3/5;

	final private MainPanel mainPanel;

	public PianoLayoutPanel(MainPanel mainPanel)
	{
		this.mainPanel = mainPanel;
		this.addMouseListener(Fp.onClick(e -> {
			if (e.getX() < getPreferredSize().width) { // i'm not greedy, these Note-s just does not fit on Staff
				Combo notePressed = new Combo(KeyEvent.ALT_MASK, Combo.tuneToAscii(getPressed(e)));
				mainPanel.sheetContainer.getHandler().handleKey(notePressed);
			}
		}));
	}

	/** @return - tune of piano key pressed */
	private int getPressed(MouseEvent e)
	{
		int tune = FIRST_TUNE + INote.fromIvory(e.getX() / IVORY_WIDTH);
		if (e.getY() < EBONY_LENGTH) {
			tune -= e.getX() % IVORY_WIDTH < EBONY_WIDTH / 2 ? 1 : 0;
			tune += e.getX() % IVORY_WIDTH > IVORY_WIDTH - EBONY_WIDTH / 2 ? 1 : 0;
		}

		return tune;
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(IVORY_WIDTH * INote.ivoryIndex(TUNE_COUNT), IVORY_LENGTH);
	};

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		drawVanBascoLikePianoLayout(g, getNoteSet());
	}

	// draws such piano layout so it fitted to Rectangle r
	private static void drawVanBascoLikePianoLayout(Graphics g, Set<INote> highlightEm)
	{
		Rectangle baseRect = new Rectangle(0, 0, IVORY_WIDTH * INote.ivoryIndex(TUNE_COUNT), IVORY_LENGTH);

		// performance
		g.setColor(Color.WHITE);
		g.fillRect(baseRect.x, baseRect.y, baseRect.width, baseRect.height);
		// style
		g.setColor(new Color(225,225,225));
		g.fillRect(baseRect.x, baseRect.y + baseRect.height - 3, baseRect.width, 3);

		IntStream ivoryTunes = IntStream.range(FIRST_TUNE, FIRST_TUNE + TUNE_COUNT).filter(t -> !INote.isEbony(t));
		IntStream ebonyTunes = IntStream.range(FIRST_TUNE, FIRST_TUNE + TUNE_COUNT).filter(INote::isEbony);

		ivoryTunes.forEach(tune -> {
			int ivoryIndex = INote.ivoryIndex(tune) - INote.ivoryIndex(FIRST_TUNE);
			int pos = baseRect.x + ivoryIndex * IVORY_WIDTH;

			Rectangle keyRect = new Rectangle(pos, baseRect.x, IVORY_WIDTH, baseRect.height);

			g.setColor(Color.BLACK);
			g.drawRect(keyRect.x, keyRect.y, keyRect.width, keyRect.height);

			if (highlightEm.stream().anyMatch(n -> n.getTune() == tune)) {
				int channel = highlightEm.stream().filter(n -> n.getTune() == tune).findAny().get().getChannel();
				g.setColor(ImageStorage.getColorByChannel(channel));
				g.fillRect(keyRect.x + 2, keyRect.y + 2, keyRect.width - 3, keyRect.height - 3);
			}
		});

		ebonyTunes.forEach(tune -> {
			int ivoryNeighborIndex = INote.ivoryIndex(tune) - INote.ivoryIndex(FIRST_TUNE);
			int pos = baseRect.x + ivoryNeighborIndex * IVORY_WIDTH - EBONY_WIDTH / 2;

			Rectangle keyRect = new Rectangle(pos, baseRect.x, EBONY_WIDTH, EBONY_LENGTH);

			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(keyRect.x, keyRect.y, keyRect.width, keyRect.height);
			g.setColor(Color.BLACK);
			g.drawRect(keyRect.x, keyRect.y, keyRect.width, keyRect.height);

			if (highlightEm.stream().anyMatch(n -> n.getTune() == tune)) {
				int channel = highlightEm.stream().filter(n -> n.getTune() == tune).findAny().get().getChannel();
				g.setColor(ImageStorage.getColorByChannel(channel));
				g.fillRect(keyRect.x + 2, keyRect.y + 2, keyRect.width - 3, keyRect.height - 3);
			}
		});
	}

	synchronized private Set<INote> getNoteSet()
	{
		Set<INote> result = new TreeSet<>();

		Staff staff = mainPanel.sheetContainer.getFocusedChild().staff;

		int index = staff.getFocusedIndex();
		if (index > -1) {
			Fraction sum = staff.getChordList().get(index).getFraction().negate();

			while (sum.compareTo(ImageStorage.getTallLimit()) < 0 && index > -1) {

				Chord chord = staff.getChordList().get(index);
				sum = sum.add(chord.getFraction());

				final Fraction finalSum = sum;
				chord.noteStream(n -> n.getLength().compareTo(finalSum) > 0).forEach(result::add);

				--index;
			}
		}

		return result;
	}
}