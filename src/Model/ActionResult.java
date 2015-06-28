package Model;

// TODO: rename to "Explain"
public class ActionResult<C> {

	final private Boolean success;
	final private String explanation;
	final private C data;

	public ActionResult(C data) { // it will be funny if C is String =D
		this.data = data;
		this.success = true;
		this.explanation = null;
	}

	public ActionResult(String explanation) {
		this.data = null;
		this.success = false;
		this.explanation = explanation;
	}

	public Boolean isSuccess() {
		return this.success;
	}

	public C getData() {
		return this.data;
	}
}

