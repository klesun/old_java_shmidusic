package Storyspace.Article;

import Gui.Constants;
import Model.AbstractHandler;
import Model.ComboMouse;
import Model.IModel;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;


public class Paragraph extends JTextArea implements IModel {

	private Article parent = null;
	private AbstractHandler handler = null;

	public Paragraph(Article parent) {
		setLineWrap(true);
		setWrapStyleWord(true);
		super.setFont(Constants.PROJECT_FONT);

		handler = new AbstractHandler(this) {
			public Boolean mousePressedFinal(ComboMouse combo) { return combo.leftButton; }
			public Boolean mouseDraggedFinal(ComboMouse combo) { return combo.leftButton; }
		};
		this.addMouseListener(handler);
		this.addMouseMotionListener(handler);

		this.parent = parent;
	}

	public int getHeightIfWidthWas(int width) {

		JTextArea par = new JTextArea(this.getText());
		par.setLineWrap(true);
		par.setWrapStyleWord(true);
		par.setFont(this.getFont());

		par.setSize(new Dimension(width, 1));

		Rectangle r = null;
		try { r = par.modelToView(par.getText().length()); }
		catch (BadLocationException e) { Runtime.getRuntime().exit("Lolwhat?".length()); }

		return r.y + r.height + 1 * getFontMetrics(getFont()).getHeight(); // + 1 cuz i wanna separate them
	}

	// field getters/setters

	public Paragraph setTextValue(String value) {
		super.setText(value);
		return this;
	}

	@Override
	public IModel getFocusedChild() { return null; }
	@Override
	public IModel getModelParent() { return parent; }
	@Override
	public AbstractHandler getHandler() { return handler; }

	@Override
	public JSONObject getJsonRepresentation() {
		// TODO: soon
		return null;
	}
	@Override
	public IModel reconstructFromJson(JSONObject jsObject) throws JSONException {
		// TODO: soon
		return null;
	}
}
