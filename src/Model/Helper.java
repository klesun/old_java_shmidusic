package Model;

import Model.Field.Arr;
import Model.Field.Field;
import BlockSpacePkg.ArticlePkg.Article;
import BlockSpacePkg.ArticlePkg.Paragraph;
import BlockSpacePkg.BlockSpace;
import BlockSpacePkg.Image.ImagePanel;
import BlockSpacePkg.StaffPkg.Accord.Accord;
import BlockSpacePkg.StaffPkg.Accord.Nota.Nota;
import BlockSpacePkg.StaffPkg.Staff;
import BlockSpacePkg.StaffPkg.StaffPanel;
import BlockSpacePkg.Block;
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
			if (field.get().getClass() != Boolean.class || field.get() != field.defaultValue) { // Issue[69]
				dict.put(field.getName(), field.getJsonValue());
			}
		}
	}

	public IModel reconstructFromJson(JSONObject jsObject) {
		for (Field field : fieldStorage) {
			if (jsObject.has(field.getName())) { field.setValueFromJsObject(jsObject); }
			else { Logger.warning("Source does not have field [" + field.getName() + "] for class {" + model.getClass().getSimpleName() + "}"); }
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
		if (model.getClass() == BlockSpace.class) {
			return Arrays.asList(new Block(new ImagePanel((BlockSpace)model), (BlockSpace)model));
		} else if (model.getClass() == Block.class) {
			Block scroll = (Block)model;
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

	public static Fraction limit(Fraction value, Fraction min, Fraction max) {
		value = value.compareTo(min) < 0 ? min : value;
		value = value.compareTo(max) > 0 ? max : value;
		return value;
	}
}
