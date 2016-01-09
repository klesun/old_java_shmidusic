package org.klesun_model;

import org.klesun_model.field.Field;
import org.klesun_model.field.IField;

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

	private List<IField> fieldStorage = new ArrayList<>();

	public List<IField> getFieldStorage() { return fieldStorage; }
}
