package net.devtech.oeel.v0.api.access;

import com.google.common.hash.Hasher;

public interface HashFunction<T> {
	boolean hash(Hasher hasher, T value);
}
