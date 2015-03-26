package Gui;

import Model.Staff;
import Musica.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class Window extends JFrame implements ActionListener {

	SheetMusic albert;
	public KeyEventHandler keyHandler = null;
	int XP_MINWIDTH = 1024;
//	int XP_MINHEIGHT = 735/2; // потому что знаю
	int XP_MINHEIGHT = 540; // my beloved netbook
	
	public Window(){
		super("Да будет такая музыка!"); //Заголовок окна
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setLayout(new BorderLayout());
		JScrollPane elder;
		this.add(elder = new JScrollPane(albert = new SheetMusic(this)), BorderLayout.CENTER);
		elder.getVerticalScrollBar().setUnitIncrement(16);
		albert.scroll = elder;
		
		this.setSize(XP_MINWIDTH, XP_MINHEIGHT); 
		this.setLocationRelativeTo(null);
		
		this.getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener(){
			@Override
			public void ancestorMoved(HierarchyEvent e) {}
			@Override
			public void ancestorResized(HierarchyEvent e) {
				String s = e.paramString();
				int p = 0;
				for (int i=0; i<3; i++){
					p = s.indexOf(',', p+1);
				}
				int p2 = s.indexOf('x', p+1);
				int p3 = s.indexOf(',', p2+1);
				int w = Integer.parseInt(s.substring(p+1, p2));
				int h = Integer.parseInt(s.substring(p2+1, p3));
				albert.refresh();
			}
		});
		
		this.keyHandler = new KeyEventHandler(albert, Window.this);
		this.addKeyListener(this.keyHandler);
	}  
	
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		System.out.println(evt);
		
	}
}