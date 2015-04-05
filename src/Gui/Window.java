package Gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class Window extends JFrame implements ActionListener {

	SheetPanel sheetPanel;
	public KeyEventHandler keyHandler = null;
	int XP_MINWIDTH = 1024;
	int XP_MINHEIGHT = 540; // my beloved netbook
	
	public Window(){
		super("Да будет такая музыка!"); //Заголовок окна
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setSize(XP_MINWIDTH, XP_MINHEIGHT);
		//this.setLocationRelativeTo(null);

		JScrollPane scrollBar = new JScrollPane(sheetPanel = new SheetPanel(this));
		this.add(scrollBar, BorderLayout.CENTER);
		scrollBar.getVerticalScrollBar().setUnitIncrement(16); // не в хуй ебу, вероятно как-то связано с размером картинки ноты
		sheetPanel.scrollBar = scrollBar;

		// window resize handler
		this.getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener() {
			@Override
			public void ancestorMoved(HierarchyEvent e) {}
			@Override
			public void ancestorResized(HierarchyEvent e) { sheetPanel.refreshImageSizes(); }
		});
		
		this.keyHandler = new KeyEventHandler(sheetPanel, this);
		this.addKeyListener(this.keyHandler);
	}  

	@Override
	public void actionPerformed(ActionEvent evt) {
		System.out.println(evt);
	}
}