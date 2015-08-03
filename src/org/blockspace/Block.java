package org.blockspace;
import org.klesun_model.AbstractHandler;
import org.klesun_model.Helper;
import org.klesun_model.IComponent;
import org.klesun_model.IModel;
import org.sheet_midusic.stuff.OverridingDefaultClasses.TruLabel;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class Block extends JPanel implements IComponent, IModel {
	
	final public IBlockSpacePanel content;

	final private BlockSpace parent;
	final private AbstractHandler handler;
	final private Helper modelHelper = new Helper(this);

	final private static int TITLE_HEIGHT = 20;
	final private static int BORDER_WIDTH = 4;
	final private static Border unfocusedBorder = BorderFactory.createMatteBorder(4, 4, 4, 4, Color.LIGHT_GRAY);
	final private static Border focusedBorder = BorderFactory.createMatteBorder(4, 4, 4, 4, Color.GRAY);

	final private static int DEFAULT_X = 200;
	final private static int DEFAULT_Y = 150;
	final private static int DEFAULT_WIDTH = 300;
	final private static int DEFAULT_HEIGHT = 300;

	JPanel titlePanel = new JPanel();

	// for return from fullscreen mode
	private Dimension lastSize = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	private Point lastPosition = new Point(DEFAULT_X, DEFAULT_Y);
	private Boolean isFullscreen = false;

	public Block(IBlockSpacePanel content, BlockSpace parent) {
		super();
		this.content = content;
		this.parent = parent;

		this.setBorder(unfocusedBorder);
		this.setLocation(DEFAULT_X, DEFAULT_Y);
		this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

		this.setLayout(new BorderLayout());

		handler = new BlockHandler(this);

//		titlePanel.setSize(getWidth(), TITLE_HEIGHT);
		titlePanel.add(new TruLabel("Unsaved " + content.getClass().getSimpleName()));
		this.add(titlePanel, BorderLayout.PAGE_START);
		this.add((Component)content, BorderLayout.CENTER);
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		titlePanel.setSize(getWidth(), TITLE_HEIGHT);
	}
	
	// implementing IModel

	@Override
	public IComponent getFocusedChild() { return IComponent.class.cast(content); }
	@Override
	public BlockSpace getModelParent() { return this.parent; }
	@Override
	public AbstractHandler getHandler() { return this.handler; }
	@Override
	public Helper getModelHelper() {
		return modelHelper;
	}

	@Override
	public JSONObject getJsonRepresentation() {
		return new JSONObject()
			.put("x", getLocation().getX())
			.put("y", getLocation().getY())
			.put("width", getWidth())
			.put("height", getHeight())
			.put("title", getTitle());
	}
	@Override
	public Block reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.setLocation(jsObject.getInt("x"), jsObject.getInt("y"));
		this.setSize(new Dimension(jsObject.getInt("width"), jsObject.getInt("height")));
		this.setTitle(jsObject.getString("title"));
		return this;
	}

	public Boolean isFullscreen() {
		return isFullscreen;
	}

	// event handles

	public Block setTitle(String title) {
		this.titlePanel.removeAll();
		this.titlePanel.add(new TruLabel(title));
		this.titlePanel.validate();
		return this;
	}

	public String getTitle() { return TruLabel.class.cast(titlePanel.getComponent(0)).getText(); }

	public void gotFocus() {
		getModelParent().pushToFront(this);

		setBorder(Block.focusedBorder);
		titlePanel.setBackground(new Color(200, 200, 200));

		getModelParent().getWindow().updateMenuBar();
	}

	public void lostFocus() {
		setBorder(Block.unfocusedBorder);
		titlePanel.setBackground(new Color(238, 238, 238));

//		getModelParent().getWindow().updateMenuBar();
	}

	public void switchFullscreen() {

		if (!isFullscreen) {
			lastPosition = getLocation();
			lastSize = getSize();
			fitToScreen();
			getModelParent().getWindow().setTitle(getTitle());

			getModelParent().getSettings().scale(1);
		} else {
			setLocation(lastPosition);
			setSize(lastSize);

			getModelParent().getSettings().scale(-1);
		}
		validate();

		content.requestFocus();
		isFullscreen = !isFullscreen;
	}

	public void fitToScreen() {
		int dw = 2 * BORDER_WIDTH;
		int dh = TITLE_HEIGHT + BORDER_WIDTH;

		setLocation(-BORDER_WIDTH, -TITLE_HEIGHT);
		setSize(getModelParent().getWidth() + dw, getModelParent().getHeight() + dh);
	}

	@Override
	public String toString() {
		return "Scroll: " + " " + getTitle();
	}
}