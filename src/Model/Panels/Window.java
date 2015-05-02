package Model.Panels;

import Gui.Settings;
import Model.Combo;
import Model.Staff.Staff;
import OverridingDefaultClasses.Scroll;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Window extends JFrame implements ActionListener {

	public SheetPanel sheetPanel;
	public Storyspace storyspace;

	int XP_MINWIDTH = 1024;
	int XP_MINHEIGHT = 540; // my beloved netbook

	public Boolean isFullscreen = true;
	public ArrayList<Staff> staffList = new ArrayList<>(); // i think we could store each staff in separate panel

	JPanel cards = new JPanel();

	final private static String CARDS_FULLSCREEN = "fullscreen";
	final private static String CARDS_STORYSPACE = "storyspace";

	public Window() {
		super("Да будет такая музыка!"); //Заголовок окна
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(XP_MINWIDTH, XP_MINHEIGHT);

		cards.setLayout(new CardLayout());
		this.add(cards);

		cards.add(new Scroll(sheetPanel = new SheetPanel(this)), CARDS_FULLSCREEN);
		cards.add(storyspace = new Storyspace(this), CARDS_STORYSPACE);

		storyspace.addMusicBlock(Combo.makeFake()); // for user-friendship there will be one initial staff
		switchFullscreen(Combo.makeFake());
	}

	// it is windowed fullscreen!
	public void switchFullscreen(Combo combo) {
		this.isFullscreen = !this.isFullscreen;
		if (this.isFullscreen) {
			sheetPanel.setStaff(getFocusedPanel().getStaff());
			((CardLayout)cards.getLayout()).show(cards, CARDS_FULLSCREEN);
			sheetPanel.requestFocus();
			Settings.inst().scaleUp(combo);
		} else {
			((CardLayout)cards.getLayout()).show(cards, CARDS_STORYSPACE);
			Settings.inst().scaleDown(combo);
		}
		this.validate();
		this.repaint();
	}

	public SheetPanel getFocusedPanel() {
		return (SheetPanel)getFocusOwner(); // deeds gonna be sad when you inovate textfields... but for such cases we can just return null. and we should not need to use this method anyway
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		System.out.println(evt);
	}
}