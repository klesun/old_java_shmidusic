package Stuff.test;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// multiple JTextAreas in one scrollPane
/** @see http://stackoverflow.com/questions/7818387 */
public class ScrollGrid extends JPanel {

	private static final int N = 16;
	private JTextArea last;
	private int index;

	public ScrollGrid() {
		this.setLayout(new GridLayout(0, 1, 3, 3));
		for (int i = 0; i < N; i++) {
			this.add(create());
		}
	}

	private JTextArea create() {
		last =  new JTextArea("Stackoverflow…" + ++index);
		last.setLineWrap(true);
		return last;
	}

	private void display() {
		JFrame f = new JFrame("ScrollGrid");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(new JScrollPane(this));
		f.add(new JButton(new AbstractAction("Add") {

			@Override
			public void actionPerformed(ActionEvent e) {
				add(create());
				revalidate();
				scrollRectToVisible(last.getBounds());
			}
		}), BorderLayout.SOUTH);
		f.pack();
		f.setSize(200, 160);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new ScrollGrid().display();
			}
		});
	}
}