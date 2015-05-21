package Model.Field;

public class Str extends AbstractModelField {

	public Str(String name, String value) {
		super(name, value);
	}

	@Override
	public String getValue() {
		return String.class.cast(super.getValue());
	}

	// lolwhy "not overrides"?
	public Str setValue(String value) {
		super.setValue(value);
		return this;
	}
}
