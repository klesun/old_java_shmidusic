package Storyspace;
import Model.AbstractHandler;
import Model.IModel;
import Stuff.OverridingDefaultClasses.Scroll;
import Stuff.OverridingDefaultClasses.TruLabel;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class StoryspaceScroll extends Scroll implements IModel {
	
	Component /* IModel */ content = null;
	AbstractHandler handler = null;

	final private static int titleHeight = 20;
	final private static Border unfocusedBorder = BorderFactory.createMatteBorder(titleHeight, 4, 4, 4, Color.LIGHT_GRAY);
	final private static Border focusedBorder = BorderFactory.createMatteBorder(titleHeight, 4, 4, 4, Color.GRAY);

	JPanel titlePanel = new JPanel();


	public StoryspaceScroll(Component content) {
		super(content);
		this.content = content;

		this.setBorder(unfocusedBorder);
		this.setPreferredSize(new Dimension(200, 200));
		this.setLocation(200, 150);
		this.setSize(300, 300);
//		this.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);

		addListeners();

		titlePanel.setSize(getWidth(), titleHeight);
		titlePanel.add(new TruLabel("Unsaved " + content.getClass().getSimpleName()));
		this.add(titlePanel);
	}
	
	private void addListeners() {
		handler = new StoryspaceScrollHandler(this);
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
	public void getJsonRepresentation(JSONObject dict) {
		dict.put("x", getLocation().getX());
		dict.put("y", getLocation().getY());
		dict.put("width", getWidth());
		dict.put("height", getHeight());
		dict.put("title", getTitle());
	}
	@Override
	public StoryspaceScroll reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.setLocation(jsObject.getInt("x"), jsObject.getInt("y"));
		this.setSize(new Dimension(jsObject.getInt("width"), jsObject.getInt("height")));
		this.setTitle(jsObject.getString("title"));
		return this;
	}

	// event handles

	public StoryspaceScroll setTitle(String title) {
		this.titlePanel.removeAll();
		this.titlePanel.add(new TruLabel(title));
		return this;
	}

	public String getTitle() {
		return TruLabel.class.cast(titlePanel.getComponent(0)).getText();
	}

	public void gotFocus() {
		setBorder(StoryspaceScroll.focusedBorder);
		titlePanel.setBackground(new Color(200, 200, 200));
	}

	public void lostFocus() {
		setBorder(StoryspaceScroll.unfocusedBorder);
		titlePanel.setBackground(new Color(238, 238, 238));
	}
}