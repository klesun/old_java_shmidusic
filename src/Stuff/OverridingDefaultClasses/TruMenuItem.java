package Stuff.OverridingDefaultClasses;

import javax.swing.*;

public class TruMenuItem extends JMenuItem {

	public TruMenuItem(String caption) {
		super(caption);
	}

	@Override
	public void setAccelerator(KeyStroke keyStroke) {
		super.setAccelerator(keyStroke);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "none");
	}

}