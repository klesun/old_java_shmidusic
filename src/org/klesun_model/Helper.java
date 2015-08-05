package org.klesun_model;

import org.klesun_model.field.Field;

import java.util.ArrayList;
import java.util.List;

// TODO: rename to ModelHelper
public class Helper
{
	IModel model;

	public Helper(IModel model) {
		this.model = model;
	}

	// field getters

	private List<Field> fieldStorage = new ArrayList<>();

	public List<Field> getFieldStorage() { return fieldStorage; }
}
