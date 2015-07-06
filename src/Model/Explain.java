package Model;

public class Explain<C> {

	final private Boolean success;
	final private String explanation;
	final private C data;

	public Explain(C data) { // it will be funny if C is String =D
		this.data = data;
		this.success = true;
		this.explanation = null;
	}

	public Explain(String explanation) {
		this.data = null;
		this.success = false;
		this.explanation = explanation;
	}

	public Explain(Boolean success, String explanationIfFail) {
		this.data = null;
		this.success = success;
		this.explanation = success ? null : explanationIfFail;
	}

	public Boolean isSuccess() {
		return this.success;
	}

	public C getData() {
		return this.data;
	}

	public String getExplanation() {
		return explanation;
	}
}

