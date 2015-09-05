package org.shmidusic.stuff.test;

// we shall experiment with the time parameter of JComponent::repaint()
// i presume time we specify is time when buffers will be swapped?

import org.shmidusic.stuff.tools.Fp;

import javax.swing.*;
import java.awt.*;

public class SwingRepaintTimingTest extends JPanel
{
	private static JLabel counter;

	public static void main(String[] args) {
		JFrame window = new JFrame("zhopa");
		window.setLayout(new BorderLayout());

		counter = new JLabel("0");
		window.add(counter, BorderLayout.EAST);

		window.add(new SwingRepaintTimingTest(), BorderLayout.WEST);
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.setSize(1600, 800);

		window.setVisible(true);
	}

	private SwingRepaintTimingTest() {
		super();
		setPreferredSize(new Dimension(1440, 800));
		setFocusable(true);
		requestFocus();

		addKeyListener(Fp.onKey(k -> repaintWithin(8000)));
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawRect(10, 10, 800, 800);

		for (int i = 0; i < 50000; ++i) {
			int x = (int) (Math.random() * 1440);
			int y = (int) (Math.random() * 800);

			g.setColor(new Color(x * 255 / 1440, y * 255 / 800, 0));
			g.drawLine(10, 10, x, y);
		}
	}

	/** repaints into hidden buffer and swaps em after millis */
	// apparently does not repaint into hidden buffer
	private void repaintWithin(int millis)
	{
		new Thread(() -> {
			for (int i = millis / 1000; i >= 0; --i) {
				counter.setText("" + i);
				try { Thread.sleep(1000); } catch (InterruptedException exc) {}
			}
		}).start();

		Timer swingTimer = new Timer(millis, k -> repaint());
		swingTimer.setRepeats(false);
		swingTimer.start();
	}
}
