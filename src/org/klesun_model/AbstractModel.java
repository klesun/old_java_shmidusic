
package org.klesun_model;

// AbstractModel is an IModel with attached Helper instance the Helper
// is used to store field list, since an interface can't have properties...

// TODO: probably no need for Helper anymore - move the fieldStorage here

public abstract class AbstractModel implements IModel {

	protected Helper h = new Helper(this);

	@Override
	public String toString() {
		return this.getJsonRepresentation().toString();
	}

	@Override
	final public Helper getModelHelper() { return h; }
}
