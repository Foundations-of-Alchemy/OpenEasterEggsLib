package net.devtech.oeel.v0.api.util.func;

import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Iter {
	static <T> Iterable<T> iter(Supplier<Stream<T>> supplier) {
		return () -> supplier.get().iterator();
	}
}
