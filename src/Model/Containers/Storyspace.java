package Model.Containers;

import Model.AbstractHandler;
import Model.Combo;
import Model.ComboMouse;
import Model.Containers.Panels.ImagePanel;
import Model.Containers.Panels.MusicPanel;
import Model.Containers.Panels.TextPanel;
import Model.IModel;
import OverridingDefaultClasses.Scroll;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Storyspace extends JPanel implements IModel {

	private MajesticWindow window = null;

	private ArrayList<Component> /* implements IModel */ modelChildList = new ArrayList<>();

	private AbstractHandler handler = null;

	public Storyspace(MajesticWindow window) {
		this.window = window;
		setLayout(null);
		setFocusable(true);

		handler = this.makeHandler();
		addKeyListener(handler);
		addMouseMotionListener(handler);
		addMouseListener(handler);

		this.setBackground(Color.DARK_GRAY);
	}

	public void addModelChild(Component /*IModel*/ child) {
		modelChildList.add(child);
		this.add(new ResizableScroll(child));
		this.validate();
		child.requestFocus();
	}

	// getters

	public MajesticWindow getWindow() { return this.window; }

	// overriding IModel

	@Override
	public IModel getModelParent() { return null; } // Storyspace is root parent
	@Override
	public IModel getFocusedChild() { // stupid java: JPanel is NOT focused even when one of it's children is focused
		Component focused = window.getFocusOwner();
		return modelChildList.contains(focused) ? IModel.class.cast(focused) : null;
	}
	@Override
	public AbstractHandler getHandler() { return this.handler; }

	@Override
	public JSONObject getJsonRepresentation() {
		JSONObject dict = new JSONObject();
		dict.put("childBlockList", new JSONArray(modelChildList.stream().map(child -> IModel.class.cast(child).getJsonRepresentation()).toArray()));
		return dict;
	}
	@Override
	public Storyspace reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.removeAll();
		this.modelChildList.clear();
		JSONArray childBlockList = jsObject.getJSONArray("childBlockList");
		for (int i = 0; i < childBlockList.length(); ++i) {
			JSONObject childJs = childBlockList.getJSONObject(i);
			Component /*IModel*/ child = makeChildByClassName(childJs.getString("className"));
			IModel.class.cast(child).reconstructFromJson(childJs);
			this.addModelChild(child);
		}
		return this;
	}

	// private methods

	private static Class<?extends Component>[] childClasses = new Class[]{TextPanel.class, ImagePanel.class, MusicPanel.class};

	private static Component /* IModel */ makeChildByClassName(String className) {
		Component obj = null;
		for (int i = 0; i < childClasses.length; ++i) {
			if (childClasses[i].getSimpleName() == className) {
				try { obj = childClasses[i].newInstance(); }
				catch (InstantiationException e) { e.printStackTrace(); Runtime.getRuntime().halt(666); }
				catch (IllegalAccessException e) { e.printStackTrace(); Runtime.getRuntime().halt(777); }
			}
		}
		return obj;
	}

	// event handles
	
	private AbstractHandler makeHandler() {
		return new AbstractHandler(this) {
			@Override
			protected void initActionMap() {
				addCombo(ctrl, k.VK_M).setDo(((Storyspace) this.getContext())::addMusicBlock);
				addCombo(ctrl, k.VK_T).setDo(((Storyspace) this.getContext())::addTextBlock);
				addCombo(ctrl, k.VK_I).setDo(((Storyspace) this.getContext())::addImageBlock);

				addCombo(ctrl, k.VK_EQUALS).setDo(((Storyspace) this.getContext())::scale);
				addCombo(ctrl, k.VK_MINUS).setDo(((Storyspace) this.getContext())::scale);
			}
			@Override
			public Boolean mousePressedFinal(ComboMouse mouse) {
				if (mouse.leftButton) {
					requestFocus();
				}
				return true;
			}
			@Override
			public Boolean mouseDraggedFinal(ComboMouse mouse) {
				Arrays.asList(getComponents()).stream().forEach(component
					-> component.setLocation(component.getX() + mouse.dx, component.getY() + mouse.dy));
				mouseLocation.move(mouse.dx, mouse.dy);
				return true;
			}
		};
	}

	public void scale(Combo combo) {
		int sign = combo.getSign();
		double factor = sign == -1 ? 0.75 : 1 / 0.75;
		for (Component child: modelChildList) {
			Scroll scroll = Scroll.class.cast(child.getParent().getParent()); // =D

			int width = (int) (scroll.getWidth() * factor);
			int height = (int) (scroll.getHeight() * factor);
			int x = (int) (scroll.getX() * factor);
			int y = (int) (scroll.getY() * factor);

			scroll.setSize(width, height);
			scroll.setLocation(x, y);
			scroll.validate();

			// TODO: child.scale(combo)
		}
	}

	public MusicPanel addMusicBlock(Combo combo) { return new MusicPanel(this); }
	public void addTextBlock(Combo combo) { new TextPanel(this); }
	public void addImageBlock(Combo combo) { new ImagePanel(this); }
}
