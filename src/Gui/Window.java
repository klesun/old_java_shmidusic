package Gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class Window extends JFrame implements ActionListener {

	public SheetPanel sheetPanel;
	public JScrollPane sheetPanelContainer;
	public KeyEventHandler keyHandler = null;
	public JPanel storyspacePanel;
	public JScrollPane storyspacePanelContainer;
	int XP_MINWIDTH = 1024;
	int XP_MINHEIGHT = 540; // my beloved netbook
	
	public Window() {
		super("Да будет такая музыка!"); //Заголовок окна
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setSize(XP_MINWIDTH, XP_MINHEIGHT);
		//this.setLocationRelativeTo(null);

		this.makeSheetPanel();
		this.makeStoryspacePanel();

		this.add(storyspacePanelContainer, BorderLayout.CENTER);
		// it actually not adds, but replaces if with BorderLayout.CENTER as i can judge
		// at least for i in range(0, 10000) did not kill my ram
		this.add(sheetPanelContainer, BorderLayout.CENTER);
//		this.add(storyspacePanelContainer, BorderLayout.CENTER);
	}

	public void switchFullscreen() {

	}

	private void makeSheetPanel() {
		this.sheetPanel = new SheetPanel(this);
		this.sheetPanelContainer = new JScrollPane(sheetPanel);
		this.sheetPanelContainer.getVerticalScrollBar().setUnitIncrement(16); // 16 не в хуй ебу, вероятно как-то связано с размером картинки ноты
		this.sheetPanel.scrollBar = this.sheetPanelContainer;

		this.keyHandler = new KeyEventHandler(sheetPanel, this);
		this.addKeyListener(this.keyHandler);
	}

	private void makeStoryspacePanel() {
		sheetPanelContainer.setSize(200, 200);
		sheetPanelContainer.setLocation(50, 40);

		this.storyspacePanel = new JPanel();
		storyspacePanel.setLayout(null);
		storyspacePanel.add(sheetPanelContainer);

		storyspacePanelContainer = new JScrollPane(storyspacePanel);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		System.out.println(evt);
	}
}