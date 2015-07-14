package stuff.OverridingDefaultClasses;

import gui.Constants;

import javax.swing.*;

public class TruLabel extends JLabel {
	public TruLabel(String text) {
		super(text);
		setFont(Constants.PROJECT_FONT);
	}

	public TruLabel(String text, int align) {
		super(text, align);
		setFont(Constants.PROJECT_FONT);
	}
}
