package org.klesun_model;

import org.shmidusic.stuff.tools.Logger;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** my implementation of Optional XD */
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

    // TODO: it should be (failurePredicate, explanation) it will make much more sense
	public Explain(Boolean success, String explanationIfFail) {
		this.data = null;
		this.success = success;
		this.explanation = success ? null : explanationIfFail;
	}

    public static Explain butIf(Boolean failureCondition, String explanation) {
        return new Explain(!failureCondition, explanation);
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

	public <T> Explain<T>  ifSuccess(Function<C, Explain<T>> lambda) {
		return this.isSuccess() ? lambda.apply(this.getData()) : new Explain<T>(false, explanation);
	}

    public <T> Explain<T> andThen(Supplier<Explain<T>> lambda) {
        return this.isSuccess() ? lambda.get() : new Explain<T>(false, this.explanation);
    }

    public Explain<C> andIf(Predicate<C> failPred, String explanation) {
        return failPred.test(getData()) ? new Explain<>(false, explanation) : this;
    }

	public Explain<C> whenSuccess(Consumer<C> lambda) {
		return ifSuccess(c -> { lambda.accept(c); return this; });
	}

    public Explain<C> whenFailure(Runnable onFailure) {
        if (!this.isSuccess()) {
            onFailure.run();
        }
        return this;

    }

	public Explain<C> runIfSuccess(Runnable lambda) {
		lambda.run();
		return this;
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

    public Explain<C> whileIf(Supplier<Boolean> cond, Function<C, Explain<C>> iteration)
    {
        return cond.get() ? iteration.apply(this.data).whileIf(cond, iteration) : this;
    }
}