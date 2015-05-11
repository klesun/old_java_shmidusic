package Model.Containers;
import Gui.Constants;
import Model.AbstractHandler;
import Model.ComboMouse;
import Model.IModel;
import OverridingDefaultClasses.Scroll;
import OverridingDefaultClasses.TruLabel;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class ResizableScroll extends Scroll implements IModel {
	
	Component /* IModel */ content = null;
	AbstractHandler handler = null;

	final private static int titleHeight = 16;
	final private static Border unfocusedBorder = BorderFactory.createMatteBorder(titleHeight, 2, 2, 2, Color.LIGHT_GRAY);
	final private static Border focusedBorder = BorderFactory.createMatteBorder(titleHeight, 2, 2, 2, Color.GRAY);

	JPanel titlePanel = new JPanel();


	public ResizableScroll(Component content) {
		super(content);
		this.content = content;

		this.setBorder(unfocusedBorder);
		this.setPreferredSize(new Dimension(200, 200));
		this.setLocation(200, 150);
		this.setSize(300, 300);

		addListeners();

		titlePanel.setSize(getWidth(), titleHeight);
		titlePanel.add(new TruLabel("Unsaved " + content.getClass().getSimpleName()));
		this.add(titlePanel);
	}
	
	private void addListeners() {
		handler = new ResizableScrollHandler(this);
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		titlePanel.setSize(getWidth(), titleHeight);
	}
	
	// implementing IModel

	@Override
	public IModel getFocusedChild() { return IModel.class.cast(content); }
	@Override
	public Storyspace getModelParent() { return Storyspace.class.cast(getParent()); }
	@Override
	public AbstractHandler getHandler() { return this.handler; }

	@Override
	public JSONObject getJsonRepresentation() { return IModel.class.cast(content).getJsonRepresentation(); }
	@Override
	public ResizableScroll reconstructFromJson(JSONObject jsObject) throws JSONException {
		IModel.class.cast(content).reconstructFromJson(jsObject);
		return this;
	}

	// event handles

	public ResizableScroll setTitle(File file) {
		this.titlePanel.removeAll();
		this.titlePanel.add(new TruLabel(file.getName()));
		return this;
	}

	public void gotFocus() {
		setBorder(ResizableScroll.focusedBorder);
		titlePanel.setBackground(new Color(200, 200, 200));
	}

	public void lostFocus() {
		setBorder(ResizableScroll.unfocusedBorder);
		titlePanel.setBackground(new Color(238, 238, 238));
	}
}