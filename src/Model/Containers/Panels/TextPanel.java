package Model.Containers.Panels;

import Gui.Constants;
import Model.AbstractHandler;
import Model.AbstractModel;
import Model.ComboMouse;
import Model.Containers.StoryspaceScroll;
import Model.Containers.Storyspace;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;

public class TextPanel extends JTextArea implements IStoryspacePanel {

	private StoryspaceScroll scroll = null;
	private AbstractHandler handler = null;

	public TextPanel(Storyspace parentStoryspace) {
		super();
		this.setLineWrap(true);
		this.setFont(Constants.PROJECT_FONT);
		handler = new AbstractHandler(this) {
			public Boolean mousePressedFinal(ComboMouse combo) { return combo.leftButton; }
			public Boolean mouseDraggedFinal(ComboMouse combo) { return combo.leftButton; }
		};
		this.addMouseListener(handler);
		this.addMouseMotionListener(handler);
		scroll = parentStoryspace.addModelChild(this);
	}

	@Override
	public StoryspaceScroll getStoryspaceScroll() { return scroll; }
	@Override
	public AbstractModel getFocusedChild() { return null; } // no Model children
	@Override
	public StoryspaceScroll getModelParent() { return StoryspaceScroll.class.cast(getParent().getParent()); } // =D
	@Override
	public AbstractHandler getHandler() { return this.handler; }

	@Override
	public JSONObject getJsonRepresentation() {
		JSONObject dict = new JSONObject();
		dict.put("className", getClass().getSimpleName());
		dict.put("text", getText());
		return dict;
	}

	@Override
	public TextPanel reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.setText(jsObject.getString("text"));
		return this;
	}
}
