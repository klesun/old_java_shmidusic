package Model.Containers;
import Model.AbstractHandler;
import Model.ComboMouse;
import Model.IModel;
import OverridingDefaultClasses.Scroll;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class ResizableScroll extends Scroll implements IModel {
	
	Component /* IModel */ content = null;
	AbstractHandler handler = null;

	public ResizableScroll(Component content) {
		super(content);
		this.content = content;

		this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
		this.setPreferredSize(new Dimension(200, 200));
		this.setLocation(200, 150);
		this.setSize(300, 300);

		addListeners();
	}
	
	private void addListeners() {
		handler = new ResizableScrollHandler(this);
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
}