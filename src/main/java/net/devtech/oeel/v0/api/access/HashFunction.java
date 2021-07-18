package net.devtech.oeel.v0.api.access;

import com.google.common.hash.Hasher;
import io.github.astrarre.util.v0.api.func.IterFunc;
import net.devtech.oeel.v0.api.util.VersionedHasher;

public interface HashFunction<T> {
	IterFunc<HashFunction<?>> COMBINE = iter -> (hasher, val) -> {
		VersionedHasher versioned = new VersionedHasher(hasher);
		int version = versioned.getVersion();
		for(HashFunction function : iter) {
			function.hash(versioned, val);
			if(versioned.getVersion() != version) {
				return;
			}
		}
	};

	static <T> IterFunc<HashFunction<T>> combine() {
		return (IterFunc)COMBINE;
	}

	void hash(Hasher hasher, T val);
}
