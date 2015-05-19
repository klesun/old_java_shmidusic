package Model.Field;

public class Bool extends AbstractModelField {

	public Bool(String name, Boolean value) {
		super(name, value);
	}

	@Override
	public Boolean getValue() {
		return Boolean.class.cast(super.getValue());
	}

	// lolwhy "not overrides"?
	public Bool setValue(Integer value) {
		super.setValue(value);
		return this;
	}
}
