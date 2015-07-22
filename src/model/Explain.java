package model;

import stuff.tools.Logger;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class Explain<C> {

	final private Boolean success;
	final private String explanation;
	final private C data;

	private Boolean implicit = false;

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

	// if "implicit" is true, alert wont appear even if isSuccess() == false
	public Explain<C> setImplicit(Boolean value) {
		this.implicit = value;
		return this;
	}

	public Boolean isImplicit() {
		return this.implicit;
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

	public Explain ifSuccess(Function<C, Explain> lambda) {
		return this.isSuccess() 	? lambda.apply(this.getData()) : this;
	}
}

