package org.shmidusic.stuff.OverridingDefaultClasses;

import java.util.Objects;

// Copyright (c) 2014 Daniel S. Dickstein

@FunctionalInterface
public interface TriConsumer<T, U, V> {
	public void accept(T t, U u, V v);

	public default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
		Objects.requireNonNull(after);
		return (a, b, c) -> {
			accept(a, b, c);
			after.accept(a, b, c);
		};
	}
}