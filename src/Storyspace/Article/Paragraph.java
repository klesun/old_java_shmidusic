package Storyspace.Article;

import Gui.Constants;
import Model.*;
import Model.Field.AbstractModelField;
import Model.Field.Int;
import Stuff.Tools.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;


public class Paragraph extends JTextArea implements IComponentModel {

	private Article parent = null;
	private AbstractHandler handler = null;
	private Helper modelHelper = new Helper(this);

	private Int score = modelHelper.addField("score", 0);

	public Paragraph(Article parent) {
		setLineWrap(true);
		setWrapStyleWord(true);
		setTabSize(4);
		super.setFont(Constants.PROJECT_FONT);

		handler = new AbstractHandler(this) {
			public Boolean mousePressedFinal(ComboMouse combo) { return combo.leftButton; }
			public Boolean mouseDraggedFinal(ComboMouse combo) { return combo.leftButton; }
			@Override
			protected void initActionMap() {
				addNumberComboList(ctrl, getContext()::setScore);
			}
			public Paragraph getContext() { return (Paragraph)super.getContext(); }
		};
		this.addMouseListener(handler);
		this.addMouseMotionListener(handler);
		this.addKeyListener(handler);

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

	public Integer getScore() { return score.getValue(); }
	public Paragraph setScore(Integer value) {
		score.setValue(value);
		updateBgColor();
		return this;
	}

	@Override
	public IModel getFocusedChild() { return null; }
	@Override
	public IModel getModelParent() { return parent; }
	@Override
	public AbstractHandler getHandler() { return handler; }

	@Override
	public void getJsonRepresentation(JSONObject dict) { dict.put("text", getText()); }
	@Override
	public IModel reconstructFromJson(JSONObject jsObject) throws JSONException {
		modelHelper.reconstructFromJson(jsObject);
		setText(jsObject.getString("text"));
		updateBgColor();
		return this;
	}

	@Override
	public Helper getModelHelper() {
		return modelHelper;
	}

	// event handles

	private void updateBgColor() {
		// TODO: move this method somewhere with name getColorBetween(c1, c2, fraction)
		Color color = getScore() == 0
			? Color.WHITE
			: new Color(192 + 63 - 63 * getScore() / 9, 192 + 63 * getScore() / 9, 192);

		setBackground(color);
	}
}
