package net.devtech.oeel.v0.api.util.func;

import java.util.function.Function;

import io.github.astrarre.util.v0.api.Validate;

public interface UFunc<A, B> extends Function<A, B> {
	static <A, B> UFunc<A, B> of(UFunc<A, B> pred) {
		return pred;
	}

	@Override
	default B apply(A t) {
		try {
			return this.applyUnsafe(t);
		} catch(Throwable throwable) {
			throw Validate.rethrow(throwable);
		}
	}

	B applyUnsafe(A t) throws Throwable;
}
