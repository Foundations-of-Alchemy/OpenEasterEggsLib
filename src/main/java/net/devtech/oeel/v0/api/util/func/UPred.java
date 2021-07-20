package net.devtech.oeel.v0.api.util.func;

import java.util.function.Predicate;

import io.github.astrarre.util.v0.api.Validate;

public interface UPred<T> extends Predicate<T> {
	static <T> UPred<T> of(UPred<T> pred) {
		return pred;
	}

	@Override
	default boolean test(T t) {
		try {
			return this.testUnsafe(t);
		} catch(Throwable throwable) {
			throw Validate.rethrow(throwable);
		}
	}

	boolean testUnsafe(T t) throws Throwable;
}
