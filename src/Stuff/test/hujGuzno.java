package Stuff.test;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;

public class hujGuzno {

	public static JTextArea createTextAreaFitToText(String message, Dimension minimalSize){

		JTextArea aMessagePanel = new JTextArea();
		aMessagePanel.setText(message);

        /*for modelToView to work, the text area has to be sized. It doesn't matter if it's visible or not.*/
		aMessagePanel.setPreferredSize(minimalSize);
		aMessagePanel.setSize(minimalSize);

		Rectangle r = null;
		try { r = aMessagePanel.modelToView(aMessagePanel.getDocument().getLength()); }
		catch (BadLocationException e) { Runtime.getRuntime().exit("Lolwhat?".length()); }


		Dimension d = new Dimension(minimalSize.width, r.y + r.height);
		aMessagePanel.setPreferredSize(d);
		return aMessagePanel;

	}

}
