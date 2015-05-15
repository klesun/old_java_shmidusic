package OverridingDefaultClasses;

import javax.swing.*;
import java.awt.*;

public class Scroll extends JScrollPane {

	public Scroll(Component content) {
		super(content);
		this.getVerticalScrollBar().setUnitIncrement(16);
//		this.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
	}
}
