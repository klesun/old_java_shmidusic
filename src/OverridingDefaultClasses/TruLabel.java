package OverridingDefaultClasses;

import Gui.Constants;

import javax.swing.*;

public class TruLabel extends JLabel {
	public TruLabel(String text) {
		super(text);
		setFont(Constants.PROJECT_FONT);
	}
}
