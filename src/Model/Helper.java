package Model;

import Model.Field.Arr;
import Model.Field.Field;
import Storyspace.Article.Article;
import Storyspace.Article.Paragraph;
import Storyspace.Image.ImagePanel;
import Storyspace.Staff.Accord.Accord;
import Storyspace.Staff.Accord.Nota.Nota;
import Storyspace.Staff.Staff;
import Storyspace.Staff.StaffPanel;
import Storyspace.Storyspace;
import Storyspace.StoryspaceScroll;
import Stuff.Tools.Logger;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Helper {

	IModel model;

	public Helper(IModel model) {
		this.model = model;
	}

	@Deprecated
	public static JSONObject getJsonRepresentation(IModel model) {
		JSONObject dict = new JSONObject();
		model.getJsonRepresentation(dict);

		// TODO: arghhh! No instanceof! Interface::getHelper mazafaka!
		if (model instanceof Paragraph) {
			Helper helper = Paragraph.class.cast(model).getModelHelper();
			for (Field field : helper.fieldStorage) {
				dict.put(field.getName(), field.get());
			}
		}

		return dict;
	}

	// TODO: use it instead of the deprecated above
	public JSONObject getJsonRepresentation() {
		return Helper.getJsonRepresentation(model);
	}

	public void getJsonRepresentation(JSONObject dict) {
		for (Field field : fieldStorage) {
			dict.put(field.getName(), field.getJsonValue());
		}
	}

	public IModel reconstructFromJson(JSONObject jsObject) {
		for (Field field : fieldStorage) {
			if (jsObject.has(field.getName())) { field.setValueFromJsObject(jsObject); }
			else { Logger.warning("Source does not have field [" + field.getName() + "] for class {" + getClass().getSimpleName() + "}"); }
		}
		return model;
	}

	public Cursor getDefaultCursor() {
		return  model instanceof Paragraph ? Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR) :
				Cursor.getDefaultCursor();
	}

	// field getters

	private List<Field> fieldStorage = new ArrayList<>();

	public List<Field> getFieldStorage() { return fieldStorage; }

	@Deprecated // use direct construct instead
	public Field<Integer> addField(String fieldName, Integer fieldValue) {
		return new Field<>(fieldName, fieldValue, model);
	}
	@Deprecated // use direct construct instead
	public Field<Fraction> addField(String fieldName, Fraction fieldValue) {
		return new Field<>(fieldName, fieldValue, model);
	}
	@Deprecated // use direct construct instead
	public Field<String> addField(String fieldName, String fieldValue) {
		return new Field<>(fieldName, fieldValue, model);
	}
	@Deprecated // use direct construct instead
	public Field<Boolean> addField(String fieldName, Boolean fieldValue) {
		return new Field<>(fieldName, fieldValue, model);
	}
	@Deprecated // use direct construct instead
	public Arr<? extends AbstractModel> addField(String fieldName, List<? extends AbstractModel> fieldValue, Class<? extends AbstractModel> cls) {
		return (Arr)new Arr(fieldName, fieldValue, model, cls);
	}

	// retarded language
	public List<IComponentModel> makeFakePossibleChildListForClassMethods() {
		if (model.getClass() == Storyspace.class) {
			return Arrays.asList(new StoryspaceScroll(new ImagePanel((Storyspace)model), (Storyspace)model));
		} else if (model.getClass() == StoryspaceScroll.class) {
			StoryspaceScroll scroll = (StoryspaceScroll)model;
			return Arrays.asList(new StaffPanel(scroll.getModelParent()), new Article(scroll.getModelParent()), new ImagePanel(scroll.getModelParent()));
		} else if (model.getClass() == Article.class) {
			return Arrays.asList(new Paragraph((Article)model));
		} else if (model.getClass() == StaffPanel.class) {
			return Arrays.asList(new Staff((StaffPanel)model));
		} else if (model.getClass() == Staff.class) {
			return Arrays.asList(new Accord((Staff)model));
		} else if (model.getClass() == Accord.class) {
			return Arrays.asList(new Nota((Accord)model));
		} else {
			return new ArrayList<>();
		}
	}

	final public static int limit(int value, int min, int max) { return Math.min(Math.max(value, min), max); }
}
