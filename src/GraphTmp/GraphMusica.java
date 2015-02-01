package GraphTmp;

import Musica.*;
import Tools.KeyEventi;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class GraphMusica extends JFrame implements ActionListener {

    final NotnyStan stan;
    DrawPanel Albert;    
    int XP_MINWIDTH = 1024/2;
    int XP_MINHEIGHT = 735/2; // потому что знаю
    
    public GraphMusica(){
        super("Да будет такая музыка!"); //Заголовок окна

        this.stan = new NotnyStan();;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setLayout(new BorderLayout());
        JScrollPane elder;
        this.add(elder = new JScrollPane(Albert = new DrawPanel(stan)), BorderLayout.CENTER);
        elder.getVerticalScrollBar().setUnitIncrement(16);

        stan.drawPanel = Albert;
        Albert.scroll = elder;
        
        
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
                Albert.stretch(w,h);
            }
        });               

	    this.addKeyListener(new KeyEventi(stan, Albert, GraphMusica.this));
    }  


	@Override
	public void actionPerformed(ActionEvent evt) {
		System.out.println(evt);
		
	}

}