package model;

import model.field.Field;
import blockspace.article.Article;
import blockspace.article.Paragraph;
import blockspace.BlockSpace;
import blockspace.Image.ImagePanel;
import blockspace.staff.accord.Accord;
import blockspace.staff.accord.nota.Nota;
import blockspace.staff.Staff;
import blockspace.staff.StaffPanel;
import blockspace.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Helper {

	IModel model;

	public Helper(IModel model) {
		this.model = model;
	}

	// field getters

	private List<Field> fieldStorage = new ArrayList<>();

	public List<Field> getFieldStorage() { return fieldStorage; }

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
}
