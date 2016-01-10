package org.shmidusic.stuff.OverridingDefaultClasses;

import javax.swing.*;

// sorry for that abomination, but it kinda was best solution
// i found on Stack Overflow how to include key combination caption
// ... or was it to prevent action on the key press since we handle it ourself...

public class TruMenuItem extends JMenuItem
{
	public TruMenuItem(String caption) {
		super(caption);
	}

	@Override
	public void setAccelerator(KeyStroke keyStroke) {
		super.setAccelerator(keyStroke);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "none");
	}

}