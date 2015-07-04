package Storyspace;
import Model.AbstractHandler;
import Model.Helper;
import Model.IComponentModel;
import Stuff.OverridingDefaultClasses.Pnt;
import Stuff.OverridingDefaultClasses.Scroll;
import Stuff.OverridingDefaultClasses.TruLabel;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class StoryspaceScroll extends Scroll implements IComponentModel {
	
	final public IStoryspacePanel content;

	final private Storyspace parent;
	final private AbstractHandler handler;
	final private Helper modelHelper = new Helper(this);

	final private static int TITLE_HEIGHT = 20;
	final private static int BORDER_WIDTH = 4;
	final private static Border unfocusedBorder = BorderFactory.createMatteBorder(TITLE_HEIGHT, 4, 4, 4, Color.LIGHT_GRAY);
	final private static Border focusedBorder = BorderFactory.createMatteBorder(TITLE_HEIGHT, 4, 4, 4, Color.GRAY);

	final private static int DEFAULT_X = 200;
	final private static int DEFAULT_Y = 150;
	final private static int DEFAULT_WIDTH = 300;
	final private static int DEFAULT_HEIGHT = 300;

	JPanel titlePanel = new JPanel();

	// for return from fullscreen mode
	private Dimension lastSize = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	private Point lastPosition = new Point(DEFAULT_X, DEFAULT_Y);
	private Boolean isFullscreen = false;

	public StoryspaceScroll(IStoryspacePanel content, Storyspace parent) {
		super((Component)content);
		this.content = content;
		this.parent = parent;

		this.setBorder(unfocusedBorder);
		this.setLocation(DEFAULT_X, DEFAULT_Y);
		this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

		handler = new StoryspaceScrollHandler(this);

		titlePanel.setSize(getWidth(), TITLE_HEIGHT);
		titlePanel.add(new TruLabel("Unsaved " + content.getClass().getSimpleName()));
		this.add(titlePanel);
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		titlePanel.setSize(getWidth(), TITLE_HEIGHT);
	}
	
	// implementing IModel

	@Override
	public IComponentModel getFocusedChild() { return IComponentModel.class.cast(content); }
	@Override
	public Storyspace getModelParent() { return this.parent; }
	@Override
	public AbstractHandler getHandler() { return this.handler; }
	@Override
	public Helper getModelHelper() {
		return modelHelper;
	}

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

	public Boolean isFullscreen() {
		return isFullscreen;
	}

	// event handles

	public StoryspaceScroll setTitle(String title) {
		this.titlePanel.removeAll();
		this.titlePanel.add(new TruLabel(title));
		this.titlePanel.validate();
		return this;
	}

	public String getTitle() { return TruLabel.class.cast(titlePanel.getComponent(0)).getText(); }

	public void gotFocus() {
		getModelParent().pushToFront(this);

		setBorder(StoryspaceScroll.focusedBorder);
		titlePanel.setBackground(new Color(200, 200, 200));
	}

	public void lostFocus() {
		setBorder(StoryspaceScroll.unfocusedBorder);
		titlePanel.setBackground(new Color(238, 238, 238));
	}

	public void switchFullscreen() {
		if (!isFullscreen) {
			lastPosition = getLocation();
			lastSize = getSize();
			fitToScreen();
			getModelParent().getWindow().setTitle(getTitle());
		} else {
			setLocation(lastPosition);
			setSize(lastSize);
		}

		content.requestFocus();
		isFullscreen = !isFullscreen;
	}

	public void fitToScreen() {
		int dw = 2 * BORDER_WIDTH;
		int dh = TITLE_HEIGHT + BORDER_WIDTH;

		setLocation(-BORDER_WIDTH, -TITLE_HEIGHT);
		// TODO: does not take into account decreasing size - undoable scrollbars appear
		setSize(getModelParent().getWidth() + dw, getModelParent().getHeight() + dh);
	}

	@Override
	public String toString() {
		return "Scroll: " + " " + getTitle();
	}
}