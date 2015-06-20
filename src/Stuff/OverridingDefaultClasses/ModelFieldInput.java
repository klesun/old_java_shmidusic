package Stuff.OverridingDefaultClasses;

import Model.Field.Field;

import javax.swing.*;

public class ModelFieldInput extends JTextField {

	Field owner;

	public ModelFieldInput(Field owner) {
		super(owner.get().toString());
		this.owner = owner;
	}

	public Field getOwner() { return this.owner; }

}
