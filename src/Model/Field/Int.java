package Model.Field;

public class Int extends AbstractModelField {

	public Int(String name, Integer value) {
		super(name, value);
	}

	@Override
	public Integer getValue() {
		return Integer.class.cast(super.getValue());
	}

	// lolwhy "not overrides"?
	public Int setValue(Integer value) {
		super.setValue(value);
		return this;
	}
}
