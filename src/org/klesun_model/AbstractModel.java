
package org.klesun_model;


import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractModel implements IModel {

	protected Helper h = new Helper(this);

	@Override
	public String toString() {
		return this.getJsonRepresentation().toString();
	}

	@Override
	final public Helper getModelHelper() { return h; }
}
