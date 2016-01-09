package org.shmidusic.stuff.OverridingDefaultClasses;

import org.klesun_model.field.Field;
import org.klesun_model.field.IField;
import org.shmidusic.stuff.tools.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class ModelFieldInput {

	Field owner;
	final JComponent comp;
	final Function<JComponent, String> getter;

	public ModelFieldInput(Field owner)
	{
		this.owner = owner;

		if (!owner.isFinal()) {
			Tuple<JComponent, Function<JComponent, String>> compRepr = makeInputByField(owner);

			comp = compRepr.first;
			getter = compRepr.second;
		} else {
			comp = new JLabel(owner.get().toString(), SwingConstants.CENTER);
			comp.setBackground(Color.white);
			comp.setOpaque(true);
			getter = c -> {
				Logger.fatal("Trying to set final field? Not on my watch!");
				return "-100";
			};
		}
	}

	public JComponent getComponent() {
		return comp;
	}

	public String getValue() {
		return getter.apply(comp);
	}

	/** @return one of [JTextfield, JCheckbox, JNumberInput] */
	private static Tuple<JComponent, Function<JComponent, String>> makeInputByField(Field field)
	{
		if (field.elemClass == String.class) {
			return new Tuple<>(
					new JTextField(field.get().toString()),
					c -> ((JTextField)c).getText());
		} else if (field.elemClass == Boolean.class) {
			return new Tuple<>(
					new JCheckBox(null, null, (Boolean)field.get()),
					c -> new Boolean(((JCheckBox)c).isSelected()).toString());
		} else if (field.elemClass == Integer.class) {
			Field<Integer> genField = field;
			int min = genField.normalize.apply(Integer.MIN_VALUE);
			int max = genField.normalize.apply(Integer.MAX_VALUE);
			return new Tuple<>(
					new JSpinner(new SpinnerNumberModel(genField.get().intValue(), min, max, 1)),
					c -> ((JSpinner)c).getValue().toString());
		} else {
			Logger.fatal("I dunno such primitive type: " + field.elemClass.getSimpleName());
			return null;
		}
	}

	public Field getOwner() { return this.owner; }

	private static class Tuple<T, U>
	{
		final public T first;
		final public U second;

		public Tuple(T first, U second)
		{
			this.first = first;
			this.second = second;
		}
	}
}
