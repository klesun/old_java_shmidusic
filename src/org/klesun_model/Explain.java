package org.klesun_model;

import org.sheet_midusic.stuff.tools.Logger;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

public class Explain<C> {

	final private Boolean success;
	final private String explanation;
	final private C data;

	private Boolean implicit = false;

	public Explain(C data) {
		this.data = data;
		this.success = true;
		this.explanation = null;
	}

	public Explain(String explanation, Exception exc) {
		this(false, explanation + " " + exc.getClass() + " " + exc.getMessage());
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
		return this.isSuccess() ? lambda.apply(this.getData()) : this;
	}

	public C dieIfFailure()
	{
		if (isSuccess()) {
			return data;
		} else {
			Logger.fatal("You asked for it. " + explanation);
			return null;
		}
	}

	// i would love to exactly specify, what kind of exception, but i cant catch a generic (damn java)
	public static Explain tryException(Callable riskyLambda)
	{
		try {
			riskyLambda.call();
		} catch (Exception exc) {
			return new Explain(false, "Failed: " + exc.getClass().getSimpleName() + " " + exc.getMessage());
		}

		return new Explain(true);
	}

	public static <T, R> Function<T, Explain<R>> mkPred(Predicate<T> pred, Function<T, R> func)
	{
		return e -> pred.test(e)
				? new Explain<>(func.apply(e))
				: new Explain<>(false, "lox");
	}
}