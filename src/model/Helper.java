package model;

import blockspace.staff.accord.Chord;
import model.field.Field;
import blockspace.article.Article;
import blockspace.article.Paragraph;
import blockspace.BlockSpace;
import blockspace.Image.ImagePanel;
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
}
