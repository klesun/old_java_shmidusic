
package model;


import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractModel implements IModel {

	private IComponentModel parent = null;
	protected Helper h = new Helper(this);

	public AbstractModel(IComponentModel parent) { // TODO: i'm not sure, that parent is ALWAYS ComponentModel. It's just a temporary hack.
		this.parent = parent;
	}

	@Override
	public String toString() {
		return this.getJsonRepresentation().toString();
	}

	@Override
	final public Helper getModelHelper() { return h; }
	public IComponentModel getModelParent() { return this.parent; }

	public List<String> getFieldList() {
		return getModelHelper().getFieldStorage().stream().map(f -> f.getName()).collect(Collectors.toList());
	}
}
