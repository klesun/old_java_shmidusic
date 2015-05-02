package Gui;

import Model.Combo;
import Model.Staff.Staff;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Window extends JFrame implements ActionListener {

	public SheetPanel sheetPanel;
	public JPanel storyspacePanel;

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

		this.addSheetPanel();
		this.addStoryspacePanel();

		this.addMusicBlock(Combo.makeFake()); // for user-friendship there will be one initial staff
	}

	public void addMusicBlock(Combo combo) {
		Staff staff = new Staff(this);
		this.staffList.add(staff);
		SheetPanel staffBlock = staff.blockPanel;
		staffBlock.setFocusedIndex(staffList.indexOf(staff));
		sheetPanel.setFocusedIndex(staffList.indexOf(staff));

		this.storyspacePanel.add(staffBlock.scrollBar);
		staffBlock.scrollBar.setLocation(200, 150);
		staffBlock.scrollBar.setSize(300, 300);

		staffBlock.requestFocus();
	}

	// it is windowed fullscreen!
	public void switchFullscreen(Combo combo) {
		this.isFullscreen = !this.isFullscreen;
		if (this.isFullscreen) {
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

	private void addSheetPanel() {
		this.sheetPanel = new SheetPanel(this);
		cards.add(sheetPanel.scrollBar, CARDS_FULLSCREEN);
	}

	private void addStoryspacePanel() {

		this.storyspacePanel = new JPanel();
		storyspacePanel.setLayout(null);

		cards.add(new JScrollPane(storyspacePanel), CARDS_STORYSPACE);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		System.out.println(evt);
	}
}