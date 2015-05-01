package Gui;

import Model.Staff.Staff;
import test.ResizableScrollPane;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Window extends JFrame implements ActionListener {

	public SheetPanel sheetPanel;
	public JScrollPane sheetPanelContainer;
	public KeyEventHandler keyHandler = null;
	public JPanel storyspacePanel;
	public JScrollPane storyspacePanelContainer;
	int XP_MINWIDTH = 1024;
	int XP_MINHEIGHT = 540; // my beloved netbook

	public Boolean isFullscreen = true;
	public ArrayList<Staff> staffList = new ArrayList<>();

	JPanel cards = new JPanel();
	
	public Window() {
		super("Да будет такая музыка!"); //Заголовок окна
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		this.setLayout(new BorderLayout());
		this.setSize(XP_MINWIDTH, XP_MINHEIGHT);
		//this.setLocationRelativeTo(null);

		cards.setLayout(new CardLayout());
		this.add(cards);

		this.makeSheetPanel();
		this.makeStoryspacePanel();

		cards.add(sheetPanelContainer, "pizda");
		cards.add(storyspacePanelContainer, "suka");

		this.addMusicBlock(); // for user-friendship
	}

	public void addMusicBlock() {
		Staff staff = new Staff(this);
		this.staffList.add(staff);
		SheetPanel staffBlock = staff.blockPanel;
		staffBlock.setFocusedIndex(staffList.indexOf(staff));
		sheetPanel.setFocusedIndex(staffList.indexOf(staff));

		this.storyspacePanel.add(staffBlock.scrollBar);
		staffBlock.scrollBar.setLocation(200, 150);
		staffBlock.scrollBar.setSize(300, 300);
	}

	// it is windowed fullscreen!
	public void switchFullscreen() {
		this.isFullscreen = !this.isFullscreen;
		if (this.isFullscreen) {
			((CardLayout)cards.getLayout()).show(cards, "pizda");
			ImageStorage.inst().changeScale(999); // hahahahahahahahha
		} else {
			((CardLayout)cards.getLayout()).show(cards, "suka");
			ImageStorage.inst().changeScale(-999); // hahahahahahahahha
		}
		this.validate();
		this.repaint();
	}

	public SheetPanel getFocusedPanel() {
		return this.isFullscreen
			? sheetPanel
			: sheetPanel.getFocusedStaff().blockPanel; // человек очень... сложно мыслит
	}

	private void makeSheetPanel() {
		this.sheetPanel = new SheetPanel(this);
		this.sheetPanelContainer = sheetPanel.scrollBar;

		this.keyHandler = new KeyEventHandler(this);
		this.addKeyListener(this.keyHandler);
	}

	private void makeStoryspacePanel() {

		this.storyspacePanel = new JPanel();
		storyspacePanel.setLayout(null);
		storyspacePanelContainer = new JScrollPane(storyspacePanel);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		System.out.println(evt);
	}
}