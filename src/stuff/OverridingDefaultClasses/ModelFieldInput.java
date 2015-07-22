package stuff.OverridingDefaultClasses;

import model.field.Field;

import javax.swing.*;

public class ModelFieldInput extends JTextField {

	Field owner;

	public ModelFieldInput(Field owner) {
		super(owner.get().toString());
		this.owner = owner;
		if (owner.isFinal) {
			this.setEditable(false);
		}
	}

	public Field getOwner() { return this.owner; }

}
