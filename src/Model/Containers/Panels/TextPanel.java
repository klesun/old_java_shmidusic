package Model.Containers.Panels;

import Model.AbstractHandler;
import Model.AbstractModel;
import Model.ComboMouse;
import Model.Containers.ResizableScroll;
import Model.Containers.Storyspace;
import Model.IModel;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;

public class TextPanel extends JTextArea implements IModel {

	private Storyspace parentStoryspace = null;
	private AbstractHandler handler = null;

	public TextPanel(Storyspace parentStoryspace) {
		super();
		this.setLineWrap(true);
		handler = new AbstractHandler(this) {
			@Override
			public Boolean mousePressedFinal(ComboMouse combo) { return combo.leftButton; }
			@Override
			public Boolean mouseDraggedFinal(ComboMouse combo) { return combo.leftButton; }
		};
		this.addMouseListener(handler);
		this.addMouseMotionListener(handler);
		(this.parentStoryspace = parentStoryspace).addModelChild(this);
	}

	@Override
	public AbstractModel getFocusedChild() { return null; } // no Model children
	@Override
	public ResizableScroll getModelParent() { return ResizableScroll.class.cast(getParent().getParent()); } // =D
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
