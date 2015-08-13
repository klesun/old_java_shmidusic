package org.sheet_midusic.staff.staff_panel;


import org.sheet_midusic.staff.Staff;
import org.sheet_midusic.staff.chord.Chord;
import org.sheet_midusic.stuff.graphics.Constants;
import org.sheet_midusic.stuff.graphics.ImageStorage;
import org.apache.commons.math3.fraction.Fraction;
import org.sheet_midusic.stuff.tools.jmusic_integration.INota;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class PianoLayoutPanel extends JPanel
{
	final private MainPanel mainPanel;

	public PianoLayoutPanel(MainPanel mainPanel)
	{
		this.mainPanel = mainPanel;
	}

	@Override
	public Dimension getPreferredSize()
	{
		Dimension base = super.getPreferredSize();
		return new Dimension(base.width, Constants.NORMAL_NOTA_HEIGHT); // dy() * 8
	};

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Rectangle pianoLayoutRect = new Rectangle(0, 0, this.getWidth(), this.getHeight());
		drawVanBascoLikePianoLayout(pianoLayoutRect, g);
	}

	// draws such piano layout so it fitted to Rectangle r
	private void drawVanBascoLikePianoLayout(Rectangle baseRect, Graphics g)
	{
		// performance
		g.setColor(Color.WHITE);
		g.fillRect(baseRect.x, baseRect.y, baseRect.width, baseRect.height);

		Set<INota> highlightEm = getNotaSet();

		int firstTune = 36;
		int tuneCount = 72; // 6 octaves

		IntStream ivoryTunes = IntStream.range(firstTune, firstTune + tuneCount).filter(t -> !INota.isEbony(t));
		IntStream ebonyTunes = IntStream.range(firstTune, firstTune + tuneCount).filter(INota::isEbony);

		int ivoryWidth = (int)Math.ceil(baseRect.width * 1.0 / (INota.ivoryIndex(tuneCount)));
		int ebonyWidth = ivoryWidth / 2;

		double ebonyLength = baseRect.height / 2;

		ivoryTunes.forEach(tune -> {
			int ivoryIndex = INota.ivoryIndex(tune) - INota.ivoryIndex(firstTune);
			int pos = baseRect.x + ivoryIndex * ivoryWidth;

			Rectangle keyRect = new Rectangle(pos, baseRect.x, ivoryWidth, baseRect.height);

			g.setColor(Color.BLACK);
			g.drawRect(keyRect.x, keyRect.y, keyRect.width, keyRect.height);

			if (highlightEm.stream().anyMatch(n -> n.getTune() == tune)) {
				int channel = highlightEm.stream().filter(n -> n.getTune() == tune).findAny().get().getChannel();
				g.setColor(ImageStorage.getColorByChannel(channel));
				g.fillRect(keyRect.x + 2, keyRect.y + 2, keyRect.width - 3, keyRect.height - 3);
			}
		});

		ebonyTunes.forEach(tune -> {
			int ivoryNeighborIndex = INota.ivoryIndex(tune) - INota.ivoryIndex(firstTune);
			int pos = baseRect.x + ivoryNeighborIndex * ivoryWidth - ebonyWidth / 2;

			Rectangle keyRect = new Rectangle(pos, baseRect.x, ebonyWidth, (int) ebonyLength);

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

	synchronized private Set<INota> getNotaSet()
	{
		Set<INota> result = new TreeSet<>();

		Staff staff = mainPanel.staffContainer.getFocusedChild().staff;

		int index = staff.getFocusedIndex();
		if (index > -1) {
			Fraction sum = staff.getChordList().get(index).getFraction().negate();

			while (sum.compareTo(ImageStorage.getTallLimit()) < 0 && index > -1) {

				Chord chord = staff.getChordList().get(index);
				sum = sum.add(chord.getFraction());

				final Fraction finalSum = sum;
				chord.notaStream(n -> n.getRealLength().compareTo(finalSum) > 0).forEach(result::add);

				--index;
			}
		}

		return result;
	}
}