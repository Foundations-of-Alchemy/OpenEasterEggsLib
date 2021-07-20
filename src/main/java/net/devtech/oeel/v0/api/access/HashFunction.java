package net.devtech.oeel.v0.api.access;

import io.github.astrarre.util.v0.api.func.IterFunc;
import net.devtech.oeel.v0.api.util.hash.Hasher;

public interface HashFunction<T> {
	IterFunc<HashFunction<?>> COMBINE = iter -> (hasher, val) -> {
		long version = hasher.getVersion();
		for(HashFunction function : iter) {
			function.hash(hasher, val);
			if(hasher.getVersion() != version) {
				return;
			}
		}
	};

	static <T> IterFunc<HashFunction<T>> combine() {
		return (IterFunc)COMBINE;
	}

	void hash(Hasher hasher, T val);
}
