package Stuff.OverridingDefaultClasses;

import Model.AbstractModel;
import Model.Field.ModelField;

import javax.swing.*;

public class ModelFieldInput extends JTextField {

	ModelField owner;

	public ModelFieldInput(ModelField owner) {
		super(owner.getValue().toString());
		this.owner = owner;
	}

	public ModelField getOwner() { return this.owner; }

}
